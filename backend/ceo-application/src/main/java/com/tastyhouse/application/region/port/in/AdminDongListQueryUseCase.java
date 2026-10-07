package com.tastyhouse.application.region.port.in;

import com.tastyhouse.application.region.port.out.AdminDongItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface AdminDongListQueryUseCase {

    PageResult<AdminDongItemResult> getAdminDongs(String keyword, int page, int size);
}
