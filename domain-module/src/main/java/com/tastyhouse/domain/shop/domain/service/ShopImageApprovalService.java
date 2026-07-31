package com.tastyhouse.domain.shop.domain.service;

import com.tastyhouse.domain.shop.domain.model.Shop;
import com.tastyhouse.domain.shop.domain.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.domain.model.ShopImageType;
import com.tastyhouse.domain.shop.domain.repository.ShopImageChangeRequestRepository;
import com.tastyhouse.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 가게 이미지(상표·대표이미지) 변경 승인 워크플로 불변식(도메인 서비스).
 *
 * <p>이미지 변경은 "점주 요청 → 관리자 검수 → 승인 시 가게에 반영"이라는 워크플로를 따르며, 그 규칙은
 * 요청자(ceo)와 검수자(admin)가 서로 다른 액터임에도 동일하게 유지되어야 한다. 특히 승인
 * ({@link #approveImageChange(Long)})은 <b>요청 애그리거트의 상태 전이와 가게 애그리거트의 이미지
 * 교체가 한 트랜잭션에서 반드시 함께</b> 일어나야 하는 원자 연산이다(둘 중 하나만 반영되면 "승인됐는데
 * 이미지가 안 바뀐" 상태가 남는다). {@code ShopImageChangeRequest}와 {@code Shop} 두 애그리거트 타입을
 * 함께 load &amp; save 하는 불변식 오케스트레이션(분류 C)이므로 도메인 계층에 둔다.
 *
 * <p>같은 가게·같은 이미지 유형에 PENDING 요청이 2건 생기지 않도록 요청 생성 시 중복을 막고
 * ({@code SHOP_IMAGE_CHANGE_REQUEST_ALREADY_PENDING}), 진행 중 요청이 있으면 가게 노출정지 변경을
 * 차단하는 판정({@link #existsPendingByShopId(Long)})도 이 서비스가 제공한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다.
 *
 * <p>도메인 모델은 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 {@code save}를 호출한다.
 */
public class ShopImageApprovalService {

    private final ShopImageChangeRequestRepository shopImageChangeRequestRepository;
    private final ShopRepository shopRepository;

    public ShopImageApprovalService(
        ShopImageChangeRequestRepository shopImageChangeRequestRepository,
        ShopRepository shopRepository
    ) {
        this.shopImageChangeRequestRepository = shopImageChangeRequestRepository;
        this.shopRepository = shopRepository;
    }

    /**
     * 이미지 변경을 요청한다. 같은 가게·같은 이미지 유형에 이미 PENDING 요청이 있으면 거부한다.
     *
     * @return 생성된 변경요청 식별자
     */
    public Long requestImageChange(Long shopId, ShopImageType imageType, Long imageFileId) {
        if (shopImageChangeRequestRepository.existsByShopIdAndImageTypeAndStatus(shopId, imageType, ApprovalStatus.PENDING)) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_ALREADY_PENDING);
        }

        ShopImageChangeRequest saved = shopImageChangeRequestRepository.save(
            ShopImageChangeRequest.of(shopId, imageType, imageFileId)
        );
        return saved.getId();
    }

    /**
     * 이미지 변경요청을 승인하고, 승인된 이미지를 가게에 즉시 반영한다(원자 연산).
     */
    public void approveImageChange(Long id) {
        ShopImageChangeRequest shopImageChangeRequest = shopImageChangeRequestRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_NOT_FOUND));
        shopImageChangeRequest.approve();
        shopImageChangeRequestRepository.save(shopImageChangeRequest);

        ShopId shopId = ShopId.of(shopImageChangeRequest.getShopId());
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        if (shopImageChangeRequest.getImageType() == ShopImageType.TRADEMARK) {
            shop.changeTrademarkImage(shopImageChangeRequest.getImageFileId());
        } else {
            shop.changeThumbnailImage(shopImageChangeRequest.getImageFileId());
        }
        shopRepository.save(shop);
    }

    /**
     * 이미지 변경요청을 반려한다. 가게 이미지는 바뀌지 않는다.
     */
    public void rejectImageChange(Long id, String reason) {
        ShopImageChangeRequest shopImageChangeRequest = shopImageChangeRequestRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_NOT_FOUND));
        shopImageChangeRequest.reject(reason);
        shopImageChangeRequestRepository.save(shopImageChangeRequest);
    }

    /**
     * 그 가게에 진행 중(PENDING)인 이미지 변경요청이 있는지. 노출정지 변경 차단 판정에 쓰인다.
     */
    public boolean existsPendingByShopId(Long shopId) {
        return shopImageChangeRequestRepository.existsByShopIdAndStatus(shopId, ApprovalStatus.PENDING);
    }
}
