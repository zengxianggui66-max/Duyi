package com.k12.resource.service;

import com.k12.common.BusinessException;
import com.k12.common.dto.TaxonomyEditionVO;
import com.k12.common.dto.TaxonomyGradeVO;
import com.k12.common.dto.TaxonomyModuleVO;
import com.k12.common.dto.TaxonomyResourceTypeVO;
import com.k12.common.dto.TaxonomySubjectVO;
import com.k12.common.dto.TaxonomyVolumeVO;
import com.k12.common.entity.EduCatalogNode;
import com.k12.common.entity.EduStage;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.resource.mapper.EduCatalogNodeMapper;
import com.k12.resource.mapper.EduStageMapper;
import com.k12.resource.util.UploadPlacementCatalog;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Phase 2 Step 2：上传落位 taxonomy/catalog 服务端校验
 */
@Service
public class UploadPlacementValidator {

    private final TaxonomyReadService taxonomyReadService;
    private final EduCatalogNodeMapper eduCatalogNodeMapper;
    /** Phase 6：学段状态检查 */
    private final EduStageMapper eduStageMapper;
    public UploadPlacementValidator(TaxonomyReadService taxonomyReadService, EduCatalogNodeMapper eduCatalogNodeMapper, EduStageMapper eduStageMapper) {
        this.taxonomyReadService = taxonomyReadService;
        this.eduCatalogNodeMapper = eduCatalogNodeMapper;
        this.eduStageMapper = eduStageMapper;
    }


    /** 草稿：仅校验已填字段的一致性 */
    public void validateForSave(PrimaryChineseResource resource) {
        if (resource == null) {
            return;
        }
        validatePlacementFields(resource, false);
    }

    /** 提交审核：关键落位字段必填且合法 */
    public void validateForSubmit(PrimaryChineseResource resource) {
        if (resource == null) {
            throw new BusinessException(400, "资源数据不能为空");
        }
        if (!StringUtils.hasText(resource.getStage())) {
            throw new BusinessException(400, "请选择学段");
        }
        if (!StringUtils.hasText(resource.getSubject())) {
            throw new BusinessException(400, "请选择学科");
        }
        if (!StringUtils.hasText(resource.getType())) {
            throw new BusinessException(400, "请选择资源类型");
        }
        validatePlacementFields(resource, true);
    }

    /**
     * 校验年级 code 与学段（供 API 测试与后续扩展字段）
     */
    public void validateGradeCode(String stage, String gradeCode) {
        if (!StringUtils.hasText(stage) || !StringUtils.hasText(gradeCode)) {
            return;
        }
        if (!UploadPlacementCatalog.gradeCodeBelongsToStage(stage, gradeCode)) {
            throw new BusinessException(400, "年级与学段不匹配");
        }
        List<TaxonomyGradeVO> grades = taxonomyReadService.listGrades(stage, false);
        boolean inTaxonomy = grades.stream().anyMatch(g -> matchesCode(g.getCode(), gradeCode));
        if (!inTaxonomy && !grades.isEmpty()) {
            throw new BusinessException(400, "年级不存在或已禁用");
        }
    }

    private void validatePlacementFields(PrimaryChineseResource resource, boolean strict) {
        String stage = resource.getStage();
        if (!StringUtils.hasText(stage)) {
            return;
        }

        // Phase 6：校验学段是否启用
        EduStage stageEntity = eduStageMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EduStage>()
                        .eq(EduStage::getCode, stage));
        if (stageEntity != null && (stageEntity.getStatus() == null || stageEntity.getStatus() != 1)) {
            throw new BusinessException(400, "学段「" + stage + "」已禁用，请重新选择");
        }

        if (StringUtils.hasText(resource.getSubject())) {
            requireSubjectInStage(stage, resource.getSubject());
        }

        if (StringUtils.hasText(resource.getGradeName())) {
            if (!UploadPlacementCatalog.belongsToStage(stage, resource.getGradeName())) {
                throw new BusinessException(400, "教材册别与学段不匹配");
            }
            requireVolumeInTaxonomy(stage, resource.getGradeName());
        }

        if (StringUtils.hasText(resource.getEdition()) && StringUtils.hasText(resource.getSubject())) {
            requireEditionInTaxonomy(stage, resource.getSubject(), resource.getEdition());
        }

        if (StringUtils.hasText(resource.getModule())) {
            requireModuleInTaxonomy(stage, resource.getModule());
        }

        if (StringUtils.hasText(resource.getType())) {
            requireTypeInTaxonomy(
                    stage,
                    resource.getSubject(),
                    resource.getModule(),
                    resource.getType());
        }

        if (resource.getCatalogNodeId() != null) {
            requireCatalogNodeActive(resource.getCatalogNodeId());
        }

        if (strict && StringUtils.hasText(resource.getModule())
                && isSyncModule(resource.getModule())
                && !StringUtils.hasText(resource.getGradeName())) {
            throw new BusinessException(400, "同步类资源请选择教材册别");
        }
    }

    private void requireSubjectInStage(String stage, String subject) {
        List<TaxonomySubjectVO> subjects = taxonomyReadService.listSubjects(stage, false);
        if (subjects.isEmpty()) {
            return;
        }
        boolean ok = subjects.stream().anyMatch(s -> matchesCodeOrName(s.getCode(), s.getName(), subject));
        if (!ok) {
            throw new BusinessException(400, "学科与学段不匹配或已禁用");
        }
    }

    private void requireVolumeInTaxonomy(String stage, String gradeName) {
        List<TaxonomyVolumeVO> volumes = taxonomyReadService.listVolumes(stage, false);
        if (volumes.isEmpty()) {
            return;
        }
        boolean ok = volumes.stream().anyMatch(v -> gradeName.equals(v.getName()));
        if (!ok) {
            throw new BusinessException(400, "教材册别不存在或已禁用");
        }
    }

    private void requireEditionInTaxonomy(String stage, String subject, String edition) {
        List<TaxonomyEditionVO> editions = taxonomyReadService.listEditions(stage, subject, false);
        if (editions.isEmpty()) {
            return;
        }
        boolean ok = editions.stream().anyMatch(e -> matchesCodeOrName(e.getCode(), e.getName(), edition));
        if (!ok) {
            throw new BusinessException(400, "教材版本与学段学科不匹配或已禁用");
        }
    }

    private void requireModuleInTaxonomy(String stage, String module) {
        List<TaxonomyModuleVO> modules = taxonomyReadService.listModules(stage, false);
        if (modules.isEmpty()) {
            return;
        }
        boolean ok = modules.stream().anyMatch(m -> matchesCodeOrName(m.getCode(), m.getName(), module));
        if (!ok) {
            throw new BusinessException(400, "栏目与学段不匹配或已禁用");
        }
    }

    private void requireTypeInTaxonomy(String stage, String subject, String module, String type) {
        List<TaxonomyResourceTypeVO> types = taxonomyReadService.listResourceTypes(
                stage, subject, module, false);
        if (types.isEmpty()) {
            return;
        }
        boolean ok = types.stream().anyMatch(t -> matchesCodeOrName(t.getCode(), t.getName(), type));
        if (!ok) {
            throw new BusinessException(400, "资源类型与栏目不匹配或已禁用");
        }
    }

    private void requireCatalogNodeActive(Long catalogNodeId) {
        EduCatalogNode node = eduCatalogNodeMapper.selectById(catalogNodeId);
        if (node == null) {
            throw new BusinessException(400, "目录节点不存在");
        }
        if (node.getStatus() == null || node.getStatus() != 1) {
            throw new BusinessException(400, "目录节点已禁用");
        }
    }

    private static boolean isSyncModule(String module) {
        return module.contains("同步") || module.contains("备课");
    }

    private static boolean matchesCode(String code, String input) {
        return StringUtils.hasText(code) && code.trim().equalsIgnoreCase(input.trim());
    }

    private static boolean matchesCodeOrName(String code, String name, String input) {
        if (!StringUtils.hasText(input)) {
            return false;
        }
        String trimmed = input.trim();
        return Objects.equals(code, trimmed) || Objects.equals(name, trimmed);
    }
}
