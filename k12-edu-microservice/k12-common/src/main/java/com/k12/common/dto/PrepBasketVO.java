package com.k12.common.dto;

import com.k12.common.entity.PrepBasketItem;
import lombok.Data;

import java.util.List;

@Data
public class PrepBasketVO {
    private Long basketId;
    private String name;
    private List<PrepBasketItem> items;
    private int totalCount;
    private int resourceCount;
    private int questionCount;
    private int paperCount;
    private int albumCount;
}
