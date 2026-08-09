import type { AdminDongBoundary, GeoPoint, GeoRing } from "@/feature/shop/domain";
import type { KakaoMap, KakaoMaps } from "@/lib/kakao/types";

/**
 * 배달지역 편집 캔버스 렌더러 — **비 React 순수 TS**.
 *
 * `kakao.maps.Polygon` 을 뷰포트마다 수백 개 만들고 부수면 브러시를 끄는 동안 프레임이 무너진다.
 * 대신 지도 위에 캔버스 한 장을 얹고 매 프레임 직접 그린다.
 *
 * `AbstractOverlay` 를 상속하지 않고 지도 컨테이너의 형제로 캔버스를 절대배치한다. 그리기
 * 모드에서는 지도 드래그가 꺼져 있어 좌표 동기화가 어긋날 창이 없고, 상속 방식이 요구하는
 * `onAdd`/`draw`/`onRemove` 수명주기를 React 수명주기와 이중으로 관리하지 않아도 된다.
 *
 * 색은 하드코딩하지 않고 CSS 변수를 읽어 쓴다. 다크모드 전환 시 `refreshTheme()` 을 부르면
 * 새 색으로 다시 그린다.
 */

/**
 * 한 화면 안에서 위경도 → 픽셀 변환은 선형으로 근사한다.
 *
 * 원점의 픽셀 좌표까지 이 객체에 담는다 — 별도 필드로 두면 `invalidateTransform()` 이
 * 한쪽만 비워 둘 수 있고, 그러면 다음 편집자가 낡은 원점으로 그리는 경로를 쉽게 만든다.
 */
interface ViewTransform {
  originLat: number;
  originLng: number;
  /** 원점(남서단)의 컨테이너 픽셀 좌표 */
  originX: number;
  originY: number;
  /** 위도 1도당 픽셀(y는 아래로 증가하므로 음수) */
  pixelsPerLat: number;
  /** 경도 1도당 픽셀 */
  pixelsPerLng: number;
}

export interface CanvasScene {
  /** 아직 선택되지 않은 행정동 경계 */
  boundaries: AdminDongBoundary[];
  /** 선택된 행정동 ID */
  selectedDongIds: Set<number>;
  /** 지역별 배달팁이 걸려 해제할 수 없는 행정동 ID */
  lockedDongIds: Set<number>;
  /** draft 도형 */
  rings: GeoRing[];
  /** 반경 미리보기 원. 없으면 null */
  previewCircle: GeoPoint[] | null;
  /** 커서 위치의 브러시 원(px). 그리기·지우기 모드에서만 */
  brush: { x: number; y: number; radius: number; erasing: boolean } | null;
  /** 가게 위치 */
  shop: GeoPoint;
}

interface ThemeColors {
  boundary: string;
  selectedFill: string;
  selectedStroke: string;
  lockedStroke: string;
  previewFill: string;
  previewStroke: string;
  brush: string;
  eraseBrush: string;
  shop: string;
}

/** CSS 변수를 읽어 테마 색을 만든다. 값이 비면 눈에 띄는 기본값으로 떨어진다 */
function readTheme(element: HTMLElement): ThemeColors {
  const styles = getComputedStyle(element);
  const read = (name: string, fallback: string): string => styles.getPropertyValue(name).trim() || fallback;

  const primary = read("--primary", "oklch(0.55 0.2 265)");
  const destructive = read("--destructive", "oklch(0.6 0.2 25)");
  const muted = read("--muted-foreground", "oklch(0.55 0.02 265)");

  return {
    boundary: muted,
    selectedFill: primary,
    selectedStroke: primary,
    lockedStroke: destructive,
    previewFill: primary,
    previewStroke: primary,
    brush: primary,
    eraseBrush: destructive,
    shop: destructive,
  };
}

export class DeliveryAreaCanvasOverlay {
  private readonly canvas: HTMLCanvasElement;
  private readonly context: CanvasRenderingContext2D;
  private readonly map: KakaoMap;
  private transform: ViewTransform | null = null;
  private theme: ThemeColors;
  private scene: CanvasScene | null = null;
  private frameHandle: number | null = null;

  constructor(canvas: HTMLCanvasElement, map: KakaoMap) {
    const context = canvas.getContext("2d");
    if (!context) throw new Error("캔버스 컨텍스트를 만들 수 없습니다.");

    this.canvas = canvas;
    this.context = context;
    this.map = map;
    this.theme = readTheme(canvas);
  }

  /** 컨테이너 크기가 바뀌었을 때 캔버스 해상도를 맞춘다 */
  resize(width: number, height: number): void {
    const ratio = window.devicePixelRatio || 1;
    this.canvas.width = Math.round(width * ratio);
    this.canvas.height = Math.round(height * ratio);
    this.canvas.style.width = `${width}px`;
    this.canvas.style.height = `${height}px`;
    this.context.setTransform(ratio, 0, 0, ratio, 0, 0);
    this.invalidateTransform();
  }

  /** 지도가 움직였으므로 좌표 변환 캐시를 버린다 */
  invalidateTransform(): void {
    this.transform = null;
  }

  /** 테마가 바뀌었을 때 색을 다시 읽는다 */
  refreshTheme(): void {
    this.theme = readTheme(this.canvas);
  }

  /**
   * 위경도 → 픽셀 변환 파라미터를 2점으로 역산해 캐시한다.
   *
   * 카카오 `containerPointFromCoords` 를 정점마다 부르면 수만 번 호출이 된다. 한 화면 안에서는
   * 메르카토르 왜곡이 픽셀 오차 이하라 선형 근사로 충분하므로, 두 점만 SDK 로 변환해
   * 기울기를 구하고 나머지는 곱셈·덧셈으로 처리한다.
   */
  private ensureTransform(maps: KakaoMaps): ViewTransform | null {
    if (this.transform) return this.transform;

    const bounds = this.map.getBounds();
    const sw = bounds.getSouthWest();
    const ne = bounds.getNorthEast();

    const swPoint = this.map.getProjection().containerPointFromCoords(new maps.LatLng(sw.getLat(), sw.getLng()));
    const nePoint = this.map.getProjection().containerPointFromCoords(new maps.LatLng(ne.getLat(), ne.getLng()));

    const latSpan = ne.getLat() - sw.getLat();
    const lngSpan = ne.getLng() - sw.getLng();
    if (latSpan === 0 || lngSpan === 0) return null;

    this.transform = {
      originLat: sw.getLat(),
      originLng: sw.getLng(),
      originX: swPoint.x,
      originY: swPoint.y,
      pixelsPerLat: (nePoint.y - swPoint.y) / latSpan,
      pixelsPerLng: (nePoint.x - swPoint.x) / lngSpan,
    };

    return this.transform;
  }

  private toPixel(point: GeoPoint, transform: ViewTransform): { x: number; y: number } {
    return {
      x: transform.originX + (point.longitude - transform.originLng) * transform.pixelsPerLng,
      y: transform.originY + (point.latitude - transform.originLat) * transform.pixelsPerLat,
    };
  }

  /** 다음 프레임에 한 번만 그린다 — 브러시 중 중복 렌더를 막는다 */
  render(scene: CanvasScene): void {
    this.scene = scene;
    if (this.frameHandle !== null) return;

    this.frameHandle = window.requestAnimationFrame(() => {
      this.frameHandle = null;
      this.draw();
    });
  }

  private draw(): void {
    const scene = this.scene;
    const maps = window.kakao?.maps;
    if (!scene || !maps) return;

    const transform = this.ensureTransform(maps);
    const { context } = this;
    const width = this.canvas.width / (window.devicePixelRatio || 1);
    const height = this.canvas.height / (window.devicePixelRatio || 1);

    context.clearRect(0, 0, width, height);
    if (!transform) return;

    const tracePath = (points: GeoPoint[]) => {
      context.beginPath();
      points.forEach((point, index) => {
        const { x, y } = this.toPixel(point, transform);
        if (index === 0) context.moveTo(x, y);
        else context.lineTo(x, y);
      });
      context.closePath();
    };

    // 1. 미선택 행정동 경계선
    context.lineWidth = 1;
    context.strokeStyle = this.theme.boundary;
    context.globalAlpha = 0.35;
    for (const boundary of scene.boundaries) {
      if (!boundary.rings || scene.selectedDongIds.has(boundary.adminDongId)) continue;
      for (const ring of boundary.rings) {
        tracePath(ring);
        context.stroke();
      }
    }

    // 2. 선택된 행정동 채움 — 색만이 아니라 굵은 외곽선까지 함께 준다(색맹 대비)
    for (const boundary of scene.boundaries) {
      if (!boundary.rings || !scene.selectedDongIds.has(boundary.adminDongId)) continue;
      const locked = scene.lockedDongIds.has(boundary.adminDongId);

      for (const ring of boundary.rings) {
        tracePath(ring);
        context.globalAlpha = 0.25;
        context.fillStyle = this.theme.selectedFill;
        context.fill();

        context.globalAlpha = 0.9;
        context.lineWidth = locked ? 3 : 2;
        // 3. 잠금(배달팁)은 파선으로 구분해 색에만 의존하지 않는다.
        context.setLineDash(locked ? [6, 4] : []);
        context.strokeStyle = locked ? this.theme.lockedStroke : this.theme.selectedStroke;
        context.stroke();
        context.setLineDash([]);
      }
    }

    // 4. draft 도형
    if (scene.rings.length > 0) {
      context.globalAlpha = 0.2;
      context.fillStyle = this.theme.selectedFill;
      context.beginPath();
      for (const ring of scene.rings) {
        ring.forEach((point, index) => {
          const { x, y } = this.toPixel(point, transform);
          if (index === 0) context.moveTo(x, y);
          else context.lineTo(x, y);
        });
        context.closePath();
      }
      // even-odd 라 안쪽 링이 자동으로 구멍이 된다.
      context.fill("evenodd");

      context.globalAlpha = 0.95;
      context.lineWidth = 2;
      context.strokeStyle = this.theme.selectedStroke;
      context.stroke();
    }

    // 5. 반경 미리보기 원
    if (scene.previewCircle && scene.previewCircle.length > 0) {
      tracePath(scene.previewCircle);
      context.globalAlpha = 0.12;
      context.fillStyle = this.theme.previewFill;
      context.fill();
      context.globalAlpha = 0.8;
      context.lineWidth = 2;
      context.setLineDash([8, 6]);
      context.strokeStyle = this.theme.previewStroke;
      context.stroke();
      context.setLineDash([]);
    }

    // 6. 가게 위치
    const shopPixel = this.toPixel(scene.shop, transform);
    context.globalAlpha = 1;
    context.fillStyle = this.theme.shop;
    context.beginPath();
    context.arc(shopPixel.x, shopPixel.y, 5, 0, Math.PI * 2);
    context.fill();

    // 7. 브러시 원 — 데스크톱은 커서를 숨기므로 이것이 유일한 크기 단서다
    if (scene.brush) {
      context.globalAlpha = 0.9;
      context.lineWidth = 2;
      context.strokeStyle = scene.brush.erasing ? this.theme.eraseBrush : this.theme.brush;
      context.setLineDash(scene.brush.erasing ? [4, 4] : []);
      context.beginPath();
      context.arc(scene.brush.x, scene.brush.y, scene.brush.radius, 0, Math.PI * 2);
      context.stroke();
      context.setLineDash([]);
    }

    context.globalAlpha = 1;
  }

  destroy(): void {
    if (this.frameHandle !== null) window.cancelAnimationFrame(this.frameHandle);
    this.frameHandle = null;
    this.scene = null;
  }
}
