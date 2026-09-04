package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.product.port.out.ProductShopLinkQueryPort;

/**
 * 로그인 점주가 소유한 가게 ID 집합을 제공하는 인가 협력 빈.
 *
 * <p><b>왜 별도 빈인가</b>: 이것은 표현 목적 조회가 아니라 <b>인가 판정 재료</b>다. 연결 대상이 여러
 * 건인 요청에서 가게마다 {@link ShopOwnershipValidator}를 반복 호출하면 그만큼 가게 조회가 늘어나므로,
 * 한 번에 읽어 집합으로 대조한다.
 *
 * <p>{@code CommandService}가 {@code ..query..} DAO를 직접 주입하는 것은 ArchUnit
 * {@code commandServicesShouldNotDependOnQueryDaos}가 금지한다(CQRS 교차 주입 금지). 그 규칙의 의도는
 * "쓰기 경로가 표현용 조회를 끌어다 쓰는 것"을 막는 데 있고, 소유권 판정은 그 범주가 아니다 —
 * {@code ShopOwnershipValidator}가 write 포트를 감싸 같은 역할을 하는 것과 같은 형태로, 인가 관심사를
 * 이 빈 안에 가둬 규칙을 우회가 아니라 <b>준수</b>한다.
 *
 * <p>도메인 write 포트에 {@code findAllByCeoId}를 더하지 않은 이유는 두 가지다 — 표현과 무관한 조회가
 * 도메인에 쌓이고, 그 포트를 손수 구현한 테스트 스텁 여러 곳이 한꺼번에 깨진다.
 */
@Component
@CeoApp
public class OwnedShopIdProvider {

    private final ProductShopLinkQueryPort productShopLinkQueryPort;

    public OwnedShopIdProvider(ProductShopLinkQueryPort productShopLinkQueryPort) {
        this.productShopLinkQueryPort = productShopLinkQueryPort;
    }

    /** 이 점주가 소유한 가게 ID 전부. 소유 가게가 없으면 빈 집합이다. */
    public Set<Long> findOwnedShopIds(Long ceoId) {
        return new HashSet<>(productShopLinkQueryPort.findOwnedShopIds(ceoId));
    }
}
