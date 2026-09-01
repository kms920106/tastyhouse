package com.tastyhouse.application.bug.port.out;

/**
 * 버그 제보 첨부 이미지 한 건의 조회 결과.
 *
 * <p>{@code BUG_REPORT_IMAGE}는 파일 자체를 담지 않고 {@code uploaded_file}을 FK로 참조하므로,
 * DAO가 두 테이블을 join해 파일 ID·원본 파일명·저장 경로를 함께 투영한 뒤(생성자 투영 시점에는 아직
 * 경로), fetch 직후 {@code FileUrlResolver}로 {@code imageUrl}을 표시용 URL로 재조립한다. 소비 측
 * (admin-api {@code BugReportQueryService})은 이 record를 그대로 {@code FileResponse.of(fileId, fileName,
 * imageUrl)}로 매핑하며, 파일을 추가 조회하지 않는다.
 */
public record BugReportImageResult(
    Long fileId,
    String fileName,
    String imageUrl
) {
}
