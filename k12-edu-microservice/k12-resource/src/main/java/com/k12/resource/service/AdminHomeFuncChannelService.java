package com.k12.resource.service;

import com.k12.common.dto.*;

import java.util.List;

public interface AdminHomeFuncChannelService {

    List<HomeFuncChannelAdminVO> list(boolean includeDisabled);

    HomeFuncChannelAdminVO update(Long id, HomeFuncChannelWriteDTO dto, Long adminUserId);

    void updateStatus(Long id, int status, Long adminUserId);
}
