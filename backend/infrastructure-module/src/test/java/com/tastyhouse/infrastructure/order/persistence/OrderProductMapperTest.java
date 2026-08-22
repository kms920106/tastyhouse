package com.tastyhouse.infrastructure.order.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link OrderProductMapper}의 nullable FK({@code imageFileId}) round-trip을 검증한다.
 *
 * <p>{@code ORDER_PRODUCT.image_file_id}는 대표 이미지가 없는 상품에서 null이 된다. {@code IdMapping}을
 * 거치지 않고 {@code UploadedFileId.of(entity.getImageFileId())}처럼 직접 호출했다면 그 행을 읽는 순간
 * {@code IllegalArgumentException}이 났을 것이다 — 이미지가 항상 있는 샘플 데이터로는 드러나지 않는
 * 결함 유형이라 테스트로 고정한다.
 *
 * <p>이 컬럼은 원래 경로 문자열({@code image_url})이었고, 컬럼명과 저장값이 어긋난 탓에 조회 경로가
 * {@code FileUrlResolver} 변환을 건너뛰는 장애가 있었다. 파일 ID 참조로 전환해 그 혼동을 구조적으로
 * 제거했으므로, 매핑이 ID 왕복을 정확히 보존하는지도 함께 확인한다.
 */
class OrderProductMapperTest {

    @Test
    @DisplayName("대표 이미지가 없어 imageFileId가 null인 엔티티를 도메인으로 재구성해도 예외가 나지 않는다")
    void toDomainDoesNotThrowWhenImageFileIdIsNull() {
        OrderProductJpaEntity entity = OrderProductJpaEntity.create(
            1L,
            2L,
            "이미지 없는 상품",
            null, // imageFileId: 대표 이미지 미등록
            1,
            10000,
            null,
            0,
            10000,
            0 // cupDepositAmount: 보증금 옵션 없음
        );

        OrderProduct domain = OrderProductMapper.toDomain(entity);

        assertThat(domain.getImageFileId()).isNull();
    }

    @Test
    @DisplayName("imageFileId가 null인 도메인 모델을 엔티티로 변환해도 예외 없이 null이 유지된다")
    void toEntityDoesNotThrowWhenImageFileIdIsNull() {
        OrderProduct domain = OrderProduct.of(
            OrderId.of(1L),
            ProductId.of(2L),
            "이미지 없는 상품",
            null, // imageFileId: 대표 이미지 미등록
            1,
            10000,
            null,
            0,
            10000,
            0 // cupDepositAmount: 보증금 옵션 없음
        );

        OrderProductJpaEntity entity = OrderProductMapper.toEntity(domain);

        assertThat(entity.getImageFileId()).isNull();
    }

    @Test
    @DisplayName("imageFileId가 있으면 도메인↔엔티티 왕복에서 값이 보존된다")
    void imageFileIdSurvivesRoundTrip() {
        OrderProduct domain = OrderProduct.of(
            OrderId.of(1L),
            ProductId.of(2L),
            "이미지 있는 상품",
            UploadedFileId.of(105L),
            1,
            10000,
            null,
            0,
            10000,
            0 // cupDepositAmount: 보증금 옵션 없음
        );

        OrderProductJpaEntity entity = OrderProductMapper.toEntity(domain);
        assertThat(entity.getImageFileId()).isEqualTo(105L);

        OrderProduct restored = OrderProductMapper.toDomain(entity);
        assertThat(restored.getImageFileId()).isEqualTo(UploadedFileId.of(105L));
    }
}
