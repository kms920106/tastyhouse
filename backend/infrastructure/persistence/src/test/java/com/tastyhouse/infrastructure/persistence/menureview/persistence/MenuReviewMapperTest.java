package com.tastyhouse.infrastructure.persistence.menureview.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.model.MenuReview;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class MenuReviewMapperTest {

    @Test
    @DisplayName("MenuReview → 엔티티 변환 시 모든 컬럼 값이 옮겨진다")
    void toEntityCopiesColumns() {
        MenuReviewJpaEntity entity = MenuReviewMapper.toEntity(original());

        assertThat(entity.getMemberId()).isEqualTo(92L);
        assertThat(entity.getShopId()).isEqualTo(93L);
        assertThat(entity.getProductId()).isEqualTo(94L);
        assertThat(entity.getOrderId()).isEqualTo(95L);
        assertThat(entity.getOrderProductId()).isEqualTo(96L);
        assertThat(entity.getRating()).isEqualTo(4);
        assertThat(entity.getComment()).isEqualTo("맛있어요");
        assertThat(entity.isHidden()).isTrue();
    }

    @Test
    @DisplayName("엔티티 → MenuReview 변환 시 id·생성일·수정일을 포함한 모든 필드가 복원된다")
    void toDomainRestoresAllFields() {
        MenuReview original = original();
        MenuReviewJpaEntity entity = MenuReviewMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", original.getId());
        ReflectionTestUtils.setField(entity, "createdAt", original.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", original.getUpdatedAt());

        MenuReview restored = MenuReviewMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("applyChanges는 평점·코멘트·숨김 여부를 옮긴다")
    void applyChangesCopiesWritableFields() {
        MenuReviewJpaEntity entity = MenuReviewMapper.toEntity(MenuReview.reconstitute(
            91L, MemberId.of(92L), ShopId.of(93L), ProductId.of(94L), OrderId.of(95L), OrderProductId.of(96L),
            1, "별로", false, null, null));

        MenuReviewMapper.applyChanges(entity, original());

        assertThat(entity.getRating()).isEqualTo(4);
        assertThat(entity.getComment()).isEqualTo("맛있어요");
        assertThat(entity.isHidden()).isTrue();
    }

    private static MenuReview original() {
        return MenuReview.reconstitute(
            91L, MemberId.of(92L), ShopId.of(93L), ProductId.of(94L), OrderId.of(95L), OrderProductId.of(96L),
            4, "맛있어요", true,
            LocalDateTime.of(2026, 6, 1, 10, 0),
            LocalDateTime.of(2026, 6, 2, 11, 0));
    }
}
