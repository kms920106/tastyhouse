package com.tastyhouse.application.region.port.out;

public record AdminDongTreeItemResult(
    String name,
    Long adminDongId,
    String code,
    long dongCount
) {
}
