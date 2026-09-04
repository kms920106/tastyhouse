package com.tastyhouse.webapplication.shop.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;
import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;
import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityWithCategoryResult;

/**
 * 가게 기본정보(손님 화면) — 두 읽기 포트의 여섯 조회를 모아 만드는 합성 결과.
 *
 * <p><b>챕터 10</b>에서 신설. 영업시간·휴게시간·휴무일·편의시설·사장님 한마디·편의정보를 각각
 * 조회해 한 화면 계약으로 묶으므로 공용 읽기 계약 패키지에 형제로 둘 수 없다(선례:
 * {@link ShopDetailViewResult}).
 *
 * <p><b>{@code Optional}이 비었을 때의 기본값을 서비스가 정한다.</b> 사장님 한마디와 편의정보는 없을
 * 수 있고, 그때 응답의 아홉 필드가 {@code null}이 되는 것이 계약이다 — 그 폴백을 컨트롤러로 올리면
 * 인바운드 어댑터가 두 {@code Optional}의 부재 의미를 알아야 한다. 그래서 여기서는 이미 풀린 값만
 * 담는다.
 *
 * <p>목록 네 개는 공유 읽기 계약을 그대로 담는다 — 요일 표시명 강등은 도메인 enum의 읽기 accessor
 * 호출이라 web-api의 Response {@code from}이 직접 할 수 있다.
 */
public record ShopInfoViewResult(
    List<ShopClosedDayResult> closedDays,
    List<ShopBusinessHourResult> businessHours,
    List<ShopBreakTimeResult> breakTimes,
    List<ShopAmenityWithCategoryResult> amenities,
    String ownerMessage,
    LocalDateTime ownerMessageCreatedAt,
    Boolean parkingAvailable,
    Boolean parkingPaid,
    Boolean valetAvailable,
    Boolean valetPaid,
    String directionsGuide,
    BigDecimal displayLatitude,
    BigDecimal displayLongitude
) {
}
