package com.k12.auth.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.auth.mapper.UserAdminRemarkMapper;
import com.k12.auth.mapper.UserMapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.AdminUserRemarkCreateDTO;
import com.k12.common.dto.AdminUserRemarkVO;
import com.k12.common.entity.User;
import com.k12.common.entity.UserAdminRemark;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class AdminUserRemarkService {

    private final UserAdminRemarkMapper userAdminRemarkMapper;
    private final UserMapper userMapper;
    private final AdminUserScopeService adminUserScopeService;
    public AdminUserRemarkService(UserAdminRemarkMapper userAdminRemarkMapper, UserMapper userMapper, AdminUserScopeService adminUserScopeService) {
        this.userAdminRemarkMapper = userAdminRemarkMapper;
        this.userMapper = userMapper;
        this.adminUserScopeService = adminUserScopeService;
    }


    public Page<AdminUserRemarkVO> listRemarks(Long userId, int current, int size, Long operatorId) {
        adminUserScopeService.assertCanViewUser(operatorId, userId);
        requireUser(userId);
        int page = Math.max(current, 1);
        int pageSize = Math.min(Math.max(size, 1), 50);
        Page<UserAdminRemark> pg = userAdminRemarkMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<UserAdminRemark>()
                        .eq(UserAdminRemark::getUserId, userId)
                        .orderByDesc(UserAdminRemark::getCreateTime));
        Page<AdminUserRemarkVO> result = new Page<>(pg.getCurrent(), pg.getSize(), pg.getTotal());
        result.setRecords(pg.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return result;
    }

    @Transactional
    public AdminUserRemarkVO addRemark(Long userId, AdminUserRemarkCreateDTO dto, Long operatorId, String operatorName) {
        adminUserScopeService.assertCanViewUser(operatorId, userId);
        requireUser(userId);
        UserAdminRemark row = new UserAdminRemark();
        row.setUserId(userId);
        row.setAdminUserId(operatorId != null ? operatorId : 0L);
        row.setAdminUsername(operatorName);
        row.setContent(dto.getContent().trim());
        userAdminRemarkMapper.insert(row);
        return toVo(row);
    }

    private User requireUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private AdminUserRemarkVO toVo(UserAdminRemark row) {
        AdminUserRemarkVO vo = new AdminUserRemarkVO();
        vo.setId(row.getId());
        vo.setUserId(row.getUserId());
        vo.setAdminUserId(row.getAdminUserId());
        vo.setAdminUsername(row.getAdminUsername());
        vo.setContent(row.getContent());
        vo.setCreateTime(row.getCreateTime());
        return vo;
    }
}
