package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HomePanelListVO {

    private List<HomePanelItemVO> items = new ArrayList<>();

    private int limit = 18;
}
