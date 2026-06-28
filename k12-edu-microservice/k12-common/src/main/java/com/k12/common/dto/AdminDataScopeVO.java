package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminDataScopeVO {
    private String scopeType;
    private List<String> stages = new ArrayList<>();
    private List<String> subjects = new ArrayList<>();
}
