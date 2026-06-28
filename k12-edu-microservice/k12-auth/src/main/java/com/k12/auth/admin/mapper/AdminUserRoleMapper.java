package com.k12.auth.admin.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminUserRoleMapper {

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    @Select("""
            <script>
            SELECT ur.user_id AS userId, r.code AS roleCode, r.name AS roleName
            FROM sys_user_role ur
            INNER JOIN sys_role r ON r.id = ur.role_id AND r.status = 1
            WHERE ur.user_id IN
            <foreach collection="userIds" item="uid" open="(" separator="," close=")">
              #{uid}
            </foreach>
            ORDER BY r.id
            </script>
            """)
    List<UserRoleRow> selectRoleRowsByUserIds(@Param("userIds") List<Long> userIds);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Insert("""
            <script>
            INSERT INTO sys_user_role (user_id, role_id) VALUES
            <foreach collection="roleIds" item="rid" separator=",">
              (#{userId}, #{rid})
            </foreach>
            </script>
            """)
    int batchInsert(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}
