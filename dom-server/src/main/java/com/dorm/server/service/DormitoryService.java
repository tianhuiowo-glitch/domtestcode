package com.dorm.server.service;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.dto.CreateDormitoryDTO;
import com.dorm.server.entity.dto.UpdateDormitoryDTO;
import com.dorm.server.entity.dto.UpdateDormitoryTypeDTO;
import com.dorm.server.entity.vo.DormitoryVO;

/**
 * 宿舍业务接口
 *
 * @author dorm-server
 */
public interface DormitoryService {

    /**
     * 分页查询宿舍列表
     *
     * @param page      页码
     * @param pageSize  每页大小
     * @param keyword   关键词
     * @param regionId  地域ID
     * @return 分页结果
     */
    PageVO<DormitoryVO> listDormitories(Integer page, Integer pageSize, String keyword, Integer regionId);

    /**
     * 查询宿舍详情
     *
     * @param id 宿舍ID
     * @return 宿舍 VO
     */
    DormitoryVO getDormitoryById(Integer id);

    /**
     * 新增宿舍
     *
     * @param dto 新增参数
     * @return 新增后的宿舍 VO
     */
    DormitoryVO createDormitory(CreateDormitoryDTO dto);

    /**
     * 更新宿舍
     *
     * @param id  宿舍ID
     * @param dto 更新参数（含version）
     * @return 更新后的宿舍 VO
     */
    DormitoryVO updateDormitory(Integer id, UpdateDormitoryDTO dto);

    /**
     * 删除宿舍（软删除）
     *
     * @param id 宿舍ID
     */
    void deleteDormitory(Integer id);

    /**
     * 宿舎タイプを変更（全室空室の場合のみ許可）
     *
     * @param id  宿舎ID
     * @param dto 新しい宿舎タイプ
     * @return 更新後の宿舎 VO
     */
    DormitoryVO updateDormitoryType(Integer id, UpdateDormitoryTypeDTO dto);
}
