package com.tastyhouse.infrastructure.order.query;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.port.FileStoragePort;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 주문 상품 조회 결과의 <b>재조립 단계</b>를 검증한다 — {@code OrderQueryDao#findOrderProducts}가 fetch
 * 직후 수행하는 "저장 경로 → 표시용 URL 변환 + 옵션 덧붙이기"를 DB 없이 재현한다.
 *
 * <p>이 단계가 회귀 지점이다. 과거 {@code ORDER_PRODUCT.image_url}은 컬럼명과 달리 저장 경로를 담고
 * 있었는데 조회 경로가 이를 URL로 간주해 {@code FileUrlResolver}를 건너뛴 탓에, 응답이 호스트 없는
 * 경로(`2026/04/19/....png`)를 그대로 내려보내 프론트엔드 {@code next/image}가 크래시했다. 변환을
 * 되돌리면 이 테스트가 실패한다.
 *
 * <p>DAO 전체는 DB가 필요해(이 리포지토리에 DAO 테스트 선례가 없다) 여기서는 변환 계약만 고정한다 —
 * QueryDSL 투영·join 자체는 e2e에서 확인한다.
 */
class OrderProductResultTest {

    private static final String STORED_PATH = "2026/04/19/a3f511fd-a444-49a1-b5d9-3ed0c40bd965.png";
    private static final String BASE_URL = "https://firebasestorage.example/v0/b/bucket/o";

    /** 경로를 URL로 바꾸는 실제 규칙을 모사한다(Firebase: 경로 인코딩 + {@code ?alt=media}). */
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
            new OrderProductOptionResult(1L, 10L, "맵기", "아주 맵게", 500)
        );

        OrderProductResult reassembled = projected.withResolvedImageUrl(
            fileUrlResolver.resolve(projected.imageUrl()),
            options
        );

        assertThat(reassembled.options()).isEqualTo(options);
        assertThat(reassembled.name()).isEqualTo("상품");
        assertThat(reassembled.quantity()).isEqualTo(2);
        assertThat(reassembled.originalPrice()).isEqualTo(9000);
        assertThat(reassembled.totalPrice()).isEqualTo(18000);
    }

    private OrderProductResult projectedWithImage(String imagePath) {
        return new OrderProductResult(1L, 3L, "상품", imagePath, 2, 9000, null, 0, 18000);
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
