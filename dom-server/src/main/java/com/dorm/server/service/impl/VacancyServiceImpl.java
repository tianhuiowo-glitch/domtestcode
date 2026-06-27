package com.dorm.server.service.impl;

import com.dorm.server.constant.RedisKeyConstants;
import com.dorm.server.constant.SystemConstants;
import com.dorm.server.entity.vo.DormitoryVO;
import com.dorm.server.entity.vo.RoomVO;
import com.dorm.server.entity.vo.VacancySummaryVO;
import com.dorm.server.mapper.DormitoryMapper;
import com.dorm.server.mapper.RoomMapper;
import com.dorm.server.service.VacancyService;
import com.dorm.server.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 空房业务实现类
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VacancyServiceImpl implements VacancyService {

    private final DormitoryMapper dormitoryMapper;
    private final RoomMapper roomMapper;
    private final RedisUtil redisUtil;

    @Override
    public List<VacancySummaryVO> getVacancySummary() {
        // 尝试从 Redis 缓存获取
        Object cached = redisUtil.get(RedisKeyConstants.VACANCY_SUMMARY);
        if (cached != null) {
            log.info("[空房汇总] 命中缓存");
            @SuppressWarnings("unchecked")
            List<VacancySummaryVO> result = (List<VacancySummaryVO>) cached;
            return result;
        }

        // 查询所有宿舍
        List<DormitoryVO> dormitories = dormitoryMapper.selectAllForVacancy();
        List<VacancySummaryVO> summaryList = new ArrayList<>(dormitories.size());

        for (DormitoryVO dorm : dormitories) {
            // 查询该宿舍的房间 VO 列表（含当前入住人数，避免 Integer.MAX_VALUE 全量分页）
            List<RoomVO> rooms = roomMapper.selectVoListByDormitoryId(dorm.getId());

            int totalRooms = rooms.size();
            // 统计有在住人员的房间数（currentOccupancy > 0）
            long occupiedRooms = rooms.stream()
                    .filter(r -> r.getCurrentOccupancy() != null && r.getCurrentOccupancy() > 0)
                    .count();

            int vacant = totalRooms - (int) occupiedRooms;
            double occupancyRate = totalRooms > 0
                    ? Math.round((double) occupiedRooms / totalRooms * 1000) / 10.0
                    : 0.0;
            // 空室率（0〜1 の小数、フロントエンド用）
            double vacancyRate = totalRooms > 0
                    ? Math.round((double) vacant / totalRooms * 1000) / 1000.0
                    : 0.0;

            VacancySummaryVO vo = new VacancySummaryVO();
            vo.setDormitoryId(dorm.getId());
            vo.setDormitoryName(dorm.getName());
            vo.setRegionName(dorm.getRegionName());
            vo.setTotalRooms(totalRooms);
            vo.setOccupiedRooms((int) occupiedRooms);
            vo.setVacantRooms(vacant);
            vo.setOccupancyRate(occupancyRate);
            // フロントエンド契約フィールド
            vo.setDormitoryType(dorm.getDormitoryType());
            vo.setMaintenanceRooms(0);
            vo.setVacancyRate(vacancyRate);
            summaryList.add(vo);
        }

        // 写入缓存，5分钟过期
        redisUtil.set(RedisKeyConstants.VACANCY_SUMMARY, summaryList,
                SystemConstants.SUMMARY_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("[空房汇总] 查询完成，宿舍数={}", summaryList.size());

        return summaryList;
    }
}
