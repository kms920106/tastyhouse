package com.tastyhouse.domain.product.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.product.vo.BbqCategoryId;
import com.tastyhouse.domain.product.vo.BbqMenuId;
import com.tastyhouse.domain.product.vo.ProductId;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다.
 */
class ProductBbqTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)이고 옵션 미동기화 상태다")
    void of_createsTransientProductBbq() {
        ProductBbq bbq = ProductBbq.of(ProductId.of(1L), BbqMenuId.of(100L), BbqCategoryId.of(200L), false);

        assertThat(bbq.getId()).isNull();
        assertThat(bbq.getProductId()).isEqualTo(ProductId.of(1L));
        assertThat(bbq.getBbqMenuId()).isEqualTo(BbqMenuId.of(100L));
        assertThat(bbq.getBbqCategoryId()).isEqualTo(BbqCategoryId.of(200L));
        assertThat(bbq.isOptionsSynced()).isFalse();
    }

    @Test
    @DisplayName("markOptionsSynced는 옵션 동기화 완료 상태로 변경한다")
    void markOptionsSynced_marksSynced() {
        ProductBbq bbq = ProductBbq.of(ProductId.of(1L), BbqMenuId.of(100L), BbqCategoryId.of(200L), false);

        bbq.markOptionsSynced();

        assertThat(bbq.isOptionsSynced()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ProductBbq bbq = ProductBbq.reconstitute(1L, ProductId.of(10L), BbqMenuId.of(100L), BbqCategoryId.of(200L), true);

        assertThat(bbq.getId()).isEqualTo(1L);
        assertThat(bbq.getProductId()).isEqualTo(ProductId.of(10L));
        assertThat(bbq.isOptionsSynced()).isTrue();
    }
}
