package com.tastyhouse.webapplication.shop.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.product.port.StorePriceVerificationPort;

/**
 * 가게의 매장가격 인증 플래그를 읽어주는 협력 빈.
 *
 * <p><b>왜 별도 컴포넌트인가</b> — 이 플래그에는 표현 목적 투영을 제공하는 infra {@code ..query..}
 * DAO가 없고, 출력 포트 {@link StorePriceVerificationPort}가 유일한 읽기 경로다. 그런데 그 포트는
 * 인증을 켜고 내리는 쓰기 메서드({@code verifyStorePrice}·{@code clearStorePriceVerification})를 함께
 * 갖고 있어, {@code *QueryService}가 직접 주입하면 조회 트랜잭션({@code readOnly = true})에 쓰기
 * 경로가 열린다(CQRS 교차 주입 금지의 취지).
 *
 * <p>그래서 읽기를 이 협력 빈에 가두고 <b>읽기 메서드만</b> 노출한다. {@code ceo-api}의 같은 이름
 * 컴포넌트가 동일한 배치이며, 그쪽은 최근 인증 요청까지 함께 읽는 반면 손님 화면은 뱃지 판정에 쓰는
 * 플래그 하나만 필요해 이 클래스는 그 메서드만 갖는다.
 */
@Component
public class StorePriceVerificationReader {

    private final StorePriceVerificationPort storePriceVerificationPort;

    public StorePriceVerificationReader(StorePriceVerificationPort storePriceVerificationPort) {
        this.storePriceVerificationPort = storePriceVerificationPort;
    }

    /**
     * 현재 인증 플래그 — 승인 이력이 아니라 <b>지금</b> 인증 상태다(승인 후에도 배달가가 매장가를 넘으면
     * 해제된다). '매장과 같은 가격' 뱃지의 유일한 조건이다.
     */
    public boolean readVerified(Long shopId) {
        return storePriceVerificationPort.isStorePriceVerified(shopId);
    }
}
