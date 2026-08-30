package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

/**
 * 가게 기본 정보 조회 포트(CQRS query 측 아웃바운드 포트) — 여러 앱이 함께 쓴다.
 *
 * <p>전부 {@code (Long shopId) → 가게 속성} 형태이고, 반환 Result가 원시타입과 도메인 enum만 담는다
 * (예: {@code ShopClosedDayResult(Long id, ClosedDayType closedDayType)}). <b>점주가 설정하고 회원이 보고
 * 관리자가 검수하는 "가게라는 도메인 개념" 자체</b>이지 특정 앱의 표현 계약이 아니다.
 *
 * <p>그래서 소비자별로 쪼개지 않고 하나의 계약으로 둔다. 쪼개면 같은 조회 하나를 고칠 때 두세 파일을
 * 고쳐야 하고(규칙 3), 어느 한 앱에 몰아주면 나머지 앱이 그 앱의 모듈을 의존하게 된다. 소유자를
 * 앱이 아니게 만들면 두 문제가 함께 사라진다 — 이 포트는 <b>챕터 05에서 domain-module로 이동</b>한다.
 *
 * <p>구성은 3앱 공통 5개({@code findShopImageUrls}·{@code findOrderMethods}·{@code findBusinessHours}·
 * {@code findBreakTimes}·{@code findClosedDays})와, 착수 시 재실측해 같은 성격으로 판정한 2앱 공유
 * 8개다. 앱 전용 표현 투영({@code *ManagementDetailResult} 등)은 여기 두지 않고 각 앱 포트가 소유한다.
 */
public interface ShopBasicInfoQueryPort {

    Optional<ShopImageUrlsResult> findShopImageUrls(Long shopId);

    List<ShopOrderMethodResult> findOrderMethods(Long shopId);

    List<ShopBusinessHourResult> findBusinessHours(Long shopId);

    List<ShopBreakTimeResult> findBreakTimes(Long shopId);

    List<ShopClosedDayResult> findClosedDays(Long shopId);

    List<ShopPhoneNumberResult> findPhoneNumbers(Long shopId);

    Optional<ShopConvenienceInfoResult> findConvenienceInfo(Long shopId);

    Optional<ShopOriginInfoResult> findOriginInfo(Long shopId);

    Optional<ShopOwnerMessageResult> findLatestOwnerMessage(Long shopId);

    List<ShopHygieneBadgeResult> findHygieneBadges(Long shopId);

    List<ShopAmenityAssignmentResult> findAmenityAssignments(Long shopId);

    List<ShopBannerImageResult> findBannerImages(Long shopId);

    List<ShopPhotoCategoryResult> findPhotoCategories(Long shopId);
}
