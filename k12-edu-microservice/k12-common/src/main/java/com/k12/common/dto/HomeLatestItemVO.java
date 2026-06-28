package com.k12.common.dto;

import lombok.Data;

@Data
public class HomeLatestItemVO {

    private Long id;

    private String title;

    /** yyyy-MM-dd */
    private String date;

    /** resource | article | link */
    private String itemType;

    private Long resourceId;

    private String resourceSource;

    private Long articleId;

    private String linkPath;
}
