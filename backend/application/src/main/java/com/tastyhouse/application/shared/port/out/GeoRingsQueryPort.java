package com.tastyhouse.application.shared.port.out;

import java.util.List;

import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;

public interface GeoRingsQueryPort {

    List<GeoRing> resolveRings(String encoded);

    GeoPolygon resolvePolygon(String encoded);
}
