package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("competition_pack_item")
public class CompetitionPackageItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long packageId;
    private Long resourceId;
    private Integer sort;
}
