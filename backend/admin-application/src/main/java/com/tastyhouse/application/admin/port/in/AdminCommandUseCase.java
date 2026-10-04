package com.tastyhouse.application.admin.port.in;

public interface AdminCommandUseCase {

    Long createAdmin(AdminCreateCommand command);
}
