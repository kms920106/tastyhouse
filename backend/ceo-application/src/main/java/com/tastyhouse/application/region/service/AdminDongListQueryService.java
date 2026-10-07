package com.tastyhouse.application.region.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.region.port.in.AdminDongListQueryUseCase;
import com.tastyhouse.application.region.port.out.AdminDongItemResult;
import com.tastyhouse.application.region.port.out.AdminDongQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class AdminDongListQueryService implements AdminDongListQueryUseCase {

    private final AdminDongQueryPort adminDongQueryPort;

    public AdminDongListQueryService(AdminDongQueryPort adminDongQueryPort) {
        this.adminDongQueryPort = adminDongQueryPort;
    }

    @Override
    public PageResult<AdminDongItemResult> getAdminDongs(String keyword, int page, int size) {
        return adminDongQueryPort.findAdminDongPage(keyword, PageQuery.of(page, size));
    }
}
