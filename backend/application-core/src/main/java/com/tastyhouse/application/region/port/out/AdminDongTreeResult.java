package com.tastyhouse.application.region.port.out;

import java.util.List;

public record AdminDongTreeResult(
    String level,
    List<AdminDongTreeItemResult> items
) {
}
