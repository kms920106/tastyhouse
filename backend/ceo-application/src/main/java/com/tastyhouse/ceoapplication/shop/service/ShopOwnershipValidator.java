package com.tastyhouse.ceoapplication.shop.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 점주가 자기 소유 가게에만 접근하도록 강제하는 검증기.
 *
 * <p>점주 소유권은 admin(무제한)·web(회원 관점)과 구분되는 ceo-api 고유의 인가 관심사이므로,
 * core가 아니라 presentation(ceo-api)에 둔다. 모든 가게 관리 엔드포인트는 실행 전에 이 검증기를
 * 통과해 로그인한 점주(ceoId)가 대상 가게(shopId)의 소유자임을 확인한다.
 *
 * <p>CQRS 전환 후에는 core application 서비스가 아니라 domain write 포트({@link ShopRepository})를
 * 직접 주입한다 — 소유권 검증은 command 경로의 선행 조건이라 도메인 모델을 로드해야 하며, 표현용
 * 투영만 제공하는 query DAO로는 대체할 수 없다.
 */
@Component
public class ShopOwnershipValidator {

    private final ShopRepository shopRepository;

    public ShopOwnershipValidator(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    /**
     * 로그인 점주가 대상 가게의 소유자인지 검증하고, 소유 가게 도메인을 반환한다.
     *
     * @throws BusinessException 가게의 소유 점주가 로그인 점주와 다르거나 미배정인 경우
     */
    public Shop validateOwnership(Long ceoId, Long shopId) {
        Shop shop = shopRepository.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        if (shop.getCeoId() == null || !shop.getCeoId().equals(CeoId.of(ceoId))) {
            throw new BusinessException(ErrorCode.SHOP_ACCESS_DENIED);
        }
        return shop;
    }
}
