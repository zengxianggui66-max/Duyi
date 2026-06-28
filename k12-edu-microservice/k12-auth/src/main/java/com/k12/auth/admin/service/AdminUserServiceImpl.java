package com.k12.auth.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.auth.admin.guard.AdminUserGuard;
import com.k12.auth.admin.mapper.AdminUserRoleMapper;
import com.k12.auth.admin.mapper.UserRoleRow;
import com.k12.auth.mapper.UserLoginLogMapper;
import com.k12.auth.mapper.UserMapper;
import com.k12.auth.mapper.UserOAuthBindMapper;
import com.k12.auth.service.UserService;
import com.k12.common.BusinessException;
import com.k12.common.dto.AdminOAuthBindVO;
import com.k12.common.dto.AdminUserBatchStatusDTO;
import com.k12.common.dto.AdminUserDetailVO;
import com.k12.common.dto.AdminUserListVO;
import com.k12.common.dto.AdminUserUpdateDTO;
import com.k12.common.entity.User;
import com.k12.common.entity.UserOAuthBind;
import com.k12.common.util.RoleUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;
    private final UserOAuthBindMapper userOAuthBindMapper;
    private final UserLoginLogMapper userLoginLogMapper;
    private final UserService userService;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final AdminUserGuard adminUserGuard;
    private final AdminUserScopeService adminUserScopeService;
    public AdminUserServiceImpl(UserMapper userMapper, UserOAuthBindMapper userOAuthBindMapper, UserLoginLogMapper userLoginLogMapper, UserService userService, AdminUserRoleMapper adminUserRoleMapper, AdminUserGuard adminUserGuard, AdminUserScopeService adminUserScopeService) {
        this.userMapper = userMapper;
        this.userOAuthBindMapper = userOAuthBindMapper;
        this.userLoginLogMapper = userLoginLogMapper;
        this.userService = userService;
        this.adminUserRoleMapper = adminUserRoleMapper;
        this.adminUserGuard = adminUserGuard;
        this.adminUserScopeService = adminUserScopeService;
    }


    private static final Map<String, String> OAUTH_TYPE_NAMES = Map.of(
            "wechat", "微信",
            "qq", "QQ",
            "wework", "企业微信");

    @Override
    public Page<AdminUserListVO> listUsers(
            int current,
            int size,
            String keyword,
            String portalRole,
            Integer status,
            Boolean staffOnly,
            Long operatorId) {
        int page = Math.max(current, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);

        LambdaQueryWrapper<User> wrapper = buildListWrapper(keyword, portalRole, status, staffOnly, operatorId);
        Page<User> raw = userMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Page<AdminUserListVO> result = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        List<AdminUserListVO> records = raw.getRecords().stream().map(this::toListVo).collect(Collectors.toList());
        if (Boolean.TRUE.equals(staffOnly) && !records.isEmpty()) {
            fillAdminRoles(records);
        }
        result.setRecords(records);
        return result;
    }

    @Override
    public List<AdminUserListVO> listUsersForExport(
            String keyword,
            String portalRole,
            Integer status,
            Boolean staffOnly,
            Long operatorId) {
        LambdaQueryWrapper<User> wrapper = buildListWrapper(keyword, portalRole, status, staffOnly, operatorId);
        wrapper.last("LIMIT 5000");
        return userMapper.selectList(wrapper).stream().map(this::toListVo).collect(Collectors.toList());
    }

    private LambdaQueryWrapper<User> buildListWrapper(
            String keyword,
            String portalRole,
            Integer status,
            Boolean staffOnly,
            Long operatorId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (Boolean.TRUE.equals(staffOnly)) {
            wrapper.eq(User::getRole, RoleUtils.STAFF_ROLE);
        } else if (Boolean.FALSE.equals(staffOnly)) {
            wrapper.ne(User::getRole, RoleUtils.STAFF_ROLE);
        }
        if (StringUtils.hasText(portalRole)) {
            wrapper.eq(User::getRole, portalRole.trim());
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(User::getUsername, kw)
                    .or().like(User::getNickname, kw)
                    .or().like(User::getPhone, kw));
        }
        applyAuditUploaderScope(wrapper, operatorId);
        wrapper.orderByDesc(User::getCreateTime);
        return wrapper;
    }

    private void applyAuditUploaderScope(LambdaQueryWrapper<User> wrapper, Long operatorId) {
        if (!adminUserScopeService.isAuditUploaderScope(operatorId)) {
            return;
        }
        List<Long> allowed = adminUserScopeService.listVisibleUploaderUserIds(operatorId);
        if (allowed.isEmpty()) {
            wrapper.eq(User::getId, -1L);
        } else {
            wrapper.in(User::getId, allowed);
        }
    }

    @Override
    public AdminUserDetailVO getUserDetail(Long id, Long operatorId) {
        adminUserScopeService.assertCanViewUser(operatorId, id);
        User user = requireUser(id);
        AdminUserDetailVO vo = toDetailVo(user);
        vo.setOauthBinds(listOAuthBinds(id));
        return vo;
    }

    @Override
    @Transactional
    public void updateUser(Long id, AdminUserUpdateDTO dto, Long operatorId) {
        User user = requireUser(id);
        if (RoleUtils.isAdmin(user.getRole())) {
            throw new BusinessException(400, "请在「管理员账号」中管理 staff 用户");
        }
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname().trim());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail().trim());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (dto.getBirthday() != null) {
            user.setBirthday(dto.getBirthday());
        }
        if (StringUtils.hasText(dto.getPortalRole())) {
            RoleUtils.requireAdminAssignableRole(dto.getPortalRole());
            user.setRole(dto.getPortalRole().trim());
        }
        userMapper.updateById(user);
    }

    @Override
    public void disableUser(Long id, Long operatorId) {
        User user = requireUser(id);
        if (RoleUtils.isAdmin(user.getRole())) {
            adminUserGuard.assertCanManageStaffAccount(operatorId, id);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return;
        }
        userService.updateStatus(id, 0);
    }

    @Override
    public void enableUser(Long id, Long operatorId) {
        User user = requireUser(id);
        if (RoleUtils.isAdmin(user.getRole())) {
            adminUserGuard.assertCanManageStaffAccount(operatorId, id);
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            return;
        }
        userService.updateStatus(id, 1);
    }

    @Override
    public String resetPassword(Long id, Long operatorId) {
        User user = requireUser(id);
        if (RoleUtils.isAdmin(user.getRole())) {
            adminUserGuard.assertCanManageStaffAccount(operatorId, id);
        }
        return userService.resetPassword(id);
    }

    @Override
    public int batchUpdateStatus(AdminUserBatchStatusDTO dto, Long operatorId) {
        int targetStatus = dto.getStatus() != null && dto.getStatus() == 0 ? 0 : 1;
        int count = 0;
        for (Long id : dto.getIds()) {
            if (id == null) {
                continue;
            }
            if (targetStatus == 0) {
                disableUser(id, operatorId);
            } else {
                enableUser(id, operatorId);
            }
            count++;
        }
        return count;
    }

    private User requireUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private AdminUserListVO toListVo(User user) {
        AdminUserListVO vo = new AdminUserListVO();
        fillBase(vo, user);
        vo.setLastLoginTime(userLoginLogMapper.findLastSuccessTime(user.getId()));
        return vo;
    }

    private AdminUserDetailVO toDetailVo(User user) {
        AdminUserDetailVO vo = new AdminUserDetailVO();
        fillBase(vo, user);
        vo.setEmail(user.getEmail());
        vo.setGender(user.getGender());
        vo.setBirthday(user.getBirthday());
        vo.setUpdateTime(user.getUpdateTime());
        vo.setLastLoginTime(userLoginLogMapper.findLastSuccessTime(user.getId()));
        return vo;
    }

    private void fillBase(AdminUserListVO vo, User user) {
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhoneMasked(maskPhone(user.getPhone()));
        vo.setPortalRole(user.getRole());
        vo.setPortalRoleName(RoleUtils.getRoleName(user.getRole()));
        vo.setStatus(user.getStatus());
        vo.setRegisterFrom(inferRegisterFrom(user));
        vo.setCreateTime(user.getCreateTime());
        vo.setStaff(RoleUtils.isAdmin(user.getRole()));
    }

    private void fillAdminRoles(List<AdminUserListVO> records) {
        List<Long> userIds = records.stream().map(AdminUserListVO::getId).collect(Collectors.toList());
        List<UserRoleRow> rows = adminUserRoleMapper.selectRoleRowsByUserIds(userIds);
        Map<Long, List<UserRoleRow>> byUser = new LinkedHashMap<>();
        for (UserRoleRow row : rows) {
            byUser.computeIfAbsent(row.getUserId(), k -> new ArrayList<>()).add(row);
        }
        for (AdminUserListVO vo : records) {
            List<UserRoleRow> userRoles = byUser.getOrDefault(vo.getId(), List.of());
            vo.setAdminRoles(userRoles.stream().map(UserRoleRow::getRoleCode).collect(Collectors.toList()));
            vo.setAdminRoleLabel(userRoles.stream().map(UserRoleRow::getRoleName).collect(Collectors.joining("、")));
        }
    }

    private List<AdminOAuthBindVO> listOAuthBinds(Long userId) {
        List<UserOAuthBind> binds = userOAuthBindMapper.selectList(
                new LambdaQueryWrapper<UserOAuthBind>().eq(UserOAuthBind::getUserId, userId));
        return binds.stream().map(b -> {
            AdminOAuthBindVO vo = new AdminOAuthBindVO();
            vo.setId(b.getId());
            vo.setOauthType(b.getOauthType());
            vo.setOauthTypeName(OAUTH_TYPE_NAMES.getOrDefault(
                    b.getOauthType() != null ? b.getOauthType().toLowerCase(Locale.ROOT) : "",
                    b.getOauthType()));
            vo.setOauthId(maskOAuthId(b.getOauthId()));
            vo.setNickname(b.getNickname());
            vo.setAvatar(b.getAvatar());
            vo.setBindTime(b.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    private String inferRegisterFrom(User user) {
        Long bindCount = userOAuthBindMapper.selectCount(
                new LambdaQueryWrapper<UserOAuthBind>().eq(UserOAuthBind::getUserId, user.getId()));
        if (bindCount != null && bindCount > 0) {
            return "oauth";
        }
        if (StringUtils.hasText(user.getPhone())
                && user.getPhone().equals(user.getUsername())) {
            return "phone";
        }
        if (StringUtils.hasText(user.getOauthType()) && StringUtils.hasText(user.getOauthId())) {
            return "oauth";
        }
        return "account";
    }

    static String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private static String maskOAuthId(String id) {
        if (!StringUtils.hasText(id) || id.length() <= 8) {
            return id;
        }
        return id.substring(0, 4) + "****" + id.substring(id.length() - 4);
    }
}
