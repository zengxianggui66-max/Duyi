package com.k12.resource.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.NavTargetDTO;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * Validates and serializes NavTarget JSON for home ops CMS.
 */
public final class HomeNavTargetValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "browse", "search", "route", "external", "scroll", "vip");

    private HomeNavTargetValidator() {
    }

    public static NavTargetDTO parseAndValidate(String json) {
        if (!StringUtils.hasText(json)) {
            throw new BusinessException(400, "nav_target 不能为空");
        }
        try {
            NavTargetDTO target = MAPPER.readValue(json, NavTargetDTO.class);
            validate(target);
            return target;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(400, "nav_target JSON 格式无效");
        }
    }

    public static NavTargetDTO validate(NavTargetDTO target) {
        if (target == null || !StringUtils.hasText(target.getType())) {
            throw new BusinessException(400, "nav_target.type 不能为空");
        }
        String type = target.getType().trim();
        if (!ALLOWED_TYPES.contains(type)) {
            throw new BusinessException(400, "不支持的 nav_target.type: " + type);
        }
        switch (type) {
            case "browse" -> {
                requireText(target.getStageKey(), "browse 类型需要 stageKey");
                requireText(target.getSubjectKey(), "browse 类型需要 subjectKey");
                requireText(target.getVersionKey(), "browse 类型需要 versionKey");
            }
            case "search" -> {
                requireText(target.getKeyword(), "search 类型需要 keyword");
                if (target.getSearchEngine() == null) {
                    target.setSearchEngine("mysql");
                }
            }
            case "route" -> requireText(target.getRoutePath(), "route 类型需要 routePath");
            case "external" -> requireText(target.getExternalUrl(), "external 类型需要 externalUrl");
            case "scroll" -> requireText(target.getScrollTarget(), "scroll 类型需要 scrollTarget");
            case "vip" -> { /* no extra fields */ }
            default -> throw new BusinessException(400, "不支持的 nav_target.type: " + type);
        }
        target.setType(type);
        return target;
    }

    public static String toJson(NavTargetDTO target) {
        NavTargetDTO validated = validate(target);
        try {
            return MAPPER.writeValueAsString(validated);
        } catch (JsonProcessingException e) {
            throw new BusinessException(400, "nav_target 序列化失败");
        }
    }

    public static NavTargetDTO parseLenient(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            NavTargetDTO target = MAPPER.readValue(json, NavTargetDTO.class);
            validate(target);
            return target;
        } catch (Exception e) {
            return null;
        }
    }

    public static String stageKeysToJson(List<String> stageKeys) {
        if (stageKeys == null || stageKeys.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(stageKeys);
        } catch (JsonProcessingException e) {
            throw new BusinessException(400, "stageKeys 格式无效");
        }
    }

    private static void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, message);
        }
    }
}
