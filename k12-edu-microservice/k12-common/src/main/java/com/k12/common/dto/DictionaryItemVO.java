package com.k12.common.dto;

import com.k12.common.entity.EduBrowseTag;
import com.k12.common.entity.EduExamScene;
import com.k12.common.entity.EduFileFormat;
import com.k12.common.entity.EduRegion;
import com.k12.common.entity.EduTeachingScene;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 字典项 C 端读模型
 */
@Data
public class DictionaryItemVO {

    private Integer id;
    private String code;
    private String name;
    private Integer sort;
    private Integer status;
    private String examLevel;
    private String extensions;
    private String previewType;
    private Integer parentId;
    private Integer level;
    private String tagGroup;
    private List<String> applicableStages;
    private List<String> applicableModules;

    public static DictionaryItemVO fromExamScene(EduExamScene row) {
        DictionaryItemVO vo = new DictionaryItemVO();
        vo.setId(row.getId());
        vo.setCode(row.getCode());
        vo.setName(row.getName());
        vo.setExamLevel(row.getExamLevel());
        vo.setSort(row.getSort());
        vo.setStatus(row.getStatus());
        return vo;
    }

    public static DictionaryItemVO fromTeachingScene(EduTeachingScene row) {
        DictionaryItemVO vo = new DictionaryItemVO();
        vo.setId(row.getId());
        vo.setCode(row.getCode());
        vo.setName(row.getName());
        vo.setSort(row.getSort());
        vo.setStatus(row.getStatus());
        return vo;
    }

    public static DictionaryItemVO fromFileFormat(EduFileFormat row) {
        DictionaryItemVO vo = new DictionaryItemVO();
        vo.setId(row.getId());
        vo.setCode(row.getCode());
        vo.setName(row.getName());
        vo.setExtensions(row.getExtensions());
        vo.setPreviewType(row.getPreviewType());
        vo.setSort(row.getSort());
        vo.setStatus(row.getStatus());
        return vo;
    }

    public static DictionaryItemVO fromRegion(EduRegion row) {
        DictionaryItemVO vo = new DictionaryItemVO();
        vo.setId(row.getId());
        vo.setCode(row.getCode());
        vo.setName(row.getName());
        vo.setParentId(row.getParentId());
        vo.setLevel(row.getLevel());
        vo.setSort(row.getSort());
        vo.setStatus(row.getStatus());
        return vo;
    }

    public static DictionaryItemVO fromBrowseTag(EduBrowseTag row, List<String> stages, List<String> modules) {
        DictionaryItemVO vo = new DictionaryItemVO();
        vo.setId(row.getId());
        vo.setCode(row.getCode());
        vo.setName(row.getName());
        vo.setTagGroup(row.getTagGroup());
        vo.setApplicableStages(stages != null ? stages : Collections.emptyList());
        vo.setApplicableModules(modules != null ? modules : Collections.emptyList());
        vo.setSort(row.getSort());
        vo.setStatus(row.getStatus());
        return vo;
    }
}
