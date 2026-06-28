package com.k12.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminResourceBatchDTO {

    private List<Long> ids;
    private String action;
}
