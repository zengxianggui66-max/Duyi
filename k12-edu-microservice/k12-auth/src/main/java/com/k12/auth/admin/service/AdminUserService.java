package com.k12.auth.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.AdminUserBatchStatusDTO;
import com.k12.common.dto.AdminUserDetailVO;
import com.k12.common.dto.AdminUserListVO;
import com.k12.common.dto.AdminUserUpdateDTO;

import java.util.List;

public interface AdminUserService {

    Page<AdminUserListVO> listUsers(
            int current,
            int size,
            String keyword,
            String portalRole,
            Integer status,
            Boolean staffOnly,
            Long operatorId);

    List<AdminUserListVO> listUsersForExport(
            String keyword,
            String portalRole,
            Integer status,
            Boolean staffOnly,
            Long operatorId);

    AdminUserDetailVO getUserDetail(Long id, Long operatorId);

    void updateUser(Long id, AdminUserUpdateDTO dto, Long operatorId);

    void disableUser(Long id, Long operatorId);

    void enableUser(Long id, Long operatorId);

    String resetPassword(Long id, Long operatorId);

    int batchUpdateStatus(AdminUserBatchStatusDTO dto, Long operatorId);
}
