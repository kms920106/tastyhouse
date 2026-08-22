package com.tastyhouse.domain.product.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.repository.ProductExposureHourRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 노출기간 설정 저장 규칙의 순수 단위 테스트.
 *
 * <p>핵심은 <b>요일 묶음과 개별 요일의 혼용 금지</b>다 — 그런 조합을 애초에 저장하지 못하게 하면
 * SQL 술어(목록)와 계산기(상세)의 판정이 갈릴 여지가 사라진다.
 */
class ProductExposureServiceTest {

    private static final ProductId PRODUCT_ID = ProductId.of(10L);
    private static final ShopId SHOP_ID = ShopId.of(1L);

    @Test
    @DisplayName("★ 요일 묶음과 개별 요일을 함께 설정하면 거부한다")
    void replaceSchedule_mixedDayTypes_rejected() {
        Fixture fixture = new Fixture();

        assertThatThrownBy(() -> fixture.service.replaceSchedule(PRODUCT_ID, null, null, List.of(
            hour(DayType.WEEKDAY, LocalTime.of(11, 0), LocalTime.of(14, 0)),
            hour(DayType.MONDAY, LocalTime.of(18, 0), LocalTime.of(20, 0))
        )))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_EXPOSURE_DAY_TYPE_MIXED);
    }

    @Test
    @DisplayName("같은 요일이 두 번 오면 거부한다 — 유니크 제약 위반을 미리 막는다")
    void replaceSchedule_duplicateDayType_rejected() {
        Fixture fixture = new Fixture();

        assertThatThrownBy(() -> fixture.service.replaceSchedule(PRODUCT_ID, null, null, List.of(
            hour(DayType.MONDAY, LocalTime.of(11, 0), LocalTime.of(14, 0)),
            hour(DayType.MONDAY, LocalTime.of(18, 0), LocalTime.of(20, 0))
        )))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_EXPOSURE_DAY_TYPE_MIXED);
    }

    @Test
    @DisplayName("개별 요일끼리는 여러 건 함께 설정할 수 있다")
    void replaceSchedule_multipleSpecificDays_allowed() {
        Fixture fixture = new Fixture();

        fixture.service.replaceSchedule(PRODUCT_ID, null, null, List.of(
            hour(DayType.MONDAY, LocalTime.of(11, 0), LocalTime.of(14, 0)),
            hour(DayType.TUESDAY, LocalTime.of(11, 0), LocalTime.of(14, 0))
        ));

        assertThat(fixture.hours.rows).hasSize(2);
    }

    @Test
    @DisplayName("요일 묶음끼리는 여러 건 함께 설정할 수 있다")
    void replaceSchedule_multipleGroupDays_allowed() {
        Fixture fixture = new Fixture();

        fixture.service.replaceSchedule(PRODUCT_ID, null, null, List.of(
            hour(DayType.WEEKDAY, LocalTime.of(11, 0), LocalTime.of(14, 0)),
            hour(DayType.WEEKEND, LocalTime.of(12, 0), LocalTime.of(15, 0))
        ));

        assertThat(fixture.hours.rows).hasSize(2);
    }

    @Test
    @DisplayName("노출 종료일이 시작일보다 빠르면 거부한다")
    void replaceSchedule_invalidPeriod_rejected() {
        Fixture fixture = new Fixture();

        assertThatThrownBy(() -> fixture.service.replaceSchedule(
            PRODUCT_ID, LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 19), List.of()))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_EXPOSURE_PERIOD_INVALID);
    }

    @Test
    @DisplayName("replace-all이다 — 기존 시간대를 지우고 새 목록으로 바꾼다")
    void replaceSchedule_isReplaceAll() {
        Fixture fixture = new Fixture();
        fixture.service.replaceSchedule(PRODUCT_ID, null, null,
            List.of(hour(DayType.MONDAY, LocalTime.of(11, 0), LocalTime.of(14, 0))));

        fixture.service.replaceSchedule(PRODUCT_ID, null, null,
            List.of(hour(DayType.TUESDAY, LocalTime.of(18, 0), LocalTime.of(20, 0))));

        assertThat(fixture.hours.rows).hasSize(1);
        assertThat(fixture.hours.rows.getFirst().getDayType()).isEqualTo(DayType.TUESDAY);
    }

    @Test
    @DisplayName("빈 목록을 보내면 요일·시간 제약이 없는 상태가 된다")
    void replaceSchedule_emptyHours_clearsConstraint() {
        Fixture fixture = new Fixture();
        fixture.service.replaceSchedule(PRODUCT_ID, null, null,
            List.of(hour(DayType.MONDAY, LocalTime.of(11, 0), LocalTime.of(14, 0))));

        fixture.service.replaceSchedule(PRODUCT_ID, null, null, List.of());

        assertThat(fixture.hours.rows).isEmpty();
    }

    @Test
    @DisplayName("스케줄 해제는 기간·시간대를 비우되 숨김 상태는 건드리지 않는다")
    void clearSchedule_keepsVisibility() {
        Fixture fixture = new Fixture();
        fixture.product.deactivate();
        fixture.service.replaceSchedule(PRODUCT_ID, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
            List.of(hour(DayType.MONDAY, LocalTime.of(11, 0), LocalTime.of(14, 0))));

        fixture.service.clearSchedule(PRODUCT_ID);

        assertThat(fixture.hours.rows).isEmpty();
        assertThat(fixture.product.getExposureStartDate()).isNull();
        assertThat(fixture.product.getExposureEndDate()).isNull();
        // 숨김은 점주의 별개 의사이므로 스케줄 해제가 되살리지 않는다.
        assertThat(fixture.product.isVisible()).isFalse();
    }

    private ProductExposureHour hour(DayType dayType, LocalTime start, LocalTime end) {
        return ProductExposureHour.of(PRODUCT_ID, dayType, start, end);
    }

    // ── 픽스처 ─────────────────────────────────────────────────────────────────────

    private static final class Fixture {

        private final Product product = Product.reconstitute(
            10L, SHOP_ID, ProductCategoryId.of(2L), "떡볶이", null, 8000, null, null, 0,
            false, null, false, null, true, 0,
            false, false, null, false, null, null, null, null, null, null
        );
        private final FakeExposureHourRepository hours = new FakeExposureHourRepository();
        private final ProductExposureService service;

        private Fixture() {
            this.service = new ProductExposureService(
                new StubProductRepository(product), hours, new ProductExposureCalculator());
        }
    }

    private static final class FakeExposureHourRepository implements ProductExposureHourRepository {

        private final List<ProductExposureHour> rows = new ArrayList<>();

        @Override
        public List<ProductExposureHour> saveAll(List<ProductExposureHour> hours) {
            rows.addAll(hours);
            return hours;
        }

        @Override
        public List<ProductExposureHour> findAllByProductId(ProductId productId) {
            return List.copyOf(rows);
        }

        @Override
        public void deleteAllByProductId(ProductId productId) {
            rows.clear();
        }
    }

    private record StubProductRepository(Product product) implements ProductRepository {

        @Override
        public Optional<Product> findById(ProductId id) {
            return Optional.of(product);
        }

        @Override
        public Optional<Product> findByIdIncludingDeleted(ProductId id) {
            return Optional.of(product);
        }

        @Override
        public Product save(Product product) {
            return product;
        }

        @Override
        public List<Product> findAllByShopIdAndIdIn(ShopId shopId, List<ProductId> ids) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long countVisibleByShopId(ShopId shopId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long countVisibleRepresentativeByShopId(ShopId shopId) {
            throw new UnsupportedOperationException();
        }

        /**
         * 이 스텁을 쓰는 테스트는 대표 메뉴 상한(최대 6개)을 검증하지 않으므로 호출되지 않는다.
         * 조용히 0을 돌려주면 상한 판정이 항상 통과해 테스트가 잘못된 전제 위에서 성공한다.
         */
        @Override
        public long countRepresentativeByShopId(ShopId shopId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Product> findAllSoldOutExpiredBefore(java.time.LocalDateTime baseTime) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByShopIdAndName(ShopId shopId, String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByShopIdAndNameAndIdNot(ShopId shopId, String name, ProductId excludedId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Product> findAllByShopIdAndCategoryId(ShopId shopId, ProductCategoryId productCategoryId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long countByCategoryId(ProductCategoryId productCategoryId) {
            throw new UnsupportedOperationException();
        }
    }
}
