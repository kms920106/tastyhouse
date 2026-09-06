package com.tastyhouse.domain.region.model;

import java.util.List;

import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoRing;

public class AdminDong {
    private final Long id;
    private final String code;
    private final String sidoName;
    private final String sigunguName;
    private final String dongName;
    private final boolean active;
    private final GeoPoint center;
    private final List<GeoRing> boundary;

    private AdminDong(
        Long id,
        String code,
        String sidoName,
        String sigunguName,
        String dongName,
        boolean active,
        GeoPoint center,
        List<GeoRing> boundary
    ) {
        this.id = id;
        this.code = code;
        this.sidoName = sidoName;
        this.sigunguName = sigunguName;
        this.dongName = dongName;
        this.active = active;
        this.center = center;
        this.boundary = boundary == null ? List.of() : List.copyOf(boundary);
    }

    public static final int CODE_LENGTH = 10;

    public static AdminDong of(
        String code,
        String sidoName,
        String sigunguName,
        String dongName,
        boolean active,
        GeoPoint center,
        List<GeoRing> boundary
    ) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("행정동 코드는 필수입니다.");
        }
        if (code.length() != CODE_LENGTH) {
            throw new IllegalArgumentException(
                "행정동 코드는 " + CODE_LENGTH + "자리여야 합니다(행정기관코드): " + code);
        }
        requireName(sidoName, "시/도 이름");
        requireName(sigunguName, "시/군/구 이름");
        requireName(dongName, "행정동 이름");

        return new AdminDong(null, code, sidoName, sigunguName, dongName, active, center, boundary);
    }

    private static void requireName(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + "은(는) 필수입니다.");
        }
    }

    public static AdminDong reconstitute(
        Long id,
        String code,
        String sidoName,
        String sigunguName,
        String dongName,
        boolean active,
        GeoPoint center,
        List<GeoRing> boundary
    ) {
        return new AdminDong(id, code, sidoName, sigunguName, dongName, active, center, boundary);
    }

    public String fullName() {
        return this.sidoName + " " + this.sigunguName + " " + this.dongName;
    }

    public boolean hasCenter() {
        return this.center != null;
    }

    public boolean hasBoundary() {
        return !this.boundary.isEmpty();
    }

    public Long getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public String getSidoName() {
        return this.sidoName;
    }

    public String getSigunguName() {
        return this.sigunguName;
    }

    public String getDongName() {
        return this.dongName;
    }

    public boolean isActive() {
        return this.active;
    }

    public GeoPoint getCenter() {
        return this.center;
    }

    public List<GeoRing> getBoundary() {
        return this.boundary;
    }
}
