package com.k12.auth.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.dto.AdminFeatureFlagItemVO;
import com.k12.common.dto.AdminFeatureFlagsVO;
import com.k12.common.entity.SysConfig;
import com.k12.common.mapper.SysConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FeatureFlagService {

    private record FlagMeta(String shortKey, String label, String scope, String description) {}

    private static final Map<String, FlagMeta> FLAG_META = Map.ofEntries(
            Map.entry("feature.homeOpsApi.enabled",
                    new FlagMeta("homeOpsApiEnabled", "首页运营 API", "runtime", "关闭后 C 端可回退静态 Banner/热词")),
            Map.entry("feature.taxonomyApi.enabled",
                    new FlagMeta("taxonomyApiEnabled", "分类维度 API", "runtime", "学段/学科/版本走 API")),
            Map.entry("feature.dictionaryApi.enabled",
                    new FlagMeta("dictionaryApiEnabled", "字典标签 API", "runtime", "业务字典与标签走 API")),
            Map.entry("feature.resourceUnifiedRead.enabled",
                    new FlagMeta("resourceUnifiedReadEnabled", "统一资源读取总开关", "runtime", "资源频道统一链路总开关")),
            Map.entry("feature.topicUnifiedRead.enabled",
                    new FlagMeta("topicUnifiedReadEnabled", "专题统一读取", "runtime", "专题频道走统一资源读取")),
            Map.entry("feature.cultureUnifiedRead.enabled",
                    new FlagMeta("cultureUnifiedReadEnabled", "传统文化统一读取", "runtime", "传统文化频道走统一资源读取")),
            Map.entry("feature.competitionUnifiedRead.enabled",
                    new FlagMeta("competitionUnifiedReadEnabled", "竞赛统一读取", "runtime", "竞赛频道走统一资源读取")),
            Map.entry("feature.primaryChineseUnifiedRead.enabled",
                    new FlagMeta("primaryChineseUnifiedReadEnabled", "学科统一读取", "runtime", "学科频道走统一资源读取")),
            Map.entry("feature.legacyReadApi410.enabled",
                    new FlagMeta("legacyReadApi410Enabled", "旧读 API 410 下线", "runtime", "开启后 legacy 读 GET 返回 410 并指向 /api/resources/*")),
            Map.entry("feature.catalogBrowse.enabled",
                    new FlagMeta("catalogBrowseEnabled", "目录树浏览", "buildTime", "需重新构建前端（VITE_USE_CATALOG_BROWSE）")),
            Map.entry("feature.masterWrite.enabled",
                    new FlagMeta("masterWriteEnabled", "主表上传写入", "buildTime", "需重新构建前端（VITE_USE_MASTER_WRITE）")),
            Map.entry("feature.searchEngineAuto.enabled",
                    new FlagMeta("searchEngineAutoEnabled", "搜索 OpenSearch auto", "runtime", "searchEngine=auto 时优先引擎"))
    );

    private final SysConfigMapper sysConfigMapper;
    public FeatureFlagService(SysConfigMapper sysConfigMapper) {
        this.sysConfigMapper = sysConfigMapper;
    }


    public Map<String, Boolean> getPublicFlags() {
        Map<String, Boolean> out = new LinkedHashMap<>();
        for (SysConfig row : listFeatureRows()) {
            FlagMeta meta = FLAG_META.get(row.getConfigKey());
            if (meta == null || !"runtime".equals(meta.scope())) {
                continue;
            }
            out.put(meta.shortKey(), parseBool(row.getConfigValue()));
        }
        return out;
    }

    public AdminFeatureFlagsVO getAdminView() {
        AdminFeatureFlagsVO vo = new AdminFeatureFlagsVO();
        for (SysConfig row : listFeatureRows()) {
            FlagMeta meta = FLAG_META.get(row.getConfigKey());
            if (meta == null) {
                continue;
            }
            AdminFeatureFlagItemVO item = new AdminFeatureFlagItemVO();
            item.setKey(meta.shortKey());
            item.setLabel(meta.label());
            item.setScope(meta.scope());
            item.setDescription(meta.description());
            item.setEnabled(parseBool(row.getConfigValue()));
            vo.getFlags().add(item);
        }
        return vo;
    }

    private List<SysConfig> listFeatureRows() {
        return sysConfigMapper.selectList(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getGroupCode, "feature")
                .eq(SysConfig::getValueType, "bool")
                .orderByAsc(SysConfig::getId));
    }

    private boolean parseBool(String raw) {
        return StringUtils.hasText(raw) && Boolean.parseBoolean(raw.trim());
    }
}
