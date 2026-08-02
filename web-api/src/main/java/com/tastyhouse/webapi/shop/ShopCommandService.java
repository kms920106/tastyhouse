package com.tastyhouse.webapi.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.shop.domain.service.ShopLifecycleService;

/**
 * 회원용 가게 변경 서비스(CQRS command 측).
 *
 * <p>회원이 가게에 대해 수행하는 변경은 즐겨찾기 토글 하나뿐이다. 즐겨찾기 등록 시 가게 존재를
 * 확인하는 규칙은 도메인 서비스 {@link ShopLifecycleService}가 담당한다.
 */
@Service
@Transactional
public class ShopCommandService {

    private final ShopLifecycleService shopLifecycleService;

    public ShopCommandService(ShopLifecycleService shopLifecycleService) {
        this.shopLifecycleService = shopLifecycleService;
    }

    /**
     * 즐겨찾기를 토글한다.
     *
     * @return 토글 후 즐겨찾기 상태(true = 등록됨)
     */
    public boolean toggleBookmark(Long shopId, Long memberId) {
        return shopLifecycleService.toggleBookmark(shopId, MemberId.of(memberId));
    }
}
