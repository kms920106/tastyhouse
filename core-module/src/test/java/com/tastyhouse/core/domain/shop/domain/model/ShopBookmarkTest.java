package com.tastyhouse.core.domain.shop.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopBookmarkTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientBookmark() {
        ShopBookmark bookmark = ShopBookmark.of(1L, MemberId.of(2L));

        assertThat(bookmark.getId()).isNull();
        assertThat(bookmark.getShopId()).isEqualTo(1L);
        assertThat(bookmark.getMemberId()).isEqualTo(MemberId.of(2L));
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopBookmark bookmark = ShopBookmark.reconstitute(1L, 2L, MemberId.of(3L));

        assertThat(bookmark.getId()).isEqualTo(1L);
        assertThat(bookmark.getShopId()).isEqualTo(2L);
        assertThat(bookmark.getMemberId()).isEqualTo(MemberId.of(3L));
    }
}
