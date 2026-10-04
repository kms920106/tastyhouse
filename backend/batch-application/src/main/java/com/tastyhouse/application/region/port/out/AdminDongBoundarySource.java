package com.tastyhouse.application.region.port.out;

import java.util.List;

public record AdminDongBoundarySource(
    String code,
    String sidoName,
    String sigunguName,
    String dongName,
    List<BoundaryRing> boundary
) {

    public AdminDongBoundarySource {
        boundary = boundary == null ? List.of() : List.copyOf(boundary);
    }
}
