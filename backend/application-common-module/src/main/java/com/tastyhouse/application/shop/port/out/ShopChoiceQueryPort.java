package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 가게 큐레이션 조회 포트(CQRS query 측 아웃바운드 포트) — 회원 화면용.
 *
 * <p>에디터가 고른 가게 목록과 역 목록을 회원 화면에 노출한다. 큐레이션을 편집하는 관리 화면 조회는
 * {@link ShopChoiceManagementQueryPort}가 소유한다.
 *
 * <p>{@link #findEditorChoices}·{@link #findAllStations}는 두 포트가 함께 쓰는 <b>공유 메서드</b>라
 * 양쪽에 선언만 중복한다 — 관리자는 회원에게 보이는 것과 같은 목록을 편집 화면에서 확인한다.
 */
public interface ShopChoiceQueryPort {

    /** 공유 메서드 — {@link ShopChoiceManagementQueryPort}에도 같은 시그니처로 선언돼 있다. */
    PageResult<EditorChoiceResult> findEditorChoices(PageQuery pageQuery);

    /** 공유 메서드 — {@link ShopChoiceManagementQueryPort}에도 같은 시그니처로 선언돼 있다. */
    List<StationResult> findAllStations();
}
