package com.tastyhouse.application.ceo.port.out.write;

public record CeoState(
    Long id,
    String username,
    String password,
    String name,
    String businessRegistrationNumber,
    String phoneNumber,
    String email,
    String status
) {
}
