package com.tastyhouse.domain.shop.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 가게 위생 인증 뱃지 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopHygieneBadgeJpaEntity} + {@code ShopHygieneBadgeMapper}가 담당한다. 등록/삭제만
 * 있고 수정은 없는 애그리거트라 전 필드가 {@code final}이다.
 */
@Getter
public class ShopHygieneBadge {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId; // 가게 ID (SHOP.id 참조)
    private final HygieneBadgeType badgeType; // 위생 인증 유형
    private final LocalDate certifiedDate; // 인증일
    private final String lastInspectionMonth; // 세스코 최근 점검월 ("2026-03" 형태, nullable)
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ShopHygieneBadge(
        Long id,
        ShopId shopId,
        HygieneBadgeType badgeType,
        LocalDate certifiedDate,
        String lastInspectionMonth,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.badgeType = badgeType;
        this.certifiedDate = certifiedDate;
        this.lastInspectionMonth = lastInspectionMonth;
        this.createdAt = createdAt;
    }

    /**
     * 신규 위생 인증 뱃지를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static ShopHygieneBadge of(
        ShopId shopId,
        HygieneBadgeType badgeType,
        LocalDate certifiedDate,
        String lastInspectionMonth
    ) {
        return new ShopHygieneBadge(null, shopId, badgeType, certifiedDate, lastInspectionMonth, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopHygieneBadge reconstitute(
        Long id,
        ShopId shopId,
        HygieneBadgeType badgeType,
        LocalDate certifiedDate,
        String lastInspectionMonth,
        LocalDateTime createdAt
    ) {
        return new ShopHygieneBadge(id, shopId, badgeType, certifiedDate, lastInspectionMonth, createdAt);
    }
}
