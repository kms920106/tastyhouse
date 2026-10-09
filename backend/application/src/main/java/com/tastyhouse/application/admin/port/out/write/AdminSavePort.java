package com.tastyhouse.application.admin.port.out.write;

import com.tastyhouse.domain.admin.model.Admin;

public interface AdminSavePort {

    Admin save(Admin admin);
}
