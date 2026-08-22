package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 메뉴모음컷 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ShopMenuCollectionImage}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사
 * 필드)만 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ShopMenuCollectionImageMapper}가
 * 수행한다.
 *
 * <p>승인 상태({@code status})가 별도 요청 테이블이 아니라 이 행에 있는 이유는 검수 대상이 "이 이미지
 * 자체"라 요청과 결과물이 1:1이기 때문이다 — 분리하면 승인 시 행을 옮겨 담아야 하고, 점주가 승인 전부터
 * 관리하는 {@code sort}가 그 순간 흔들린다.
 */
@Entity
@Table(name = "SHOP_MENU_COLLECTION_IMAGE")
public class ShopMenuCollectionImageJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ApprovalStatus status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    protected ShopMenuCollectionImageJpaEntity() {
    }

    private ShopMenuCollectionImageJpaEntity(
        Long shopId,
        Long imageFileId,
        Integer sort,
        ApprovalStatus status,
        String rejectReason
    ) {
        this.shopId = shopId;
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.status = status;
        this.rejectReason = rejectReason;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopMenuCollectionImageMapper#toEntity}에서만
     * 호출한다.
     */
    static ShopMenuCollectionImageJpaEntity create(
        Long shopId,
        Long imageFileId,
        Integer sort,
        ApprovalStatus status,
        String rejectReason
    ) {
        return new ShopMenuCollectionImageJpaEntity(shopId, imageFileId, sort, status, rejectReason);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·불변
     * 필드({@code shopId}·{@code imageFileId})는 건드리지 않는다.
     *
     * <p>{@code sort}가 변경 대상에 포함되는 이유는 순서 변경이 승인 없이 즉시 반영되는 정상 경로이기
     * 때문이다 — 상태 전이만 복사하면 순서 변경이 조용히 유실된다.
     */
    void applyChanges(Integer sort, ApprovalStatus status, String rejectReason) {
        this.sort = sort;
        this.status = status;
        this.rejectReason = rejectReason;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getImageFileId() {
        return this.imageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }

    public ApprovalStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }
}
