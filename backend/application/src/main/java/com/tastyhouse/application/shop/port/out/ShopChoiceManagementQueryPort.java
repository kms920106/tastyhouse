package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 가게 큐레이션 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>에디터 추천 편집에 필요한 상세와 태그 목록을 조회한다. 회원 노출 조회는
 * {@code ShopChoiceQueryPort}가 소유한다.
 *
 * <p>{@link #findEditorChoices}·{@link #findAllStations}는 두 포트가 함께 쓰는 <b>공유 메서드</b>라
 * 양쪽에 선언만 중복한다.
 */
public interface ShopChoiceManagementQueryPort {

    /** 공유 메서드 — {@code ShopChoiceQueryPort}에도 같은 시그니처로 선언돼 있다. */
    PageResult<EditorChoiceResult> findEditorChoices(PageQuery pageQuery);

    /** 공유 메서드 — {@code ShopChoiceQueryPort}에도 같은 시그니처로 선언돼 있다. */
    List<StationResult> findAllStations();

    Optional<ShopChoiceDetailResult> findShopChoiceById(Long id);

    List<TagResult> findAllTags();
}
