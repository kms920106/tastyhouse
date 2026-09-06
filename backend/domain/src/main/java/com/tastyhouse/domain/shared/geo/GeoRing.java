package com.tastyhouse.domain.shared.geo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record GeoRing(
    List<GeoPoint> points
) {
    public static final int MIN_POINTS = 3;

    public GeoRing {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("링에는 좌표가 필요합니다.");
        }
        points = List.copyOf(normalize(points));
        if (points.size() < MIN_POINTS) {
            throw new IllegalArgumentException("링은 서로 다른 좌표가 " + MIN_POINTS + "개 이상이어야 합니다.");
        }
    }

    public static GeoRing of(List<GeoPoint> points) {
        return new GeoRing(points);
    }

    private static List<GeoPoint> normalize(List<GeoPoint> rawPoints) {
        List<GeoPoint> normalized = new ArrayList<>(rawPoints.size());
        for (GeoPoint point : rawPoints) {
            if (point == null) {
                throw new IllegalArgumentException("링에 빈 좌표가 포함될 수 없습니다.");
            }
            if (normalized.isEmpty() || !normalized.getLast().isSameLocation(point)) {
                normalized.add(point);
            }
        }

        while (normalized.size() > 1 && normalized.getFirst().isSameLocation(normalized.getLast())) {
            normalized.removeLast();
        }

        return normalized;
    }

    public int vertexCount() {
        return this.points.size();
    }

    public GeoBoundingBox boundingBox() {
        return GeoBoundingBox.enclosing(this.points);
    }

    public double maxDistanceMetersFrom(GeoPoint center) {
        double max = 0;
        for (GeoPoint point : this.points) {
            max = Math.max(max, center.distanceMetersTo(point));
        }
        return max;
    }

    public List<GeoPoint> sample(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("샘플 개수는 1 이상이어야 합니다.");
        }
        if (this.points.size() <= limit) {
            return this.points;
        }

        List<GeoPoint> sampled = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            sampled.add(this.points.get((int) ((long) i * this.points.size() / limit)));
        }
        return Collections.unmodifiableList(sampled);
    }
}
