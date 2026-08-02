package com.tastyhouse.domain.product.service;

import com.tastyhouse.domain.product.port.ProductReviewStatisticsPort;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;

/**
 * 상품 리뷰 통계 갱신 도메인 서비스(순수 POJO).
 *
 * <p>리뷰가 등록·삭제될 때 상품 애그리거트의 평점·리뷰 수를 다시 계산해 반영한다. 리뷰 집계는 review
 * 도메인에 속한 데이터이므로 출력 포트 {@link ProductReviewStatisticsPort}로 읽고, 그 결과를 상품
 * 애그리거트에 쓰는 크로스 애그리거트 오케스트레이션이다(분류 C).
 *
 * <p>이벤트 수신(리스너)은 infrastructure-module의 {@code product/listener}가 담당하고, 이 서비스는
 * 갱신 규칙만 갖는다 — 어느 모듈이 리뷰 이벤트를 발행하든 같은 규칙이 적용되도록 분리했다.
 * {@code @Service}/{@code @Transactional}을 갖지 않으며 빈 등록은 {@code DomainServiceConfig}가 한다.
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
     * 상품의 노출 리뷰 수와 평균 평점을 다시 계산해 반영한다. 상품이 없으면 조용히 넘어간다
     * (이벤트 처리 중 이미 삭제된 상품일 수 있음).
     */
    public void updateReviewStats(Long productId) {
        productRepository.findById(ProductId.of(productId)).ifPresent(product -> {
            Long count = productReviewStatisticsPort.countVisibleReviewsByProductId(productId);
            Double rating = calculateAverageRating(productId);
            product.updateReviewStats(rating, count != null ? count.intValue() : 0);
            productRepository.save(product);
        });
    }

    /**
     * 맛·양·가격 세 평점의 평균. 세 값이 모두 없으면 null(평점 미산정)을 반환하며, 일부만 있으면 있는
     * 항목만으로 평균을 내고 소수점 첫째 자리로 반올림한다.
     */
    private Double calculateAverageRating(Long productId) {
        Double taste = productReviewStatisticsPort.getAverageTasteRatingByProductId(productId);
        Double amount = productReviewStatisticsPort.getAverageAmountRatingByProductId(productId);
        Double price = productReviewStatisticsPort.getAveragePriceRatingByProductId(productId);
        if (taste == null && amount == null && price == null) {
            return null;
        }
        double t = taste != null ? taste : 0.0;
        double a = amount != null ? amount : 0.0;
        double p = price != null ? price : 0.0;
        int divisor = (taste != null ? 1 : 0) + (amount != null ? 1 : 0) + (price != null ? 1 : 0);
        return Math.round((t + a + p) / divisor * 10.0) / 10.0;
    }
}
