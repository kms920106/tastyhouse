package com.tastyhouse.domain.shared.geo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class InteriorPoint {
    private static final int COORDINATE_SCALE = 6;

    private InteriorPoint() {
    }

    public static GeoPoint of(List<GeoRing> rings) {
        if (rings == null || rings.isEmpty()) {
            return null;
        }

        double scanLatitude = midLatitude(rings.getFirst());
        List<Double> crossings = crossingLongitudes(rings, scanLatitude);
        if (crossings.size() < 2) {
            return null;
        }

        crossings.sort(null);

        double widest = -1;
        double bestLongitude = 0;
        for (int i = 0; i + 1 < crossings.size(); i += 2) {
            double width = crossings.get(i + 1) - crossings.get(i);
            if (width > widest) {
                widest = width;
                bestLongitude = (crossings.get(i) + crossings.get(i + 1)) / 2;
            }
        }

        if (widest < 0) {
            return null;
        }
        return GeoPoint.of(scaled(scanLatitude), scaled(bestLongitude));
    }

    private static double midLatitude(GeoRing outerRing) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (GeoPoint point : outerRing.points()) {
            double latitude = point.latitude().doubleValue();
            min = Math.min(min, latitude);
            max = Math.max(max, latitude);
        }
        return (min + max) / 2;
    }

    private static List<Double> crossingLongitudes(List<GeoRing> rings, double scanLatitude) {
        List<Double> crossings = new ArrayList<>();
        for (GeoRing ring : rings) {
            List<GeoPoint> points = ring.points();
            int size = points.size();
            for (int i = 0; i < size; i++) {
                GeoPoint from = points.get(i);
                GeoPoint to = points.get((i + 1) % size);

                double fromLatitude = from.latitude().doubleValue();
                double toLatitude = to.latitude().doubleValue();
                if ((fromLatitude > scanLatitude) == (toLatitude > scanLatitude)) {
                    continue;
                }

                double fromLongitude = from.longitude().doubleValue();
                double toLongitude = to.longitude().doubleValue();
                double ratio = (scanLatitude - fromLatitude) / (toLatitude - fromLatitude);
                crossings.add(fromLongitude + ratio * (toLongitude - fromLongitude));
            }
        }
        return crossings;
    }

    private static BigDecimal scaled(double value) {
        return BigDecimal.valueOf(value).setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
    }
}
