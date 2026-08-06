package com.tastyhouse.domain.region.model;

/**
 * 행정동 마스터 순수 도메인 모델.
 *
 * <p>Java 애플리케이션 계층에 생성 경로가 없는 <b>read-only 애그리거트</b>다({@code RecommendedKeyword}
 * 선례) — 행정동 목록은 행정표준코드 시드({@code insert.sql})가 소유하고, 애플리케이션은 조회만 한다.
 * 그래서 {@code of}를 두지 않고 {@link #reconstitute}만 공개한다.
 *
 * <p>{@code ADMIN_DONG}은 감사 컬럼(생성·수정 일시)이 없는 마스터 테이블이라 감사 시각을 필드로 두지 않는다.
 */
public class AdminDong {

    private final Long id;
    private final String code;
    private final String sidoName;
    private final String sigunguName;
    private final String dongName;
    private final boolean active;

    private AdminDong(Long id, String code, String sidoName, String sigunguName, String dongName, boolean active) {
        this.id = id;
        this.code = code;
        this.sidoName = sidoName;
        this.sigunguName = sigunguName;
        this.dongName = dongName;
        this.active = active;
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     *
     * <p>생성 팩토리({@code of})가 없는 것은 의도된 것이다 — 위 클래스 Javadoc 참고.
     */
    public static AdminDong reconstitute(
        Long id,
        String code,
        String sidoName,
        String sigunguName,
        String dongName,
        boolean active
    ) {
        return new AdminDong(id, code, sidoName, sigunguName, dongName, active);
    }

    /**
     * 표시용 전체 행정동 이름. {@code "서울특별시 강남구 역삼1동"} 형태로 공백 join 한다.
     */
    public String fullName() {
        return this.sidoName + " " + this.sigunguName + " " + this.dongName;
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
}
