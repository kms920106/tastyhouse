package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBookmark;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;
import com.tastyhouse.domain.shop.repository.ShopBookmarkRepository;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.repository.StationRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.StationId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 가게 애그리거트 생애주기 불변식(도메인 서비스).
 *
 * <p>가게 생성·수정은 지하철역({@code Station}) 존재를 확인해야 하고, 노출 상태 변경은 진행 중인 이미지
 * 변경요청이 있으면 차단되어야 하며({@code SHOP_STATUS_CHANGE_BLOCKED_BY_PENDING_REQUEST}), 사장님
 * 한마디는 길이 제한과 금칙어 검수를 통과해야 한다. 모두 {@code Shop} 외의 애그리거트를 함께 읽어
 * 판정하는 크로스 애그리거트 규칙이며, 액터(admin 가게 CRUD · ceo 노출설정/가게소개)가 달라도 동일하게
 * 유지되어야 하므로 도메인 계층에 둔다(분류 C).
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code ShopDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다.
 *
 * <p>도메인 모델은 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 {@code save}를 호출한다.
 *
 * <p><b>변경이력 기록도 이 서비스가 소유한다</b>(운영 분류 {@code HOLIDAY_CLOSURE}·
 * {@code SHOP_VISIBILITY}). 이미 {@code Shop}을 로드해 둔 상태라 추가 조회 없이 변경 전 값을 얻을 수 있고,
 * 소비 모듈의 {@code CommandService}는 CQRS 교차 주입 금지로 변경 전 값을 볼 수 없다. 변경 주체
 * ({@link ShopChangeActor})는 도메인이 인증을 모르므로 마지막 파라미터로 명시 전달받는다.
 */
public class ShopLifecycleService {

    private static final int SHOP_INTRODUCTION_MAX_LENGTH = 500;

    private final ShopRepository shopRepository;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopBookmarkRepository shopBookmarkRepository;
    private final StationRepository stationRepository;
    private final ShopImageApprovalService shopImageApprovalService;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;
    private final ShopCeoAssignmentRecorder shopCeoAssignmentRecorder;

    public ShopLifecycleService(
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ShopBookmarkRepository shopBookmarkRepository,
        StationRepository stationRepository,
        ShopImageApprovalService shopImageApprovalService,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopCeoAssignmentRecorder shopCeoAssignmentRecorder
    ) {
        this.shopRepository = shopRepository;
        this.shopDetailRepository = shopDetailRepository;
        this.shopBookmarkRepository = shopBookmarkRepository;
        this.stationRepository = stationRepository;
        this.shopImageApprovalService = shopImageApprovalService;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
        this.shopCeoAssignmentRecorder = shopCeoAssignmentRecorder;
    }

    /**
     * 가게를 생성한다. 지정한 지하철역이 존재해야 한다.
     *
     * <p>점주를 함께 배정하면 접근권한 부여 이력({@code GRANT})을 남긴다 — 이것이 그 점주가 이 가게의
     * 개인정보에 접근할 수 있게 된 시점이므로, 나중에 {@code ShopCeoAssignmentService}로 배정한 경우와
     * 구별 없이 같은 이력에 남아야 한다. {@code ShopCeoAssignmentRecorder}를 생성자 필수 의존으로 두는
     * 이유는 {@code ShopChangeHistoryRecorder} 선례와 같다 — 새 배정 경로를 만들 때 배선 누락이 컴파일
     * 단계에서 드러난다.
     */
    public Shop createShop(
        Long adminId,
        Long ceoId,
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId
    ) {
        validateStationExists(stationId);
        Shop shop = Shop.of(
            StationId.of(stationId),
            name,
            latitude,
            longitude,
            roadAddress,
            lotAddress,
            phoneNumber,
            thumbnailImageFileId == null ? null : UploadedFileId.of(thumbnailImageFileId)
        );
        shop.assignCeo(ceoId == null ? null : CeoId.of(ceoId));
        Shop savedShop = shopRepository.save(shop);

        if (ceoId != null) {
            shopCeoAssignmentRecorder.recordGrant(savedShop.getShopId(), CeoId.of(ceoId), adminId);
        }
        return savedShop;
    }

    /**
     * 가게 기본 정보를 수정한다. 지정한 지하철역이 존재해야 한다.
     */
    public void updateShop(
        ShopId shopId,
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId
    ) {
        validateStationExists(stationId);
        Shop shop = loadShop(shopId);
        shop.update(
            StationId.of(stationId),
            name,
            latitude,
            longitude,
            roadAddress,
            lotAddress,
            phoneNumber,
            thumbnailImageFileId == null ? null : UploadedFileId.of(thumbnailImageFileId)
        );
        shopRepository.save(shop);
    }

    public void closeShop(ShopId shopId) {
        Shop shop = loadShop(shopId);
        shop.close();
        shopRepository.save(shop);
    }

    /**
     * 일회용컵 보증금제 대상 사업자 지정/해제를 반영한다. <b>admin 전용</b>이다.
     *
     * <p>변경이력({@code ShopChangeHistory})을 남기지 않는 이유는 그 이력이 <b>점주의 가게 설정 변경</b>을
     * 기록하는 것이기 때문이다. 이것은 점주가 바꾼 값이 아니라 외부 규제 지정 사실을 관리자가 반영한
     * 것이므로 성격이 다르다.
     */
    public void changeCupDepositEnabled(ShopId shopId, boolean cupDepositEnabled) {
        Shop shop = loadShop(shopId);
        shop.changeCupDepositEnabled(cupDepositEnabled);
        shopRepository.save(shop);
    }

    /**
     * 공휴일 휴무 여부를 설정한다.
     */
    public void updateHolidayClosure(ShopId shopId, boolean closedOnPublicHolidays, ShopChangeActor actor) {
        Shop shop = loadShop(shopId);
        String previousValue = describeHolidayClosure(shop.isClosedOnPublicHolidays());

        shop.updateHolidayClosure(closedOnPublicHolidays);
        shopRepository.save(shop);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.HOLIDAY_CLOSURE,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeHolidayClosure(shop.isClosedOnPublicHolidays())
        );
    }

    /**
     * 가게 노출 상태(노출정지)를 변경한다. 진행 중인 이미지 변경 승인 요청이 있으면 상태 변경을 차단한다.
     */
    public void changeVisibility(ShopId shopId, boolean hidden, ShopChangeActor actor) {
        if (shopImageApprovalService.existsPendingByShopId(shopId.value())) {
            throw new BusinessException(ErrorCode.SHOP_STATUS_CHANGE_BLOCKED_BY_PENDING_REQUEST);
        }
        Shop shop = loadShop(shopId);
        String previousValue = describeVisibility(shop.isHidden());

        if (hidden) {
            shop.hide();
        } else {
            shop.show();
        }
        shopRepository.save(shop);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.SHOP_VISIBILITY,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeVisibility(shop.isHidden())
        );
    }

    /**
     * 공휴일 휴무 설정을 한 줄로 요약한다(예: {@code "공휴일 휴무"} / {@code "공휴일 정상영업"}).
     */
    private String describeHolidayClosure(boolean closedOnPublicHolidays) {
        return closedOnPublicHolidays ? "공휴일 휴무" : "공휴일 정상영업";
    }

    /**
     * 가게 노출 상태를 한 줄로 요약한다(예: {@code "노출정지"} / {@code "노출중"}).
     */
    private String describeVisibility(boolean hidden) {
        return hidden ? "노출정지" : "노출중";
    }

    /**
     * 사장님 한마디(가게소개)를 새로 등록한다. 최대 {@value #SHOP_INTRODUCTION_MAX_LENGTH}자 제한과
     * 금칙어 검수를 통과해야 한다.
     *
     * <p>변경이력({@code INTRODUCTION})을 함께 남긴다. 사장님 한마디는 갱신이 아니라 append-only 이력이라
     * "현재 노출 문구"는 최신 행이므로, 저장 전에 최신 행을 읽어 변경 전 값으로 삼는다. 화면상으로는
     * 항상 수정이므로 처음 등록이든 재등록이든 {@code UPDATE}로 기록한다 — 점주에게는 "한마디를 바꿨다"
     * 한 가지 동작이고, 등록/수정 구분은 이력 목록에서 의미가 없다.
     *
     * <p>문구 전문을 그대로 담는다(자르지 않는다). {@code previous_value}/{@code new_value}는 TEXT
     * 컬럼이고, 무엇이 어떻게 달라졌는지 보려면 500자 원문이 필요하다.
     */
    public void createOwnerMessage(Long shopId, String message, ShopChangeActor actor) {
        if (message != null && message.length() > SHOP_INTRODUCTION_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_INTRODUCTION_TOO_LONG);
        }
        prohibitedWordValidator.validate(message);

        String previousValue = describeIntroduction(
            shopDetailRepository.findLatestOwnerMessage(shopId)
                .map(ShopOwnerMessageHistory::getMessage)
                .orElse(null)
        );

        ShopOwnerMessageHistory ownerMessageHistory = ShopOwnerMessageHistory.of(ShopId.of(shopId), message);
        shopDetailRepository.saveOwnerMessage(ownerMessageHistory);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.INTRODUCTION,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeIntroduction(message)
        );
    }

    /**
     * 사장님 한마디를 이력 요약으로 만든다 — 원문 그대로이며, 비어 있으면 "미설정"으로 적는다.
     */
    private String describeIntroduction(String message) {
        return message == null || message.isBlank() ? ShopChangeValueFormatter.unset() : message;
    }

    /**
     * 즐겨찾기를 토글한다. 새로 등록할 때만 가게 존재를 확인한다.
     *
     * @return 토글 후 즐겨찾기 상태(true = 등록됨)
     */
    public boolean toggleBookmark(Long shopId, MemberId memberId) {
        if (shopBookmarkRepository.existsByShopIdAndMemberId(shopId, memberId)) {
            shopBookmarkRepository.deleteByShopIdAndMemberId(shopId, memberId);
            return false;
        }
        loadShop(ShopId.of(shopId));
        shopBookmarkRepository.save(ShopBookmark.of(ShopId.of(shopId), memberId));
        return true;
    }

    private Shop loadShop(ShopId shopId) {
        return shopRepository.findById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    private void validateStationExists(Long stationId) {
        if (!stationRepository.existsById(stationId)) {
            throw new ResourceNotFoundException(ErrorCode.STATION_NOT_FOUND);
        }
    }
}
