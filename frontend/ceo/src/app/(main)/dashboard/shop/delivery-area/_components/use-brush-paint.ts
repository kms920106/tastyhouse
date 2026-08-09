"use client";

import * as React from "react";

import type { GeoPoint, GeoRing } from "@/feature/shop/domain";
import { distanceMeters, interpolate, strokeToRings } from "@/feature/shop/geo";

/**
 * 브러시 그리기 — Pointer Events 전담 훅.
 *
 * 마우스·터치·펜을 한 갈래로 처리하려고 Pointer Events 만 쓴다. 지도 제스처와의 충돌은
 * 두 가지 탈출구로 푼다.
 * - 손가락이 2개 이상이면 진행 중인 획을 버리고 지도에 제스처를 양보한다(모바일에서
 *   "한 손가락=칠하기, 두 손가락=이동/확대").
 * - 데스크톱은 Space 를 누르는 동안 임시로 이동 모드가 된다(호출부가 처리).
 */

export interface BrushPaintOptions {
  /** 그리기·지우기 중 무엇인지. null 이면 브러시가 꺼진 상태(이동 모드) */
  mode: "paint" | "erase" | null;
  /** 브러시 반지름(px) — 화면 좌표계 기준이라 확대할수록 실제 지리 반경이 작아진다 */
  radiusPx: number;
  /** 컨테이너 픽셀 좌표를 지도 좌표로 바꾼다 */
  toGeoPoint: (x: number, y: number) => GeoPoint | null;
  /** 픽셀 반지름이 그 지점에서 몇 미터인지 */
  radiusMetersAt: (point: GeoPoint, radiusPx: number) => number;
  /** 한 획이 끝났을 때 도형을 넘긴다. 획 하나가 되돌리기 1단위다 */
  onStrokeEnd: (rings: GeoRing[], mode: "paint" | "erase") => void;
  /** 브러시 원을 그리기 위한 커서 위치 통지 */
  onCursorMove: (cursor: { x: number; y: number } | null) => void;
}

export interface BrushPaintHandlers {
  onPointerDown: (event: React.PointerEvent<HTMLCanvasElement>) => void;
  onPointerMove: (event: React.PointerEvent<HTMLCanvasElement>) => void;
  onPointerUp: (event: React.PointerEvent<HTMLCanvasElement>) => void;
  onPointerLeave: (event: React.PointerEvent<HTMLCanvasElement>) => void;
  onPointerCancel: (event: React.PointerEvent<HTMLCanvasElement>) => void;
  /** 획을 긋는 중인지 — 호출부가 지도 드래그를 잠글 때 쓴다 */
  isDrawing: boolean;
}

export function useBrushPaint(options: BrushPaintOptions): BrushPaintHandlers {
  const [isDrawing, setIsDrawing] = React.useState(false);

  // 렌더 사이클과 분리한다 — 포인터가 움직일 때마다 리렌더하면 브러시가 끊긴다.
  const samplesRef = React.useRef<GeoPoint[]>([]);
  const lastPointRef = React.useRef<GeoPoint | null>(null);
  const activePointersRef = React.useRef<Set<number>>(new Set());

  // 최신 옵션을 ref 로 들고 있어야 핸들러가 매번 새로 만들어지지 않는다.
  const optionsRef = React.useRef(options);
  optionsRef.current = options;

  const cancelStroke = React.useCallback((event: React.PointerEvent<HTMLCanvasElement>) => {
    samplesRef.current = [];
    lastPointRef.current = null;
    setIsDrawing(false);
    if (event.currentTarget.hasPointerCapture(event.pointerId)) {
      event.currentTarget.releasePointerCapture(event.pointerId);
    }
  }, []);

  const finishStroke = React.useCallback(() => {
    const { mode, radiusPx, radiusMetersAt, onStrokeEnd } = optionsRef.current;
    const samples = samplesRef.current;

    // 상태를 먼저 비운다. pointerup 뒤에 pointerleave 가 이어 들어와도 여기서 걸러져
    // 같은 획이 두 번 커밋되지 않는다 — 획 하나가 되돌리기 1단위라는 규칙이 깨지면 안 된다.
    samplesRef.current = [];
    lastPointRef.current = null;
    setIsDrawing(false);

    if (!mode) return;

    /*
     * 움직이지 않은 제스처(누르고 그 자리에서 뗌)는 획으로 보지 않는다.
     *
     * `pointerdown` 이 샘플 1개를 넣으므로 `samples.length === 1` 은 "이동이 한 번도 간격을
     * 넘지 못했다"는 뜻이다. 그대로 커밋하면 브러시 원 하나가 도형에 찍히는데, 그 원은 어떤
     * 행정동도 품지 못할 만큼 작아 **화면에는 아무 변화가 없으면서** 도형만 달라진다. 그러면
     * 되돌리기·저장 버튼이 켜지고 이탈 경고까지 뜬다 — 사용자가 보기엔 아무것도 안 했는데
     * "저장하지 않은 변경 내용"이 생긴 상태다.
     *
     * 이 판정을 도형 비교(`sameRings`)로는 대신할 수 없다. 원이 실제로 찍히긴 하므로 도형은
     * 진짜로 달라지고, 비교는 정직하게 "변경됨"을 돌려준다. 걸러야 하는 것은 결과가 아니라
     * 이동 없는 제스처 자체이므로 여기서 막는다. 지역 선택은 이동 모드의 클릭 토글이 담당한다.
     */
    if (samples.length <= 1) return;

    const rings = strokeToRings(samples, (point) => radiusMetersAt(point, radiusPx));
    if (rings.length > 0) onStrokeEnd(rings, mode);
  }, []);

  const onPointerDown = React.useCallback(
    (event: React.PointerEvent<HTMLCanvasElement>) => {
      const { mode, toGeoPoint } = optionsRef.current;
      activePointersRef.current.add(event.pointerId);

      // 두 손가락은 항상 지도에 양보한다.
      if (activePointersRef.current.size > 1) {
        cancelStroke(event);
        return;
      }

      if (!mode) return;

      const rect = event.currentTarget.getBoundingClientRect();
      const point = toGeoPoint(event.clientX - rect.left, event.clientY - rect.top);
      if (!point) return;

      /*
       * 포인터 캡처는 "있으면 좋은 것"이지 획의 전제 조건이 아니다.
       *
       * 캔버스 밖으로 끌어도 이벤트를 계속 받으려고 잡지만, 실패할 수 있다 — 활성 포인터가 아닌
       * 합성 이벤트(자동화 검증)나 이미 놓인 포인터에서 `NotFoundError` 가 난다. 여기서 예외가
       * 그대로 올라가면 아래 샘플 초기화가 통째로 건너뛰어져 **획이 시작조차 되지 않는다.**
       * 캡처 없이도 캔버스 안에서의 그리기는 정상 동작하므로 실패는 삼키고 진행한다.
       */
      try {
        event.currentTarget.setPointerCapture(event.pointerId);
      } catch {
        // 캡처 없이 진행한다 — pointerleave 가 획을 마무리한다.
      }

      samplesRef.current = [point];
      lastPointRef.current = point;
      setIsDrawing(true);
    },
    [cancelStroke],
  );

  const onPointerMove = React.useCallback(
    (event: React.PointerEvent<HTMLCanvasElement>) => {
      const { mode, radiusPx, toGeoPoint, radiusMetersAt, onCursorMove } = optionsRef.current;
      const rect = event.currentTarget.getBoundingClientRect();
      const x = event.clientX - rect.left;
      const y = event.clientY - rect.top;

      onCursorMove(mode ? { x, y } : null);

      if (!mode || !lastPointRef.current) return;

      // 도중에 손가락이 하나 더 닿으면 획을 버리고 지도 제스처로 넘긴다.
      if (activePointersRef.current.size > 1) {
        cancelStroke(event);
        return;
      }

      // 브라우저가 합쳐 버린 중간 이벤트까지 살려내 빠른 드래그의 손실을 막는다.
      const coalesced =
        typeof event.nativeEvent.getCoalescedEvents === "function"
          ? event.nativeEvent.getCoalescedEvents()
          : [event.nativeEvent];

      for (const raw of coalesced) {
        const point = toGeoPoint(raw.clientX - rect.left, raw.clientY - rect.top);
        const previous = lastPointRef.current;
        if (!point || !previous) continue;

        /*
         * 점 사이를 브러시 반경의 절반 간격으로 채워야 빠르게 끌 때 구멍이 남지 않는다.
         *
         * 간격에 못 미치는 이동은 샘플로 만들지 않되 **기준점은 그대로 둔다.** 기준점까지 옮기면
         * 이동량이 매 이벤트마다 초기화되어, 잘게 쪼개져 들어오는 이동이 영영 간격을 넘지 못한다.
         * 유지하면 이동이 누적되어 간격을 넘는 순간 샘플이 된다.
         */
        const stepMeters = Math.max(radiusMetersAt(point, radiusPx) / 2, 1);
        if (distanceMeters(previous, point) < stepMeters) continue;

        samplesRef.current.push(...interpolate(previous, point, stepMeters));
        lastPointRef.current = point;
      }
    },
    [cancelStroke],
  );

  const onPointerUp = React.useCallback(
    (event: React.PointerEvent<HTMLCanvasElement>) => {
      activePointersRef.current.delete(event.pointerId);
      if (event.currentTarget.hasPointerCapture(event.pointerId)) {
        event.currentTarget.releasePointerCapture(event.pointerId);
      }
      finishStroke();
    },
    [finishStroke],
  );

  const onPointerLeave = React.useCallback(
    (event: React.PointerEvent<HTMLCanvasElement>) => {
      activePointersRef.current.delete(event.pointerId);
      if (event.currentTarget.hasPointerCapture(event.pointerId)) {
        event.currentTarget.releasePointerCapture(event.pointerId);
      }
      optionsRef.current.onCursorMove(null);
      // 캔버스를 벗어나며 끝난 획도 유효한 획으로 커밋한다.
      if (lastPointRef.current) finishStroke();
    },
    [finishStroke],
  );

  /**
   * OS 가 제스처를 가로챌 때(알림, 전화, 시스템 스와이프) 들어온다.
   *
   * 이걸 처리하지 않으면 취소된 포인터 ID 가 `activePointersRef` 에 영영 남는다. 그러면
   * 이후 모든 획이 "손가락 2개"로 오판돼 조용히 취소되고, 화면을 다시 마운트하기 전까지
   * 그리기가 통째로 먹통이 된다.
   */
  const onPointerCancel = React.useCallback(
    (event: React.PointerEvent<HTMLCanvasElement>) => {
      activePointersRef.current.delete(event.pointerId);
      optionsRef.current.onCursorMove(null);
      cancelStroke(event);
    },
    [cancelStroke],
  );

  return { onPointerDown, onPointerMove, onPointerUp, onPointerLeave, onPointerCancel, isDrawing };
}
