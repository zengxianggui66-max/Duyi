package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AdminTaxonomySubjectWriteDTO {

    @NotNull(message = "学段不能为空")
    private Integer stageId;

    @NotBlank(message = "编码不能为空")
    private String code;

    @NotBlank(message = "名称不能为空")
    private String name;

    private String icon;
    private Integer sort;
    private Integer status;
    /** 绑定的教材版本 ID 列表 */
    private List<Integer> editionIds;
    /** 绑定的栏目 ID 列表（首页浮层/浏览页可见栏目） */
    private List<Integer> moduleIds;
    /** 绑定的同步备课资料类型 ID 列表（叶子节点） */
    private List<Integer> resourceTypeIds;
}
