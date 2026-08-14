package com.tastyhouse.ceoapi.review;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.review.repository.ShopReviewDisplaySettingRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;

/**
 * 점주 리뷰 표시 설정 명령 서비스(CQRS command 측).
 *
 * <p>정렬 설정은 다른 애그리거트와 얽히지 않는 단일 애그리거트 upsert라 도메인 서비스를 두지 않고 write
 * 포트를 직접 다룬다. 이 서비스는 소유권 검증·트랜잭션 경계·VO 승격·명시적 {@code save}만 담당한다.
 *
 * <p>도메인 모델은 순수 POJO라 더티 체킹이 없으므로 변경 후 반드시 {@code save}를 호출한다.
 */
@Service
@Transactional
public class ShopReviewCommandService {

    private final ShopReviewDisplaySettingRepository shopReviewDisplaySettingRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopReviewCommandService(
        ShopReviewDisplaySettingRepository shopReviewDisplaySettingRepository,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopReviewDisplaySettingRepository = shopReviewDisplaySettingRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 리뷰 정렬 설정을 저장한다 — 설정 행이 없으면 생성, 있으면 갱신(upsert).
     *
     * <p>행을 미리 만들지 않는 설계이므로 기존 가게에 대한 백필 없이 이 경로에서 처음 생성된다.
     *
     * <p><b>같은 값을 다시 저장하면 {@code updatedAt}이 갱신되지 않는다.</b> 도메인 값이 실제로 바뀌지
     * 않으면 Hibernate 더티 체킹이 UPDATE를 발행하지 않아 {@code @LastModifiedDate}가 그대로이기 때문이며,
     * 이는 의도된 동작이다 — {@code updatedAt}은 "정렬 설정이 마지막으로 <i>바뀐</i> 시각"이라 라디오 버튼을
     * 그대로 두고 저장한 것까지 변경으로 기록하면 그 의미가 흐려진다. 저장 버튼을 누른 시각을 별도로 보여줘야
     * 한다면 그것은 감사 로그의 관심사다.
     */
    public void changeSortType(Long ceoId, Long shopId, String sortType) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ReviewSortType newSortType = ReviewSortType.from(sortType);
        ShopReviewDisplaySetting setting = shopReviewDisplaySettingRepository.findByShopId(ShopId.of(shopId))
            .orElseGet(() -> ShopReviewDisplaySetting.of(ShopId.of(shopId), newSortType));
        setting.changeSortType(newSortType);

        shopReviewDisplaySettingRepository.save(setting);
    }
}
