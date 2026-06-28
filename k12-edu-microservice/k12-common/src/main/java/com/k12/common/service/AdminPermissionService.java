package com.k12.common.service;

import com.k12.common.dto.AdminDataScopeVO;
import com.k12.common.mapper.SharedAdminRbacMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "k12.admin.rbac", name = "enabled", havingValue = "true")
public class AdminPermissionService {

    public static final String SUPER_ADMIN = "super_admin";

    private final SharedAdminRbacMapper sharedAdminRbacMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<String> loadPermissionCodes(Long userId) {
        if (userId == null) {
            return List.of();
        }
        if (sharedAdminRbacMapper.countSuperAdminRole(userId) > 0) {
            return sharedAdminRbacMapper.selectAllPermissionCodes();
        }
        return sharedAdminRbacMapper.selectPermissionCodesByUserId(userId);
    }

    public List<String> loadRoleCodes(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return sharedAdminRbacMapper.selectRoleCodesByUserId(userId);
    }

    public boolean hasPermission(Long userId, String permissionCode) {
        if (!StringUtils.hasText(permissionCode) || userId == null) {
            return false;
        }
        if (sharedAdminRbacMapper.countSuperAdminRole(userId) > 0) {
            return true;
        }
        return loadPermissionCodes(userId).contains(permissionCode);
    }

    public AdminDataScopeVO resolveDataScope(Long userId) {
        AdminDataScopeVO vo = new AdminDataScopeVO();
        vo.setScopeType("ALL");
        if (userId == null) {
            return vo;
        }
        if (sharedAdminRbacMapper.countSuperAdminRole(userId) > 0) {
            return vo;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT ds.scope_type, ds.scope_value
                FROM sys_data_scope ds
                INNER JOIN sys_user_role ur ON ur.role_id = ds.role_id
                WHERE ur.user_id = ? AND ds.status = 1
                """, userId);
        if (rows.isEmpty()) {
            return vo;
        }
        Set<String> stages = new LinkedHashSet<>();
        Set<String> subjects = new LinkedHashSet<>();
        boolean hasAll = false;
        boolean hasSelf = false;
        for (Map<String, Object> row : rows) {
            String scopeType = String.valueOf(row.get("scope_type"));
            if ("ALL".equals(scopeType)) {
                hasAll = true;
                break;
            }
            if ("SELF".equals(scopeType)) {
                hasSelf = true;
            }
            if ("AUDIT_UPLOADER".equals(scopeType)) {
                vo.setScopeType("AUDIT_UPLOADER");
                return vo;
            }
            if ("STAGE_SUBJECT".equals(scopeType)) {
                parseStageSubject(row.get("scope_value"), stages, subjects);
            }
        }
        if (hasAll) {
            vo.setScopeType("ALL");
            return vo;
        }
        if (hasSelf) {
            vo.setScopeType("SELF");
            return vo;
        }
        if (!stages.isEmpty() || !subjects.isEmpty()) {
            vo.setScopeType("STAGE_SUBJECT");
            vo.setStages(new ArrayList<>(stages));
            vo.setSubjects(new ArrayList<>(subjects));
        }
        return vo;
    }

    private void parseStageSubject(Object scopeValue, Set<String> stages, Set<String> subjects) {
        if (scopeValue == null) {
            return;
        }
        try {
            Map<String, Object> map = objectMapper.readValue(String.valueOf(scopeValue),
                    new TypeReference<Map<String, Object>>() {});
            Object stageObj = map.get("stages");
            Object subjectObj = map.get("subjects");
            if (stageObj instanceof List<?> stageList) {
                stageList.forEach(v -> stages.add(String.valueOf(v)));
            }
            if (subjectObj instanceof List<?> subjectList) {
                subjectList.forEach(v -> subjects.add(String.valueOf(v)));
            }
        } catch (Exception ignored) {
            // ignore malformed JSON
        }
    }
}
