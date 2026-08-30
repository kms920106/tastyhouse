package com.tastyhouse.ceoapplication.shop.service;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.shop.port.out.ShopQueryPort;

/**
 * 가게에 배정된 음식 유형(카테고리) 이름을 읽어주는 협력 빈.
 *
 * <p><b>왜 별도 컴포넌트인가</b> — 채식 설정 요청은 product 컨텍스트의 명령이지만 판정 근거가 shop
 * 컨텍스트의 카테고리 이름이다. product 도메인 서비스가 shop 모델을 직접 참조하면 컨텍스트 경계
 * 위반이라 도메인은 {@code Set<String>}을 <b>주입받는</b> 형태로 설계돼 있고, 그 값을 채우는 일이
 * 표현 계층의 몫으로 남는다.
 *
 * <p>그 값을 {@code *CommandService}가 직접 읽으면 명령 서비스가 infra {@code ..query..}에 결합돼
 * CQRS 교차 주입 금지에 걸린다. 그래서 조회를 이 협력 빈에 가둔다 —
 * {@link ShopOwnershipValidator}가 write 포트를 자기 안에 가둬 다수의 {@code *QueryService}에
 * 제공하는 것과 같은 형태이며, 대안(shop write 포트를 product 서비스에 주입)보다 의존 방향이 낫다.
 */
@Component
public class ShopFoodTypeCategoryReader {

    private final ShopQueryPort shopQueryPort;

    public ShopFoodTypeCategoryReader(ShopQueryPort shopQueryPort) {
        this.shopQueryPort = shopQueryPort;
    }

    /**
     * 가게 카테고리 표시명 집합. 배정이 없으면 빈 집합이다(그 경우 이름 기반 거절 근거가 없다).
     */
    public Set<String> readCategoryNames(Long shopId) {
        return Set.copyOf(shopQueryPort.findFoodTypeCategoryNames(shopId));
    }
}
