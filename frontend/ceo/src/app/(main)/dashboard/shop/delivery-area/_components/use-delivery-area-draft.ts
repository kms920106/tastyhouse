"use client";

import * as React from "react";

import { DELIVERY_AREA_HISTORY_LIMIT } from "@/feature/shop/constants";
import type { GeoRing } from "@/feature/shop/domain";
import { differenceRings, unionRings } from "@/feature/shop/geo";

/**
 * 배달지역 편집 draft 상태.
 *
 * `zustand` 가 설치돼 있지만 쓰지 않는다 — 라우트를 떠나면 draft 는 사라져야 정상이고,
 * 전역 스토어에 두면 다른 가게로 옮겼을 때 이전 편집 내용이 새어 나온다.
 *
 * 히스토리는 스냅샷이 아니라 **델타**로 보관한다. 정점 5000개짜리 도형을 50단계 복사하면
 * 메모리가 수십 MB 로 불어난다. 대신 되돌리기는 초기 상태부터 액션을 다시 적용해 만든다.
 */

export interface DeliveryAreaDraftState {
  /** 지도에 그린 도형 */
  rings: GeoRing[];
  /** 선택된 행정동 ID 집합 — 트리·검색·반경으로 직접 고른 것 */
  adminDongIds: Set<number>;
}

/**
 * draft 를 바꾸는 모든 조작.
 *
 * `STROKE` 는 pointerdown~pointerup 한 번이 1건이다. 동 단위로 쪼개면 되돌리기가
 * 쓸모없어진다 — 한 번 그은 획을 여러 번 눌러 지워야 하기 때문이다.
 */
export type DeliveryAreaDraftAction =
  | { type: "STROKE"; mode: "paint" | "erase"; rings: GeoRing[] }
  | { type: "APPLY_RADIUS"; dongIds: number[]; replace: boolean }
  | { type: "TOGGLE_DONG"; dongId: number }
  | { type: "SET_DONGS"; dongIds: number[] }
  | { type: "CLEAR_POLYGON" };

interface HistoryState {
  /** 되돌리기의 바닥 — baseline 또는 마지막 저장 시점 */
  base: DeliveryAreaDraftState;
  /** base 위에 순서대로 적용된 조작들 */
  applied: DeliveryAreaDraftAction[];
  /** 되돌린 조작들. 새 조작이 들어오면 비운다 */
  undone: DeliveryAreaDraftAction[];
}

function applyAction(state: DeliveryAreaDraftState, action: DeliveryAreaDraftAction): DeliveryAreaDraftState {
  switch (action.type) {
    case "STROKE": {
      const rings =
        action.mode === "paint" ? unionRings(state.rings, action.rings) : differenceRings(state.rings, action.rings);
      return { ...state, rings };
    }
    case "APPLY_RADIUS": {
      // 덮어쓰기는 직접 고른 동만 교체한다. 도형은 그대로 두고 별도로 관리된다.
      const next = action.replace ? new Set(action.dongIds) : new Set([...state.adminDongIds, ...action.dongIds]);
      return { ...state, adminDongIds: next };
    }
    case "TOGGLE_DONG": {
      const next = new Set(state.adminDongIds);
      if (next.has(action.dongId)) next.delete(action.dongId);
      else next.add(action.dongId);
      return { ...state, adminDongIds: next };
    }
    case "SET_DONGS":
      return { ...state, adminDongIds: new Set(action.dongIds) };
    case "CLEAR_POLYGON":
      return { ...state, rings: [] };
  }
}

/** base 부터 액션을 다시 적용해 현재 상태를 만든다 */
function replay(history: HistoryState): DeliveryAreaDraftState {
  return history.applied.reduce(applyAction, history.base);
}

type HistoryAction =
  | DeliveryAreaDraftAction
  | { type: "UNDO" }
  | { type: "REDO" }
  | { type: "RESET" }
  | { type: "COMMIT"; state: DeliveryAreaDraftState };

function historyReducer(history: HistoryState, action: HistoryAction): HistoryState {
  // 저장이 끝났으면 저장된 상태가 새 바닥이 된다 — 이후 되돌리기는 여기까지만 간다.
  if (action.type === "COMMIT") {
    return { base: action.state, applied: [], undone: [] };
  }

  if (action.type === "UNDO") {
    if (history.applied.length === 0) return history;
    const applied = history.applied.slice(0, -1);
    const last = history.applied[history.applied.length - 1];
    return { ...history, applied, undone: [...history.undone, last] };
  }

  if (action.type === "REDO") {
    if (history.undone.length === 0) return history;
    const restored = history.undone[history.undone.length - 1];
    return {
      ...history,
      applied: [...history.applied, restored],
      undone: history.undone.slice(0, -1),
    };
  }

  if (action.type === "RESET") {
    return { ...history, applied: [], undone: [] };
  }

  /*
   * 아무것도 바꾸지 못한 조작은 히스토리에 넣지 않는다.
   *
   * 빈 곳을 지우거나 이미 칠한 곳을 덧칠하면 도형이 그대로인 채로 히스토리만 쌓인다.
   * 그러면 되돌리기 버튼이 켜지고, 되돌릴 것이 없는데도 "저장하지 않은 변경 내용이
   * 사라집니다" 이탈 경고가 뜬다 — 실제 변경이 없다는 것과 어긋난다.
   *
   * 판정은 획(`STROKE`)에만 한다. 나머지 조작은 집합 연산이라 결과가 뻔하고, 여기서 걸러 봐야
   * `replay` 한 번을 더 돌리는 비용만 든다. 획은 pointerup 당 한 번뿐이라 그 비용을 감당할 만하다.
   *
   * 여기서 걸리는 것은 "이미 칠한 곳을 덧칠했다"처럼 **결과가 같은** 획뿐이다. 이동이 없는
   * 제스처(누르고 그 자리에서 뗌)는 브러시 원이 실제로 찍혀 도형이 진짜로 달라지므로 이 비교로는
   * 걸러지지 않는다 — 그쪽은 획을 만들기 전에 `use-brush-paint.ts` 가 막는다.
   */
  if (action.type === "STROKE") {
    const before = replay(history);
    const after = applyAction(before, action);
    if (sameRings(before.rings, after.rings)) return history;
  }

  // 새 조작이 들어오면 되돌렸던 이력은 더 이상 이어붙일 수 없다.
  const applied = [...history.applied, action];
  if (applied.length <= DELIVERY_AREA_HISTORY_LIMIT) {
    return { ...history, applied, undone: [] };
  }

  // 스택이 넘치면 가장 오래된 조작을 base 에 흡수시킨다 — 그만큼 되돌리기 범위가 줄어든다.
  const [oldest, ...rest] = applied;
  return { base: applyAction(history.base, oldest), applied: rest, undone: [] };
}

export interface DeliveryAreaDraft {
  state: DeliveryAreaDraftState;
  dispatch: (action: DeliveryAreaDraftAction) => void;
  undo: () => void;
  redo: () => void;
  /** baseline 으로 되돌린다 */
  reset: () => void;
  /** 저장 성공 후 현재 상태를 새 baseline 으로 삼는다 */
  commit: (next: DeliveryAreaDraftState) => void;
  canUndo: boolean;
  canRedo: boolean;
  /** baseline 과 달라졌는지 — 이탈 확인·저장 버튼 활성화 판정에 쓴다 */
  isDirty: boolean;
  /** 현재 baseline 의 도형. 저장 시 "도형이 바뀌었는지" 판정에 쓴다 */
  baselineRings: GeoRing[];
}

function sameDongs(a: Set<number>, b: Set<number>): boolean {
  if (a.size !== b.size) return false;
  for (const id of a) if (!b.has(id)) return false;
  return true;
}

/** 두 도형이 같은지 — 정점까지 비교한다. 저장 판정처럼 드물게 도는 곳에서만 쓴다 */
export function sameRings(a: GeoRing[], b: GeoRing[]): boolean {
  if (a.length !== b.length) return false;
  return a.every((ring, index) => {
    const other = b[index];
    if (ring.length !== other.length) return false;
    return ring.every(
      (point, pointIndex) =>
        point.latitude === other[pointIndex].latitude && point.longitude === other[pointIndex].longitude,
    );
  });
}

export function useDeliveryAreaDraft(baseline: DeliveryAreaDraftState): DeliveryAreaDraft {
  const [history, dispatchHistory] = React.useReducer(historyReducer, {
    base: baseline,
    applied: [],
    undone: [],
  } satisfies HistoryState);

  // 히스토리가 델타라서 현재 상태는 매번 재생해 얻는다. 조작이 있을 때만 다시 계산한다.
  const state = React.useMemo(() => replay(history), [history]);

  const dispatch = React.useCallback((action: DeliveryAreaDraftAction) => dispatchHistory(action), []);
  const undo = React.useCallback(() => dispatchHistory({ type: "UNDO" }), []);
  const redo = React.useCallback(() => dispatchHistory({ type: "REDO" }), []);
  const reset = React.useCallback(() => dispatchHistory({ type: "RESET" }), []);
  const commit = React.useCallback(
    (next: DeliveryAreaDraftState) => dispatchHistory({ type: "COMMIT", state: next }),
    [],
  );

  const isDirty =
    !sameRings(state.rings, history.base.rings) || !sameDongs(state.adminDongIds, history.base.adminDongIds);

  return {
    state,
    dispatch,
    undo,
    redo,
    reset,
    commit,
    canUndo: history.applied.length > 0,
    canRedo: history.undone.length > 0,
    isDirty,
    baselineRings: history.base.rings,
  };
}
