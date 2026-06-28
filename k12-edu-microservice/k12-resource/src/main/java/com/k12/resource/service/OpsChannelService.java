package com.k12.resource.service;

import com.k12.common.dto.*;

import java.util.List;

public interface OpsChannelService {

    OpsChannelBootstrapVO getBootstrap(String code);

    List<OpsChannelAdminVO> listAdmin(boolean includeDisabled);

    OpsChannelAdminVO updateChannel(Long id, OpsChannelWriteDTO dto, Long adminUserId);

    void updateChannelStatus(Long id, int status, Long adminUserId);

    List<OpsChannelTabVO> listTabs(String channelCode, boolean includeDisabled);

    OpsChannelTabVO updateTab(Long tabId, OpsChannelTabWriteDTO dto, Long adminUserId);

    List<OpsChannelAlbumVO> listAlbums(String channelCode, boolean includeDisabled);

    OpsChannelAlbumVO createAlbum(String channelCode, OpsChannelAlbumWriteDTO dto, Long adminUserId);

    OpsChannelAlbumVO updateAlbum(Long albumId, OpsChannelAlbumWriteDTO dto, Long adminUserId);

    void deleteAlbum(Long albumId, Long adminUserId);
}
