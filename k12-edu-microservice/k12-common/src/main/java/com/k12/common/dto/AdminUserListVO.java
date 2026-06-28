package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminUserListVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    /** 脱敏手机号 */
    private String phoneMasked;
    /** 门户身份 teacher/student/parent/pending/admin */
    private String portalRole;
    private String portalRoleName;
    private Integer status;
    /** account / phone / oauth */
    private String registerFrom;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
    /** 是否 staff（user.role=admin） */
    private Boolean staff;

    /** 后台 sys_role 编码，如 super_admin */
    private List<String> adminRoles;
    /** 后台角色展示名，逗号分隔 */
    private String adminRoleLabel;
}
