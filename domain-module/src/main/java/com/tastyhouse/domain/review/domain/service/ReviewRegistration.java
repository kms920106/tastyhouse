package com.tastyhouse.domain.review.domain.service;

import java.util.List;

import com.tastyhouse.domain.review.domain.model.Review;

/**
 * 리뷰 등록·수정 결과(도메인 서비스 반환값).
 *
 * <p>리뷰 본문 애그리거트와 함께 저장된 첨부 이미지 파일 식별자·태그명을 묶어 돌려준다. 이미지·태그는
 * 각각 별도 애그리거트({@code ReviewImage}/{@code ReviewTag})라 {@link Review} 안에서 조회할 수 없고,
 * 등록 직후 응답이 이 둘을 함께 필요로 하므로 저장 시점에 확정된 값을 그대로 실어 보낸다.
 *
 * <p>표현 계층 DTO가 아니라 도메인 서비스의 반환 계약이므로 domain 계층에 둔다 — 소비 모듈의
 * {@code ReviewCommandService}가 이를 Response로 조립한다.
 */
public record ReviewRegistration(
    Review review,
    List<Long> uploadedFileIds,
    List<String> tags
) {
}
