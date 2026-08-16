package com.tastyhouse.domain.product.service;

import com.tastyhouse.domain.product.port.ProductReviewStatisticsPort;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;

/**
 * 상품 메뉴 평가 통계 갱신 도메인 서비스(순수 POJO).
 *
 * <p>메뉴 평가가 등록·수정·삭제될 때 상품 애그리거트의 평점·평가 수를 다시 계산해 반영한다. 집계는
 * menureview 도메인에 속한 데이터이므로 출력 포트 {@link ProductReviewStatisticsPort}로 읽고, 그 결과를
 * 상품 애그리거트에 쓰는 크로스 애그리거트 오케스트레이션이다(분류 C).
 *
 * <p><b>근거가 MENU_REVIEW로 이관되며 계산이 단순해졌다.</b> 과거에는 REVIEW의 맛·양·가격 3항목 중
 * 존재하는 것만 부분 평균하는 로직을 여기 두었으나, 메뉴 평가는 축이 {@code rating} 하나뿐이라 포트의
 * 평균값을 그대로 쓴다. 소수 첫째 자리 반올림은 유지한다.
 *
 * <p>이벤트 수신(리스너)은 infrastructure-module의 {@code ProductMenuReviewEventListener}가 담당하고,
 * 이 서비스는 갱신 규칙만 갖는다 — 어느 모듈이 이벤트를 발행하든 같은 규칙이 적용되도록 분리했다.
 * {@code @Service}/{@code @Transactional}을 갖지 않으며 빈 등록은 {@code ProductDomainConfig}가 한다.
 */
public class ProductReviewStatsService {

    private final ProductRepository productRepository;
    private final ProductReviewStatisticsPort productReviewStatisticsPort;

    public ProductReviewStatsService(
        ProductRepository productRepository,
        ProductReviewStatisticsPort productReviewStatisticsPort
    ) {
        this.productRepository = productRepository;
        this.productReviewStatisticsPort = productReviewStatisticsPort;
    }

    /**
     * 상품의 노출 메뉴 평가 수와 평균 평점을 다시 계산해 반영한다. 상품이 없으면 조용히 넘어간다
     * (이벤트 처리 중 이미 삭제된 상품일 수 있음).
     *
     * <p>평가가 0건이면 평균은 {@code null}(평점 미산정)이 된다 — 0.0으로 떨어뜨리면 "평점 0점"과
     * 구분되지 않는다.
     */
    public void updateReviewStats(Long productId) {
        productRepository.findById(ProductId.of(productId)).ifPresent(product -> {
            Long count = productReviewStatisticsPort.countVisibleMenuReviewsByProductId(productId);
            Double rating = roundToTenth(productReviewStatisticsPort.getAverageMenuRatingByProductId(productId));
            product.updateReviewStats(rating, count != null ? count.intValue() : 0);
            productRepository.save(product);
        });
    }

    /**
     * 소수 첫째 자리로 반올림한다. 대상 평가가 없어 평균이 {@code null}이면 그대로 {@code null}이다.
     */
    private Double roundToTenth(Double rating) {
        return rating == null ? null : Math.round(rating * 10.0) / 10.0;
    }
}
