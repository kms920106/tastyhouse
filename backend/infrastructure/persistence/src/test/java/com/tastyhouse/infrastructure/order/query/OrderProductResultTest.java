package com.tastyhouse.infrastructure.order.query;

import java.util.List;

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
    @DisplayName("투영된 저장 경로가 표시용 URL로 변환돼 담긴다 — 경로가 그대로 남으면 안 된다")
    void resolvesStoredPathIntoDisplayUrl() {
        OrderProductResult projected = projectedWithImage(STORED_PATH);

        OrderProductResult reassembled = projected.withResolvedImageUrl(
            fileUrlResolver.resolve(projected.imageUrl()),
            List.of()
        );

        assertThat(reassembled.imageUrl())
            .isNotEqualTo(STORED_PATH)
            .startsWith(BASE_URL)
            .endsWith("?alt=media");
    }

    @Test
    @DisplayName("대표 이미지가 없어 경로가 null이면 imageUrl도 null이고, 주문 라인 자체는 유지된다")
    void keepsLineWithNullImageUrlWhenProductHasNoImage() {
        OrderProductResult projected = projectedWithImage(null);

        OrderProductResult reassembled = projected.withResolvedImageUrl(
            fileUrlResolver.resolve(projected.imageUrl()),
            List.of()
        );

        assertThat(reassembled.imageUrl()).isNull();
        assertThat(reassembled.orderProductId()).isEqualTo(1L);
        assertThat(reassembled.name()).isEqualTo("상품");
    }

    @Test
    @DisplayName("재조립 시 이미지 외 스냅샷 필드와 옵션이 함께 보존된다")
    void preservesSnapshotFieldsAndAttachesOptions() {
        OrderProductResult projected = projectedWithImage(STORED_PATH);
        List<OrderProductOptionResult> options = List.of(
            new OrderProductOptionResult(1L, 10L, "맵기", "아주 맵게", 500, "NORMAL", null, 0)
        );

        OrderProductResult reassembled = projected.withResolvedImageUrl(
            fileUrlResolver.resolve(projected.imageUrl()),
            options
        );

        assertThat(reassembled.options()).isEqualTo(options);
        assertThat(reassembled.name()).isEqualTo("상품");
        assertThat(reassembled.priceName()).isEqualTo("곱빼기");
        assertThat(reassembled.quantity()).isEqualTo(2);
        assertThat(reassembled.originalPrice()).isEqualTo(9000);
        assertThat(reassembled.totalPrice()).isEqualTo(18000);
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
