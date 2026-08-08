package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shop.model.RiderGuideActionType;
import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.repository.ShopRiderGuideRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 라이더 안내 불변식과 변경 이력 기록을 원자적으로 묶는 도메인 서비스.
 *
 * <p>라이더 안내는 승인 워크플로가 아니라 <b>등록 즉시 반영 + 관리자 사후 검수</b> 모델이다. 점주 변경과
 * 관리자 조치(수정 요청·삭제)가 같은 이력 테이블에 기록되며, 어느 경로든 문구 등록 기준
 * ({@link ShopRiderGuideValidator})을 동일하게 통과해야 한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다. 도메인이 프레임워크-프리라 더티 체킹이 없으므로 변경 후
 * {@code repository.save(...)}를 명시적으로 호출한다.
 */
public class ShopRiderGuideService {

    private final ShopRiderGuideRepository shopRiderGuideRepository;
    private final ShopRepository shopRepository;
    private final ShopRiderGuideValidator shopRiderGuideValidator;

    public ShopRiderGuideService(
        ShopRiderGuideRepository shopRiderGuideRepository,
        ShopRepository shopRepository,
        ShopRiderGuideValidator shopRiderGuideValidator
    ) {
        this.shopRiderGuideRepository = shopRiderGuideRepository;
        this.shopRepository = shopRepository;
        this.shopRiderGuideValidator = shopRiderGuideValidator;
    }

    /**
     * 안내 문구를 등록·수정한다. 빈 값이면 문구를 비운다(= 삭제). 점주·관리자 어느 액터든 같은 경로를 쓴다.
     */
    public void updateVisitGuide(Long shopId, String visitGuide, RiderGuideActorType actorType, Long actorId) {
        Shop shop = findActiveShop(shopId);
        shopRiderGuideValidator.validate(shop, visitGuide);

        ShopRiderGuide riderGuide = findOrCreate(shopId);
        String previousVisitGuide = riderGuide.getVisitGuide();

        riderGuide.changeVisitGuide(visitGuide);
        shopRiderGuideRepository.save(riderGuide);

        shopRiderGuideRepository.saveHistory(ShopRiderGuideHistory.of(
            ShopId.of(shopId),
            actorType,
            actorId,
            RiderGuideActionType.UPDATE,
            previousVisitGuide,
            riderGuide.getVisitGuide(),
            null
        ));
    }

    /**
     * 관리자 삭제 조치 — 부적합 문구를 비우고 사유와 함께 이력을 남긴다. 픽업 위치는 건드리지 않는다.
     */
    public void deleteVisitGuide(Long shopId, Long adminId, String reason) {
        // 가게 존재를 먼저 확인해, 잘못된 shopId를 "문구 없음"이 아니라 SHOP_NOT_FOUND로 구분해 알린다.
        findShop(shopId);

        ShopRiderGuide riderGuide = shopRiderGuideRepository.findByShopId(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_RIDER_VISIT_GUIDE_NOT_FOUND));

        String previousVisitGuide = riderGuide.getVisitGuide();
        if (previousVisitGuide == null) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_RIDER_VISIT_GUIDE_NOT_FOUND);
        }

        riderGuide.changeVisitGuide(null);
        shopRiderGuideRepository.save(riderGuide);

        shopRiderGuideRepository.saveHistory(ShopRiderGuideHistory.of(
            ShopId.of(shopId),
            RiderGuideActorType.ADMIN,
            adminId,
            RiderGuideActionType.DELETION,
            previousVisitGuide,
            null,
            reason
        ));
    }

    /**
     * 관리자 수정 요청 — 문구는 그대로 두고 이력만 남긴다. 점주 알림 발송은 후속 과제이며 이번 범위는
     * 이력 기록까지다.
     *
     * @return 생성된 이력 ID
     */
    public Long requestRevision(Long shopId, Long adminId, String reason) {
        // 가게 존재를 먼저 확인해, 잘못된 shopId를 "문구 없음"이 아니라 SHOP_NOT_FOUND로 구분해 알린다.
        findShop(shopId);

        ShopRiderGuide riderGuide = shopRiderGuideRepository.findByShopId(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_RIDER_VISIT_GUIDE_NOT_FOUND));

        String currentVisitGuide = riderGuide.getVisitGuide();
        if (currentVisitGuide == null) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_RIDER_VISIT_GUIDE_NOT_FOUND);
        }

        ShopRiderGuideHistory history = shopRiderGuideRepository.saveHistory(ShopRiderGuideHistory.of(
            ShopId.of(shopId),
            RiderGuideActorType.ADMIN,
            adminId,
            RiderGuideActionType.REVISION_REQUEST,
            currentVisitGuide,
            currentVisitGuide,
            reason
        ));
        return history.getId();
    }

    /**
     * 픽업 위치를 등록·수정한다. 문구와 달리 이력을 남기지 않는다 — 검수 대상은 안내 문구이고, 픽업 위치는
     * 라이더 제보에 따라 교정되는 사실 정보이기 때문이다.
     */
    public void updatePickupLocation(
        Long shopId,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        findActiveShop(shopId);

        ShopRiderGuide riderGuide = findOrCreate(shopId);
        riderGuide.changePickupLocation(roadAddress, lotAddress, detailAddress, latitude, longitude);
        shopRiderGuideRepository.save(riderGuide);
    }

    /**
     * 픽업 위치를 비워 가게 실주소로 폴백시킨다. 이미 미설정 상태여도 예외 없이 통과한다(멱등).
     *
     * <p>행이 아직 없으면 아무것도 만들지 않고 끝낸다 — 빈 행을 새로 넣으면 아무것도 등록한 적 없는 가게가
     * 관리자 검수 목록에 나타나기 때문이다.
     */
    public void clearPickupLocation(Long shopId) {
        findActiveShop(shopId);

        shopRiderGuideRepository.findByShopId(ShopId.of(shopId)).ifPresent(riderGuide -> {
            riderGuide.clearPickupLocation();
            shopRiderGuideRepository.save(riderGuide);
        });
    }

    private ShopRiderGuide findOrCreate(Long shopId) {
        return shopRiderGuideRepository.findByShopId(ShopId.of(shopId))
            .orElseGet(() -> ShopRiderGuide.of(ShopId.of(shopId)));
    }

    private Shop findShop(Long shopId) {
        return shopRepository.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    /**
     * 폐업 가게는 라이더 안내를 수정할 수 없다.
     */
    private Shop findActiveShop(Long shopId) {
        Shop shop = findShop(shopId);
        if (shop.isPermanentlyClosed()) {
            throw new BusinessException(ErrorCode.SHOP_ALREADY_PERMANENTLY_CLOSED);
        }
        return shop;
    }
}
