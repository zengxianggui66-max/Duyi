package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("prep_basket_item")
public class PrepBasketItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long basketId;
    /** resource | question | paper | album */
    private String itemType;
    private Long refId;
    private String title;
    private String subtitle;
    private String coverUrl;
    private String metaJson;
    private Integer sortOrder;
    private BigDecimal score;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
