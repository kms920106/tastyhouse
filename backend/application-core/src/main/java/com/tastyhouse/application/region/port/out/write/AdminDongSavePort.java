package com.tastyhouse.application.region.port.out.write;

import java.util.List;

import com.tastyhouse.domain.region.model.AdminDong;

public interface AdminDongSavePort {

    AdminDongSyncResult synchronize(List<AdminDong> adminDongs);
}
