package com.dorm.server.service.impl;

import com.dorm.server.constant.RedisKeyConstants;
import com.dorm.server.constant.SystemConstants;
import com.dorm.server.entity.Department;
import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.vo.DepartmentVO;
import com.dorm.server.mapper.DepartmentMapper;
import com.dorm.server.service.DepartmentService;
import com.dorm.server.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 部门业务实现类
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final RedisUtil redisUtil;

    @Override
    public List<DepartmentVO> listDepartments() {
        // 优先从缓存获取
        Object cached = redisUtil.get(RedisKeyConstants.DEPARTMENT_LIST);
        if (cached != null) {
            log.info("[部门列表] 命中缓存");
            @SuppressWarnings("unchecked")
            List<DepartmentVO> result = (List<DepartmentVO>) cached;
            return result;
        }

        List<DepartmentVO> departments = departmentMapper.selectAll();
        // 缓存1小时
        redisUtil.set(RedisKeyConstants.DEPARTMENT_LIST, departments,
                SystemConstants.DICT_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("[部门列表] 查询完成，count={}", departments.size());
        return departments;
    }

    @Override
    public PageVO<DepartmentVO> listDepartmentsPage(Integer page, Integer pageSize, String keyword) {
        int offset = (page - 1) * pageSize;
        List<DepartmentVO> items = departmentMapper.selectPage(keyword, offset, pageSize);
        Long total = departmentMapper.selectPageCount(keyword);
        log.info("[部门分页查询] page={}, pageSize={}, keyword={}, total={}", page, pageSize, keyword, total);
        return PageVO.of(items, total, page, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentVO createDepartment(String name, Integer sortOrder) {
        Department dept = new Department();
        dept.setName(name);
        dept.setSortOrder(sortOrder != null ? sortOrder : 1);
        departmentMapper.insert(dept);

        // キャッシュ削除（一覧キャッシュを無効化）
        redisUtil.delete(RedisKeyConstants.DEPARTMENT_LIST);
        log.info("[部门新增] id={}, name={}", dept.getId(), dept.getName());

        DepartmentVO vo = new DepartmentVO();
        vo.setId(dept.getId());
        vo.setName(dept.getName());
        vo.setSortOrder(dept.getSortOrder());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentVO updateDepartment(Integer id, String name, Integer sortOrder) {
        Department dept = new Department();
        dept.setId(id);
        dept.setName(name);
        dept.setSortOrder(sortOrder != null ? sortOrder : 1);
        departmentMapper.updateById(dept);

        // キャッシュ削除（一覧キャッシュを無効化）
        redisUtil.delete(RedisKeyConstants.DEPARTMENT_LIST);
        log.info("[部门更新] id={}, name={}", id, name);

        DepartmentVO vo = new DepartmentVO();
        vo.setId(id);
        vo.setName(name);
        vo.setSortOrder(dept.getSortOrder());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Integer id) {
        departmentMapper.softDeleteById(id);

        // キャッシュ削除（一覧キャッシュを無効化）
        redisUtil.delete(RedisKeyConstants.DEPARTMENT_LIST);
        log.info("[部门删除] id={}", id);
    }
}
