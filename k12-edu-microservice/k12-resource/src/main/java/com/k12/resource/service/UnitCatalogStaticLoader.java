package com.k12.resource.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.common.dto.UnitTreeNodeVO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * classpath unit-catalog.json 只读访问（与 DB 树解耦，避免 CatalogService 循环依赖）
 */
@Slf4j
@Component
public class UnitCatalogStaticLoader {

    private final ObjectMapper objectMapper;
    public UnitCatalogStaticLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    private Map<String, List<UnitTreeNodeVO>> catalogByVolumeKey = Map.of();

    @PostConstruct
    public void loadCatalog() {
        try (InputStream in = new ClassPathResource("catalog/unit-catalog.json").getInputStream()) {
            catalogByVolumeKey = objectMapper.readValue(in, new TypeReference<>() {});
            log.info("单元目录静态配置已加载，共 {} 个册别", catalogByVolumeKey.size());
        } catch (Exception e) {
            log.warn("单元目录静态配置加载失败，将仅使用数据库数据: {}", e.getMessage());
            catalogByVolumeKey = Map.of();
        }
    }

    public List<UnitTreeNodeVO> getTree(String volumeKey, String gradeName) {
        String key = resolveVolumeKey(volumeKey, gradeName);
        return copyTree(catalogByVolumeKey.get(key));
    }

    public String resolveVolumeKey(String volumeKey, String gradeName) {
        if (StringUtils.hasText(volumeKey)) {
            return volumeKey.trim();
        }
        if (!StringUtils.hasText(gradeName)) {
            return "";
        }
        if (gradeName.contains("一年级") && gradeName.contains("下册")) return "y1s2";
        if (gradeName.contains("一年级") && gradeName.contains("上册")) return "y1s1";
        if (gradeName.contains("二年级") && gradeName.contains("下册")) return "y2s2";
        if (gradeName.contains("二年级") && gradeName.contains("上册")) return "y2s1";
        if (gradeName.contains("三年级") && gradeName.contains("下册")) return "y3s2";
        if (gradeName.contains("三年级") && gradeName.contains("上册")) return "y3s1";
        if (gradeName.contains("七年级") && gradeName.contains("下册")) return "j7s2";
        if (gradeName.contains("七年级") && gradeName.contains("上册")) return "j7s1";
        if (gradeName.contains("高一") && gradeName.contains("下册")) return "s10s2";
        if (gradeName.contains("高一") && gradeName.contains("上册")) return "s10s1";
        return "";
    }

    private List<UnitTreeNodeVO> copyTree(List<UnitTreeNodeVO> source) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        List<UnitTreeNodeVO> copy = new ArrayList<>();
        for (UnitTreeNodeVO node : source) {
            UnitTreeNodeVO n = new UnitTreeNodeVO();
            n.setName(node.getName());
            n.setSubUnits(new ArrayList<>(node.getSubUnits() != null ? node.getSubUnits() : List.of()));
            copy.add(n);
        }
        return copy;
    }
}
