package com.tastyhouse.domain.product.repository;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.vo.ProductId;

/**
 * 상품 이미지 write 포트.
 *
 * <p>화면 갤러리용 이미지 목록 조회는 {@code ProductQueryDao#findProductImageUrls}가 담당한다.
 * {@link #findRepresentativeImageFileId}는 주문 생성 시 주문 라인에 <b>스냅샷으로 기록</b>하는
 * 값(주문 당시 상품 이미지)이라 command 경로에서 소비되므로 이 포트에 남는다 — 표현 목적 조회가 아니다.
 */
public interface ProductImageRepository {

    /**
     * 상품의 대표 이미지(노출 중 최소 sort) 파일 ID. 없으면 null.
     *
     * <p>경로 문자열이 아니라 {@code UPLOADED_FILE.id}를 반환한다 — 주문 라인이 이 값을 스냅샷해
     * 두면 이후 상품 대표 이미지가 교체돼도 과거 주문은 주문 당시 파일을 계속 가리킨다. 표시용 URL
     * 변환은 조회 시점에 {@code FileUrlResolver}가 담당한다.
     */
    UploadedFileId findRepresentativeImageFileId(ProductId productId);

    ProductImage save(ProductImage productImage);
}
