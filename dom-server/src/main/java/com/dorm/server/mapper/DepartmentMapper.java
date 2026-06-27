package com.dorm.server.mapper;

import com.dorm.server.entity.Department;
import com.dorm.server.entity.vo.DepartmentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门持久层接口
 *
 * @author dorm-server
 */
public interface DepartmentMapper {

    /**
     * 查询所有部门列表（按 sort_order 升序）
     *
     * @return 部门 VO 列表
     */
    List<DepartmentVO> selectAll();

    /**
     * 分页查询部门列表（含关键词过滤）
     *
     * @param keyword  搜索关键词（部门名称模糊匹配，null=不过滤）
     * @param offset   分页偏移量
     * @param pageSize 每页大小
     * @return 部门 VO 列表
     */
    List<DepartmentVO> selectPage(@Param("keyword") String keyword,
                                  @Param("offset") int offset,
                                  @Param("pageSize") int pageSize);

    /**
     * 统计分页查询总数
     *
     * @param keyword 搜索关键词（null=不过滤）
     * @return 总记录数
     */
    Long selectPageCount(@Param("keyword") String keyword);

    /**
     * 新增部门
     *
     * @param department 部门实体（id 由自增回填）
     * @return 影响行数
     */
    int insert(Department department);

    /**
     * 按ID更新部门
     *
     * @param department 部门实体（含 id 字段）
     * @return 影响行数
     */
    int updateById(Department department);

    /**
     * 按ID逻辑删除部门（设置 deleted_at）
     *
     * @param id 部门ID
     * @return 影响行数
     */
    int softDeleteById(@Param("id") Integer id);
}
