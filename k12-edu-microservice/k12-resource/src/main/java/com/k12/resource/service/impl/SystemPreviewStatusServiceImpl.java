package com.k12.resource.service.impl;

import com.k12.common.dto.AdminSystemLibreOfficeStatusVO;
import com.k12.common.dto.AdminSystemPreviewStatusVO;
import com.k12.resource.config.PreviewProperties;
import com.k12.resource.preview.LibreOfficeConverter;
import com.k12.resource.service.SystemPreviewStatusService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings("null")
public class SystemPreviewStatusServiceImpl implements SystemPreviewStatusService {

    private static final int VERSION_PROBE_TIMEOUT_SECONDS = 5;

    private final PreviewProperties previewProperties;
    private final LibreOfficeConverter libreOfficeConverter;
    public SystemPreviewStatusServiceImpl(PreviewProperties previewProperties, LibreOfficeConverter libreOfficeConverter) {
        this.previewProperties = previewProperties;
        this.libreOfficeConverter = libreOfficeConverter;
    }


    @Override
    public AdminSystemPreviewStatusVO probe() {
        AdminSystemPreviewStatusVO vo = new AdminSystemPreviewStatusVO();
        vo.setEnabled(previewProperties.isEnabled());
        vo.setPoiFallback(previewProperties.isPoiFallbackEnabled());
        vo.setAsyncEnabled(previewProperties.isAsyncEnabled());
        vo.setSampleProbe("skipped");
        vo.setLibreoffice(probeLibreOffice());
        return vo;
    }

    private AdminSystemLibreOfficeStatusVO probeLibreOffice() {
        AdminSystemLibreOfficeStatusVO lo = new AdminSystemLibreOfficeStatusVO();
        if (!previewProperties.isLibreofficeEnabled()) {
            lo.setConfigured(false);
            lo.setReachable(false);
            lo.setPath("");
            return lo;
        }

        String configuredPath = previewProperties.getLibreofficePath();
        lo.setConfigured(StringUtils.hasText(configuredPath));

        String resolved = libreOfficeConverter.getSofficePath();
        lo.setPath(resolved != null ? resolved : (StringUtils.hasText(configuredPath) ? configuredPath.trim() : ""));

        if (resolved == null) {
            lo.setReachable(false);
            if (!lo.getConfigured()) {
                lo.setConfigured(false);
            }
            return lo;
        }

        lo.setConfigured(true);
        lo.setReachable(runVersionProbe(resolved));
        return lo;
    }

    private boolean runVersionProbe(String sofficePath) {
        try {
            ProcessBuilder pb;
            if (sofficePath.contains(File.separator) || sofficePath.contains("/")) {
                pb = new ProcessBuilder(new File(sofficePath).getAbsolutePath(), "--version");
            } else {
                pb = new ProcessBuilder(sofficePath, "--version");
            }
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean finished = process.waitFor(VERSION_PROBE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return finished && process.exitValue() == 0;
        } catch (Exception ex) {
            return false;
        }
    }
}
