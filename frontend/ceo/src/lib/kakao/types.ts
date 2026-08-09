/**
 * 카카오맵 JavaScript API v3 전역 타입 — 이 앱에서 **한 벌만** 선언한다.
 *
 * 컴포넌트마다 `window.kakao` 를 재선언하면 같은 SDK를 두 가지 타입으로 보게 되어,
 * 한쪽만 고친 시그니처가 조용히 어긋난다. 지도 관련 타입이 필요하면 반드시 여기서 import 한다.
 *
 * 아래 시그니처는 공식 문서(apis.map.kakao.com/web/documentation/)에서 확인한 것이다.
 * - `setDraggable(boolean)` / `setZoomable(boolean)` / `relayout()` — 존재 확인
 * - `getProjection()` 이 주는 객체의 클래스명은 문서상 `MapProjection` 이며,
 *   메서드는 `containerPointFromCoords(LatLng): Point` / `coordsFromContainerPoint(Point): LatLng`
 * - `AbstractOverlay` 는 `Child.prototype = new kakao.maps.AbstractOverlay()` 로 상속하고
 *   `onAdd` / `draw` / `onRemove` 세 가지를 구현해야 한다
 * - 줌 레벨 범위는 지도 타입에 따라 다르다 — ROADMAP 은 1~14, SKYVIEW/HYBRID 는 0~14.
 *   이 앱은 기본 ROADMAP 만 쓰므로 1~14 를 전제한다
 * - 이벤트 `idle` / `zoom_changed` / `center_changed` 존재 확인
 */

export interface KakaoLatLng {
  getLat(): number;
  getLng(): number;
}

export interface KakaoPoint {
  x: number;
  y: number;
}

export interface KakaoLatLngBounds {
  getSouthWest(): KakaoLatLng;
  getNorthEast(): KakaoLatLng;
}

/** `map.getProjection()` 이 반환하는 좌표 변환기 */
export interface KakaoMapProjection {
  /** 지도 좌표 → 컨테이너 픽셀 좌표 */
  containerPointFromCoords(latlng: KakaoLatLng): KakaoPoint;
  /** 컨테이너 픽셀 좌표 → 지도 좌표 */
  coordsFromContainerPoint(point: KakaoPoint): KakaoLatLng;
}

export interface KakaoMap {
  getCenter(): KakaoLatLng;
  setCenter(latlng: KakaoLatLng): void;
  panTo(latlng: KakaoLatLng): void;
  /** ROADMAP 기준 1~14. 값이 작을수록 확대 */
  getLevel(): number;
  setLevel(level: number): void;
  getBounds(): KakaoLatLngBounds;
  getProjection(): KakaoMapProjection;
  /** 드래그로 지도를 옮길 수 있는지 — 그리기 모드에서 false 로 둔다 */
  setDraggable(draggable: boolean): void;
  /** 휠·핀치로 확대·축소할 수 있는지 — 그리기 모드에서 false 로 둔다 */
  setZoomable(zoomable: boolean): void;
  /** 지도 컨테이너의 크기를 바꾼 뒤에는 반드시 호출해야 한다 */
  relayout(): void;
}

/** 지도에 얹는 커스텀 오버레이의 기반 클래스 */
export interface KakaoAbstractOverlay {
  /** 지도에 붙일 때 호출된다 */
  onAdd(): void;
  /** 중심·줌·지도타입이 바뀔 때마다 호출된다 */
  draw(): void;
  /** 지도에서 뗄 때 호출된다 */
  onRemove(): void;
  /** null 을 주면 지도에서 뗀다. 반환값은 없다 — Promise 가 아니다 */
  setMap(map: KakaoMap | null): void;
  getPanels(): KakaoMapPanels;
}

/** 오버레이가 DOM 을 꽂을 수 있는 지도 내부 레이어들 */
export interface KakaoMapPanels {
  overlayLayer: HTMLElement;
}

export interface KakaoMapOptions {
  center: KakaoLatLng;
  level?: number;
  draggable?: boolean;
}

export type KakaoEventTarget = KakaoMap;

/** 이 앱이 실제로 쓰는 `kakao.maps` 네임스페이스의 부분집합 */
export interface KakaoMaps {
  LatLng: new (latitude: number, longitude: number) => KakaoLatLng;
  Point: new (x: number, y: number) => KakaoPoint;
  LatLngBounds: new (sw?: KakaoLatLng, ne?: KakaoLatLng) => KakaoLatLngBounds;
  Map: new (container: HTMLElement, options: KakaoMapOptions) => KakaoMap;
  AbstractOverlay: new () => KakaoAbstractOverlay;
  event: {
    addListener(target: KakaoEventTarget, type: string, handler: () => void): void;
    removeListener(target: KakaoEventTarget, type: string, handler: () => void): void;
  };
  /** `autoload=false` 로 로드했을 때 네임스페이스를 초기화한다 */
  load(callback: () => void): void;
}

declare global {
  interface Window {
    kakao?: { maps?: KakaoMaps };
  }
}
