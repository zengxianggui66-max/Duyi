package com.k12.auth.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.auth.admin.entity.SysPermission;
import com.k12.auth.admin.entity.SysRole;
import com.k12.auth.admin.guard.AdminUserGuard;
import com.k12.auth.admin.mapper.AdminPermissionMapper;
import com.k12.auth.admin.mapper.AdminRoleMapper;
import com.k12.auth.admin.mapper.AdminRolePermissionMapper;
import com.k12.auth.admin.mapper.AdminUserRoleMapper;
import com.k12.auth.mapper.UserMapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.AdminPermissionVO;
import com.k12.common.dto.AdminRoleCreateDTO;
import com.k12.common.dto.AdminRoleUpdateDTO;
import com.k12.common.dto.AdminRoleVO;
import com.k12.common.dto.AdminUserRoleAssignDTO;
import com.k12.common.dto.RolePermissionAssignDTO;
import com.k12.common.entity.User;
import com.k12.common.service.AdminPermissionService;
import com.k12.common.util.RoleUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminRoleService {

    private final AdminRoleMapper adminRoleMapper;
    private final AdminPermissionMapper adminPermissionMapper;
    private final AdminRolePermissionMapper adminRolePermissionMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final UserMapper userMapper;
    private final AdminUserGuard adminUserGuard;
    public AdminRoleService(AdminRoleMapper adminRoleMapper, AdminPermissionMapper adminPermissionMapper, AdminRolePermissionMapper adminRolePermissionMapper, AdminUserRoleMapper adminUserRoleMapper, UserMapper userMapper, AdminUserGuard adminUserGuard) {
        this.adminRoleMapper = adminRoleMapper;
        this.adminPermissionMapper = adminPermissionMapper;
        this.adminRolePermissionMapper = adminRolePermissionMapper;
        this.adminUserRoleMapper = adminUserRoleMapper;
        this.userMapper = userMapper;
        this.adminUserGuard = adminUserGuard;
    }


    public List<AdminRoleVO> listRoles() {
        List<SysRole> roles = adminRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .orderByAsc(SysRole::getId));
        List<AdminRoleVO> result = new ArrayList<>();
        for (SysRole role : roles) {
            result.add(toVo(role));
        }
        return result;
    }

    @Transactional
    public AdminRoleVO createRole(AdminRoleCreateDTO dto) {
        if (!StringUtils.hasText(dto.getCode()) || !StringUtils.hasText(dto.getName())) {
            throw new BusinessException(400, "角色编码和名称不能为空");
        }
        if (adminRoleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, dto.getCode())) > 0) {
            throw new BusinessException(400, "角色编码已存在");
        }
        SysRole role = new SysRole();
        role.setCode(dto.getCode().trim());
        role.setName(dto.getName().trim());
        role.setDescription(dto.getDescription());
        role.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        role.setIsBuiltin(0);
        adminRoleMapper.insert(role);
        return toVo(role);
    }

    @Transactional
    public AdminRoleVO updateRole(Long id, AdminRoleUpdateDTO dto) {
        SysRole role = requireRole(id);
        if (role.getIsBuiltin() != null && role.getIsBuiltin() == 1
                && dto.getStatus() != null && dto.getStatus() == 0) {
            throw new BusinessException(400, "内置角色不可禁用");
        }
        if (StringUtils.hasText(dto.getName())) {
            role.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            role.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            role.setStatus(dto.getStatus());
        }
        adminRoleMapper.updateById(role);
        return toVo(role);
    }

    public List<AdminPermissionVO> listPermissionTree() {
        List<SysPermission> rows = adminPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getStatus, 1)
                .orderByAsc(SysPermission::getSort)
                .orderByAsc(SysPermission::getId));
        Map<Long, AdminPermissionVO> byId = new LinkedHashMap<>();
        List<AdminPermissionVO> roots = new ArrayList<>();
        for (SysPermission row : rows) {
            AdminPermissionVO vo = toPermissionVo(row);
            byId.put(vo.getId(), vo);
        }
        for (AdminPermissionVO vo : byId.values()) {
            Long parentId = vo.getParentId() == null ? 0L : vo.getParentId();
            if (parentId == 0L) {
                roots.add(vo);
                continue;
            }
            AdminPermissionVO parent = byId.get(parentId);
            if (parent != null) {
                parent.getChildren().add(vo);
            } else {
                roots.add(vo);
            }
        }
        return roots;
    }

    @Transactional
    public void assignRolePermissions(Long roleId, RolePermissionAssignDTO dto) {
        SysRole role = requireRole(roleId);
        if (role.getIsBuiltin() != null && role.getIsBuiltin() == 1
                && AdminPermissionService.SUPER_ADMIN.equals(role.getCode())) {
            throw new BusinessException(400, "超级管理员权限不可修改");
        }
        adminRolePermissionMapper.deleteByRoleId(roleId);
        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            adminRolePermissionMapper.batchInsert(roleId, dto.getPermissionIds());
        }
    }

    @Transactional
    public void assignUserRoles(Long userId, AdminUserRoleAssignDTO dto, Long operatorId) {
        adminUserGuard.assertCanAssignRoles(operatorId, userId, dto.getRoleIds());
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!RoleUtils.isAdmin(user.getRole())) {
            throw new BusinessException(400, "仅 staff 账号（user.role=admin）可分配后台角色");
        }
        adminUserRoleMapper.deleteByUserId(userId);
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            adminUserRoleMapper.batchInsert(userId, dto.getRoleIds());
        }
    }

    public List<Long> getRolePermissionIds(Long roleId) {
        requireRole(roleId);
        return adminRolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    public List<Long> getUserRoleIds(Long userId) {
        return adminUserRoleMapper.selectRoleIdsByUserId(userId);
    }

    private SysRole requireRole(Long id) {
        SysRole role = adminRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        return role;
    }

    private AdminRoleVO toVo(SysRole role) {
        AdminRoleVO vo = new AdminRoleVO();
        vo.setId(role.getId());
        vo.setCode(role.getCode());
        vo.setName(role.getName());
        vo.setDescription(role.getDescription());
        vo.setStatus(role.getStatus());
        vo.setIsBuiltin(role.getIsBuiltin());
        vo.setCreateTime(role.getCreateTime());
        vo.setPermissionIds(adminRolePermissionMapper.selectPermissionIdsByRoleId(role.getId()));
        return vo;
    }

    private AdminPermissionVO toPermissionVo(SysPermission row) {
        AdminPermissionVO vo = new AdminPermissionVO();
        vo.setId(row.getId());
        vo.setCode(row.getCode());
        vo.setName(row.getName());
        vo.setType(row.getType());
        vo.setModule(row.getModule());
        vo.setParentId(row.getParentId());
        vo.setSort(row.getSort());
        return vo;
    }
}
