package com.tastyhouse.adminapi.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.service.ShopMenuCollectionImageService;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;

/**
 * 메뉴모음컷 검수 변경 서비스(CQRS command 측).
 *
 * <p>상태 전이 규칙(대기 상태만 처리 가능·반려 사유 필수)은 요청자 ceo·검수자 admin 양쪽이 공유하는
 * 규칙이라 도메인 서비스가 소유한다. 이 서비스는 트랜잭션 경계와 식별자 승격(Long → ID VO)만 책임진다.
 */
@Service
@Transactional
public class ShopMenuCollectionImageCommandService {

    private final ShopMenuCollectionImageService shopMenuCollectionImageService;

    public ShopMenuCollectionImageCommandService(ShopMenuCollectionImageService shopMenuCollectionImageService) {
        this.shopMenuCollectionImageService = shopMenuCollectionImageService;
    }

    public void approveMenuCollectionImage(Long id) {
        ShopMenuCollectionImageId imageId = ShopMenuCollectionImageId.of(id);
        shopMenuCollectionImageService.approve(imageId);
    }

    public void rejectMenuCollectionImage(Long id, String rejectReason) {
        ShopMenuCollectionImageId imageId = ShopMenuCollectionImageId.of(id);
        shopMenuCollectionImageService.reject(imageId, rejectReason);
    }
}
