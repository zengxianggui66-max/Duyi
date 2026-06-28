package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HomeLatestColumnWriteDTO {

    @NotBlank
    private String title;

    @NotBlank
    private String morePath;

    @NotBlank
    private String dataSource;

    private HomeLatestRuleDTO rule;

    private Integer sort;

    private Integer status;

    private String remark;
}
