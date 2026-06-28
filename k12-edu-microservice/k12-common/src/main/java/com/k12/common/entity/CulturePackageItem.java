package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("culture_package_item")
public class CulturePackageItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long packageId;
    private Long resourceId;
    private Integer sort;
}
