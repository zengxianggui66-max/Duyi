package com.k12.common.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class HomeHotWordReorderDTO {

    @NotEmpty
    private List<Item> items;

    @Data
    public static class Item {
        private Long id;
        private Integer sort;
    }
}
