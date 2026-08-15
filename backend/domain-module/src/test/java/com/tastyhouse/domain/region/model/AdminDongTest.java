package com.tastyhouse.domain.region.model;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

/**
 * 행정동 마스터 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 표시용 이름 조립만 검증한다.
 */
class AdminDongTest {

    @Nested
    @DisplayName("fullName")
    class FullName {

        @Test
        @DisplayName("시/도 · 시/군/구 · 행정동을 공백 하나로 이어 표시용 전체 이름을 만든다")
        void fullName_joinsWithSingleSpace() {
            AdminDong adminDong = AdminDong.reconstitute(1L, "1168053100", "서울특별시", "강남구", "역삼1동", true, null, List.of());

            assertThat(adminDong.fullName()).isEqualTo("서울특별시 강남구 역삼1동");
        }

        @Test
        @DisplayName("시/군/구 이름이 두 단어여도 구분자를 추가로 넣지 않는다")
        void fullName_doesNotAddExtraSeparator() {
            AdminDong adminDong = AdminDong.reconstitute(2L, "4113554000", "경기도", "성남시 분당구", "정자1동", true, null, List.of());

            assertThat(adminDong.fullName()).isEqualTo("경기도 성남시 분당구 정자1동");
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("DB 상태로부터 식별자·코드·사용 여부를 포함해 재구성한다")
        void reconstitute_restoresPersistedState() {
            AdminDong adminDong = AdminDong.reconstitute(3L, "1168053100", "서울특별시", "강남구", "역삼1동", false, null, List.of());

            assertThat(adminDong.getId()).isEqualTo(3L);
            assertThat(adminDong.getCode()).isEqualTo("1168053100");
            assertThat(adminDong.getSidoName()).isEqualTo("서울특별시");
            assertThat(adminDong.getSigunguName()).isEqualTo("강남구");
            assertThat(adminDong.getDongName()).isEqualTo("역삼1동");
            assertThat(adminDong.isActive()).isFalse();
        }
    }
}
