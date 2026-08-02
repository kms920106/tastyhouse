package com.tastyhouse.infrastructure.event.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.event.domain.model.EventStatus;

/**
 * 이벤트 관리 상세 조회 결과(admin).
 *
 * <p>표현 목적 read의 산출물이므로 domain(write 포트)이 아니라 이 query 패키지가 소유한다. 이관 이전에는
 * 도메인 모델을 로드해 {@code from(Event)}로 조립했으나, 화면 조립용 read는 도메인을 거치지 않고 JPA
 * 엔티티에서 직접 투영하도록 바꿨다(그래서 식별자도 {@code EventId} VO가 아니라 원시 {@code Long}이다).
 *
 * <p>썸네일·배너 이미지는 각각 별도 alias로 {@code uploadedFileJpaEntity}를 left join해 파일명·URL까지
 * 함께 투영한다(추가 조회 없이 3-arg {@code FileResponse}를 조립할 수 있도록). left join이므로 파일이
 * 없거나 존재하지 않으면 이름·URL이 {@code null}일 수 있다.
 *
 * <p>web 상세용 형제({@link EventDetailResult})와 같은 패키지에 공존하므로 {@code Management} 한정어를
 * 유지한다.
 */
public record EventManagementDetailResult(
    Long id,
    String name,
    String description,
    String subtitle,
    Long thumbnailImageFileId,
    String thumbnailFileName,
    String thumbnailUrl,
    Long bannerImageFileId,
    String bannerFileName,
    String bannerUrl,
    String contentHtml,
    EventStatus status,
    LocalDateTime startAt,
    LocalDateTime endAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    @QueryProjection
    public EventManagementDetailResult {
    }
}
