package com.tastyhouse.domain.shared.geo;

import java.util.List;

public final class PointInPolygon {
    private PointInPolygon() {
    }

    public static boolean contains(GeoPolygon polygon, GeoPoint point) {
        boolean inside = false;
        for (GeoRing ring : polygon.rings()) {
            if (isOnRingBoundary(ring, point)) {
                return true;
            }
            if (crossesOddTimes(ring, point)) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static boolean crossesOddTimes(GeoRing ring, GeoPoint point) {
        List<GeoPoint> points = ring.points();
        double x = point.longitude().doubleValue();
        double y = point.latitude().doubleValue();

        boolean odd = false;
        for (int i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            double xi = points.get(i).longitude().doubleValue();
            double yi = points.get(i).latitude().doubleValue();
            double xj = points.get(j).longitude().doubleValue();
            double yj = points.get(j).latitude().doubleValue();

            if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) {
                odd = !odd;
            }
        }
        return odd;
    }

    private static boolean isOnRingBoundary(GeoRing ring, GeoPoint point) {
        List<GeoPoint> points = ring.points();
        for (int i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            if (isOnSegment(points.get(j), points.get(i), point)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOnSegment(GeoPoint start, GeoPoint end, GeoPoint point) {
        double x = point.longitude().doubleValue();
        double y = point.latitude().doubleValue();
        double x1 = start.longitude().doubleValue();
        double y1 = start.latitude().doubleValue();
        double x2 = end.longitude().doubleValue();
        double y2 = end.latitude().doubleValue();

        double crossProduct = (x - x1) * (y2 - y1) - (y - y1) * (x2 - x1);
        if (Math.abs(crossProduct) > EPSILON) {
            return false;
        }

        return x >= Math.min(x1, x2) - EPSILON && x <= Math.max(x1, x2) + EPSILON
            && y >= Math.min(y1, y2) - EPSILON && y <= Math.max(y1, y2) + EPSILON;
    }

    private static final double EPSILON = 1e-9;
}
