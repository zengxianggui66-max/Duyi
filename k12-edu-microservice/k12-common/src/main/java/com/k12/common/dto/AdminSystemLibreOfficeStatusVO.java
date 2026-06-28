package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminSystemLibreOfficeStatusVO {

    private Boolean configured;
    private Boolean reachable;
    private String path;
}
