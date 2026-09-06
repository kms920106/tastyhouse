package com.tastyhouse.domain.shop.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;

public final class DeliveryAreaProjection {
    private DeliveryAreaProjection() {
    }

    public static Result project(GeoPolygon polygon, List<AdminDong> candidates) {
        if (polygon == null) {
            throw new IllegalArgumentException("환산할 도형은 필수입니다.");
        }
        if (candidates == null) {
            throw new IllegalArgumentException("후보 행정동 목록은 필수입니다(비어 있을 수는 있습니다).");
        }

        List<AdminDongId> included = new ArrayList<>();
        int unresolvedCount = 0;

        for (AdminDong candidate : candidates) {
            if (candidate.hasCenter() && polygon.contains(candidate.getCenter())) {
                included.add(AdminDongId.of(candidate.getId()));
                continue;
            }
            if (candidate.hasBoundary()) {
                if (isCoveredEnough(polygon, candidate.getBoundary())) {
                    included.add(AdminDongId.of(candidate.getId()));
                }
                continue;
            }
            if (!candidate.hasCenter()) {
                unresolvedCount++;
            }
        }

        return new Result(List.copyOf(included), unresolvedCount);
    }

    private static boolean isCoveredEnough(GeoPolygon polygon, List<GeoRing> boundary) {
        int sampled = 0;
        int contained = 0;

        for (GeoPoint point : sampleBoundary(boundary)) {
            sampled++;
            if (polygon.contains(point)) {
                contained++;
            }
        }

        return sampled > 0 && (double) contained / sampled >= ShopDeliveryAreaPolicy.COVERAGE_THRESHOLD;
    }

    private static List<GeoPoint> sampleBoundary(List<GeoRing> boundary) {
        int totalVertices = 0;
        for (GeoRing ring : boundary) {
            totalVertices += ring.vertexCount();
        }
        if (totalVertices <= ShopDeliveryAreaPolicy.BOUNDARY_SAMPLE_LIMIT) {
            List<GeoPoint> all = new ArrayList<>(totalVertices);
            for (GeoRing ring : boundary) {
                all.addAll(ring.points());
            }
            return all;
        }

        List<GeoPoint> sampled = new ArrayList<>(ShopDeliveryAreaPolicy.BOUNDARY_SAMPLE_LIMIT);
        for (GeoRing ring : boundary) {
            int quota = (int) ((long) ShopDeliveryAreaPolicy.BOUNDARY_SAMPLE_LIMIT * ring.vertexCount() / totalVertices);
            sampled.addAll(ring.sample(Math.max(1, quota)));
        }
        return sampled;
    }

    public record Result(
        List<AdminDongId> adminDongIds,
        int unresolvedCount
    ) {
        public Result {
            adminDongIds = List.copyOf(adminDongIds);
        }

        public boolean isEmpty() {
            return this.adminDongIds.isEmpty();
        }

        public int count() {
            return this.adminDongIds.size();
        }
    }
}
