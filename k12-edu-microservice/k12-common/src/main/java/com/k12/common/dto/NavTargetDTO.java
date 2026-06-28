package com.k12.common.dto;

import lombok.Data;

import java.util.Map;

/**
 * Unified navigation target for home ops (banner / quick entry / hot word).
 */
@Data
public class NavTargetDTO {

    private String type;

    private String routePath;

    private String stageKey;

    private String subjectKey;

    private String versionKey;

    private String volumeName;

    private String keyword;

    private String scrollTarget;

    private String externalUrl;

    private Boolean openInNewTab;

    private Map<String, String> query;

    /** mysql (default) | auto (reserved for OpenSearch) */
    private String searchEngine;

    private Map<String, Object> searchScope;
}
