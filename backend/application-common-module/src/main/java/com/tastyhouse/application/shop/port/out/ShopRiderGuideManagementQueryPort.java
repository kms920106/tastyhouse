package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 라이더 안내 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>전체 가게의 라이더 안내를 검색하는 관리 목록과 변경 이력을 조회한다. 점주 화면 조회는
 * {@link ShopRiderGuideQueryPort}가 소유한다.
 *
 * <p>{@link #findRiderGuide}는 두 포트가 함께 쓰는 <b>공유 메서드</b>라 양쪽에 선언만 중복한다.
 */
public interface ShopRiderGuideManagementQueryPort {

    /** 공유 메서드 — {@link ShopRiderGuideQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<ShopRiderGuideResult> findRiderGuide(Long shopId);

    PageResult<ShopRiderGuideListItemResult> findRiderGuidePage(String shopName, Boolean hasVisitGuide, PageQuery pageQuery);

    List<ShopRiderGuideHistoryResult> findHistories(Long shopId);
}
