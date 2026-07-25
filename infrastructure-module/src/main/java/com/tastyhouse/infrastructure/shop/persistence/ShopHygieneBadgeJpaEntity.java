package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.domain.shop.domain.model.HygieneBadgeType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 가게 위생 인증 뱃지 JPA 영속 모델. 순수 도메인 모델 {@code ShopHygieneBadge}와 분리된 영속 전용 엔티티다.
 */
@Getter
@Entity
@Table(name = "SHOP_HYGIENE_BADGE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopHygieneBadgeJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private HygieneBadgeType badgeType; // 위생 인증 유형

    @Column(name = "certified_date", nullable = false)
    private LocalDate certifiedDate; // 인증일

    @Column(name = "last_inspection_month", length = 7)
    private String lastInspectionMonth; // 세스코 최근 점검월 ("2026-03" 형태, nullable)

    private ShopHygieneBadgeJpaEntity(Long shopId, HygieneBadgeType badgeType, LocalDate certifiedDate, String lastInspectionMonth) {
        this.shopId = shopId;
        this.badgeType = badgeType;
        this.certifiedDate = certifiedDate;
        this.lastInspectionMonth = lastInspectionMonth;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopHygieneBadgeMapper#toEntity}에서만 호출한다.
     */
    static ShopHygieneBadgeJpaEntity create(Long shopId, HygieneBadgeType badgeType, LocalDate certifiedDate, String lastInspectionMonth) {
        return new ShopHygieneBadgeJpaEntity(shopId, badgeType, certifiedDate, lastInspectionMonth);
    }
}
