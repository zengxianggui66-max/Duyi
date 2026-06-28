package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 教材单元树节点（单元 + 课文列表）
 */
@Data
public class UnitTreeNodeVO {

    private String name;
    private List<String> subUnits = new ArrayList<>();

    public UnitTreeNodeVO() {
    }

    public UnitTreeNodeVO(String name, List<String> subUnits) {
        this.name = name;
        this.subUnits = subUnits != null ? new ArrayList<>(subUnits) : new ArrayList<>();
    }
}
