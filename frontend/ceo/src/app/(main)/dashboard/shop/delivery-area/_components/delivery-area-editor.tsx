"use client";

import * as React from "react";

import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";

import { toast } from "sonner";

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Drawer, DrawerContent, DrawerDescription, DrawerTitle, DrawerTrigger } from "@/components/ui/drawer";
import { Skeleton } from "@/components/ui/skeleton";
// 반경 적용(#7)은 쓰지 않는다. 서버에 바로 반영하면 되돌리기가 불가능해지므로, 반경 결과도
// draft 에 넣고 저장 시 다른 조작과 함께 bulk 로 커밋한다.
import {
  addDeliveryAreasAction,
  deleteDeliveryAreaPolygonAction,
  fetchDeliveryAreaPolygonAction,
  getDeliveryAreasAction,
  previewDeliveryAreaPolygonAction,
  previewDeliveryAreaRadiusAction,
  removeDeliveryAreasAction,
  saveDeliveryAreaPolygonAction,
} from "@/feature/shop/actions";
import {
  BRUSH_SIZE_OPTIONS,
  type BrushSizeOption,
  DELIVERY_AREA_DEFAULT_RADIUS_KM,
  DELIVERY_AREA_MODE_OPTIONS,
  type DeliveryAreaMode,
  KAKAO_MAP_MAX_LEVEL,
  KAKAO_MAP_MIN_LEVEL,
} from "@/feature/shop/constants";
import type {
  AdminDongBoundary,
  DeliveryAreaPolygon,
  DeliveryAreaRadiusPreview,
  GeoPoint,
  GeoRing,
  ShopDeliveryArea,
  ShopDeliveryTipRegion,
} from "@/feature/shop/domain";
import { circleRing, containsPoint } from "@/feature/shop/geo";
import { SHOP_MESSAGE, SHOP_OPERATION_COPY } from "@/feature/shop/message";
import type { DeliveryAreaRadiusFormValues } from "@/feature/shop/schema";
import { useIsMobile } from "@/hooks/use-mobile";
import { getKakaoMapAppKey } from "@/lib/env";
import type { KakaoMap, KakaoMaps } from "@/lib/kakao/types";

import { DeliveryAreaFooter } from "./delivery-area-footer";
import { DeliveryAreaQuickPanel } from "./delivery-area-quick-panel";
import type { SelectedRegion } from "./delivery-area-selection-list";
import { DeliveryAreaToolbar } from "./delivery-area-toolbar";
import { type MapViewport, useAdminDongBoundaries } from "./use-admin-dong-boundaries";
import { sameRings, useDeliveryAreaDraft } from "./use-delivery-area-draft";
import { useDeliveryAreaDraftStorage } from "./use-delivery-area-draft-storage";

// 지도는 브라우저 전용이다 — SSR 하면 window 접근에서 터진다.
const DeliveryAreaMap = dynamic(() => import("./delivery-area-map").then((module) => module.DeliveryAreaMap), {
  ssr: false,
  loading: () => <Skeleton className="h-full w-full" />,
});

/** 반경 슬라이더를 끄는 동안 서버 미리보기를 호출하는 간격 */
const RADIUS_PREVIEW_DEBOUNCE_MS = 300;

/**
 * 획을 그은 뒤 도형 환산을 요청하기까지의 간격.
 *
 * 반경보다 길게 잡는다 — 반경은 슬라이더 한 번에 값 하나지만 그리기는 획을 연달아 긋는 조작이라
 * 획마다 환산을 부르면 요청이 몰린다. 손을 멈춘 뒤에 한 번만 부른다.
 */
const POLYGON_PREVIEW_DEBOUNCE_MS = 500;

const SHOP_PATH = "/dashboard/shop";

interface DeliveryAreaEditorProps {
  shopId: number;
  shopName: string;
  shop: GeoPoint;
  deliveryAreas: ShopDeliveryArea[];
  polygon: DeliveryAreaPolygon | null;
  /** 지역별 배달팁이 걸린 지역 — 해제를 막는 판정 데이터 */
  tipRegions: ShopDeliveryTipRegion[];
}

/**
 * 배달지역 편집기 셸.
 *
 * draft 를 소유하고 지도·패널·푸터를 잇는다. 저장 시맨틱이 기존 시트와 다르다는 점이 이
 * 화면을 별도 라우트로 뽑은 이유다 — 시트는 행 단위 즉시 저장이지만 여기서는 조작을 draft 에
 * 모아 두고 "저장"에서 한 번에 커밋한다.
 */
export function DeliveryAreaEditor({
  shopId,
  shopName,
  shop,
  deliveryAreas,
  polygon,
  tipRegions,
}: DeliveryAreaEditorProps) {
  const router = useRouter();
  const [isPending, startTransition] = React.useTransition();

  /*
   * 모바일은 지도를 전체화면으로 쓰고 패널을 바텀시트로 내린다.
   *
   * `md:` 유틸리티로 두 레이아웃을 동시에 렌더하지 않고 분기하는 이유는, 바텀시트가 패널을
   * 포털(`DrawerPortal`)로 body 아래에 옮기기 때문이다. 두 벌을 함께 두면 검색·트리·선택목록이
   * 화면에 두 번 존재하게 되어 같은 행정동 체크박스가 중복 렌더되고 접근성 트리도 어긋난다.
   *
   * 훅의 임계값(768px)은 아래 `md:` 클래스와 같은 값이라 두 경로가 어긋나지 않는다.
   */
  const isMobile = useIsMobile();
  const [panelOpen, setPanelOpen] = React.useState(false);

  const hasMapKey = getKakaoMapAppKey() !== null;

  // ===== baseline =====
  // 이름은 지도 경계·검색 결과가 아니라 서버가 준 목록에서 가져온다 — 조립하지 않는다.
  const regionNames = React.useMemo(() => {
    const names = new Map<number, string>();
    for (const area of deliveryAreas) names.set(area.adminDongId, area.regionName);
    for (const region of tipRegions) names.set(region.adminDongId, region.regionName);
    return names;
  }, [deliveryAreas, tipRegions]);

  const lockedDongIds = React.useMemo(() => new Set(tipRegions.map((region) => region.adminDongId)), [tipRegions]);

  /** 지도 도형에서 환산된 지역 — 개별 제거가 아니라 도형 수정으로 다뤄야 한다 */
  const polygonDongIds = React.useMemo(
    () => new Set(deliveryAreas.filter((area) => area.source === "POLYGON").map((area) => area.adminDongId)),
    [deliveryAreas],
  );

  const baseline = React.useMemo(
    () => ({
      rings: polygon?.rings ?? [],
      // 직접 고른 지역만 draft 로 다룬다. 환산분은 도형이 바뀌면 서버가 다시 만든다.
      adminDongIds: new Set(deliveryAreas.filter((area) => area.source === "MANUAL").map((area) => area.adminDongId)),
    }),
    [deliveryAreas, polygon],
  );

  const draft = useDeliveryAreaDraft(baseline);
  const draftStorage = useDeliveryAreaDraftStorage(shopId, draft.state, draft.isDirty);

  /**
   * 이번 편집 중에 알게 된 행정동 이름.
   *
   * `regionNames`(서버 baseline)에도 없고 지도 경계에도 없는 — 즉 방금 검색·트리·반경으로 고른 —
   * 지역의 이름을 담는다. 이것이 없으면 저장 전까지 목록에 "행정동 {id}" 가 뜨는데, 지도를 못 쓰는
   * 환경에서는 검색·트리가 유일한 경로라 사용자가 무엇을 골랐는지 확인할 방법이 사라진다.
   *
   * draft(`useDeliveryAreaDraft`)에 넣지 않는 이유: 이름은 되돌리기 대상이 아니고, 히스토리를
   * 델타로 보관하는 그 구조의 액션 페이로드를 이름으로 부풀릴 이유도 없다. 한 번 알게 된 이름은
   * 선택을 해제해도 지우지 않는다 — 다시 고르면 그대로 쓰면 되고, 화면 수명 안에서만 산다.
   */
  const [learnedNames, setLearnedNames] = React.useState<Map<number, string>>(() => new Map());

  const learnNames = React.useCallback((entries: { adminDongId: number; regionName: string }[]) => {
    if (entries.length === 0) return;
    setLearnedNames((previous) => {
      // 이미 아는 이름만 들어왔으면 상태를 새로 만들지 않는다 — 불필요한 리렌더를 막는다.
      if (entries.every((entry) => previous.get(entry.adminDongId) === entry.regionName)) return previous;
      const next = new Map(previous);
      for (const entry of entries) next.set(entry.adminDongId, entry.regionName);
      return next;
    });
  }, []);

  // ===== 지도 =====
  const mapRef = React.useRef<KakaoMap | null>(null);
  const mapsRef = React.useRef<KakaoMaps | null>(null);
  const [viewport, setViewport] = React.useState<MapViewport | null>(null);
  const [mode, setMode] = React.useState<DeliveryAreaMode>(DELIVERY_AREA_MODE_OPTIONS[0]);
  const [brushSize, setBrushSize] = React.useState<BrushSizeOption>(BRUSH_SIZE_OPTIONS[1]);

  const { boundaries, truncated } = useAdminDongBoundaries(viewport);
  const boundaryList = React.useMemo(() => [...boundaries.values()], [boundaries]);

  const handleMapReady = React.useCallback((map: KakaoMap, maps: KakaoMaps) => {
    mapRef.current = map;
    mapsRef.current = maps;
  }, []);

  const zoomBy = React.useCallback((delta: number) => {
    const map = mapRef.current;
    if (!map) return;
    // 카카오는 레벨이 작을수록 확대다 — 확대 버튼이 레벨을 낮춘다.
    map.setLevel(Math.min(KAKAO_MAP_MAX_LEVEL, Math.max(KAKAO_MAP_MIN_LEVEL, map.getLevel() + delta)));
  }, []);

  // ===== 반경 미리보기 =====
  const [radiusKm, setRadiusKm] = React.useState(DELIVERY_AREA_DEFAULT_RADIUS_KM);
  const [radiusPreview, setRadiusPreview] = React.useState<DeliveryAreaRadiusPreview | null>(null);

  // 슬라이더를 끄는 동안 지도의 원은 즉시 따라오고, 서버 판정만 debounce 한다.
  const previewCircle = React.useMemo(() => circleRing(shop, radiusKm * 1000), [shop, radiusKm]);

  // 늦게 도착한 옛 응답이 최신 반경의 결과를 덮어쓰지 않도록 순번으로 거른다 —
  // 서버 액션은 AbortSignal 을 받지 못해 요청 자체를 취소할 수 없다.
  const radiusRequestSeqRef = React.useRef(0);

  React.useEffect(() => {
    const seq = ++radiusRequestSeqRef.current;

    const timer = setTimeout(() => {
      void previewDeliveryAreaRadiusAction(shopId, { radiusKm, replace: false }).then(({ success, data }) => {
        if (seq !== radiusRequestSeqRef.current) return;
        if (success && data) setRadiusPreview(data);
      });
    }, RADIUS_PREVIEW_DEBOUNCE_MS);

    return () => clearTimeout(timer);
  }, [shopId, radiusKm]);

  // ===== 그린 도형 → 행정동 환산 =====
  //
  // 도형을 행정동으로 바꾸는 판정은 서버에만 있다(경계 데이터가 서버에 있고, 저장 시 서버가
  // 같은 규칙으로 다시 환산한다). 그래서 저장 때 쓰던 환산 미리보기를 편집 중에도 부른다.
  //
  // 이것이 없으면 그리기는 **화면상 아무 반응이 없다.** 획은 `draft.state.rings` 에 정상적으로
  // 쌓이고 캔버스에도 그려지지만, "등록된 지역" 목록·푸터 `+N/-M`·행정동 채움은 모두
  // `adminDongIds` 를 기준으로 하므로 저장 전까지 그린 만큼이 하나도 반영되지 않는다.
  const [polygonProjection, setPolygonProjection] = React.useState<{
    dongs: { adminDongId: number; regionName: string }[];
    /** 이 결과가 만들어진 도형. 지금 도형과 다르면 화면에 쓰지 않는다 */
    rings: GeoRing[];
  }>({ dongs: [], rings: [] });

  // 반경과 같은 이유로 순번을 쓴다 — 서버 액션은 취소할 수 없어 늦게 온 옛 응답을 버려야 한다.
  const polygonRequestSeqRef = React.useRef(0);

  const currentRings = draft.state.rings;

  React.useEffect(() => {
    const seq = ++polygonRequestSeqRef.current;

    // 다 지웠으면 서버에 물을 것이 없다. 빈 도형은 스키마 검증에 걸리기도 한다.
    if (currentRings.length === 0) {
      setPolygonProjection({ dongs: [], rings: [] });
      return;
    }

    // 내용이 같은 도형은 다시 묻지 않는다. `rings` 는 조작 때마다 replay 로 새로 만들어져
    // 참조가 바뀌므로, 참조만 보면 트리·검색·반경 조작마다 같은 도형을 또 보내게 된다.
    if (sameRings(polygonProjection.rings, currentRings)) return;

    const timer = setTimeout(() => {
      void previewDeliveryAreaPolygonAction(shopId, { rings: currentRings })
        .then(({ success, data, message }) => {
          if (seq !== polygonRequestSeqRef.current) return;

          if (success && data) {
            setPolygonProjection({ dongs: data.adminDongs, rings: currentRings });
            return;
          }

          // 실패했으면 옛 결과를 남기지 않는다 — 남기면 검증되지 않은 도형의 환산분이
          // 아무 경고 없이 계속 표시된다. 다만 `rings` 는 시도한 도형으로 남겨 둔다.
          // 빈 배열로 되돌리면 effect 가 "아직 안 물어봤다"고 보고 곧바로 다시 요청해
          // 실패 → 재요청 → 실패가 토스트와 함께 무한히 반복된다.
          setPolygonProjection({ dongs: [], rings: currentRings });
          toast.error(message ?? SHOP_MESSAGE.DELIVERY_AREA_LOAD_FAILED);
        })
        .catch(() => {
          if (seq !== polygonRequestSeqRef.current) return;
          setPolygonProjection({ dongs: [], rings: currentRings });
          toast.error(SHOP_MESSAGE.DELIVERY_AREA_LOAD_FAILED);
        });
    }, POLYGON_PREVIEW_DEBOUNCE_MS);

    return () => clearTimeout(timer);
  }, [shopId, currentRings, polygonProjection.rings]);

  // 환산으로 알게 된 이름도 목록 표시에 쓴다 — 그린 지역이 "행정동 {id}" 로 뜨면 안 된다.
  React.useEffect(() => {
    learnNames(polygonProjection.dongs);
  }, [polygonProjection.dongs, learnNames]);

  /** 지금 그린 도형에서 환산된 행정동 */
  const polygonDraftDongIds = React.useMemo(
    () => new Set(polygonProjection.dongs.map((item) => item.adminDongId)),
    [polygonProjection.dongs],
  );

  // ===== draft 조작 =====
  const handleStroke = React.useCallback(
    (rings: GeoRing[], strokeMode: "paint" | "erase") => {
      draft.dispatch({ type: "STROKE", mode: strokeMode, rings });
    },
    [draft],
  );

  const handleToggleDong = React.useCallback(
    (dongId: number, regionName?: string) => {
      // 배달팁이 걸린 지역은 해제할 수 없다 — 서버도 409 로 막지만 먼저 알려준다.
      if (lockedDongIds.has(dongId) && draft.state.adminDongIds.has(dongId)) {
        toast.error(SHOP_OPERATION_COPY.DELIVERY_AREA_LOCKED_BY_TIP);
        return;
      }
      if (regionName) learnNames([{ adminDongId: dongId, regionName }]);
      draft.dispatch({ type: "TOGGLE_DONG", dongId });
    },
    [draft, lockedDongIds, learnNames],
  );

  const handleToggleMany = React.useCallback(
    (dongs: { adminDongId: number; regionName: string }[], selected: boolean) => {
      learnNames(dongs);
      const next = new Set(draft.state.adminDongIds);
      for (const { adminDongId } of dongs) {
        if (selected) next.add(adminDongId);
        else if (!lockedDongIds.has(adminDongId)) next.delete(adminDongId);
      }
      draft.dispatch({ type: "SET_DONGS", dongIds: [...next] });
    },
    [draft, lockedDongIds, learnNames],
  );

  /** 이동 모드에서 지도를 클릭하면 그 지점을 품은 행정동을 토글한다 */
  const handlePickPoint = React.useCallback(
    (point: GeoPoint) => {
      const hit = boundaryList.find(
        (boundary: AdminDongBoundary) => boundary.rings !== null && containsPoint(boundary.rings, point),
      );
      if (hit) handleToggleDong(hit.adminDongId);
    },
    [boundaryList, handleToggleDong],
  );

  /**
   * 검색 결과에서 "지도에서 보기" — 대표점으로 지도를 옮긴다.
   *
   * 대표점은 지도 경계 응답에만 있고 그것은 현재 뷰포트에 들어온 동만 채워진다. 즉 "지금 화면
   * 밖에 있는 동으로 옮겨 달라"는 요청은 좌표를 알 수 없어 처리할 수 없다 — 반경 미리보기가
   * 좌표를 준 동만 보충된다. 옮길 수 없으면 조용히 아무 일도 안 하는 대신 이유를 알린다.
   */
  const handleFocusDong = React.useCallback(
    (dongId: number) => {
      const map = mapRef.current;
      const maps = mapsRef.current;
      if (!map || !maps) {
        toast.error(SHOP_MESSAGE.DELIVERY_AREA_FOCUS_MAP_UNAVAILABLE);
        return;
      }

      const boundary = boundaries.get(dongId);
      const candidate = radiusPreview?.adminDongs.find((item) => item.adminDongId === dongId);
      const center = boundary ?? candidate;
      if (!center) {
        toast.error(SHOP_MESSAGE.DELIVERY_AREA_FOCUS_UNRESOLVED);
        return;
      }

      map.panTo(new maps.LatLng(center.centerLatitude, center.centerLongitude));
    },
    [boundaries, radiusPreview],
  );

  const handleApplyRadius = React.useCallback(
    (values: DeliveryAreaRadiusFormValues) => {
      // 미리보기가 아직 이번 반경의 것이 아니면(디바운스 대기 중) 적용하지 않는다 —
      // 그대로 넣으면 화면은 새 반경을 보여주는데 draft 에는 이전 반경의 동이 들어간다.
      if (!radiusPreview || radiusPreview.radiusMeters !== Math.round(values.radiusKm * 1000)) {
        toast.error(SHOP_MESSAGE.DELIVERY_AREA_RADIUS_PREVIEW_PENDING);
        return;
      }

      const dongIds = radiusPreview.adminDongs.map((item) => item.adminDongId);
      if (dongIds.length === 0) {
        toast.error(SHOP_MESSAGE.DELIVERY_AREA_EMPTY_PROJECTION);
        return;
      }

      // 미리보기 응답은 이름도 함께 주므로 목록 표시용으로 챙겨 둔다.
      learnNames(radiusPreview.adminDongs);
      draft.dispatch({ type: "APPLY_RADIUS", dongIds, replace: values.replace });
      toast.success(SHOP_MESSAGE.DELIVERY_AREA_RADIUS_APPLIED);
    },
    [draft, radiusPreview, learnNames],
  );

  // ===== 파생값 =====
  //
  // 저장 요청에 실을 증감은 **직접 고른 지역만** 다룬다. 도형 환산분은 도형을 저장하면 서버가
  // 다시 만들므로 여기에 섞으면 같은 지역을 두 번 등록하게 된다.
  const addedDongIds = React.useMemo(
    () => [...draft.state.adminDongIds].filter((id) => !baseline.adminDongIds.has(id)),
    [draft.state.adminDongIds, baseline.adminDongIds],
  );
  const removedDongIds = React.useMemo(
    () => [...baseline.adminDongIds].filter((id) => !draft.state.adminDongIds.has(id)),
    [draft.state.adminDongIds, baseline.adminDongIds],
  );

  /**
   * 도형이 baseline 과 달라졌는지.
   *
   * 참조 비교로는 부족하다 — 되돌리기로 baseline 과 같은 상태로 돌아오면 참조는 다시
   * 같아지지만, 그 사이 `commit` 으로 baseline 이 바뀌었을 수 있다. 정점 비교는 O(n) 이지만
   * 상한 5000 정점이라 렌더마다 돌아도 무시할 만하다.
   */
  const ringsChanged = !sameRings(draft.state.rings, draft.baselineRings);

  /**
   * 화면에 보이는 도형 환산분.
   *
   * 도형을 건드렸으면 환산 결과를 쓰고, 그대로면 서버가 준 baseline 을 쓴다. 섞으면 지운 지역이
   * 계속 칠해진 채로 남는다 — 서버 baseline 은 지우기 전 도형의 환산분이기 때문이다.
   *
   * 환산이 아직 지금 도형을 따라오지 못했어도(500ms 대기 중) **직전 결과를 그대로 둔다.**
   * 획을 그을 때마다 비우면 획 하나마다 "등록된 지역"이 0으로 떨어졌다가 되돌아와, 연달아 긋는
   * 동안 카운트가 계속 깜빡인다. 실제로 21곳까지 칠해 둔 상태에서 한 획을 더 긋자 2곳으로
   * 떨어지는 것으로 보였다 — 환산이 늦은 것뿐인데 그린 것이 지워진 것처럼 읽힌다.
   * 늦은 값이 잠깐 남는 쪽이, 맞는 값이 사라지는 쪽보다 오해가 적다.
   */
  const displayedPolygonDongIds = ringsChanged ? polygonDraftDongIds : polygonDongIds;

  /** 지도에 칠해 보이는 선택 = 직접 고른 지역 + 도형 환산분 */
  const displayedDongIds = React.useMemo(
    () => new Set([...draft.state.adminDongIds, ...displayedPolygonDongIds]),
    [draft.state.adminDongIds, displayedPolygonDongIds],
  );

  /**
   * 푸터에 보여줄 증감 — 저장 payload 와 달리 도형 환산분까지 포함한다.
   *
   * 점주에게 "이번 편집으로 배달지역이 몇 곳 늘고 줄었는지"는 직접 고른 것과 그린 것의 구분
   * 없이 합쳐진 결과다. 그리기가 이 숫자를 전혀 움직이지 못하면 그린 것이 반영됐는지 확인할
   * 방법이 없다.
   */
  const registeredDongIds = React.useMemo(
    () => new Set([...baseline.adminDongIds, ...polygonDongIds]),
    [baseline.adminDongIds, polygonDongIds],
  );

  const addedDisplayCount = React.useMemo(
    () => [...displayedDongIds].filter((id) => !registeredDongIds.has(id)).length,
    [displayedDongIds, registeredDongIds],
  );
  const removedDisplayCount = React.useMemo(
    () => [...registeredDongIds].filter((id) => !displayedDongIds.has(id)).length,
    [displayedDongIds, registeredDongIds],
  );

  const regions: SelectedRegion[] = React.useMemo(
    () =>
      [...displayedDongIds].map((dongId) => ({
        adminDongId: dongId,
        // 서버 baseline → 이번 편집에서 알게 된 이름 → 지도 경계 순으로 찾는다. 지도 경계는
        // 뷰포트에 들어온 동만 채워지므로 앞의 두 경로가 없으면 지도 없는 환경에서 fallback 이 뜬다.
        regionName:
          regionNames.get(dongId) ??
          learnedNames.get(dongId) ??
          boundaries.get(dongId)?.regionName ??
          `행정동 ${dongId}`,
        isLocked: lockedDongIds.has(dongId),
        fromPolygon: displayedPolygonDongIds.has(dongId) && !draft.state.adminDongIds.has(dongId),
      })),
    [
      displayedDongIds,
      regionNames,
      learnedNames,
      boundaries,
      lockedDongIds,
      displayedPolygonDongIds,
      draft.state.adminDongIds,
    ],
  );

  // ===== 저장 =====
  const [discardOpen, setDiscardOpen] = React.useState(false);
  /** 저장이 성공했음 — 트랜지션 밖에서 토스트·이동을 처리하려고 둔다 */
  const [saved, setSaved] = React.useState(false);

  const leave = React.useCallback(() => {
    router.push(`${SHOP_PATH}?shopId=${shopId}&tab=operation`);
  }, [router, shopId]);

  const handleCancel = React.useCallback(() => {
    if (draft.isDirty) setDiscardOpen(true);
    else leave();
  }, [draft.isDirty, leave]);

  /**
   * 임시 저장된 draft 를 현재 편집에 얹는다.
   *
   * 도형은 통째로 갈아끼우고(그린 그림은 부분 병합이 의미가 없다) 선택 지역은 저장분으로 맞춘다.
   */
  const handleRestoreDraft = React.useCallback(() => {
    const restored = draftStorage.restored;
    if (!restored) return;

    if (restored.rings.length > 0) {
      draft.dispatch({ type: "STROKE", mode: "paint", rings: restored.rings });
    }
    draft.dispatch({ type: "SET_DONGS", dongIds: [...restored.adminDongIds] });
    draftStorage.dismiss();
  }, [draft, draftStorage]);

  const handleSave = React.useCallback(() => {
    startTransition(async () => {
      /**
       * 저장은 여러 요청으로 나뉘므로 중간에 실패하면 일부만 반영된 상태가 된다.
       *
       * 특히 삭제는 배달팁이 걸린 동이 섞이면 409 로 통째로 거절되는데, 그 앞의 추가는 이미
       * 반영돼 있다 — 의도보다 넓은 배달지역이 남는다. 이때 baseline 을 그대로 두면 푸터의
       * `+N / -M` 이 서버와 어긋난 채로 계속 표시되고, 다시 저장하면 이미 반영된 추가를
       * 또 보낸다. 그래서 어디까지 갔든 서버 상태를 다시 읽어 baseline 을 맞추고 화면에 남긴다.
       */
      const failAndResync = async (message: string) => {
        const [areas, polygonState] = await Promise.all([
          getDeliveryAreasAction(shopId),
          fetchDeliveryAreaPolygonAction(shopId),
        ]);

        if (areas.success && areas.data) {
          draft.commit({
            rings: polygonState.data?.rings ?? [],
            adminDongIds: new Set(
              areas.data.filter((area) => area.source === "MANUAL").map((area) => area.adminDongId),
            ),
          });
        }

        toast.error(message);
        router.refresh();
      };

      // 1. 도형 변경 반영.
      if (ringsChanged) {
        if (draft.state.rings.length === 0) {
          // 전부 지웠으면 도형을 해제한다. 저장을 건너뛰면 서버에 옛 도형이 그대로 남는다.
          const deleted = await deleteDeliveryAreaPolygonAction(shopId);
          if (!deleted.success) {
            await failAndResync(deleted.message ?? SHOP_MESSAGE.DELETE_FAILED);
            return;
          }
        } else {
          // 저장 전에 환산 결과를 확인해 409 를 맞기 전에 안내한다.
          const preview = await previewDeliveryAreaPolygonAction(shopId, { rings: draft.state.rings });
          if (preview.success && preview.data && preview.data.blockedAdminDongs.length > 0) {
            const names = preview.data.blockedAdminDongs.map((item) => item.regionName).join(", ");
            toast.error(`${SHOP_MESSAGE.DELIVERY_AREA_BLOCKED_BY_TIP}: ${names}`);
            return;
          }

          const saved = await saveDeliveryAreaPolygonAction(shopId, { rings: draft.state.rings });
          if (!saved.success) {
            // 아직 아무것도 반영되지 않은 첫 단계이므로 baseline 은 그대로 유효하다.
            toast.error(saved.message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
            return;
          }
        }
      }

      // 2. 직접 고른 지역의 증감을 반영한다. 추가를 먼저 해야 중간에 0건이 되지 않는다.
      if (addedDongIds.length > 0) {
        const added = await addDeliveryAreasAction(shopId, { adminDongIds: addedDongIds });
        if (!added.success) {
          await failAndResync(added.message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
          return;
        }
      }

      if (removedDongIds.length > 0) {
        const removed = await removeDeliveryAreasAction(shopId, { adminDongIds: removedDongIds });
        if (!removed.success) {
          await failAndResync(removed.message ?? SHOP_MESSAGE.DELETE_FAILED);
          return;
        }
      }

      // 저장이 끝났으면 임시 저장분은 필요 없다.
      draftStorage.clear();

      /*
       * 성공 처리는 트랜지션 **밖**에서 한다.
       *
       * `startTransition` 안에서 `draft.commit()` 같은 상태 갱신과 `router.push()` 를 함께 부르면,
       * 커밋이 유발한 리렌더가 같은 트랜지션에 묶여 뒤따르는 네비게이션을 덮어쓴다. 실제로 저장은
       * 성공하고 `+0/-0` 으로 리셋까지 되는데 토스트도 이동도 일어나지 않았다 — 마지막 두 줄이
       * 트랜지션과 함께 버려졌기 때문이다.
       *
       * `commit` 은 이탈 경고(`beforeunload`)를 풀어야 하므로 네비게이션보다 **먼저** 끝나야 한다.
       * 그래서 커밋만 트랜지션 안에 남기고, 토스트·이동은 트랜지션이 끝난 뒤로 미룬다.
       */
      draft.commit(draft.state);
      setSaved(true);
    });
  }, [shopId, draft, draftStorage, ringsChanged, addedDongIds, removedDongIds, router.refresh]);

  /**
   * 저장 성공 뒤처리 — 토스트 + 운영정보 탭으로 이동.
   *
   * 트랜지션이 끝나고 `commit` 이 반영된 렌더에서 실행되므로 이탈 경고에 걸리지 않고,
   * 네비게이션도 트랜지션에 삼켜지지 않는다.
   */
  React.useEffect(() => {
    if (!saved) return;
    toast.success(SHOP_MESSAGE.DELIVERY_AREA_SAVE_SUCCESS);
    leave();
  }, [saved, leave]);

  // 편집 중 이탈을 막는다. 브라우저는 커스텀 문구를 무시하지만 확인 창은 띄운다.
  React.useEffect(() => {
    if (!draft.isDirty) return;

    const onBeforeUnload = (event: BeforeUnloadEvent) => {
      event.preventDefault();
      event.returnValue = "";
    };

    window.addEventListener("beforeunload", onBeforeUnload);
    return () => window.removeEventListener("beforeunload", onBeforeUnload);
  }, [draft.isDirty]);

  const willBeEmpty = displayedDongIds.size === 0;

  const mapPane = hasMapKey ? (
    <DeliveryAreaMap
      shop={shop}
      mode={mode}
      brushSize={brushSize}
      boundaries={boundaryList}
      selectedDongIds={displayedDongIds}
      lockedDongIds={lockedDongIds}
      rings={draft.state.rings}
      previewCircle={previewCircle}
      onViewportChange={setViewport}
      onStroke={handleStroke}
      onPickPoint={handlePickPoint}
      onMapReady={handleMapReady}
    />
  ) : (
    <div className="flex h-full items-center justify-center rounded-md border border-dashed p-6">
      <p className="text-center text-muted-foreground text-sm">{SHOP_OPERATION_COPY.DELIVERY_AREA_MAP_UNAVAILABLE}</p>
    </div>
  );

  const toolbar = (
    <DeliveryAreaToolbar
      mode={mode}
      onModeChange={setMode}
      brushSize={brushSize}
      onBrushSizeChange={setBrushSize}
      onUndo={draft.undo}
      onRedo={draft.redo}
      canUndo={draft.canUndo}
      canRedo={draft.canRedo}
      onZoomIn={() => zoomBy(-1)}
      onZoomOut={() => zoomBy(1)}
      disabled={!hasMapKey}
    />
  );

  const zoomHint = truncated && (
    <p className="rounded-md border bg-background/95 px-3 py-2 text-center text-muted-foreground text-xs">
      {SHOP_OPERATION_COPY.DELIVERY_AREA_ZOOM_IN_HINT}
    </p>
  );

  const quickPanel = (
    <DeliveryAreaQuickPanel
      shopId={shopId}
      selectedDongIds={draft.state.adminDongIds}
      lockedDongIds={lockedDongIds}
      regions={regions}
      radiusPreview={radiusPreview}
      onRadiusChange={setRadiusKm}
      onApplyRadius={handleApplyRadius}
      onToggleDong={handleToggleDong}
      onToggleMany={handleToggleMany}
      onFocusDong={handleFocusDong}
      isPending={isPending}
    />
  );

  const footer = (
    <DeliveryAreaFooter
      addedCount={addedDisplayCount}
      removedCount={removedDisplayCount}
      onCancel={handleCancel}
      onSave={handleSave}
      isDirty={draft.isDirty}
      isPending={isPending}
      willBeEmpty={willBeEmpty}
    />
  );

  const dialogs = (
    <>
      <AlertDialog open={discardOpen} onOpenChange={setDiscardOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>편집을 취소할까요?</AlertDialogTitle>
            <AlertDialogDescription>{SHOP_MESSAGE.DELIVERY_AREA_DISCARD_CONFIRM}</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>계속 편집</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => {
                // 버리기로 했으면 임시 저장분도 함께 지운다 — 다음 진입에서 되살아나면 안 된다.
                draftStorage.clear();
                leave();
              }}
            >
              나가기
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* 저장 전에 이탈했던 편집 내용이 남아 있으면 이어서 할지 묻는다 */}
      <AlertDialog open={draftStorage.restored !== null}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>이전 편집 내용이 있습니다</AlertDialogTitle>
            <AlertDialogDescription>{SHOP_MESSAGE.DELIVERY_AREA_DRAFT_RESTORE_PROMPT}</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={draftStorage.clear}>새로 시작</AlertDialogCancel>
            <AlertDialogAction onClick={handleRestoreDraft}>이어서 편집</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );

  /*
   * 모바일 — 지도 전체화면 + 하단 시트형 패널.
   *
   * 카드 헤더를 걷어내고 지도가 남는 높이를 전부 차지하게 한다. 데스크톱과 같은 세로 스택으로
   * 두면 지도가 작은 고정 높이 박스로 눌리고, 퀵설정·선택목록·저장 버튼을 보려면 페이지 전체를
   * 스크롤해야 해서 "지도를 보며 칠한다"는 이 화면의 전제가 무너진다.
   *
   * 툴바와 저장바는 스크롤 흐름에 두지 않고 하단에 고정한다 — 그리는 도중에 되돌리기·저장에
   * 언제든 손이 닿아야 한다.
   */
  if (isMobile) {
    return (
      /*
       * `fixed inset-0` 로 화면을 통째로 덮지 않는다 — 그러면 대시보드 헤더와 사이드바 트리거까지
       * 가려져 이 화면에서 빠져나갈 길이 저장/취소뿐이 된다. 대신 대시보드 레이아웃이 열어 둔
       * `data-content-padding="false"`(전체폭 화면용 탈출구)로 패딩만 걷어내고, 헤더 높이를 뺀
       * 나머지를 채운다. 헤더는 그대로 남아 사이드바·테마 등 기존 이동 경로가 유지된다.
       */
      <div
        data-content-padding="false"
        className="flex h-[calc(100dvh-var(--dashboard-header-height))] flex-col bg-background"
      >
        <div className="relative min-h-0 flex-1">
          {mapPane}

          {/*
            `z-20` 은 필수다. 캔버스가 `z-10` 을 갖고 있어(카카오 SDK 오버레이보다 위로
            올라가려고 — `delivery-area-map.tsx` 참조) z-index 가 없는 이 컨테이너는 DOM 상
            나중에 그려지더라도 stacking 우선순위에서 밀려 **캔버스 아래로 깔린다.** 그러면
            툴바가 지도에 덮여 보이지도 않고, 그 좌표의 hit-test 최상단이 캔버스가 되어
            클릭도 닿지 않는다 — 모바일에서 그리기·지우기 전환과 되돌리기가 통째로 막힌다.
            데스크톱 경로는 지도가 `<Card>` 안 별도 스태킹 컨텍스트에 있어 드러나지 않았다.
          */}
          <div className="pointer-events-none absolute inset-x-0 bottom-0 z-20 flex flex-col gap-2 p-2">
            {/* 힌트·툴바만 포인터를 받는다 — 컨테이너가 받으면 그 아래 지도를 칠할 수 없다 */}
            <div className="pointer-events-auto flex flex-col gap-2">
              {zoomHint}
              {toolbar}
            </div>
          </div>
        </div>

        <div className="shrink-0 border-t bg-background px-4 py-2">
          <Drawer open={panelOpen} onOpenChange={setPanelOpen}>
            <DrawerTrigger asChild>
              <Button type="button" variant="outline" className="w-full" disabled={isPending}>
                {SHOP_OPERATION_COPY.DELIVERY_AREA_PANEL_OPEN}
              </Button>
            </DrawerTrigger>
            {/*
              높이를 `max-h` 가 아니라 `h-[85dvh]` 로 **확정**한다.
              `DrawerContent` 는 `h-auto` 라서 `max-h` 만 주면 확정 높이가 없다. 그러면 아래
              패널의 `h-full`(= `height:100%`)이 기댈 기준이 없어 auto 로 풀리고, `flex-1` 도
              늘릴 여유 공간을 계산할 수 없어 패널이 콘텐츠 높이만큼 그대로 자란다. 결과적으로
              `overflow-y-auto` 가 넘칠 일이 없다고 판단해 스크롤 컨테이너가 아예 생기지 않고,
              콘텐츠는 시트 밖으로 그냥 흘러넘친다 — 시트 아래쪽의 "배달지역 조정 신청" 버튼이
              뷰포트 밖에 놓여 휠·터치 어느 쪽으로도 도달할 수 없었다.

              값은 `DrawerContent` 기본값인 `max-h-[80vh]` 에 맞춘다. 그보다 크게 적어도
              `max-height` 가 `height` 를 이겨 80vh 로 잘리므로, 적은 값과 실제 높이가 달라지기만
              한다.
            */}
            <DrawerContent className="h-[80dvh]">
              {/* 시트에는 제목이 필요하다 — 없으면 Radix 가 접근성 경고를 낸다 */}
              <DrawerTitle className="shrink-0 px-4 pt-2 text-base">
                {SHOP_OPERATION_COPY.DELIVERY_AREA_MAP_TITLE}
              </DrawerTitle>
              <DrawerDescription className="shrink-0 px-4 pb-2 text-xs">
                {SHOP_OPERATION_COPY.DELIVERY_AREA_MAP_GUIDE}
              </DrawerDescription>
              {/*
                스크롤은 패널 자신(`h-full overflow-y-auto`)이 갖는다. 여기서 한 겹 더 감싸면
                중첩 스크롤이 되어 시트 안에서 스크롤이 먹히지 않는 구간이 생긴다. 이 wrapper 는
                남는 높이만 확정해 주고(`min-h-0` 이 있어야 flex 아이템이 콘텐츠보다 작아질 수
                있다) 스크롤은 패널에 맡긴다.
              */}
              <div className="min-h-0 flex-1">{quickPanel}</div>
            </DrawerContent>
          </Drawer>
        </div>

        <div className="shrink-0">{footer}</div>

        {dialogs}
      </div>
    );
  }

  return (
    <Card className="flex h-[calc(100dvh-8rem)] flex-col overflow-hidden py-0">
      <CardHeader className="shrink-0 border-b py-4">
        <CardTitle className="text-xl leading-none">{SHOP_OPERATION_COPY.DELIVERY_AREA_MAP_TITLE}</CardTitle>
        <CardDescription>
          {shopName} · {SHOP_OPERATION_COPY.DELIVERY_AREA_MAP_GUIDE}
        </CardDescription>
      </CardHeader>

      <CardContent className="flex min-h-0 flex-1 flex-row gap-0 p-0">
        {/* 지도 flex-1 + 우측 380px 패널 */}
        <div className="relative flex min-h-64 flex-1 flex-col">
          <div className="min-h-0 flex-1">{mapPane}</div>

          <div className="pointer-events-none absolute inset-x-0 bottom-0 z-10 flex flex-col gap-2 p-2">
            <div className="pointer-events-auto flex flex-col gap-2">
              {zoomHint}
              {toolbar}
            </div>
          </div>
        </div>

        <div className="flex min-h-0 w-95 shrink-0 flex-col border-l">{quickPanel}</div>
      </CardContent>

      {footer}

      {dialogs}
    </Card>
  );
}
