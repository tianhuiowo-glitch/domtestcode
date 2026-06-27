package com.dorm.server.service.impl;

import com.dorm.server.constant.MessageConstants;
import com.dorm.server.entity.Dormitory;
import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.dto.CreateDormitoryDTO;
import com.dorm.server.entity.dto.UpdateDormitoryDTO;
import com.dorm.server.entity.dto.UpdateDormitoryTypeDTO;
import com.dorm.server.entity.vo.DormitoryVO;
import com.dorm.server.exception.BusinessException;
import com.dorm.server.mapper.DormitoryMapper;
import com.dorm.server.service.DormitoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 宿舍业务实现类
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormitoryServiceImpl implements DormitoryService {

    private final DormitoryMapper dormitoryMapper;

    @Override
    public PageVO<DormitoryVO> listDormitories(Integer page, Integer pageSize,
                                               String keyword, Integer regionId) {
        // 计算分页偏移量
        int offset = (page - 1) * pageSize;

        List<DormitoryVO> items = dormitoryMapper.selectPageList(keyword, regionId, offset, pageSize);
        Long total = dormitoryMapper.selectPageCount(keyword, regionId);

        log.info("[宿舍列表] page={}, pageSize={}, total={}", page, pageSize, total);
        return PageVO.of(items, total, page, pageSize);
    }

    @Override
    public DormitoryVO getDormitoryById(Integer id) {
        DormitoryVO vo = dormitoryMapper.selectVoById(id);
        if (vo == null) {
            throw new BusinessException(MessageConstants.DORMITORY_NOT_FOUND);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DormitoryVO createDormitory(CreateDormitoryDTO dto) {
        log.info("[新增宿舍] name={}", dto.getName());

        Dormitory dormitory = new Dormitory();
        dormitory.setRegionId(dto.getRegionId());
        dormitory.setName(dto.getName());
        dormitory.setDormitoryType(dto.getDormitoryType() != null ? dto.getDormitoryType() : "mixed");
        dormitory.setAddress(dto.getAddress());
        dormitory.setDailyRate(dto.getDailyRate());
        dormitory.setSortOrder(dto.getSortOrder());
        dormitory.setVersion(1);

        dormitoryMapper.insert(dormitory);

        // 返回完整 VO（含地域名称）
        return dormitoryMapper.selectVoById(dormitory.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DormitoryVO updateDormitory(Integer id, UpdateDormitoryDTO dto) {
        log.info("[更新宿舍] id={}, version={}", id, dto.getVersion());

        // 校验宿舍存在
        Dormitory existing = dormitoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(MessageConstants.DORMITORY_NOT_FOUND);
        }

        Dormitory dormitory = new Dormitory();
        dormitory.setId(id);
        dormitory.setRegionId(dto.getRegionId());
        dormitory.setName(dto.getName());
        // 前端表单不含 dormitoryType，使用 existing 记录的值作为回退，防止数据被清空
        dormitory.setDormitoryType(dto.getDormitoryType() != null ? dto.getDormitoryType() : existing.getDormitoryType());
        dormitory.setAddress(dto.getAddress());
        dormitory.setDailyRate(dto.getDailyRate());
        dormitory.setSortOrder(dto.getSortOrder());
        dormitory.setVersion(dto.getVersion());

        // 乐观锁更新
        int affected = dormitoryMapper.updateWithVersion(dormitory);
        if (affected == 0) {
            throw new BusinessException(MessageConstants.VERSION_CONFLICT);
        }

        return dormitoryMapper.selectVoById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDormitory(Integer id) {
        log.info("[删除宿舍] id={}", id);

        // 校验宿舍存在
        Dormitory existing = dormitoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(MessageConstants.DORMITORY_NOT_FOUND);
        }

        // 在住者チェック：入居者が1人でもいれば削除不可
        Integer activeResidentCount = dormitoryMapper.countActiveResidentsByDormitoryId(id);
        if (activeResidentCount != null && activeResidentCount > 0) {
            throw new BusinessException(MessageConstants.DORMITORY_HAS_ROOMS);
        }

        // 配下の全房間を一括ソフトデリート（連鎖削除）
        dormitoryMapper.softDeleteRoomsByDormitoryId(id);

        // 宿舎本体をソフトデリート
        dormitoryMapper.softDeleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DormitoryVO updateDormitoryType(Integer id, UpdateDormitoryTypeDTO dto) {
        log.info("[宿舎タイプ変更] id={}, newType={}", id, dto.getDormitoryType());

        // 宿舎の存在確認
        Dormitory existing = dormitoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(MessageConstants.DORMITORY_NOT_FOUND);
        }

        // 全室空室チェック：在住者が1人でもいれば変更不可
        Integer activeCount = dormitoryMapper.countActiveResidentsByDormitoryId(id);
        if (activeCount != null && activeCount > 0) {
            throw new BusinessException(MessageConstants.DORMITORY_TYPE_CHANGE_DENIED);
        }

        // 宿舎タイプを更新
        dormitoryMapper.updateDormitoryType(id, dto.getDormitoryType());

        log.info("[宿舎タイプ変更] 完了 id={}, type={}", id, dto.getDormitoryType());

        // 更新後の VO を返す
        return dormitoryMapper.selectVoById(id);
    }
}
