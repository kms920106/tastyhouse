package com.tastyhouse.application.admin.port.out.write;

public record AdminState(
    Long id,
    String username,
    String password,
    String name,
    String role,
    String status
) {
}
