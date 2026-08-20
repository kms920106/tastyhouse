package com.tastyhouse.infrastructure.product.query;

/**
 * 점주 메뉴 이미지 관리 목록 항목 투영.
 *
 * <p>{@code imageFileId}가 아니라 표시용 URL을 담는다 — 응답 계약이 파일 식별자를 노출하지 않으므로
 * 변환을 DAO가 {@code FileUrlResolver}로 끝낸다.
 */
public record ProductImageManagementResult(
    Long id,
    String imageUrl,
    Integer sort,
    boolean visible
) {

}
