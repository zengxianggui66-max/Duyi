package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminFeatureFlagItemVO {

    private String key;
    private String label;
    private Boolean enabled;
    /** runtime | buildTime */
    private String scope;
    private String description;
}
