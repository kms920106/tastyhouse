package com.tastyhouse.domain.member.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.region.vo.AdminDongId;

/**
 * 회원 배달 주소록 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code MemberDeliveryAddressJpaEntity} + {@code MemberDeliveryAddressMapper}가 담당한다.
 *
 * <p>자기 PK는 {@code XxxId} VO를 만들지 않고 {@code Long}으로 둔다 — shop 자식 애그리거트 16개의
 * 형제 관례이며, 이 애그리거트만 VO를 만들면 같은 프로젝트에서 규칙이 갈린다.
 *
 * <p>{@code adminDongId}는 서버가 주소 문자열의 행정동명으로 {@code ADMIN_DONG}을 매칭해 채우는
 * 파생 값이라 <b>nullable</b>이다(매칭 실패 시 지역별 배달팁이 적용되지 않을 뿐, 저장은 성공한다).
 */
public class MemberDeliveryAddress {

    private final Long id;
    private final MemberId memberId;
    private String alias;
    private String roadAddress;
    private String lotAddress;
    private String detailAddress;
    private AdminDongId adminDongId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean defaultAddress;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private MemberDeliveryAddress(
        Long id,
        MemberId memberId,
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        AdminDongId adminDongId,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean defaultAddress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.alias = alias;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.detailAddress = detailAddress;
        this.adminDongId = adminDongId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.defaultAddress = defaultAddress;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 배달 주소를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     *
     * <p>도로명 주소와 <b>좌표(위도·경도)를 필수</b>로 강제한다. 좌표가 없는 주소를 저장할 수 있으면
     * 거리별 배달팁 산출 시 거리를 구할 수 없어 할증이 0원이 되고, 이는 곧 <b>매출 누수이자 조작
     * 가능한 취약점</b>이다(좌표 없는 주소를 만들어 무료 배달을 얻는 경로). 좌표는 클라이언트가 주소
     * 검색 API에서 받은 값을 보내며, 서버는 이후 거리 계산에서 이 저장된 좌표만 신뢰한다.
     *
     * <p>{@link #reconstitute}는 이 검증을 <b>거치지 않는다</b> — 기존 DB 데이터가 새 불변식을 위반해도
     * 로드는 가능해야 하기 때문이다.
     */
    public static MemberDeliveryAddress of(
        MemberId memberId,
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        AdminDongId adminDongId,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean defaultAddress
    ) {
        validateAddress(roadAddress, latitude, longitude);

        return new MemberDeliveryAddress(
            null,
            memberId,
            alias,
            roadAddress,
            lotAddress,
            detailAddress,
            adminDongId,
            latitude,
            longitude,
            defaultAddress,
            null,
            null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     *
     * <p><b>{@link #of}와 달리 주소·좌표 검증을 하지 않는다</b> — 불변식 도입 이전에 저장된 행이 새
     * 규칙을 위반하더라도 로드는 가능해야 하기 때문이다.
     */
    public static MemberDeliveryAddress reconstitute(
        Long id,
        MemberId memberId,
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        AdminDongId adminDongId,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean defaultAddress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new MemberDeliveryAddress(
            id,
            memberId,
            alias,
            roadAddress,
            lotAddress,
            detailAddress,
            adminDongId,
            latitude,
            longitude,
            defaultAddress,
            createdAt,
            updatedAt
        );
    }

    /**
     * 배달 주소를 변경한다. 생성({@link #of})과 <b>같은 검증 한 벌</b>을 강제한다 — 생성만 막고 변경을
     * 열어두면 좌표 없는 주소가 곧바로 뒷문으로 들어와 위 취약점이 그대로 재현되기 때문이다.
     *
     * <p>기본 배송지 여부는 여기서 바꾸지 않는다 — 회원 전체 주소록을 봐야 판정되는 유일성 불변식이라
     * {@code MemberDeliveryAddressService}가 {@link #markAsDefault()}/{@link #unmarkDefault()}로 다룬다.
     */
    public void update(
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        AdminDongId adminDongId,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        validateAddress(roadAddress, latitude, longitude);

        this.alias = alias;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.detailAddress = detailAddress;
        this.adminDongId = adminDongId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /** 이 주소를 기본 배송지로 표시한다. 회원당 1건 유일성은 도메인 서비스가 보장한다. */
    public void markAsDefault() {
        this.defaultAddress = true;
    }

    /** 이 주소의 기본 배송지 표시를 해제한다. */
    public void unmarkDefault() {
        this.defaultAddress = false;
    }

    /** 이 주소가 주어진 회원의 것인지 판정한다. 수정·삭제·기본지정의 선행 조건이다. */
    public boolean isOwnedBy(MemberId memberId) {
        return this.memberId != null && this.memberId.equals(memberId);
    }

    /**
     * 도로명 주소 필수와 좌표 필수를 검증한다. 인스턴스 상태를 읽지 않으므로 {@code static}이다 —
     * 그래야 생성({@link #of})과 변경({@link #update})이 같은 검증 한 벌을 공유할 수 있다.
     *
     * <p>{@code BusinessException} + 전용 {@code ErrorCode}가 아니라 {@code IllegalArgumentException}인
     * 이유는, 이 둘이 HTTP 경계의 Bean Validation({@code @NotBlank}/{@code @NotNull})이 이미 거르는
     * <b>구조적 전제 조건</b>이기 때문이다. 여기서의 실패는 정상 사용자 입력이 아니라 검증을 건너뛴
     * 내부 호출 경로(배치·마이그레이션)의 결함이며, ID VO들이 null을 거부하는 방식과 같은 계열이다.
     */
    private static void validateAddress(String roadAddress, BigDecimal latitude, BigDecimal longitude) {
        if (roadAddress == null || roadAddress.isBlank()) {
            throw new IllegalArgumentException("도로명 주소는 필수입니다.");
        }
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("배달 주소의 좌표(위도·경도)는 필수입니다: latitude=" + latitude + ", longitude=" + longitude);
        }
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public String getAlias() {
        return this.alias;
    }

    public String getRoadAddress() {
        return this.roadAddress;
    }

    public String getLotAddress() {
        return this.lotAddress;
    }

    public String getDetailAddress() {
        return this.detailAddress;
    }

    /** 행정동 ID. 주소 문자열 매칭에 실패하면 null이다(지역별 배달팁 미적용). */
    public AdminDongId getAdminDongId() {
        return this.adminDongId;
    }

    public BigDecimal getLatitude() {
        return this.latitude;
    }

    public BigDecimal getLongitude() {
        return this.longitude;
    }

    /** 기본 배송지 여부. */
    public boolean isDefaultAddress() {
        return this.defaultAddress;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
