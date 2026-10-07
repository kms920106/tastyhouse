package com.tastyhouse.application.region.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tastyhouse.application.region.port.in.AdminDongTreeQueryUseCase;
import com.tastyhouse.application.region.port.out.AdminDongQueryPort;
import com.tastyhouse.application.region.port.out.AdminDongTreeItemResult;
import com.tastyhouse.application.region.port.out.AdminDongTreeResult;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;

@Service
@Transactional(readOnly = true)
class AdminDongTreeQueryService implements AdminDongTreeQueryUseCase {

    private static final String LEVEL_SIDO = "SIDO";
    private static final String LEVEL_SIGUNGU = "SIGUNGU";
    private static final String LEVEL_DONG = "DONG";

    private final AdminDongQueryPort adminDongQueryPort;

    public AdminDongTreeQueryService(AdminDongQueryPort adminDongQueryPort) {
        this.adminDongQueryPort = adminDongQueryPort;
    }

    @Override
    public AdminDongTreeResult getAdminDongTree(String sidoName, String sigunguName) {
        boolean hasSido = StringUtils.hasText(sidoName);
        boolean hasSigungu = StringUtils.hasText(sigunguName);

        if (hasSigungu && !hasSido) {
            throw new ApplicationException(
                CeoErrorCode.ADMIN_DONG_QUERY_INVALID,
                "시/군/구를 지정하려면 시/도도 함께 지정해야 합니다."
            );
        }

        if (!hasSido) {
            return toAdminDongTreeResult(LEVEL_SIDO, adminDongQueryPort.findSidoNames());
        }
        if (!hasSigungu) {
            return toAdminDongTreeResult(LEVEL_SIGUNGU, adminDongQueryPort.findSigunguNames(sidoName.trim()));
        }
        return toAdminDongTreeResult(
            LEVEL_DONG,
            adminDongQueryPort.findDongs(sidoName.trim(), sigunguName.trim())
        );
    }

    private AdminDongTreeResult toAdminDongTreeResult(String level, List<AdminDongTreeItemResult> items) {
        return new AdminDongTreeResult(level, items);
    }
}
