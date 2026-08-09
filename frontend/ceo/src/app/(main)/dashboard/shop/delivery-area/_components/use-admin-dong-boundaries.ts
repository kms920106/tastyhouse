"use client";

import * as React from "react";

import { fetchAdminDongBoundariesAction } from "@/feature/shop/actions";
import { BOUNDARY_FETCH_DEBOUNCE_MS, BOUNDARY_MIN_ZOOM_LEVEL } from "@/feature/shop/constants";
import type { AdminDongBoundary } from "@/feature/shop/domain";

/** 지도 뷰포트 — 카카오 `LatLngBounds` 를 순수 값으로 옮긴 것 */
export interface MapViewport {
  swLat: number;
  swLng: number;
  neLat: number;
  neLng: number;
  /** ROADMAP 기준 1~14. 값이 클수록 축소 */
  level: number;
}

export interface AdminDongBoundaryCache {
  /** 지금까지 받아 둔 경계 — 지도를 되돌아와도 다시 받지 않는다 */
  boundaries: Map<number, AdminDongBoundary>;
  /** 현재 뷰포트가 너무 넓어 경계를 싣지 않은 상태인지 */
  truncated: boolean;
  isLoading: boolean;
  errorMessage: string | null;
}

/**
 * 뷰포트 기반 행정동 경계 로드·캐시.
 *
 * 실제 트래픽 부담은 저장(도형 업로드)이 아니라 이 방향이다. 경계는 한 동에 수백~수천 정점이라
 * 지도를 움직일 때마다 전부 받으면 수 MB 가 오간다. 그래서
 * 1) 광역 줌에서는 아예 요청하지 않고,
 * 2) 이동이 멈춘 뒤에만(debounce) 요청하며,
 * 3) 한 번 받은 동은 캐시에 남겨 재요청하지 않고,
 * 4) 앞선 요청은 `AbortController` 로 버린다.
 */
export function useAdminDongBoundaries(viewport: MapViewport | null): AdminDongBoundaryCache {
  const [boundaries, setBoundaries] = React.useState<Map<number, AdminDongBoundary>>(() => new Map());
  const [truncated, setTruncated] = React.useState(false);
  const [isLoading, setIsLoading] = React.useState(false);
  const [errorMessage, setErrorMessage] = React.useState<string | null>(null);

  // 서버 액션은 AbortSignal 을 받지 않으므로, 도착한 응답을 버리는 방식으로 취소를 흉내낸다.
  const requestSeqRef = React.useRef(0);

  React.useEffect(() => {
    if (!viewport) return;

    // 광역 줌에서는 경계를 싣지 않는다 — "확대하면 편집할 수 있습니다" 안내로 대체한다.
    if (viewport.level > BOUNDARY_MIN_ZOOM_LEVEL) {
      setTruncated(true);
      setIsLoading(false);
      return;
    }

    const seq = ++requestSeqRef.current;
    setIsLoading(true);

    const timer = setTimeout(() => {
      void fetchAdminDongBoundariesAction({
        swLat: viewport.swLat,
        swLng: viewport.swLng,
        neLat: viewport.neLat,
        neLng: viewport.neLng,
        level: viewport.level,
      }).then(({ success, message, data }) => {
        // 그 사이 지도가 또 움직였으면 이 응답은 낡은 것이다.
        if (seq !== requestSeqRef.current) return;

        setIsLoading(false);

        if (!success || !data) {
          setErrorMessage(message ?? null);
          return;
        }

        setErrorMessage(null);
        setTruncated(data.truncated);
        if (data.items.length === 0) return;

        setBoundaries((previous) => {
          const next = new Map(previous);
          for (const item of data.items) next.set(item.adminDongId, item);
          return next;
        });
      });
    }, BOUNDARY_FETCH_DEBOUNCE_MS);

    return () => clearTimeout(timer);
  }, [viewport]);

  return { boundaries, truncated, isLoading, errorMessage };
}
