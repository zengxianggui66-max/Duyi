package com.k12.resource.service;

import com.k12.common.dto.*;
import com.k12.common.entity.*;

import java.util.List;

public interface AdminTaxonomyService {

    List<EduStage> listStages(boolean includeDisabled);

    EduStage createStage(AdminTaxonomyStageWriteDTO dto, Long adminUserId);

    EduStage updateStage(Integer id, AdminTaxonomyStageWriteDTO dto, Long adminUserId);

    void setStageStatus(Integer id, Integer status, Long adminUserId);

    void deleteStage(Integer id, Long adminUserId);

    List<AdminTaxonomySubjectAdminVO> listSubjects(Integer stageId, boolean includeDisabled);

    AdminTaxonomySubjectAdminVO createSubject(AdminTaxonomySubjectWriteDTO dto, Long adminUserId);

    AdminTaxonomySubjectAdminVO updateSubject(Integer id, AdminTaxonomySubjectWriteDTO dto, Long adminUserId);

    void setSubjectStatus(Integer id, Integer status, Long adminUserId);

    void deleteSubject(Integer id, Long adminUserId);

    /** Phase 6：支持按学段/学科筛选版本 */
    List<EduEdition> listEditions(Integer stageId, Integer subjectId, boolean includeDisabled);

    EduEdition createEdition(AdminTaxonomyEditionWriteDTO dto, Long adminUserId);

    EduEdition updateEdition(Integer id, AdminTaxonomyEditionWriteDTO dto, Long adminUserId);

    void setEditionStatus(Integer id, Integer status, Long adminUserId);

    void deleteEdition(Integer id, Long adminUserId);

    List<EduGrade> listGrades(Integer stageId, boolean includeDisabled);

    EduGrade createGrade(AdminTaxonomyGradeWriteDTO dto, Long adminUserId);

    EduGrade updateGrade(Integer id, AdminTaxonomyGradeWriteDTO dto, Long adminUserId);

    void setGradeStatus(Integer id, Integer status, Long adminUserId);

    void deleteGrade(Integer id, Long adminUserId);

    /** Phase 6：支持多维过滤册别 */
    List<EduVolume> listVolumes(Integer stageId, Integer subjectId, Integer editionId, boolean includeDisabled);

    EduVolume createVolume(AdminTaxonomyVolumeWriteDTO dto, Long adminUserId);

    EduVolume updateVolume(Integer id, AdminTaxonomyVolumeWriteDTO dto, Long adminUserId);

    /** Phase 6：册别启用/禁用 */
    void setVolumeStatus(Integer id, Integer status, Long adminUserId);

    void deleteVolume(Integer id, Long adminUserId);

    List<AdminTaxonomyModuleAdminVO> listModules(Integer stageId, boolean includeDisabled);

    AdminTaxonomyModuleAdminVO createModule(AdminTaxonomyModuleWriteDTO dto, Long adminUserId);

    AdminTaxonomyModuleAdminVO updateModule(Integer id, AdminTaxonomyModuleWriteDTO dto, Long adminUserId);

    void setModuleStatus(Integer id, Integer status, Long adminUserId);

    void deleteModule(Integer id, Long adminUserId);

    List<EduResourceType> listResourceTypes(Integer parentId, boolean includeDisabled);

    EduResourceType createResourceType(AdminTaxonomyResourceTypeWriteDTO dto, Long adminUserId);

    EduResourceType updateResourceType(Integer id, AdminTaxonomyResourceTypeWriteDTO dto, Long adminUserId);

    void setResourceTypeStatus(Integer id, Integer status, Long adminUserId);

    void deleteResourceType(Integer id, Long adminUserId);
}
