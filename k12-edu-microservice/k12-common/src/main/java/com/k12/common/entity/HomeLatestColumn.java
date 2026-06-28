package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("xinketang.home_latest_column")
public class HomeLatestColumn {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("column_key")
    private String columnKey;

    private String title;

    @TableField("more_path")
    private String morePath;

    @TableField("data_source")
    private String dataSource;

    @TableField("rule_json")
    private String ruleJson;

    private Integer sort;

    private Integer status;

    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
