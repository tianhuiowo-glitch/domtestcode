package com.dorm.server.mapper;

import com.dorm.server.entity.SysUser;
import org.apache.ibatis.annotations.Param;

/**
 * 系统用户持久层接口
 *
 * @author dorm-server
 */
public interface SysUserMapper {

    /**
     * 根据用户名查询用户（用于登录校验）
     *
     * @param username 用户名
     * @return 系统用户实体（含密码）
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 系统用户实体
     */
    SysUser selectById(@Param("id") Long id);

    /**
     * 更新最后登录时间
     *
     * @param id 用户ID
     * @return 影响行数
     */
    Integer updateLastLoginAt(@Param("id") Long id);
}
