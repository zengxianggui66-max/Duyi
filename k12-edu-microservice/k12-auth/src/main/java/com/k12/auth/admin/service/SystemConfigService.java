package com.k12.auth.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.AdminSystemConfigFieldVO;
import com.k12.common.dto.AdminSystemConfigGroupVO;
import com.k12.common.entity.SysConfig;
import com.k12.common.mapper.SysConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SystemConfigService {

    public static final String SECRET_MASK = "******";

    private static final Set<String> ALLOWED_GROUPS = Set.of(
            "upload", "preview", "storage", "sms", "oauth", "feature");

    private final SysConfigMapper sysConfigMapper;
    private final ObjectMapper objectMapper;
    public SystemConfigService(SysConfigMapper sysConfigMapper, ObjectMapper objectMapper) {
        this.sysConfigMapper = sysConfigMapper;
        this.objectMapper = objectMapper;
    }


    public AdminSystemConfigGroupVO getGroup(String groupCode) {
        String group = requireGroup(groupCode);
        List<SysConfig> rows = listByGroup(group);
        if (rows.isEmpty()) {
            throw new BusinessException(404, "配置分组不存在: " + group);
        }
        return toGroupVo(group, rows);
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminSystemConfigGroupVO updateGroup(String groupCode, Map<String, Object> values, Long operatorId) {
        if (values == null || values.isEmpty()) {
            throw new BusinessException(400, "配置内容不能为空");
        }
        String group = requireGroup(groupCode);
        Map<String, SysConfig> existing = listByGroup(group).stream()
                .collect(Collectors.toMap(this::shortKey, r -> r, (a, b) -> a, LinkedHashMap::new));

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            SysConfig row = existing.get(entry.getKey());
            if (row == null) {
                throw new BusinessException(400, "未知配置项: " + entry.getKey());
            }
            String stored = normalizeIncomingValue(row, entry.getValue());
            validateValue(row, stored);
            row.setConfigValue(stored);
            row.setUpdatedBy(operatorId);
            sysConfigMapper.updateById(row);
        }
        return getGroup(group);
    }

    private String requireGroup(String groupCode) {
        if (!StringUtils.hasText(groupCode)) {
            throw new BusinessException(400, "group 不能为空");
        }
        String group = groupCode.trim().toLowerCase();
        if (!ALLOWED_GROUPS.contains(group)) {
            throw new BusinessException(400, "不支持的配置分组: " + group);
        }
        return group;
    }

    private List<SysConfig> listByGroup(String group) {
        return sysConfigMapper.selectList(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getGroupCode, group)
                .orderByAsc(SysConfig::getId));
    }

    private AdminSystemConfigGroupVO toGroupVo(String group, List<SysConfig> rows) {
        AdminSystemConfigGroupVO vo = new AdminSystemConfigGroupVO();
        vo.setGroupCode(group);
        Map<String, Object> values = new LinkedHashMap<>();
        List<AdminSystemConfigFieldVO> fields = new ArrayList<>();
        for (SysConfig row : rows) {
            String shortKey = shortKey(row);
            values.put(shortKey, toApiValue(row));
            fields.add(toFieldVo(row, shortKey));
        }
        vo.setValues(values);
        vo.setFields(fields);
        return vo;
    }

    private AdminSystemConfigFieldVO toFieldVo(SysConfig row, String shortKey) {
        AdminSystemConfigFieldVO field = new AdminSystemConfigFieldVO();
        field.setKey(shortKey);
        field.setValueType(row.getValueType());
        field.setDescription(row.getDescription());
        field.setRequiresRestart(row.getRequiresRestart() != null && row.getRequiresRestart() == 1);
        if ("secret".equals(row.getValueType())) {
            field.setConfigured(StringUtils.hasText(row.getConfigValue()));
        }
        return field;
    }

    private Object toApiValue(SysConfig row) {
        if ("secret".equals(row.getValueType())) {
            return StringUtils.hasText(row.getConfigValue()) ? SECRET_MASK : "";
        }
        String raw = row.getConfigValue();
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        return switch (row.getValueType()) {
            case "int" -> Integer.parseInt(raw.trim());
            case "bool" -> Boolean.parseBoolean(raw.trim());
            case "json" -> parseJson(raw);
            default -> raw;
        };
    }

    private Object parseJson(String raw) {
        try {
            return objectMapper.readValue(raw, new TypeReference<Object>() {});
        } catch (Exception ex) {
            return raw;
        }
    }

    private String normalizeIncomingValue(SysConfig row, Object value) {
        if ("secret".equals(row.getValueType())) {
            if (value == null) {
                return row.getConfigValue();
            }
            String text = String.valueOf(value).trim();
            if (!StringUtils.hasText(text) || SECRET_MASK.equals(text)) {
                return row.getConfigValue();
            }
            return text;
        }
        if (value == null) {
            throw new BusinessException(400, row.getConfigKey() + " 不能为空");
        }
        if ("json".equals(row.getValueType())) {
            try {
                if (value instanceof String str) {
                    objectMapper.readTree(str);
                    return str;
                }
                return objectMapper.writeValueAsString(value);
            } catch (Exception ex) {
                throw new BusinessException(400, row.getConfigKey() + " JSON 格式无效");
            }
        }
        if ("bool".equals(row.getValueType())) {
            if (value instanceof Boolean b) {
                return Boolean.toString(b);
            }
            return String.valueOf(value).trim();
        }
        return String.valueOf(value).trim();
    }

    private void validateValue(SysConfig row, String stored) {
        if ("upload.maxSizeMb".equals(row.getConfigKey())) {
            int mb;
            try {
                mb = Integer.parseInt(stored);
            } catch (NumberFormatException ex) {
                throw new BusinessException(400, "maxSizeMb 必须为整数");
            }
            if (mb < 1 || mb > 2048) {
                throw new BusinessException(400, "maxSizeMb 必须在 1–2048 之间");
            }
        }
        if ("json".equals(row.getValueType()) && StringUtils.hasText(stored)) {
            try {
                objectMapper.readTree(stored);
            } catch (Exception ex) {
                throw new BusinessException(400, row.getConfigKey() + " JSON 格式无效");
            }
        }
    }

    /** config_key → API 短键 */
    private String shortKey(SysConfig row) {
        return switch (row.getConfigKey()) {
            case "upload.maxSizeMb" -> "maxSizeMb";
            case "upload.allowedFormats" -> "allowedFormats";
            case "preview.enabled" -> "enabled";
            case "preview.libreofficeEnabled" -> "libreofficeEnabled";
            case "preview.libreofficePath" -> "libreofficePath";
            case "preview.poiFallbackEnabled" -> "poiFallbackEnabled";
            case "preview.asyncEnabled" -> "asyncEnabled";
            case "storage.provider" -> "provider";
            case "storage.minio.endpoint" -> "minioEndpoint";
            case "storage.minio.bucketName" -> "minioBucketName";
            case "storage.minio.accessKey" -> "minioAccessKey";
            case "storage.minio.secretKey" -> "minioSecretKey";
            case "sms.provider" -> "provider";
            case "sms.mockEnabled" -> "mockEnabled";
            case "oauth.mockEnabled" -> "mockEnabled";
            case "oauth.weixin.appId" -> "weixinAppId";
            case "oauth.weixin.appSecret" -> "weixinAppSecret";
            case "oauth.qq.appId" -> "qqAppId";
            case "oauth.qq.appSecret" -> "qqAppSecret";
            case "feature.homeOpsApi.enabled" -> "homeOpsApiEnabled";
            case "feature.taxonomyApi.enabled" -> "taxonomyApiEnabled";
            case "feature.dictionaryApi.enabled" -> "dictionaryApiEnabled";
            case "feature.resourceUnifiedRead.enabled" -> "resourceUnifiedReadEnabled";
            case "feature.topicUnifiedRead.enabled" -> "topicUnifiedReadEnabled";
            case "feature.cultureUnifiedRead.enabled" -> "cultureUnifiedReadEnabled";
            case "feature.competitionUnifiedRead.enabled" -> "competitionUnifiedReadEnabled";
            case "feature.primaryChineseUnifiedRead.enabled" -> "primaryChineseUnifiedReadEnabled";
            case "feature.catalogBrowse.enabled" -> "catalogBrowseEnabled";
            case "feature.masterWrite.enabled" -> "masterWriteEnabled";
            case "feature.searchEngineAuto.enabled" -> "searchEngineAutoEnabled";
            default -> row.getConfigKey();
        };
    }
}
