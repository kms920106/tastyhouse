"use client";

import * as React from "react";

import { useTheme } from "next-themes";

import {
  BRUSH_SIZE_RADIUS_PX,
  type BrushSizeOption,
  DELIVERY_AREA_MODE_OPTIONS,
  type DeliveryAreaMode,
} from "@/feature/shop/constants";
import type { AdminDongBoundary, GeoPoint, GeoRing } from "@/feature/shop/domain";
import { distanceMeters } from "@/feature/shop/geo";
import { SHOP_OPERATION_COPY } from "@/feature/shop/message";
import { loadKakaoMaps } from "@/lib/kakao/loader";
import type { KakaoMap, KakaoMaps } from "@/lib/kakao/types";
import { cn } from "@/lib/utils";

import { DeliveryAreaCanvasOverlay } from "./delivery-area-canvas-overlay";
import type { MapViewport } from "./use-admin-dong-boundaries";
import { useBrushPaint } from "./use-brush-paint";

/** 지도 초기 줌 — 반경 3km 가 화면에 들어오는 수준 */
const INITIAL_LEVEL = 6;

interface DeliveryAreaMapProps {
  shop: GeoPoint;
  mode: DeliveryAreaMode;
  brushSize: BrushSizeOption;
  boundaries: AdminDongBoundary[];
  selectedDongIds: Set<number>;
  lockedDongIds: Set<number>;
  rings: GeoRing[];
  previewCircle: GeoPoint[] | null;
  /** 뷰포트가 바뀔 때마다 알려 경계를 다시 받게 한다 */
  onViewportChange: (viewport: MapViewport) => void;
  /** 획 하나가 끝났을 때 */
  onStroke: (rings: GeoRing[], mode: "paint" | "erase") => void;
  /** 지도 위를 클릭했을 때(이동 모드) — 그 지점의 행정동을 토글한다 */
  onPickPoint: (point: GeoPoint) => void;
  /** 부모가 `panTo` 를 호출할 수 있도록 지도 인스턴스를 넘긴다 */
  onMapReady: (map: KakaoMap, maps: KakaoMaps) => void;
}

/**
 * 카카오 지도 + 편집 캔버스.
 *
 * SSR 에서는 렌더하지 않는다(`next/dynamic` `{ ssr: false }` 로 불린다). 지도 인스턴스·캔버스·
 * 좌표 변환기는 전부 `useRef` 로 들고 렌더 사이클에서 분리한다 — 이 앱은 `reactCompiler: true`
 * 라 렌더가 잦고, 그때마다 지도를 다시 만들면 편집 중 화면이 튄다.
 */
export function DeliveryAreaMap({
  shop,
  mode,
  brushSize,
  boundaries,
  selectedDongIds,
  lockedDongIds,
  rings,
  previewCircle,
  onViewportChange,
  onStroke,
  onPickPoint,
  onMapReady,
}: DeliveryAreaMapProps) {
  const containerRef = React.useRef<HTMLDivElement>(null);
  const canvasRef = React.useRef<HTMLCanvasElement>(null);
  const mapRef = React.useRef<KakaoMap | null>(null);
  const mapsRef = React.useRef<KakaoMaps | null>(null);
  const overlayRef = React.useRef<DeliveryAreaCanvasOverlay | null>(null);

  const [isReady, setIsReady] = React.useState(false);
  const [loadFailed, setLoadFailed] = React.useState(false);
  const [cursor, setCursor] = React.useState<{ x: number; y: number } | null>(null);
  /** Space 를 누르고 있는 동안은 이동 모드로 임시 전환한다 */
  const [spaceHeld, setSpaceHeld] = React.useState(false);

  const { resolvedTheme } = useTheme();

  // 콜백을 ref 에 담아 지도 초기화 effect 가 콜백 변화로 재실행되지 않게 한다.
  const onViewportChangeRef = React.useRef(onViewportChange);
  onViewportChangeRef.current = onViewportChange;
  const onMapReadyRef = React.useRef(onMapReady);
  onMapReadyRef.current = onMapReady;

  // ===== 지도 생성 =====
  React.useEffect(() => {
    let disposed = false;

    void loadKakaoMaps()
      .then((maps) => {
        const container = containerRef.current;
        const canvas = canvasRef.current;
        if (disposed || !container || !canvas) return;

        const map = new maps.Map(container, {
          center: new maps.LatLng(shop.latitude, shop.longitude),
          level: INITIAL_LEVEL,
        });

        mapRef.current = map;
        mapsRef.current = maps;
        overlayRef.current = new DeliveryAreaCanvasOverlay(canvas, map);

        const publishViewport = () => {
          const bounds = map.getBounds();
          const sw = bounds.getSouthWest();
          const ne = bounds.getNorthEast();
          onViewportChangeRef.current({
            swLat: sw.getLat(),
            swLng: sw.getLng(),
            neLat: ne.getLat(),
            neLng: ne.getLng(),
            level: map.getLevel(),
          });
        };

        // 지도가 움직이면 좌표 변환 캐시를 버리고 경계를 다시 받는다.
        const handleIdle = () => {
          overlayRef.current?.invalidateTransform();
          publishViewport();
        };
        const handleMove = () => overlayRef.current?.invalidateTransform();

        maps.event.addListener(map, "idle", handleIdle);
        maps.event.addListener(map, "zoom_changed", handleMove);
        maps.event.addListener(map, "center_changed", handleMove);

        setIsReady(true);
        publishViewport();
        onMapReadyRef.current(map, maps);
      })
      .catch(() => {
        if (!disposed) setLoadFailed(true);
      });

    return () => {
      disposed = true;
      overlayRef.current?.destroy();
      overlayRef.current = null;
      mapRef.current = null;
      mapsRef.current = null;
    };
    // 가게 좌표는 이 라우트가 사는 동안 바뀌지 않는다 — 지도를 다시 만들 이유가 없다.
  }, [shop.latitude, shop.longitude]);

  // ===== 컨테이너 크기 추적 =====
  //
  // 지도와 캔버스를 **각각** 관찰한다. 캔버스는 저작권 표기를 비우려고 지도보다 아래가 짧아
  // (`bottom-6`) 두 박스의 높이가 다르다. 캔버스 해상도를 지도 높이로 맞추면 backing store 가
  // CSS 박스보다 커져 그려지는 좌표가 세로로 눌리고, 경계선이 지도와 어긋난 채 렌더된다.
  //
  // `isReady` 를 의존성으로 둔다. 오버레이는 `loadKakaoMaps()` 의 비동기 콜백에서 만들어지므로,
  // 마운트 직후에 관찰을 시작하면 `observe()` 가 즉시 부르는 첫 콜백 시점에 `overlayRef.current`
  // 가 아직 `null` 이라 `?.` 로 삼켜진다. 그 뒤 캔버스 박스 크기는 다시 바뀌지 않아 콜백이
  // 영영 오지 않고, backing store 가 기본값(300×150)에 멈춘 채 좌표가 뒤틀린다.
  React.useEffect(() => {
    if (!isReady) return;
    const container = containerRef.current;
    const canvas = canvasRef.current;
    if (!container || !canvas) return;

    const observer = new ResizeObserver((entries) => {
      for (const entry of entries) {
        if (entry.target === canvas) {
          const { width, height } = entry.contentRect;
          overlayRef.current?.resize(width, height);
        } else {
          // 컨테이너 크기를 바꾼 뒤에는 지도에도 알려야 타일이 어긋나지 않는다.
          mapRef.current?.relayout();
        }
      }
    });

    observer.observe(container);
    observer.observe(canvas);
    return () => observer.disconnect();
  }, [isReady]);

  // ===== Space = 임시 이동 =====
  React.useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.code === "Space") setSpaceHeld(true);
    };
    const onKeyUp = (event: KeyboardEvent) => {
      if (event.code === "Space") setSpaceHeld(false);
    };

    window.addEventListener("keydown", onKeyDown);
    window.addEventListener("keyup", onKeyUp);
    return () => {
      window.removeEventListener("keydown", onKeyDown);
      window.removeEventListener("keyup", onKeyUp);
    };
  }, []);

  const effectiveMode: DeliveryAreaMode = spaceHeld ? DELIVERY_AREA_MODE_OPTIONS[0] : mode;
  const isDrawingMode = effectiveMode !== "PAN";

  // ===== 그리기 모드에서는 지도 제스처를 잠근다 =====
  React.useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    map.setDraggable(!isDrawingMode);
    map.setZoomable(!isDrawingMode);
  }, [isDrawingMode]);

  const toGeoPoint = React.useCallback((x: number, y: number): GeoPoint | null => {
    const map = mapRef.current;
    const maps = mapsRef.current;
    if (!map || !maps) return null;

    const latlng = map.getProjection().coordsFromContainerPoint(new maps.Point(x, y));
    return { latitude: latlng.getLat(), longitude: latlng.getLng() };
  }, []);

  /**
   * 픽셀 반지름이 그 지점에서 몇 미터인지 계산한다.
   *
   * 브러시는 화면 좌표계의 원이므로, 확대할수록 실제 지리 반경이 줄어 정밀 편집이 된다.
   * 지도 중심에서 좌우로 `radiusPx` 만큼 떨어진 두 점의 실제 거리로 환산한다.
   */
  const radiusMetersAt = React.useCallback(
    (point: GeoPoint, radiusPx: number): number => {
      const map = mapRef.current;
      const maps = mapsRef.current;
      if (!map || !maps) return radiusPx;

      const center = map.getProjection().containerPointFromCoords(new maps.LatLng(point.latitude, point.longitude));
      const edge = toGeoPoint(center.x + radiusPx, center.y);
      return edge ? distanceMeters(point, edge) : radiusPx;
    },
    [toGeoPoint],
  );

  const brush = useBrushPaint({
    mode: effectiveMode === "PAINT" ? "paint" : effectiveMode === "ERASE" ? "erase" : null,
    radiusPx: BRUSH_SIZE_RADIUS_PX[brushSize],
    toGeoPoint,
    radiusMetersAt,
    onStrokeEnd: onStroke,
    onCursorMove: setCursor,
  });

  // ===== 장면 렌더 =====
  // `refreshTheme()` 이 읽는 CSS 변수 값을 `resolvedTheme` 이 좌우하지만 정적 분석으로는
  // 그 연결이 보이지 않는다. 빼면 테마를 바꿔도 다음 조작 전까지 예전 색이 남는다.
  // biome-ignore lint/correctness/useExhaustiveDependencies: 테마 값은 CSS 변수를 통해 간접적으로 읽힌다
  React.useEffect(() => {
    if (!isReady) return;

    // 캔버스 색은 CSS 변수에서 읽으므로 테마가 바뀌면 다시 읽는다. 읽기만 하고 다시 그리지
    // 않으면 다음 조작 전까지 예전 색이 남으므로, 색 갱신과 렌더를 한 effect 에서 처리한다.
    overlayRef.current?.refreshTheme();
    overlayRef.current?.render({
      boundaries,
      selectedDongIds,
      lockedDongIds,
      rings,
      previewCircle,
      brush: cursor
        ? { x: cursor.x, y: cursor.y, radius: BRUSH_SIZE_RADIUS_PX[brushSize], erasing: effectiveMode === "ERASE" }
        : null,
      shop,
    });
  }, [
    isReady,
    boundaries,
    selectedDongIds,
    lockedDongIds,
    rings,
    previewCircle,
    cursor,
    brushSize,
    effectiveMode,
    shop,
    resolvedTheme,
  ]);

  // 이동 모드에서 캔버스를 클릭하면 그 지점의 행정동을 토글한다.
  const handleClick = React.useCallback(
    (event: React.MouseEvent<HTMLCanvasElement>) => {
      if (isDrawingMode) return;
      const rect = event.currentTarget.getBoundingClientRect();
      const point = toGeoPoint(event.clientX - rect.left, event.clientY - rect.top);
      if (point) onPickPoint(point);
    },
    [isDrawingMode, onPickPoint, toGeoPoint],
  );

  if (loadFailed) {
    return (
      <div className="flex h-full min-h-64 items-center justify-center rounded-md border border-dashed p-6">
        <p className="text-center text-muted-foreground text-sm">{SHOP_OPERATION_COPY.DELIVERY_AREA_MAP_UNAVAILABLE}</p>
      </div>
    );
  }

  return (
    <div className="relative h-full w-full">
      <div ref={containerRef} className="h-full w-full" />

      {/*
        캔버스는 지도의 형제로 절대배치한다. 이동 모드에서도 클릭으로 행정동을 고를 수 있어야
        하므로 pointer-events 를 끄지 않고, 대신 그리기 모드에서만 touch-action 을 잠근다 —
        항상 잠그면 모바일에서 지도 자체를 움직일 수 없다.

        z-10 은 필수다. 카카오 SDK 가 지도 컨테이너 안에 만드는 오버레이 레이어는 computed
        `z-index: 1` 을 갖는데, 캔버스에 z-index 가 없으면(`auto`) DOM 상 나중에 그려지더라도
        stacking 우선순위에서 밀려 **항상 그 아래로 깔린다.** 그러면 hit-test 최상단이 카카오의
        `<svg>` 가 되어 pointerdown 이 캔버스에 닿지 않고, 그리기·지우기·클릭 토글이 전부
        조용히 먹통이 된다(툴바는 계속 멀쩡해 보여서 원인을 찾기 어렵다).

        바닥 여백(bottom-6)은 카카오 저작권 표기와 "카카오맵으로 이동" 링크가 지도 좌·우 하단에
        있기 때문이다. 캔버스가 그 위를 덮으면 링크를 클릭할 수 없게 되는데, 저작권 표기는
        지도 API 이용 조건이라 가려서는 안 된다. 이 띠만 비워 두면 링크는 계속 클릭 가능하고
        편집 영역은 사실상 그대로다.
      */}
      {/*
        지도는 키보드로 칠할 수 없다. 그래서 캔버스를 조작 가능한 위젯으로 노출하지 않고
        그림으로만 알린다 — 완전한 대체 경로는 같은 화면의 검색·트리가 담당한다.
      */}
      <canvas
        ref={canvasRef}
        aria-label={SHOP_OPERATION_COPY.DELIVERY_AREA_MAP_TITLE}
        className={cn(
          // `h-[calc(100%-1.5rem)]` 로 높이를 **명시**한다. `<canvas>` 는 CSS 상 대체 요소라
          // `height: auto` 인 채로 `top`+`bottom` 이 동시에 걸리면 높이가 오프셋 차이가 아니라
          // intrinsic 종횡비(기본 300×150 = 2:1)로 계산된다. 그래서 `bottom-6` 이 무시되고
          // 너비 570px → 높이 285px 로 눌려, 지도 아래쪽 70% 에서는 그리기가 아예 먹지 않았다.
          // `bottom-6` 과 같은 값(1.5rem)을 빼 저작권 표기 띠는 그대로 비워 둔다.
          "absolute inset-x-0 top-0 z-10 h-[calc(100%-1.5rem)] w-full",
          isDrawingMode ? "cursor-none touch-none" : "cursor-pointer",
          // 그리기 중에는 지도가 잠겨 있으므로 캔버스가 포인터를 독점해도 안전하다.
          brush.isDrawing && "select-none",
        )}
        onPointerDown={brush.onPointerDown}
        onPointerMove={brush.onPointerMove}
        onPointerUp={brush.onPointerUp}
        onPointerLeave={brush.onPointerLeave}
        onPointerCancel={brush.onPointerCancel}
        onClick={handleClick}
      />
    </div>
  );
}
