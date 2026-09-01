package com.tastyhouse.webapplication.shop.port.out;

import java.math.BigDecimal;
import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopPhoneNumberResult;

/**
 * 가게 상세(손님 화면) — 세 읽기 포트와 도메인 서비스 판정을 모아 만드는 합성 결과.
 *
 * <p><b>챕터 10</b>에서 신설. 이 화면 하나가 가게 단건({@code ShopQueryPort}) · 전화번호·상표
 * 이미지({@code ShopBasicInfoQueryPort}) · 배달팁 범위({@code ShopDeliveryTipQueryPort}) · 실시간
 * 주문가능 판정({@code ShopOperatingStatusService})을 함께 필요로 하므로, 공용 읽기 계약 패키지에
 * 형제로 둘 수 없다 — 그 패키지는 포트 하나의 산출물을 담는 자리다(선례:
 * {@code webapplication.member.port.out.MemberStatsResult}).
 *
 * <p>{@code operatingStatus}·{@code unavailableReason}·{@code unavailableReasonName}은 도메인 enum에서
 * 강등한 값이다. 특히 준비중 사유는 <b>영업중이면 두 필드 모두 null</b>이라는 판정이 붙으므로, 그
 * 판정을 서비스에 남기고 결과만 담는다.
 *
 * <p>{@code phoneNumbers}는 공유 읽기 계약을 그대로 담는다 — 전화번호 항목은 표현 조립이 없어
 * Response의 {@code from}이 직접 복사할 수 있다.
 */
public record ShopDetailViewResult(
    Long id,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    Double rating,
    String roadAddress,
    String lotAddress,
    String phoneNumber,
    List<ShopPhoneNumberResult> phoneNumbers,
    String trademarkImageUrl,
    String operatingStatus,
    String unavailableReason,
    String unavailableReasonName,
    int minOrderAmount,
    int minDeliveryTip,
    int maxDeliveryTip,
    boolean scheduledOrderEnabled
) {
}
