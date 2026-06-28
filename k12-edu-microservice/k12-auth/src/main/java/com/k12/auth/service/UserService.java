package com.k12.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.k12.auth.mapper.UserMapper;
import com.k12.common.BusinessException;
import com.k12.common.entity.User;
import com.k12.common.util.RoleUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    private final PasswordEncoder passwordEncoder;
    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }


    /**
     * 分页获取用户列表
     */
    public Page<User> listUsers(int current, int size, String keyword) {
        Page<User> page = new Page<>(current, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("username", keyword)
                    .or().like("nickname", keyword)
                    .or().like("phone", keyword));
        }
        return page(page, wrapper);
    }

    /**
     * 获取用户详情
     */
    public User getUser(Long id) {
        return getById(id);
    }

    /**
     * 更新用户状态
     */
    public void updateStatus(Long id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        updateById(user);
    }

    /**
     * 重置密码
     */
    public String resetPassword(Long id) {
        String newPassword = "123456";
        User user = new User();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
        return newPassword;
    }

    /**
     * 管理员修改用户身份
     */
    public void updateRole(Long id, String role) {
        RoleUtils.requireAdminAssignableRole(role);
        User existing = getById(id);
        if (existing == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if ("admin".equals(existing.getRole())) {
            throw new BusinessException(400, "不能修改管理员身份");
        }
        User user = new User();
        user.setId(id);
        user.setRole(role);
        updateById(user);
    }
}
