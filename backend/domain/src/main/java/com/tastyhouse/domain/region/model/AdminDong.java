package com.tastyhouse.domain.region.model;

import java.util.List;

import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoRing;

/**
 * 행정동 마스터 순수 도메인 모델.
 *
 * <p><b>일반 요청 경로에는 생성·변경이 없는 마스터</b>다 — web/admin/ceo 어느 api 모듈도 이 애그리거트를
 * 만들지 않고 조회만 한다. 유일한 생성 경로는 batch-module의 행정동 경계 동기화 배치이며, 그 배치가
 * 외부 원천(통계청 SGIS 파생 행정동 경계)을 읽어 마스터 전체를 교체한다. 그래서 {@link #of}는 두되
 * 호출자는 그 배치 하나로 제한된다.
 *
 * <p>(과거에는 시드 SQL({@code insert.sql})이 마스터를 소유해 {@code of} 없이 {@link #reconstitute}만
 * 공개했으나, 좌표·경계까지 포함한 3,500여 건을 사람이 관리할 수 없어 배치 동기화로 전환했다.)
 *
 * <p>{@code ADMIN_DONG}은 감사 컬럼(생성·수정 일시)이 없는 마스터 테이블이라 감사 시각을 필드로 두지 않는다.
 *
 * <p><b>좌표·경계는 전부 선택 값이다.</b> 배달지역 도형 환산을 위해 대표점({@code center})과 경계
 * ({@code boundary})를 추가했지만, 시드가 단계적으로 투입되므로(코드·좌표 먼저, 경계는 나중에) 둘 다
 * 없는 행이 정상적으로 존재한다. 환산은 {@link #hasCenter()}·{@link #hasBoundary()}로 보유 여부를 먼저
 * 확인하고, 둘 다 없으면 <b>판정 불가</b>로 분류해 조용히 포함시키지 않는다.
 *
 * <p>대표점은 centroid가 아니라 경계 <b>내부가 보장되는</b> 점이다 — 하천을 낀 오목한 동은 centroid가
 * 경계 밖에 떨어져 포함 판정이 뒤집힌다.
 */
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

    /** 행정동 코드 자릿수(행정안전부 행정기관코드 10자리). */
    public static final int CODE_LENGTH = 10;

    /**
     * 동기화 배치가 외부 원천에서 읽은 행정동 하나를 만든다. 아직 저장되지 않았으므로 {@code id}는 없다.
     *
     * <p>좌표·경계는 <b>선택</b>이다. 원천이 경계를 주지 않는 행(도서·신설 동 등)이 있을 수 있고, 그런 행도
     * 코드·이름만으로 배달지역 트리에 나타나야 하므로 여기서 거부하지 않는다 — 환산 단계가
     * {@link #hasCenter()}·{@link #hasBoundary()}로 걸러낸다.
     *
     * <p>반면 코드·이름은 없으면 마스터로서 의미가 없으므로 거부한다. 특히 <b>코드 자릿수를 검증</b>하는
     * 이유는, 원천이 8자리 통계청 분류코드와 10자리 행정기관코드를 함께 제공해 잘못된 쪽을 넣으면
     * 기존 참조와 조용히 어긋나기 때문이다.
     *
     * @param center   대표점(경계 내부 보장점). 미보유 시 {@code null}
     * @param boundary 경계 링 목록. 미보유 시 {@code null} 또는 빈 목록
     */
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

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     *
     * @param center   대표점. 좌표 미보유 시 {@code null}
     * @param boundary 경계 링 목록. 미보유 시 {@code null} 또는 빈 목록
     */
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

    /**
     * 표시용 전체 행정동 이름. {@code "서울특별시 강남구 역삼1동"} 형태로 공백 join 한다.
     */
    public String fullName() {
        return this.sidoName + " " + this.sigunguName + " " + this.dongName;
    }

    /** 대표점 좌표를 보유하고 있는지. 배달지역 환산의 1차 규칙이 이 좌표를 쓴다. */
    public boolean hasCenter() {
        return this.center != null;
    }

    /** 경계 폴리곤을 보유하고 있는지. 배달지역 환산의 2차 규칙(정점 샘플 비율)이 이 경계를 쓴다. */
    public boolean hasBoundary() {
        return !this.boundary.isEmpty();
    }

    public Long getId() {
        return this.id;
    }

    /** 행정동 코드(10자리). */
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

    /** 사용 여부. */
    public boolean isActive() {
        return this.active;
    }

    /** 경계 내부가 보장되는 대표점. 미보유 시 {@code null}. */
    public GeoPoint getCenter() {
        return this.center;
    }

    /** 경계 링 목록(불변). 미보유 시 빈 목록. */
    public List<GeoRing> getBoundary() {
        return this.boundary;
    }
}
