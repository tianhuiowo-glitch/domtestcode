package com.dorm.server.service;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.vo.DepartmentVO;

import java.util.List;

/**
 * 部门业务接口
 *
 * @author dorm-server
 */
public interface DepartmentService {

    /**
     * 查询所有部门列表（按排序号升序）
     *
     * @return 部门列表
     */
    List<DepartmentVO> listDepartments();

    /**
     * 分页查询部门列表
     *
     * @param page     页码（从1开始）
     * @param pageSize 每页条数
     * @param keyword  搜索关键词（null=不过滤）
     * @return 分页结果
     */
    PageVO<DepartmentVO> listDepartmentsPage(Integer page, Integer pageSize, String keyword);

    /**
     * 新增部门
     *
     * @param name      部门名称
     * @param sortOrder 排序号（null 时默认 1）
     * @return 新增后的部门 VO
     */
    DepartmentVO createDepartment(String name, Integer sortOrder);

    /**
     * 更新部门
     *
     * @param id        部门ID
     * @param name      部门名称
     * @param sortOrder 排序号（null 时默认 1）
     * @return 更新后的部门 VO
     */
    DepartmentVO updateDepartment(Integer id, String name, Integer sortOrder);

    /**
     * 逻辑删除部门
     *
     * @param id 部门ID
     */
    void deleteDepartment(Integer id);
}
