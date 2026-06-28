package com.k12.auth.admin.service;



import com.k12.auth.admin.entity.SysMenu;

import com.k12.auth.admin.mapper.AdminRbacMapper;

import com.k12.auth.mapper.UserMapper;

import com.k12.common.BusinessException;

import com.k12.common.dto.AdminMeVO;

import com.k12.common.dto.AdminMenuVO;

import com.k12.common.entity.User;

import com.k12.common.service.AdminPermissionService;

import com.k12.common.util.RoleUtils;

import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;



import java.util.ArrayList;

import java.util.Comparator;

import java.util.HashSet;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.Set;



@Service
public class AdminRbacService {



    private final UserMapper userMapper;

    private final AdminRbacMapper adminRbacMapper;

    private final AdminPermissionService adminPermissionService;
    public AdminRbacService(UserMapper userMapper, AdminRbacMapper adminRbacMapper, AdminPermissionService adminPermissionService) {
        this.userMapper = userMapper;
        this.adminRbacMapper = adminRbacMapper;
        this.adminPermissionService = adminPermissionService;
    }




    public AdminMeVO getAdminMe(Long userId) {

        User user = userMapper.selectById(userId);

        if (user == null) {

            throw new BusinessException(401, "用户不存在");

        }

        requireStaffUser(user);

        AdminMeVO vo = new AdminMeVO();

        vo.setId(user.getId());

        vo.setUsername(user.getUsername());

        vo.setNickname(user.getNickname());

        vo.setAvatar(user.getAvatar());

        vo.setRole(user.getRole());

        List<String> roles = adminPermissionService.loadRoleCodes(userId);

        vo.setRoles(roles);

        vo.setPermissions(adminPermissionService.loadPermissionCodes(userId));

        return vo;

    }



    public List<String> listPermissions(Long userId) {

        requireStaffUserById(userId);

        return adminPermissionService.loadPermissionCodes(userId);

    }



    public List<AdminMenuVO> listMenus(Long userId) {
        requireStaffUserById(userId);
        boolean superAdmin = adminPermissionService.loadRoleCodes(userId).contains(AdminPermissionService.SUPER_ADMIN);
        Set<String> permissionSet = new HashSet<>(adminPermissionService.loadPermissionCodes(userId));
        List<SysMenu> rows = adminRbacMapper.selectActiveMenus();
        List<AdminMenuVO> flat = new ArrayList<>();
        for (SysMenu row : rows) {
            if (!superAdmin
                    && StringUtils.hasText(row.getPermissionCode())
                    && !permissionSet.contains(row.getPermissionCode())) {
                continue;
            }
            flat.add(toMenuVo(row));
        }
        return pruneEmptyGroups(buildTree(flat));
    }



    private User requireStaffUserById(Long userId) {

        User user = userMapper.selectById(userId);

        if (user == null) {

            throw new BusinessException(401, "用户不存在");

        }

        requireStaffUser(user);

        return user;

    }



    private void requireStaffUser(User user) {

        RoleUtils.requireStaffRole(user.getRole());

        List<String> roles = adminPermissionService.loadRoleCodes(user.getId());

        if (roles.isEmpty()) {

            throw new BusinessException(403, "未分配后台角色，请联系管理员");

        }

    }



    private AdminMenuVO toMenuVo(SysMenu row) {

        AdminMenuVO vo = new AdminMenuVO();

        vo.setId(row.getId());

        vo.setParentId(row.getParentId());

        vo.setTitle(row.getTitle());

        vo.setPath(row.getPath());

        vo.setName(row.getName());

        vo.setIcon(row.getIcon());

        vo.setComponent(row.getComponent());

        vo.setPermissionCode(row.getPermissionCode());

        vo.setSort(row.getSort());

        return vo;

    }



    private List<AdminMenuVO> buildTree(List<AdminMenuVO> flat) {

        Map<Long, AdminMenuVO> byId = new LinkedHashMap<>();

        List<AdminMenuVO> roots = new ArrayList<>();

        for (AdminMenuVO item : flat) {

            byId.put(item.getId(), item);

        }

        for (AdminMenuVO item : flat) {

            Long parentId = item.getParentId() == null ? 0L : item.getParentId();

            if (parentId == 0L) {

                roots.add(item);

                continue;

            }

            AdminMenuVO parent = byId.get(parentId);

            if (parent != null) {

                parent.getChildren().add(item);

            } else {

                roots.add(item);

            }

        }

        sortMenuTree(roots);
        return roots;

    }



    private List<AdminMenuVO> pruneEmptyGroups(List<AdminMenuVO> nodes) {

        List<AdminMenuVO> kept = new ArrayList<>();

        for (AdminMenuVO node : nodes) {

            if (node.getChildren() != null && !node.getChildren().isEmpty()) {

                node.setChildren(pruneEmptyGroups(node.getChildren()));

            }

            boolean groupShell = !StringUtils.hasText(node.getComponent());

            boolean hasChildren = node.getChildren() != null && !node.getChildren().isEmpty();

            if (hasChildren || !groupShell) {

                kept.add(node);

            }

        }

        return kept;

    }



    private void sortMenuTree(List<AdminMenuVO> nodes) {

        nodes.sort(Comparator
                .comparing(AdminMenuVO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(AdminMenuVO::getId, Comparator.nullsLast(Long::compareTo)));

        for (AdminMenuVO node : nodes) {

            if (!node.getChildren().isEmpty()) {

                sortMenuTree(node.getChildren());

            }

        }

    }

}

