package com.k12.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("search_engine_shadow_log")
public class SearchEngineShadowLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String keyword;
    private String apiPath;
    private Integer mysqlCostMs;
    private Integer engineCostMs;
    private String mysqlTopDocIds;
    private String engineTopDocIds;
    private BigDecimal top3OverlapRate;
    private BigDecimal top10OverlapRate;
    private Integer mysqlTotal;
    private Integer engineTotal;
    private String engineError;
    private LocalDateTime createTime;
}
