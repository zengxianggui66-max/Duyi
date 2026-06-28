package com.k12.common.dto;

import lombok.Data;

@Data
public class ResourceWriteResultVO {

    private Long id;
    private boolean masterWritten;
    private boolean wideSynced;
    private boolean placementSynced;
}
