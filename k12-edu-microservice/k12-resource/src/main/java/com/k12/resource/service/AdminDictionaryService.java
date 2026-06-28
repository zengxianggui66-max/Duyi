package com.k12.resource.service;

import com.k12.common.dto.*;
import com.k12.common.entity.*;

import java.util.List;

/**
 * Phase 5-D：字典管理端 CRUD
 */
public interface AdminDictionaryService {

    List<EduExamScene> listExamScenes(boolean includeDisabled);

    EduExamScene createExamScene(AdminDictionaryExamSceneWriteDTO dto, Long adminUserId);

    EduExamScene updateExamScene(Integer id, AdminDictionaryExamSceneWriteDTO dto, Long adminUserId);

    void setExamSceneStatus(Integer id, Integer status, Long adminUserId);

    void deleteExamScene(Integer id, Long adminUserId);

    List<EduFileFormat> listFileFormats(boolean includeDisabled);

    EduFileFormat createFileFormat(AdminDictionaryFileFormatWriteDTO dto, Long adminUserId);

    EduFileFormat updateFileFormat(Integer id, AdminDictionaryFileFormatWriteDTO dto, Long adminUserId);

    void setFileFormatStatus(Integer id, Integer status, Long adminUserId);

    void deleteFileFormat(Integer id, Long adminUserId);

    List<EduRegion> listRegions(Integer parentId, boolean includeDisabled);

    EduRegion createRegion(AdminDictionaryRegionWriteDTO dto, Long adminUserId);

    EduRegion updateRegion(Integer id, AdminDictionaryRegionWriteDTO dto, Long adminUserId);

    void setRegionStatus(Integer id, Integer status, Long adminUserId);

    void deleteRegion(Integer id, Long adminUserId);

    List<EduTeachingScene> listTeachingScenes(boolean includeDisabled);

    EduTeachingScene createTeachingScene(AdminDictionaryTeachingSceneWriteDTO dto, Long adminUserId);

    EduTeachingScene updateTeachingScene(Integer id, AdminDictionaryTeachingSceneWriteDTO dto, Long adminUserId);

    void setTeachingSceneStatus(Integer id, Integer status, Long adminUserId);

    void deleteTeachingScene(Integer id, Long adminUserId);

    List<EduBrowseTag> listBrowseTags(boolean includeDisabled);

    EduBrowseTag createBrowseTag(AdminDictionaryBrowseTagWriteDTO dto, Long adminUserId);

    EduBrowseTag updateBrowseTag(Integer id, AdminDictionaryBrowseTagWriteDTO dto, Long adminUserId);

    void setBrowseTagStatus(Integer id, Integer status, Long adminUserId);

    void deleteBrowseTag(Integer id, Long adminUserId);
}
