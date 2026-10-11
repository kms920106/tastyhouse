package com.tastyhouse.application.region.port.out;

import java.util.List;

public record AdminDongBoundariesResult(
    boolean truncated,
    List<AdminDongBoundaryViewResult> items
) {
}
