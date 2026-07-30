package com.tastyhouse.core.domain.shop.domain.service;

import java.math.BigDecimal;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopBookmark;
import com.tastyhouse.core.domain.shop.domain.model.ShopOwnerMessageHistory;
import com.tastyhouse.core.domain.shop.domain.repository.ShopBookmarkRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopDetailRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.core.domain.shop.domain.repository.StationRepository;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

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
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다.
 *
 * <p>도메인 모델은 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 {@code save}를 호출한다.
 */
public class ShopLifecycleService {

    private static final int SHOP_INTRODUCTION_MAX_LENGTH = 500;

    private final ShopRepository shopRepository;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopBookmarkRepository shopBookmarkRepository;
    private final StationRepository stationRepository;
    private final ShopImageApprovalService shopImageApprovalService;
    private final ProhibitedWordValidator prohibitedWordValidator;

    public ShopLifecycleService(
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ShopBookmarkRepository shopBookmarkRepository,
        StationRepository stationRepository,
        ShopImageApprovalService shopImageApprovalService,
        ProhibitedWordValidator prohibitedWordValidator
    ) {
        this.shopRepository = shopRepository;
        this.shopDetailRepository = shopDetailRepository;
        this.shopBookmarkRepository = shopBookmarkRepository;
        this.stationRepository = stationRepository;
        this.shopImageApprovalService = shopImageApprovalService;
        this.prohibitedWordValidator = prohibitedWordValidator;
    }

    /**
     * 가게를 생성한다. 지정한 지하철역이 존재해야 한다.
     */
    public Shop createShop(
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
            stationId,
            name,
            latitude,
            longitude,
            roadAddress,
            lotAddress,
            phoneNumber,
            thumbnailImageFileId
        );
        shop.assignCeo(ceoId);
        return shopRepository.save(shop);
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
            stationId,
            name,
            latitude,
            longitude,
            roadAddress,
            lotAddress,
            phoneNumber,
            thumbnailImageFileId
        );
        shopRepository.save(shop);
    }

    public void closeShop(ShopId shopId) {
        Shop shop = loadShop(shopId);
        shop.close();
        shopRepository.save(shop);
    }

    /**
     * 공휴일 휴무 여부를 설정한다.
     */
    public void updateHolidayClosure(ShopId shopId, boolean closedOnPublicHolidays) {
        Shop shop = loadShop(shopId);
        shop.updateHolidayClosure(closedOnPublicHolidays);
        shopRepository.save(shop);
    }

    /**
     * 가게 노출 상태(노출정지)를 변경한다. 진행 중인 이미지 변경 승인 요청이 있으면 상태 변경을 차단한다.
     */
    public void changeVisibility(ShopId shopId, boolean hidden) {
        if (shopImageApprovalService.existsPendingByShopId(shopId.value())) {
            throw new BusinessException(ErrorCode.SHOP_STATUS_CHANGE_BLOCKED_BY_PENDING_REQUEST);
        }
        Shop shop = loadShop(shopId);
        if (hidden) {
            shop.hide();
        } else {
            shop.show();
        }
        shopRepository.save(shop);
    }

    /**
     * 사장님 한마디(가게소개)를 새로 등록한다. 최대 {@value #SHOP_INTRODUCTION_MAX_LENGTH}자 제한과
     * 금칙어 검수를 통과해야 한다.
     */
    public void createOwnerMessage(Long shopId, String message) {
        if (message != null && message.length() > SHOP_INTRODUCTION_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_INTRODUCTION_TOO_LONG);
        }
        prohibitedWordValidator.validate(message);
        ShopOwnerMessageHistory ownerMessageHistory = ShopOwnerMessageHistory.of(shopId, message);
        shopDetailRepository.saveOwnerMessage(ownerMessageHistory);
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
        shopBookmarkRepository.save(ShopBookmark.of(shopId, memberId));
        return true;
    }

    private Shop loadShop(ShopId shopId) {
        return shopRepository.findById(shopId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    private void validateStationExists(Long stationId) {
        if (!stationRepository.existsById(stationId)) {
            throw new EntityNotFoundException(ErrorCode.STATION_NOT_FOUND);
        }
    }
}
