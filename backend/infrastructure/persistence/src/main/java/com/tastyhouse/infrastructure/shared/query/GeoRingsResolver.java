package com.tastyhouse.infrastructure.shared.query;

import java.util.List;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.shared.port.out.GeoRingsQueryPort;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.infrastructure.shared.persistence.GeoPolygonTextCodec;

@Component
public class GeoRingsResolver implements GeoRingsQueryPort {
    @Override
    public List<GeoRing> resolveRings(String encoded) {
        return GeoPolygonTextCodec.decodeRings(encoded);
    }

    @Override
    public GeoPolygon resolvePolygon(String encoded) {
        List<GeoRing> rings = resolveRings(encoded);
        return rings.isEmpty() ? null : GeoPolygon.of(rings);
    }
}
