package com.tastyhouse.application.region.port.out.write;

public record AdminDongState(
    Long id,
    String code,
    String sidoName,
    String sigunguName,
    String dongName,
    boolean active,
    AdminDongCenterSnapshot center,
    AdminDongBoundarySnapshot boundary
) {
}
