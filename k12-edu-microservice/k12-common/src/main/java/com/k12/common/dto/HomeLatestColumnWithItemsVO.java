package com.k12.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class HomeLatestColumnWithItemsVO {

    private String key;

    private String title;

    private String morePath;

    /** rule | manual | api */
    private String dataSource;

    private List<HomeLatestItemVO> items;
}
