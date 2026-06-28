package com.k12.common.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SharedAdminRbacMapper {

    @Select("""
            SELECT p.code
            FROM sys_permission p
            INNER JOIN sys_role_permission rp ON rp.permission_id = p.id
            INNER JOIN sys_user_role ur ON ur.role_id = rp.role_id
            WHERE ur.user_id = #{userId}
              AND p.status = 1
            GROUP BY p.code, p.sort
            ORDER BY p.sort
            """)
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT r.code
            FROM sys_role r
            INNER JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
              AND r.status = 1
            """)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(1)
            FROM sys_role r
            INNER JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
              AND r.code = 'super_admin'
              AND r.status = 1
            """)
    int countSuperAdminRole(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(DISTINCT ur.user_id)
            FROM sys_user_role ur
            INNER JOIN sys_role r ON r.id = ur.role_id AND r.code = 'super_admin' AND r.status = 1
            INNER JOIN `user` u ON u.id = ur.user_id AND u.role = 'admin' AND u.status = 1 AND u.deleted = 0
            """)
    int countActiveSuperAdminUsers();

    @Select("""
            SELECT p.code
            FROM sys_permission p
            WHERE p.status = 1
            ORDER BY p.sort, p.id
            """)
    List<String> selectAllPermissionCodes();
}
