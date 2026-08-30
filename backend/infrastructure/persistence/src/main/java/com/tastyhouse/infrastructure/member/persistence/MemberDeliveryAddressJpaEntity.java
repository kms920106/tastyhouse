package com.tastyhouse.infrastructure.member.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 회원 배달 주소록 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code MemberDeliveryAddress}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사
 * 필드)만 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code MemberDeliveryAddressMapper}가
 * 수행한다.
 *
 * <p>FK({@code member_id}·{@code admin_dong_id})는 연관관계가 아니라 raw {@code Long}으로 둔다.
 * {@code admin_dong_id}는 nullable(행정동 매칭 실패)이므로 매퍼가 {@code IdMapping}으로 null-안전하게
 * 승격·언패킹한다.
 */
@Entity
@Table(
    name = "MEMBER_DELIVERY_ADDRESS",
    indexes = @Index(name = "idx_member_delivery_address_member_id", columnList = "member_id")
)
public class MemberDeliveryAddressJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 회원 ID (MEMBER.id 참조)

    @Column(name = "alias", length = 50)
    private String alias; // 주소 별칭 (집/회사 등)

    @Column(name = "road_address", nullable = false, length = 500)
    private String roadAddress; // 도로명 주소

    @Column(name = "lot_address", length = 500)
    private String lotAddress; // 지번 주소

    @Column(name = "detail_address", length = 200)
    private String detailAddress; // 상세 주소

    @Column(name = "admin_dong_id")
    private Long adminDongId; // 행정동 ID (ADMIN_DONG.id 참조, 매칭 실패 시 NULL)

    @Column(name = "latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude; // 위도

    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude; // 경도

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress; // 기본 배송지 여부

    protected MemberDeliveryAddressJpaEntity() {
    }

    private MemberDeliveryAddressJpaEntity(
        Long memberId,
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        Long adminDongId,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean defaultAddress
    ) {
        this.memberId = memberId;
        this.alias = alias;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.detailAddress = detailAddress;
        this.adminDongId = adminDongId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.defaultAddress = defaultAddress;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code MemberDeliveryAddressMapper#toEntity}에서만 호출한다.
     */
    static MemberDeliveryAddressJpaEntity create(
        Long memberId,
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        Long adminDongId,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean defaultAddress
    ) {
        return new MemberDeliveryAddressJpaEntity(
            memberId,
            alias,
            roadAddress,
            lotAddress,
            detailAddress,
            adminDongId,
            latitude,
            longitude,
            defaultAddress
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     * {@code member_id}는 생성 이후 바뀌지 않으므로 대상이 아니다.
     */
    void applyChanges(
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        Long adminDongId,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean defaultAddress
    ) {
        this.alias = alias;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.detailAddress = detailAddress;
        this.adminDongId = adminDongId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.defaultAddress = defaultAddress;
    }

    public Long getId() {
        return this.id;
    }

    public Long getMemberId() {
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

    public Long getAdminDongId() {
        return this.adminDongId;
    }

    public BigDecimal getLatitude() {
        return this.latitude;
    }

    public BigDecimal getLongitude() {
        return this.longitude;
    }

    public boolean isDefaultAddress() {
        return this.defaultAddress;
    }
}
