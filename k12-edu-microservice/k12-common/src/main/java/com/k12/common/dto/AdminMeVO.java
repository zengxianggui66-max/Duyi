package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminMeVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
    private List<String> roles = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
}
