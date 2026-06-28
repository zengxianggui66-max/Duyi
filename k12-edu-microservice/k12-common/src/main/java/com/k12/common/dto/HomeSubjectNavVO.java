package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase 5-F: aggregated home subject detail overlay payload
 */
@Data
public class HomeSubjectNavVO {

    private HomeSubjectNavSubjectVO subject;
    private HomeSubjectNavSyncPrepVO syncPrep;
    private HomeSubjectNavModuleSectionVO reviewPrep;
    private HomeSubjectNavModuleSectionVO promotionPrep;

    @Data
    public static class HomeSubjectNavSubjectVO {
        private Integer id;
        private String code;
        private String name;
        private String icon;
        private String stageCode;
        private String stageName;
    }

    @Data
    public static class HomeSubjectNavSyncPrepVO {
        private List<HomeSubjectNavEditionItemVO> editions = new ArrayList<>();
        private List<HomeSubjectNavResourceTypeItemVO> resourceTypes = new ArrayList<>();
    }

    @Data
    public static class HomeSubjectNavEditionItemVO {
        private Integer id;
        private String code;
        private String name;
        private Boolean isNew;
        private Integer sort;
    }

    @Data
    public static class HomeSubjectNavResourceTypeItemVO {
        private Integer id;
        private String code;
        private String name;
        private Integer sort;
    }

    @Data
    public static class HomeSubjectNavModuleSectionVO {
        private String label;
        private String title;
        private List<HomeSubjectNavModuleItemVO> modules = new ArrayList<>();
    }

    @Data
    public static class HomeSubjectNavModuleItemVO {
        private Integer id;
        private String code;
        private String name;
        private String icon;
        private Integer sort;
    }
}
