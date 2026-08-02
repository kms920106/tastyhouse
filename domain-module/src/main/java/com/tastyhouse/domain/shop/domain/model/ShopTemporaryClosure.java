package com.tastyhouse.domain.shop.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import lombok.Getter;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 상점 임시 휴무 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopTemporaryClosureJpaEntity} + {@code ShopTemporaryClosureMapper}가 담당한다. 상태전이(update)가 없어
 * 생성과 삭제만 존재한다.
 */
@Getter
public class ShopTemporaryClosure {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDateTime createdAt;

    private ShopTemporaryClosure(Long id, ShopId shopId, LocalDate startDate, LocalDate endDate, LocalDateTime createdAt) {
        this.id = id;
        this.shopId = shopId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
    }

    /**
     * 신규 임시 휴무를 생성한다. 아직 영속되지 않았으므로 식별자와 감사 시각은 없다.
     */
    public static ShopTemporaryClosure of(ShopId shopId, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.SHOP_TEMPORARY_CLOSURE_INVALID_PERIOD);
        }

        return new ShopTemporaryClosure(null, shopId, startDate, endDate, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopTemporaryClosure reconstitute(Long id, ShopId shopId, LocalDate startDate, LocalDate endDate, LocalDateTime createdAt) {
        return new ShopTemporaryClosure(id, shopId, startDate, endDate, createdAt);
    }

    /**
     * 임시 휴무 기간의 총 일수(시작일·종료일 포함)를 계산한다.
     */
    public long days() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
