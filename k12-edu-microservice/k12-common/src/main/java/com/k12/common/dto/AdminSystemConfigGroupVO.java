package com.k12.common.dto;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class AdminSystemConfigGroupVO {

    private String groupCode;
    private Map<String, Object> values = new LinkedHashMap<>();
    private List<AdminSystemConfigFieldVO> fields;
}
