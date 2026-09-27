package com.tastyhouse.application.region.port.out;

import java.util.List;

public record BoundaryRing(
    List<BoundaryCoordinate> coordinates
) {
    public BoundaryRing {
        coordinates = coordinates == null ? List.of() : List.copyOf(coordinates);
    }
}
