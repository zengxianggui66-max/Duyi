package com.k12.resource.service;

import com.k12.common.dto.TaxonomyEditionVO;
import com.k12.common.dto.TaxonomyModuleVO;
import com.k12.common.dto.TaxonomyResourceTypeVO;
import com.k12.common.dto.TaxonomyVolumeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 上传位置筛选项：纯 taxonomy 聚合（Phase 2 Step 2，不再 merge 历史 DISTINCT 脏数据）
 */
@Slf4j
@Service
public class UploadPlacementService {

    private final TaxonomyReadService taxonomyReadService;
    public UploadPlacementService(TaxonomyReadService taxonomyReadService) {
        this.taxonomyReadService = taxonomyReadService;
    }


    public Map<String, Object> getUploadFilterOptions(String stage, String subject, String module) {
        Map<String, Object> options = new HashMap<>();
        String stageKey = StringUtils.hasText(stage) ? stage.trim() : "";
        String subjectKey = StringUtils.hasText(subject) ? subject.trim() : "";
        String moduleKey = StringUtils.hasText(module) ? module.trim() : "";

        options.put("gradeNames", listVolumeNames(stageKey));
        options.put("editions", listEditionNames(stageKey, subjectKey));
        options.put("modules", listModuleNames(stageKey));
        options.put("types", listTypeNames(stageKey, subjectKey, moduleKey));
        return options;
    }

    private List<String> listVolumeNames(String stageKey) {
        if (!StringUtils.hasText(stageKey)) {
            return List.of();
        }
        List<TaxonomyVolumeVO> volumes = taxonomyReadService.listVolumes(stageKey, false);
        List<String> names = new ArrayList<>();
        for (TaxonomyVolumeVO volume : volumes) {
            if (StringUtils.hasText(volume.getName())) {
                names.add(volume.getName());
            }
        }
        return names;
    }

    private List<String> listEditionNames(String stageKey, String subjectKey) {
        if (!StringUtils.hasText(subjectKey)) {
            return List.of();
        }
        List<TaxonomyEditionVO> editions = taxonomyReadService.listEditions(stageKey, subjectKey, false);
        List<String> names = new ArrayList<>();
        for (TaxonomyEditionVO edition : editions) {
            if (StringUtils.hasText(edition.getName())) {
                names.add(edition.getName());
            }
        }
        return names;
    }

    private List<String> listModuleNames(String stageKey) {
        if (!StringUtils.hasText(stageKey)) {
            return List.of();
        }
        List<TaxonomyModuleVO> modules = taxonomyReadService.listModules(stageKey, false);
        List<String> names = new ArrayList<>();
        for (TaxonomyModuleVO module : modules) {
            if (StringUtils.hasText(module.getName())) {
                names.add(module.getName());
            }
        }
        return names;
    }

    private List<String> listTypeNames(String stageKey, String subjectKey, String moduleKey) {
        if (!StringUtils.hasText(stageKey)) {
            return List.of();
        }
        List<TaxonomyResourceTypeVO> types = taxonomyReadService.listResourceTypes(
                stageKey, subjectKey, moduleKey, false);
        List<String> names = new ArrayList<>();
        for (TaxonomyResourceTypeVO type : types) {
            if (StringUtils.hasText(type.getName())) {
                names.add(type.getName());
            }
        }
        return names;
    }
}
