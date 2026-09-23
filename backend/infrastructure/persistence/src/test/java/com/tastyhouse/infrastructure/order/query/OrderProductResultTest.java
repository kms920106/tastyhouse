package com.tastyhouse.infrastructure.order.query;

import java.util.List;

import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.order.port.out.OrderProductOptionResult;
import com.tastyhouse.application.order.port.out.OrderProductResult;
import com.tastyhouse.domain.file.port.FileStoragePort;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProductResultTest {
    private static final String STORED_PATH = "2026/04/19/a3f511fd-a444-49a1-b5d9-3ed0c40bd965.png";
    private static final String BASE_URL = "https://firebasestorage.example/v0/b/bucket/o";

    private final FileUrlResolver fileUrlResolver = new FileUrlResolver(new FakeFileStoragePort());

    @Test
    @DisplayName("urlOf로 감싼 저장 경로 슬롯은 표시용 URL로 채워진다 — 경로가 그대로 남으면 안 된다")
    void resolvesStoredPathIntoDisplayUrl() {
        Object imageUrl = urlSlotOf(STORED_PATH);

        assertThat(imageUrl)
            .asString()
            .isNotEqualTo(STORED_PATH)
            .startsWith(BASE_URL)
            .endsWith("?alt=media");
    }

    @Test
    @DisplayName("대표 이미지가 없어 경로가 null이면 URL 슬롯도 null이다")
    void yieldsNullUrlWhenProductHasNoImage() {
        assertThat(urlSlotOf(null)).isNull();
    }

    @Test
    @DisplayName("옵션을 붙일 때 이미지 URL과 스냅샷 필드가 함께 보존된다")
    void preservesSnapshotFieldsAndAttachesOptions() {
        String displayUrl = BASE_URL + "/image.png?alt=media";
        OrderProductResult projected = projectedWithImage(displayUrl);
        List<OrderProductOptionResult> options = List.of(
            new OrderProductOptionResult(1L, 10L, "맵기", "아주 맵게", 500, "NORMAL", null, 0)
        );

        OrderProductResult withOptions = projected.withOptions(options);

        assertThat(withOptions.options()).isEqualTo(options);
        assertThat(withOptions.imageUrl()).isEqualTo(displayUrl);
        assertThat(withOptions.orderProductId()).isEqualTo(1L);
        assertThat(withOptions.name()).isEqualTo("상품");
        assertThat(withOptions.priceName()).isEqualTo("곱빼기");
        assertThat(withOptions.quantity()).isEqualTo(2);
        assertThat(withOptions.originalPrice()).isEqualTo(9000);
        assertThat(withOptions.totalPrice()).isEqualTo(18000);
    }

    private Object urlSlotOf(String storedPath) {
        StringPath filePath = Expressions.stringPath("filePath");
        FactoryExpression<?> urlSlot = (FactoryExpression<?>) fileUrlResolver.urlOf(filePath);
        return urlSlot.newInstance(storedPath);
    }

    private OrderProductResult projectedWithImage(String imagePath) {
        return new OrderProductResult(1L, 3L, "상품", "곱빼기", imagePath, 2, 9000, null, 0, 18000);
    }

    private static final class FakeFileStoragePort implements FileStoragePort {
        @Override
        public String store(byte[] content, String storedFilename, String datePath, String contentType) {
            throw new UnsupportedOperationException("조회 변환만 검증한다");
        }

        @Override
        public String getFileUrl(String filePath) {
            return BASE_URL + "/" + filePath.replace("/", "%2F") + "?alt=media";
        }

        @Override
        public void delete(String filePath) {
            throw new UnsupportedOperationException("조회 변환만 검증한다");
        }
    }
}
