package com.k12.common.dto;

import lombok.Data;

@Data
public class ResourceMigrationQualityDashboardVO {

    private Integer unplacedResources;

    private Integer emptyFileResources;

    private Integer approvedNotPublished;

    private Integer publishedButInvisible;

    private Integer catalogNodesWithoutResources;

    private Integer orphanResourcesWithoutCatalog;

    private Integer aliasTotal;

    private Integer aliasEnabled;
}
