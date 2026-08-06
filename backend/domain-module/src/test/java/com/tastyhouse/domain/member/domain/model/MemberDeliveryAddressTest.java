package com.tastyhouse.domain.member.domain.model;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.region.vo.AdminDongId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 회원 배달 주소 애그리거트 단위 테스트.
 *
 * <p>좌표 필수 검증이 이 테스트의 핵심이다 — 좌표 없는 주소가 저장되면 거리별 배달팁 할증이 0원이 되어
 * 매출 누수이자 조작 가능한 취약점이 된다. 생성뿐 아니라 <b>변경</b> 경로도 같은 검증을 통과해야 한다.
 */
class MemberDeliveryAddressTest {

    private static final MemberId MEMBER_ID = MemberId.of(1L);
    private static final MemberId OTHER_MEMBER_ID = MemberId.of(2L);
    private static final BigDecimal LATITUDE = new BigDecimal("37.501234");
    private static final BigDecimal LONGITUDE = new BigDecimal("127.039876");

    @Nested
    @DisplayName("생성(of)")
    class Creation {

        @Test
        @DisplayName("도로명 주소와 좌표가 모두 있으면 생성된다")
        void of_createsAddress() {
            MemberDeliveryAddress address = createAddress(LATITUDE, LONGITUDE);

            assertThat(address.getId()).isNull();
            assertThat(address.getMemberId()).isEqualTo(MEMBER_ID);
            assertThat(address.getRoadAddress()).isEqualTo("서울특별시 강남구 테헤란로 123");
            assertThat(address.getLatitude()).isEqualByComparingTo(LATITUDE);
            assertThat(address.getLongitude()).isEqualByComparingTo(LONGITUDE);
            assertThat(address.isDefaultAddress()).isFalse();
        }

        @Test
        @DisplayName("위도가 없으면 거부한다 — 좌표 없는 주소는 거리별 배달팁을 0원으로 만든다")
        void of_rejectsNullLatitude() {
            assertThatThrownBy(() -> createAddress(null, LONGITUDE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("좌표");
        }

        @Test
        @DisplayName("경도가 없으면 거부한다")
        void of_rejectsNullLongitude() {
            assertThatThrownBy(() -> createAddress(LATITUDE, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("좌표");
        }

        @Test
        @DisplayName("도로명 주소가 null이면 거부한다")
        void of_rejectsNullRoadAddress() {
            assertThatThrownBy(() -> MemberDeliveryAddress.of(
                MEMBER_ID, "집", null, null, null, null, LATITUDE, LONGITUDE, false
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("도로명 주소");
        }

        @Test
        @DisplayName("도로명 주소가 공백뿐이면 거부한다")
        void of_rejectsBlankRoadAddress() {
            assertThatThrownBy(() -> MemberDeliveryAddress.of(
                MEMBER_ID, "집", "   ", null, null, null, LATITUDE, LONGITUDE, false
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("도로명 주소");
        }

        @Test
        @DisplayName("행정동은 매칭 실패를 뜻하는 null을 허용한다")
        void of_allowsNullAdminDongId() {
            MemberDeliveryAddress address = createAddress(LATITUDE, LONGITUDE);

            assertThat(address.getAdminDongId()).isNull();
        }
    }

    @Nested
    @DisplayName("변경(update)")
    class Update {

        @Test
        @DisplayName("생성과 같은 좌표 필수 검증을 강제한다 — 변경을 열어두면 뒷문이 된다")
        void update_rejectsNullCoordinates() {
            MemberDeliveryAddress address = createAddress(LATITUDE, LONGITUDE);

            assertThatThrownBy(() -> address.update(
                "회사", "서울특별시 강남구 테헤란로 500", null, null, null, null, LONGITUDE
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("좌표");
        }

        @Test
        @DisplayName("도로명 주소가 비면 거부한다")
        void update_rejectsBlankRoadAddress() {
            MemberDeliveryAddress address = createAddress(LATITUDE, LONGITUDE);

            assertThatThrownBy(() -> address.update(
                "회사", "", null, null, null, LATITUDE, LONGITUDE
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("도로명 주소");
        }

        @Test
        @DisplayName("유효한 값이면 주소와 행정동을 갱신하고 기본 배송지 여부는 건드리지 않는다")
        void update_changesAddressKeepingDefaultFlag() {
            MemberDeliveryAddress address = MemberDeliveryAddress.of(
                MEMBER_ID, "집", "서울특별시 강남구 테헤란로 123", null, null, null, LATITUDE, LONGITUDE, true
            );

            address.update(
                "회사",
                "서울특별시 강남구 테헤란로 500",
                "서울특별시 강남구 역삼1동 678-9",
                "10층",
                AdminDongId.of(1168064000L),
                new BigDecimal("37.505000"),
                new BigDecimal("127.045000")
            );

            assertThat(address.getAlias()).isEqualTo("회사");
            assertThat(address.getRoadAddress()).isEqualTo("서울특별시 강남구 테헤란로 500");
            assertThat(address.getDetailAddress()).isEqualTo("10층");
            assertThat(address.getAdminDongId()).isEqualTo(AdminDongId.of(1168064000L));
            assertThat(address.isDefaultAddress()).isTrue();
        }
    }

    @Nested
    @DisplayName("기본 배송지 표시")
    class DefaultFlag {

        @Test
        @DisplayName("markAsDefault와 unmarkDefault가 표시를 전환한다")
        void markAndUnmark() {
            MemberDeliveryAddress address = createAddress(LATITUDE, LONGITUDE);

            address.markAsDefault();
            assertThat(address.isDefaultAddress()).isTrue();

            address.unmarkDefault();
            assertThat(address.isDefaultAddress()).isFalse();
        }
    }

    @Nested
    @DisplayName("소유권 판정(isOwnedBy)")
    class Ownership {

        @Test
        @DisplayName("같은 회원이면 true를 반환한다")
        void isOwnedBy_returnsTrueForOwner() {
            MemberDeliveryAddress address = createAddress(LATITUDE, LONGITUDE);

            assertThat(address.isOwnedBy(MEMBER_ID)).isTrue();
        }

        @Test
        @DisplayName("다른 회원이면 false를 반환한다")
        void isOwnedBy_returnsFalseForOtherMember() {
            MemberDeliveryAddress address = createAddress(LATITUDE, LONGITUDE);

            assertThat(address.isOwnedBy(OTHER_MEMBER_ID)).isFalse();
        }
    }

    @Nested
    @DisplayName("재구성(reconstitute)")
    class Reconstitute {

        @Test
        @DisplayName("좌표가 없는 기존 행도 로드할 수 있다 — 불변식 도입 이전 데이터 보호")
        void reconstitute_skipsValidation() {
            assertThatCode(() -> MemberDeliveryAddress.reconstitute(
                10L, MEMBER_ID, null, null, null, null, null, null, null, false, null, null
            )).doesNotThrowAnyException();
        }
    }

    private static MemberDeliveryAddress createAddress(BigDecimal latitude, BigDecimal longitude) {
        return MemberDeliveryAddress.of(
            MEMBER_ID,
            "집",
            "서울특별시 강남구 테헤란로 123",
            "서울특별시 강남구 역삼1동 678-9",
            "101동 1001호",
            null,
            latitude,
            longitude,
            false
        );
    }
}
