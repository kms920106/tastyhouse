package com.tastyhouse.domain.shop.domain.model;

import com.tastyhouse.domain.shop.model.ShopChoice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.shop.vo.ShopId;

class ShopChoiceTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientShopChoice() {
        ShopChoice shopChoice = ShopChoice.of(ShopId.of(1L), "제목", "내용");

        assertThat(shopChoice.getId()).isNull();
        assertThat(shopChoice.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(shopChoice.getTitle()).isEqualTo("제목");
        assertThat(shopChoice.getContent()).isEqualTo("내용");
    }

    @Test
    @DisplayName("update는 제목·내용을 변경한다")
    void update_changesFields() {
        ShopChoice shopChoice = ShopChoice.of(ShopId.of(1L), "제목", "내용");

        shopChoice.update("새 제목", "새 내용");

        assertThat(shopChoice.getTitle()).isEqualTo("새 제목");
        assertThat(shopChoice.getContent()).isEqualTo("새 내용");
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopChoice shopChoice = ShopChoice.reconstitute(1L, ShopId.of(2L), "제목", "내용");

        assertThat(shopChoice.getId()).isEqualTo(1L);
        assertThat(shopChoice.getShopId()).isEqualTo(ShopId.of(2L));
    }
}
