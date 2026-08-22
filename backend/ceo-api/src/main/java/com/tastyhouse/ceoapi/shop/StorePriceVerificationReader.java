package com.tastyhouse.ceoapi.shop;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.port.StorePriceVerificationPort;
import com.tastyhouse.domain.product.repository.StorePriceVerificationRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게의 매장 가격 인증 현황(최근 요청 + 인증 플래그)을 읽어주는 협력 빈.
 *
 * <p><b>왜 별도 컴포넌트인가</b> — 이 조회에는 표현 목적 투영을 제공하는 infra {@code ..query..} DAO가
 * 없다. 인증 요청 애그리거트는 관리자 검수 목록용 DAO만 갖고 있고, 점주 화면이 필요한 것은 "가게의
 * 최신 1건 + 현재 플래그"라 write 포트({@link StorePriceVerificationRepository})와 출력 포트
 * ({@link StorePriceVerificationPort})가 유일한 읽기 경로다.
 *
 * <p>그 포트를 {@code *QueryService}가 직접 주입하면 CQRS 교차 주입 금지(조회 → 쓰기)에 걸린다. 그래서
 * 읽기를 이 협력 빈에 가둔다 — {@link ShopOwnershipValidator}가 {@code ShopRepository}를 자기 안에
 * 가둬 다수의 {@code *QueryService}에 제공하고, {@code ShopFoodTypeCategoryReader}가 같은 형태로
 * 반대 방향을 가두는 것과 동일한 배치다. 이 빈은 <b>읽기 메서드만</b> 노출해 조회 트랜잭션에서 쓰기
 * 경로가 열리지 않게 한다.
 */
@Component
public class StorePriceVerificationReader {

    private final StorePriceVerificationRepository storePriceVerificationRepository;
    private final StorePriceVerificationPort storePriceVerificationPort;

    public StorePriceVerificationReader(
        StorePriceVerificationRepository storePriceVerificationRepository,
        StorePriceVerificationPort storePriceVerificationPort
    ) {
        this.storePriceVerificationRepository = storePriceVerificationRepository;
        this.storePriceVerificationPort = storePriceVerificationPort;
    }

    /** 가게의 가장 최근 인증 요청. 한 번도 요청하지 않았으면 비어 있다. */
    public Optional<StorePriceVerification> readLatest(Long shopId) {
        return storePriceVerificationRepository.findLatestByShopId(ShopId.of(shopId));
    }

    /** 현재 인증 플래그 — 최근 요청 상태와 별개 축이다(승인 후에도 배달가가 매장가를 넘으면 해제된다). */
    public boolean readVerified(Long shopId) {
        return storePriceVerificationPort.isStorePriceVerified(shopId);
    }
}
