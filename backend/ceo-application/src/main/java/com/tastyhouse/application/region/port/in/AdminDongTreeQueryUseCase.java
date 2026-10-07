package com.tastyhouse.application.region.port.in;

import com.tastyhouse.application.region.port.out.AdminDongTreeResult;

public interface AdminDongTreeQueryUseCase {

    AdminDongTreeResult getAdminDongTree(String sidoName, String sigunguName);
}
