package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;

/**
 * 요청처리 현황 상세 — 공통 축과 유형 전용 서브 결과를 함께 담는다.
 *
 * <p><b>챕터 09</b>에서 신설. 상세 조립은 유형별로 <b>다른 읽기 포트를 한 번 더 조회</b>하고
 * (이미지 변경·조정 신청·리뷰 게시중단), 원본 애그리거트의 상태 enum을 통합 상태
 * ({@link ShopRequestStatus})로 옮기는 <b>switch 매핑</b>을 거친다. 둘 다 application의 일이므로
 * 표현 계약이 대신할 수 없어, 해소된 결과를 이 record에 담아 넘긴다.
 *
 * <p>{@code contractAmending}·{@code attachmentLabel}도 여기서 채운다 —
 * {@code ShopRequestType#isContractAmending}·{@code #getAttachmentLabel}은 api 모듈에 허용된 읽기
 * accessor 3종이 아니라 도메인 로직이므로({@code apiModuleShouldOnlyReadDomainEnums}) 호출이 application에
 * 남아야 한다. 반면 {@code requestType}·{@code status} 자체는 그대로 넘겨 표현 계약이
 * {@code name()}·{@code getDescription()}으로 강등한다(챕터 07의 정상 경로).
 *
 * <p>유형 전용 서브 결과 셋 중 <b>최대 하나만</b> 채워지고 나머지는 {@code null}이다. 매장 가격 인증은
 * 전용 서브 결과가 없어 셋이 모두 {@code null}이다.
 */
public record ShopRequestDetailViewResult(
    Long requestId,
    ShopRequestType requestType,
    String summary,
    ShopRequestStatus status,
    String rejectReason,
    boolean contractAmending,
    boolean hasAttachment,
    long commentCount,
    LocalDateTime requestedAt,
    LocalDateTime processedAt,
    String attachmentLabel,
    String attachmentUrl,
    ShopRequestImageChangeDetailResult imageChange,
    ShopRequestAdjustmentDetailResult deliveryAreaAdjustment,
    ShopRequestReviewBlindDetailResult reviewBlind
) {
}
