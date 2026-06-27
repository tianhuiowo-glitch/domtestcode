package com.dorm.server.service;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.dto.CheckoutDTO;
import com.dorm.server.entity.dto.CreateCheckinDTO;
import com.dorm.server.entity.vo.CheckinVO;
import com.dorm.server.entity.vo.EmployeeLookupVO;

/**
 * 入住业务接口
 * 映射 residence_histories 表（checkin 规格）
 *
 * @author dorm-server
 */
public interface CheckinService {

    /**
     * 分页查询入住记录
     *
     * @param page        页码
     * @param pageSize    每页大小
     * @param keyword     关键词
     * @param status      状态（active/checked_out）
     * @param dormitoryId 宿舍ID
     * @return 分页结果
     */
    PageVO<CheckinVO> listCheckins(Integer page, Integer pageSize, String keyword,
                                   String status, Integer dormitoryId);

    /**
     * 查询入住详情
     *
     * @param id 记录ID
     * @return 入住 VO
     */
    CheckinVO getCheckinById(Integer id);

    /**
     * 新增入住记录
     *
     * @param dto 新增参数
     * @return 新增后的入住 VO
     */
    CheckinVO createCheckin(CreateCheckinDTO dto);

    /**
     * 办理退住
     *
     * @param id  入住记录ID
     * @param dto 退住参数（含退住日期、version）
     * @return 更新后的入住 VO
     */
    CheckinVO checkout(Integer id, CheckoutDTO dto);

    /**
     * 员工查询（根据社員番号查询最新记录）
     * 若无记录则返回默认值
     *
     * @param employeeId 社員番号
     * @return 员工查询 VO
     */
    EmployeeLookupVO lookupEmployee(String employeeId);
}
