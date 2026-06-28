package com.k12.common.dto;

import lombok.Data;

import java.util.Map;

/**
 * 资源栏目（C 端 taxonomy 读模型）
 */
@Data
public class TaxonomyModuleVO {

    private Integer id;
    private String code;
    private String name;
    private String icon;
    private String moduleCategory;
    private String description;
    private Integer sort;
    private Integer status;
    private Integer stageId;
    private String stageCode;
    private String stageName;

    public static TaxonomyModuleVO fromRow(Map<String, Object> row, Integer stageId, String stageCode, String stageName) {
        TaxonomyModuleVO vo = new TaxonomyModuleVO();
        vo.setId(toInt(row.get("id")));
        vo.setCode(toStr(row.get("code")));
        vo.setName(toStr(row.get("name")));
        vo.setIcon(toStr(row.get("icon")));
        vo.setModuleCategory(toStr(row.get("module_category")));
        vo.setDescription(toStr(row.get("description")));
        vo.setSort(toInt(row.get("sort")));
        vo.setStatus(toInt(row.get("status")));
        vo.setStageId(stageId);
        vo.setStageCode(stageCode);
        vo.setStageName(stageName);
        return vo;
    }

    public static TaxonomyModuleVO fromFallback(int id, String code, String name, EduStageRef stage, int sort) {
        TaxonomyModuleVO vo = new TaxonomyModuleVO();
        vo.setId(id);
        vo.setCode(code);
        vo.setName(name);
        vo.setSort(sort);
        vo.setStatus(1);
        if (stage != null) {
            vo.setStageId(stage.id());
            vo.setStageCode(stage.code());
            vo.setStageName(stage.name());
        }
        return vo;
    }

    private static String toStr(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public record EduStageRef(Integer id, String code, String name) {}
}
