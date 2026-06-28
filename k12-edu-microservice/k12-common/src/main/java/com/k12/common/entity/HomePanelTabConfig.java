package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页专区 Tab 查询配置
 */
@Data
@TableName("xinketang.home_panel_tab_config")
public class HomePanelTabConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("panel_code")
    private String panelCode;

    @TableField("tab_key")
    private String tabKey;

    @TableField("filter_key")
    private String filterKey;

    @TableField("tab_label")
    private String tabLabel;

    @TableField("module_names")
    private String moduleNames;

    @TableField("exclude_module_names")
    private String excludeModuleNames;

    @TableField("resource_type_names")
    private String resourceTypeNames;

    @TableField("title_keyword")
    private String titleKeyword;

    @TableField("query_mode")
    private String queryMode;

    private Integer sort;

    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
