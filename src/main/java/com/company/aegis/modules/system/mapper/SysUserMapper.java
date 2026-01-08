package com.company.aegis.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.aegis.modules.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * Get user roles by user id
     */
    @Select("SELECT r.code FROM sys_role r " +
            "LEFT JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * Get user permissions by user id (via roles)
     */
    @Select("SELECT p.code FROM sys_permission p " +
            "LEFT JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "LEFT JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}
