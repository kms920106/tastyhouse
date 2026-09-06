package com.tastyhouse.application.admin.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface AdminQueryUseCase {

    boolean existsByUsername(String username);
}
