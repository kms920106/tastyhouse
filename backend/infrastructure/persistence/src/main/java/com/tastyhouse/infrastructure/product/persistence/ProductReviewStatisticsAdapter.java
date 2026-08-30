package com.tastyhouse.infrastructure.product.persistence;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.product.port.ProductReviewStatisticsPort;
import com.tastyhouse.infrastructure.menureview.query.MenuReviewStatisticsQueryDao;

/**
 * 상품 메뉴 평가 통계 조회 포트({@link ProductReviewStatisticsPort}) 어댑터.
 *
 * <p>집계 조회 자체는 menureview 도메인 소유이므로 {@link MenuReviewStatisticsQueryDao}에 두고, 이
 * 어댑터는 상품 도메인이 필요로 하는 메서드만 골라 위임한다({@code rank}의
 * {@code MemberReviewCountAdapter}와 같은 형태). 덕분에 상품 쪽 코드는 메뉴 평가의 read model이나
 * QueryDSL을 알지 않는다.
 *
 * <p><b>위임 대상이 {@code ReviewStatisticsQueryDao} → {@link MenuReviewStatisticsQueryDao}로 바뀌었다</b>
 * — {@code PRODUCT.rating}의 근거가 REVIEW에서 MENU_REVIEW로 이관됐기 때문이다. 클래스 위치·이름은
 * 그대로 두므로 infra {@code LayerRulesTest}의 {@code persistenceShouldNotDependOnQuery} 봉인 목록에
 * 항목이 늘지 않는다.
 */
@Component
public class ProductReviewStatisticsAdapter implements ProductReviewStatisticsPort {

    private final MenuReviewStatisticsQueryDao menuReviewStatisticsQueryDao;

    public ProductReviewStatisticsAdapter(MenuReviewStatisticsQueryDao menuReviewStatisticsQueryDao) {
        this.menuReviewStatisticsQueryDao = menuReviewStatisticsQueryDao;
    }

    @Override
    public Long countVisibleMenuReviewsByProductId(Long productId) {
        return menuReviewStatisticsQueryDao.countVisibleByProductId(productId);
    }

    @Override
    public Double getAverageMenuRatingByProductId(Long productId) {
        return menuReviewStatisticsQueryDao.getAverageRatingByProductId(productId);
    }
}
