package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shop.model.RiderGuideActionType;
import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
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
 * <p><b>이 서비스는 이력 테이블 두 개에 쓴다.</b> {@code SHOP_RIDER_GUIDE_HISTORY}(위 검수 워크플로용,
 * 사유·수정요청·삭제 개념 보유)는 액터 무관하게 기존대로 남기고, 가게 변경이력
 * {@code SHOP_CHANGE_HISTORY}({@code RIDER_VISIT_GUIDE}·{@code RIDER_PICKUP_LOCATION})는
 * <b>점주 변경만</b> 남긴다. 후자는 점주 화면의 "내가 무엇을 바꿨는가" 목록이라 관리자 검수 조치
 * (수정 요청·삭제·픽업 위치 교정)가 섞이면 점주가 하지 않은 변경이 자기 이력에 나타난다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code ShopDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다. 도메인이 프레임워크-프리라 더티 체킹이 없으므로 변경 후
 * {@code repository.save(...)}를 명시적으로 호출한다.
 */
public class ShopRiderGuideService {

    private final ShopRiderGuideRepository shopRiderGuideRepository;
    private final ShopRepository shopRepository;
    private final ShopRiderGuideValidator shopRiderGuideValidator;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopRiderGuideService(
        ShopRiderGuideRepository shopRiderGuideRepository,
        ShopRepository shopRepository,
        ShopRiderGuideValidator shopRiderGuideValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopRiderGuideRepository = shopRiderGuideRepository;
        this.shopRepository = shopRepository;
        this.shopRiderGuideValidator = shopRiderGuideValidator;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    /**
     * 안내 문구를 등록·수정한다. 빈 값이면 문구를 비운다(= 삭제). 점주·관리자 어느 액터든 같은 경로를 쓴다.
     *
     * <p>기존 {@code SHOP_RIDER_GUIDE_HISTORY} 기록은 그대로 유지하고, <b>점주 변경일 때만</b> 가게
     * 변경이력({@code RIDER_VISIT_GUIDE})도 함께 남긴다({@link #toShopChangeActor}). 두 이력은 목적이
     * 달라 공존한다 — 전자는 "등록 즉시 반영 + 관리자 사후 검수" 워크플로의 근거로 사유·수정요청·삭제
     * 개념을 담고, 후자는 점주 화면의 "내가 무엇을 바꿨는가" 목록이다. 관리자 검수 조치는 점주가 한
     * 변경이 아니므로 후자에 담지 않는다.
     *
     * <p>문구 전문을 그대로 담는다(자르지 않는다).
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

        if (actorType == RiderGuideActorType.CEO) {
            shopChangeHistoryRecorder.record(
                ShopId.of(shopId),
                ShopChangeType.RIDER_VISIT_GUIDE,
                ShopChangeActionType.UPDATE,
                toShopChangeActor(actorType, actorId),
                describeVisitGuide(previousVisitGuide),
                describeVisitGuide(riderGuide.getVisitGuide())
            );
        }
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
     * 픽업 위치를 등록·수정한다. {@code SHOP_RIDER_GUIDE_HISTORY}에는 여전히 남기지 않는다 — 그 이력의
     * 검수 대상은 안내 문구이고, 픽업 위치는 라이더 제보에 따라 교정되는 사실 정보이기 때문이다.
     *
     * <p>다만 <b>점주가 직접 바꾼 경우에만</b> 가게 변경이력({@code RIDER_PICKUP_LOCATION})을 남긴다 —
     * 점주 화면의 "내가 무엇을 바꿨는가" 목록에는 픽업 위치 변경도 들어가야 한다. 관리자가 라이더 제보를
     * 반영해 교정한 것은 점주의 변경이 아니므로 담지 않는다.
     */
    public void updatePickupLocation(
        Long shopId,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        RiderGuideActorType actorType,
        Long actorId
    ) {
        findActiveShop(shopId);

        ShopRiderGuide riderGuide = findOrCreate(shopId);
        // 변경 전 요약을 갱신 전에 확정한다 — 같은 인스턴스를 제자리에서 바꾸므로 나중에 읽으면 변경 후 값이다.
        String previousValue = describePickupLocation(riderGuide);

        riderGuide.changePickupLocation(roadAddress, lotAddress, detailAddress, latitude, longitude);
        shopRiderGuideRepository.save(riderGuide);

        if (actorType == RiderGuideActorType.CEO) {
            shopChangeHistoryRecorder.record(
                ShopId.of(shopId),
                ShopChangeType.RIDER_PICKUP_LOCATION,
                ShopChangeActionType.UPDATE,
                toShopChangeActor(actorType, actorId),
                previousValue,
                describePickupLocation(riderGuide)
            );
        }
    }

    /**
     * 픽업 위치를 비워 가게 실주소로 폴백시킨다. 이미 미설정 상태여도 예외 없이 통과한다(멱등).
     *
     * <p>행이 아직 없으면 아무것도 만들지 않고 끝낸다 — 빈 행을 새로 넣으면 아무것도 등록한 적 없는 가게가
     * 관리자 검수 목록에 나타나기 때문이다. 이때는 지운 것이 없으므로 변경이력도 남기지 않는다.
     *
     * <p>점주 해제만 변경이력({@code RIDER_PICKUP_LOCATION}, {@code DELETE})에 남는다.
     */
    public void clearPickupLocation(Long shopId, RiderGuideActorType actorType, Long actorId) {
        findActiveShop(shopId);

        shopRiderGuideRepository.findByShopId(ShopId.of(shopId)).ifPresent(riderGuide -> {
            String previousValue = describePickupLocation(riderGuide);

            riderGuide.clearPickupLocation();
            shopRiderGuideRepository.save(riderGuide);

            if (actorType == RiderGuideActorType.CEO) {
                shopChangeHistoryRecorder.record(
                    ShopId.of(shopId),
                    ShopChangeType.RIDER_PICKUP_LOCATION,
                    ShopChangeActionType.DELETE,
                    toShopChangeActor(actorType, actorId),
                    previousValue,
                    null
                );
            }
        });
    }

    /**
     * 라이더 안내의 액터 표현을 가게 변경이력의 액터 표현으로 옮긴다.
     *
     * <p>파라미터를 하나 더 받지 않고 <b>기존 {@code actorType}/{@code actorId}에서 파생</b>한다. 두 값을
     * 별도로 받으면 호출부가 서로 어긋난 액터를 넘길 수 있고(예: {@code CEO} + admin id) 컴파일러가 그것을
     * 잡아주지 못한다 — 같은 사실을 두 번 전달받지 않는 편이 안전하다.
     */
    private ShopChangeActor toShopChangeActor(RiderGuideActorType actorType, Long actorId) {
        return actorType == RiderGuideActorType.CEO
            ? ShopChangeActor.ceo(actorId)
            : ShopChangeActor.admin(actorId);
    }

    /**
     * 라이더 안내 문구를 이력 요약으로 만든다 — 원문 그대로이며, 비어 있으면 "미설정"으로 적는다.
     */
    private String describeVisitGuide(String visitGuide) {
        return visitGuide == null || visitGuide.isBlank() ? ShopChangeValueFormatter.unset() : visitGuide;
    }

    /**
     * 픽업 위치를 한 줄로 요약한다(예: {@code "서울 강남구 테헤란로 123 (뒷문)"}). 도로명주소가 없으면
     * "미설정"으로 적는다 — 좌표만 남은 상태는 화면에서 위치로 취급되지 않는다.
     */
    private String describePickupLocation(ShopRiderGuide riderGuide) {
        String roadAddress = riderGuide.getPickupRoadAddress();
        if (roadAddress == null || roadAddress.isBlank()) {
            return ShopChangeValueFormatter.unset();
        }
        String detailAddress = riderGuide.getPickupDetailAddress();
        return detailAddress == null || detailAddress.isBlank()
            ? roadAddress
            : roadAddress + " (" + detailAddress + ")";
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
