package com.tastyhouse.infrastructure.mybatis.notice.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeMyBatisQueryAdapterTest {

    @Test
    @DisplayName("검색어가 null이거나 공백이면 조건을 걸지 않는다")
    void blankKeywordHasNoPattern() {
        assertThat(NoticeMyBatisQueryAdapter.containsPattern(null)).isNull();
        assertThat(NoticeMyBatisQueryAdapter.containsPattern("")).isNull();
        assertThat(NoticeMyBatisQueryAdapter.containsPattern("   ")).isNull();
    }

    @Test
    @DisplayName("검색어는 소문자로 바꾸고 앞뒤를 %로 감싼다")
    void wrapsLowercasedKeyword() {
        assertThat(NoticeMyBatisQueryAdapter.containsPattern("Notice")).isEqualTo("%notice%");
    }

    @Test
    @DisplayName("LIKE 와일드카드와 이스케이프 문자는 !로 이스케이프한다")
    void escapesWildcardsAndEscapeChar() {
        assertThat(NoticeMyBatisQueryAdapter.containsPattern("50%")).isEqualTo("%50!%%");
        assertThat(NoticeMyBatisQueryAdapter.containsPattern("a_b")).isEqualTo("%a!_b%");
        assertThat(NoticeMyBatisQueryAdapter.containsPattern("Ab!")).isEqualTo("%ab!!%");
    }
}
