package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HomeLatestItemWriteDTO {

    @NotBlank
    private String title;

    /** yyyy-MM-dd */
    private String itemDate;

    private Long resourceId;

    private String resourceSource;

    private String linkPath;

    private Long articleId;

    private Integer sort;

    private Integer status;

    private String remark;
}
