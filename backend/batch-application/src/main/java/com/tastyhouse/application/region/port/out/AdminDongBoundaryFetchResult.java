package com.tastyhouse.application.region.port.out;

import java.util.List;

public record AdminDongBoundaryFetchResult(
    List<AdminDongBoundarySource> sources,
    boolean failed
) {

    public AdminDongBoundaryFetchResult {
        sources = sources == null ? List.of() : List.copyOf(sources);
    }

    public static AdminDongBoundaryFetchResult fetched(List<AdminDongBoundarySource> sources) {
        return new AdminDongBoundaryFetchResult(sources, false);
    }

    public static AdminDongBoundaryFetchResult fetchFailed() {
        return new AdminDongBoundaryFetchResult(List.of(), true);
    }
}
