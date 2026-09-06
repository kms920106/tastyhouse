package com.tastyhouse.domain.region.repository;

public record AdminDongSyncResult(
    int inserted,
    int updated,
    int deactivated
) {
    public static AdminDongSyncResult of(int inserted, int updated, int deactivated) {
        return new AdminDongSyncResult(inserted, updated, deactivated);
    }

    public int appliedCount() {
        return this.inserted + this.updated;
    }
}
