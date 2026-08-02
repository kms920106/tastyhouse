package com.tastyhouse.adminapi.shop;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.model.HygieneBadgeType;
import com.tastyhouse.domain.shop.domain.model.ShopHygieneBadge;
import com.tastyhouse.domain.shop.domain.repository.ShopHygieneBadgeRepository;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * admin용 가게 위생 인증 뱃지 등록·삭제 서비스(CQRS command 측).
 *
 * <p>admin 전용이며 수정(update)은 지원하지 않는다. 단일 애그리거트 연산이라 도메인 서비스로 하강하지
 * 않고 write 포트로 직접 다루며, 경계 타입 승격(String → {@link HygieneBadgeType})을 담당한다.
 *
 * <p>CQRS 규칙대로 <b>식별자만</b> 반환한다 — 등록 응답 조립은 커밋 이후 컨트롤러가
 * {@link ShopHygieneBadgeQueryService}로 재조회해 담당한다.
 */
@Service
@Transactional
public class ShopHygieneBadgeCommandService {

    private final ShopHygieneBadgeRepository shopHygieneBadgeRepository;

    public ShopHygieneBadgeCommandService(ShopHygieneBadgeRepository shopHygieneBadgeRepository) {
        this.shopHygieneBadgeRepository = shopHygieneBadgeRepository;
    }

    /**
     * @return 등록된 위생 인증 뱃지 식별자
     */
    public Long createHygieneBadge(
        Long shopId,
        String badgeType,
        LocalDate certifiedDate,
        String lastInspectionMonth
    ) {
        ShopHygieneBadge saved = shopHygieneBadgeRepository.save(
            ShopHygieneBadge.of(
                ShopId.of(shopId),
                HygieneBadgeType.from(badgeType),
                certifiedDate,
                lastInspectionMonth
            )
        );
        return saved.getId();
    }

    public void deleteHygieneBadge(Long hygieneBadgeId) {
        shopHygieneBadgeRepository.findById(hygieneBadgeId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_HYGIENE_BADGE_NOT_FOUND));
        shopHygieneBadgeRepository.deleteById(hygieneBadgeId);
    }
}
