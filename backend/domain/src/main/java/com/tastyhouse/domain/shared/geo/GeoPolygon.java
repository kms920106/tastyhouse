package com.tastyhouse.domain.shared.geo;

import java.util.ArrayList;
import java.util.List;

public record GeoPolygon(
    List<GeoRing> rings
) {
    public GeoPolygon {
        if (rings == null || rings.isEmpty()) {
            throw new IllegalArgumentException("도형에는 링이 하나 이상 필요합니다.");
        }

        for (GeoRing ring : rings) {
            if (ring == null) {
                throw new IllegalArgumentException("도형에 빈 링이 포함될 수 없습니다.");
            }
        }
        rings = List.copyOf(rings);
    }

    public static GeoPolygon of(List<GeoRing> rings) {
        return new GeoPolygon(rings);
    }

    public boolean contains(GeoPoint point) {
        return PointInPolygon.contains(this, point);
    }

    public GeoBoundingBox boundingBox() {
        return GeoBoundingBox.enclosing(allPoints());
    }

    public double maxDistanceMetersFrom(GeoPoint center) {
        double max = 0;
        for (GeoRing ring : this.rings) {
            max = Math.max(max, ring.maxDistanceMetersFrom(center));
        }
        return max;
    }

    public int ringCount() {
        return this.rings.size();
    }

    public int vertexCount() {
        int total = 0;
        for (GeoRing ring : this.rings) {
            total += ring.vertexCount();
        }
        return total;
    }

    public List<GeoPoint> allPoints() {
        List<GeoPoint> points = new ArrayList<>(vertexCount());
        for (GeoRing ring : this.rings) {
            points.addAll(ring.points());
        }
        return points;
    }
}
