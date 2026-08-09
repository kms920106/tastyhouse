import polygonClipping, {
  type MultiPolygon as ClipMultiPolygon,
  type Polygon as ClipPolygon,
  type Ring as ClipRing,
} from "polygon-clipping";

import type { GeoPoint, GeoRing } from "./domain";

/**
 * 배달지역 편집에 쓰는 순수 기하 함수 모음.
 *
 * `time.ts` 와 같은 성격이다 — React·DOM·카카오 SDK 를 일절 참조하지 않으므로 단독으로
 * 테스트할 수 있고, 캔버스 렌더 루프에서 매 프레임 호출해도 안전하다.
 *
 * 좌표는 전부 `{ latitude, longitude }` 객체로 다룬다. `polygon-clipping` 은 GeoJSON 과 같은
 * `[lng, lat]` 튜플을 쓰므로 이 파일의 경계에서만 변환하고, 밖으로는 객체 형태만 내보낸다.
 */

/** 지구 평균 반지름(m) */
const EARTH_RADIUS_METERS = 6_371_008.8;

/** 반경 원을 근사할 정점 수 — 백엔드 `CIRCLE_SEGMENTS` 와 맞춘다 */
export const CIRCLE_SEGMENTS = 72;

const toRadians = (degrees: number): number => (degrees * Math.PI) / 180;
const toDegrees = (radians: number): number => (radians * 180) / Math.PI;

/**
 * 두 지점 사이의 거리(m) — 하버사인.
 *
 * `ShopSearchQueryDao` 의 `METERS_PER_DEGREE = 111000` 사각 근사를 재사용하지 않는다.
 * 위경도 양쪽에 같은 값을 쓰기 때문에 위도 37.5°에서 동서 방향이 약 21% 좁게 나오고,
 * 200m 에서는 무해하지만 7km 로 확대하면 약 1.6km 가 어긋난다.
 */
export function distanceMeters(from: GeoPoint, to: GeoPoint): number {
  const fromLatRad = toRadians(from.latitude);
  const toLatRad = toRadians(to.latitude);
  const deltaLatRad = toRadians(to.latitude - from.latitude);
  const deltaLngRad = toRadians(to.longitude - from.longitude);

  const a = Math.sin(deltaLatRad / 2) ** 2 + Math.cos(fromLatRad) * Math.cos(toLatRad) * Math.sin(deltaLngRad / 2) ** 2;

  return 2 * EARTH_RADIUS_METERS * Math.asin(Math.min(1, Math.sqrt(a)));
}

/**
 * 중심점에서 반경 `radiusMeters` 인 원을 정다각형으로 근사한다.
 *
 * 위도 1도의 길이는 어디서나 거의 같지만 경도 1도는 극으로 갈수록 짧아지므로
 * `cos(위도)` 로 나눠 보정한다. 그러지 않으면 위도가 높을수록 원이 동서로 늘어난다.
 */
export function circleRing(center: GeoPoint, radiusMeters: number, segments = CIRCLE_SEGMENTS): GeoRing {
  const latRad = toRadians(center.latitude);
  const deltaLat = toDegrees(radiusMeters / EARTH_RADIUS_METERS);
  const cosLat = Math.cos(latRad);
  // 극점 근처에서 0으로 나누는 것을 막는다. 국내 위도에서는 걸리지 않는 방어선이다.
  const deltaLng = toDegrees(radiusMeters / (EARTH_RADIUS_METERS * Math.max(cosLat, 1e-6)));

  return Array.from({ length: segments }, (_, index) => {
    const angle = (2 * Math.PI * index) / segments;
    return {
      latitude: center.latitude + deltaLat * Math.sin(angle),
      longitude: center.longitude + deltaLng * Math.cos(angle),
    };
  });
}

/** 폴리곤의 모든 정점 중 기준점에서 가장 먼 거리(m). 7km 상한 판정에 쓴다 */
export function maxDistanceMetersFrom(rings: GeoRing[], center: GeoPoint): number {
  let max = 0;
  for (const ring of rings) {
    for (const point of ring) {
      const distance = distanceMeters(center, point);
      if (distance > max) max = distance;
    }
  }
  return max;
}

/** 링 전체의 총 정점 수 — 상한 검증·표시에 쓴다 */
export function countVertices(rings: GeoRing[]): number {
  return rings.reduce((sum, ring) => sum + ring.length, 0);
}

/**
 * 점이 폴리곤 안에 있는지 — ray casting(even-odd).
 *
 * 링을 홀수 번 통과하면 내부다. 바깥 링 안에 든 링은 자동으로 hole 로 동작하므로
 * 링의 방향(시계/반시계)을 따로 맞출 필요가 없다.
 */
export function containsPoint(rings: GeoRing[], point: GeoPoint): boolean {
  let inside = false;

  for (const ring of rings) {
    for (let i = 0, j = ring.length - 1; i < ring.length; j = i++) {
      const a = ring[i];
      const b = ring[j];
      // 두 정점이 걸치는 위도 구간에 점의 위도가 들어갈 때만 교차를 따진다.
      const straddles = a.latitude > point.latitude !== b.latitude > point.latitude;
      if (!straddles) continue;

      const intersectLng =
        ((b.longitude - a.longitude) * (point.latitude - a.latitude)) / (b.latitude - a.latitude) + a.longitude;
      if (point.longitude < intersectLng) inside = !inside;
    }
  }

  return inside;
}

/** 링들을 감싸는 경계 상자. 정점이 없으면 null */
export function boundingBox(
  rings: GeoRing[],
): { minLat: number; maxLat: number; minLng: number; maxLng: number } | null {
  let minLat = Number.POSITIVE_INFINITY;
  let maxLat = Number.NEGATIVE_INFINITY;
  let minLng = Number.POSITIVE_INFINITY;
  let maxLng = Number.NEGATIVE_INFINITY;
  let found = false;

  for (const ring of rings) {
    for (const point of ring) {
      found = true;
      if (point.latitude < minLat) minLat = point.latitude;
      if (point.latitude > maxLat) maxLat = point.latitude;
      if (point.longitude < minLng) minLng = point.longitude;
      if (point.longitude > maxLng) maxLng = point.longitude;
    }
  }

  return found ? { minLat, maxLat, minLng, maxLng } : null;
}

// ===== polygon-clipping 경계 =====

/** `polygon-clipping` 은 GeoJSON 과 같은 `[lng, lat]` 순서를 쓴다 */
function toClipRing(ring: GeoRing): ClipRing {
  const points: ClipRing = ring.map((point) => [point.longitude, point.latitude]);
  // GeoJSON 규약대로 링을 닫아 준다. 열린 링을 주면 마지막 변이 누락된다.
  const first = points[0];
  const last = points[points.length - 1];
  if (first && last && (first[0] !== last[0] || first[1] !== last[1])) points.push([first[0], first[1]]);
  return points;
}

/**
 * 편집기의 평탄한 링 목록을 클리퍼가 이해하는 "폴리곤 → [외곽, 구멍…]" 구조로 되돌린다.
 *
 * **이 재구성이 없으면 지우기로 만든 구멍이 조용히 메워진다.** 평탄한 목록은 캔버스의
 * even-odd 채움과 `containsPoint` 에는 충분하지만, 그대로 클리퍼에 넣으면 구멍 링이
 * 전부 독립된 외곽 링으로 승격돼 면적으로 바뀐다. 그러면 "구멍을 뚫어 둔 채 엉뚱한 곳에
 * 한 획 더 긋는" 순간 구멍이 union 되어 사라지는데, 화면에도 목록에도 아무 신호가 없다.
 *
 * 어떤 링이 다른 링 안에 들어 있으면 구멍으로 본다 — even-odd 규약과 같은 판정이다.
 * 링 개수 상한이 20 이라 O(n²) 포함 검사는 무시할 만하다.
 */
function toClipGeom(rings: GeoRing[]): ClipMultiPolygon {
  if (rings.length === 0) return [];

  // 각 링이 몇 겹 안에 들어 있는지 센다. 홀수면 구멍, 짝수면 외곽이다.
  const depths = rings.map((ring, index) => {
    const probe = ring[0];
    let depth = 0;
    rings.forEach((other, otherIndex) => {
      if (otherIndex !== index && containsPoint([other], probe)) depth += 1;
    });
    return depth;
  });

  const polygons: ClipPolygon[] = [];
  const outerIndexes: number[] = [];

  rings.forEach((ring, index) => {
    if (depths[index] % 2 !== 0) return;
    outerIndexes.push(index);
    polygons.push([toClipRing(ring)]);
  });

  // 구멍은 자신을 감싸는 가장 안쪽 외곽 링에 붙인다.
  rings.forEach((ring, index) => {
    if (depths[index] % 2 === 0) return;

    let bestSlot = -1;
    let bestDepth = -1;
    outerIndexes.forEach((outerIndex, slot) => {
      if (!containsPoint([rings[outerIndex]], ring[0])) return;
      if (depths[outerIndex] > bestDepth) {
        bestDepth = depths[outerIndex];
        bestSlot = slot;
      }
    });

    // 감싸는 외곽을 못 찾으면(수치 오차 등) 버리지 않고 독립 폴리곤으로 둔다.
    if (bestSlot === -1) polygons.push([toClipRing(ring)]);
    else polygons[bestSlot].push(toClipRing(ring));
  });

  return polygons;
}

/**
 * 연산 결과를 편집기가 쓰는 링 목록으로 되돌린다.
 *
 * `polygon-clipping` 은 `[polygon][ring][point]` 3중 배열을 주는데, 첫 링이 외곽이고
 * 나머지가 hole 이다. 편집기는 even-odd 판정을 쓰므로 hole 도 그대로 평탄화해 담으면 된다.
 * 각 링의 닫는 점(첫 점과 같은 마지막 점)은 저장·표시에 불필요하므로 떼어낸다.
 */
function fromClipGeom(geom: ClipMultiPolygon): GeoRing[] {
  const rings: GeoRing[] = [];

  for (const polygon of geom) {
    for (const ring of polygon) {
      const points: GeoRing = ring.map(([lng, lat]) => ({ latitude: lat, longitude: lng }));
      const first = points[0];
      const last = points[points.length - 1];
      if (
        points.length > 1 &&
        first &&
        last &&
        first.latitude === last.latitude &&
        first.longitude === last.longitude
      ) {
        points.pop();
      }
      // 3점 미만은 면적이 없어 저장 스키마(링당 정점 ≥ 3)도 통과하지 못한다.
      if (points.length >= 3) rings.push(points);
    }
  }

  return rings;
}

/**
 * 도형에 새 도형을 더한다(그리기).
 *
 * 기존 도형이 비어 있으면 더할 것이 없으므로 새 도형을 그대로 정규화해 돌려준다.
 */
export function unionRings(base: GeoRing[], addition: GeoRing[]): GeoRing[] {
  if (addition.length === 0) return base;
  if (base.length === 0) return fromClipGeom(polygonClipping.union(toClipGeom(addition)));

  return fromClipGeom(polygonClipping.union(toClipGeom(base), toClipGeom(addition)));
}

/**
 * 도형에서 새 도형을 뺀다(지우기).
 *
 * 전부 지워지면 빈 배열이 되며, 이는 "도형 없음"이라는 정상 상태다.
 */
export function differenceRings(base: GeoRing[], subtraction: GeoRing[]): GeoRing[] {
  if (base.length === 0 || subtraction.length === 0) return base;

  return fromClipGeom(polygonClipping.difference(toClipGeom(base), toClipGeom(subtraction)));
}

/**
 * 브러시 스트로크를 도형으로 만든다.
 *
 * `pointermove` 는 듬성듬성 들어오므로 점 사이를 이어 붙이지 않으면 빠르게 끌 때 구멍이 남는다.
 * 각 샘플 지점에 원을 놓고 전부 union 해 소시지 모양을 만든다. 샘플 간격은 호출부가
 * 브러시 반경의 절반 이하로 잡아 원들이 반드시 겹치게 한다.
 */
export function strokeToRings(samples: GeoPoint[], radiusMetersAt: (point: GeoPoint) => number): GeoRing[] {
  if (samples.length === 0) return [];

  const circles = samples.map((point) => circleRing(point, radiusMetersAt(point)));
  return fromClipGeom(polygonClipping.union(toClipGeom(circles)));
}

/**
 * 두 점 사이를 일정 간격으로 채운 샘플 목록.
 *
 * 시작점은 넣지 않는다 — 직전 스트로크 구간이 이미 그 점을 포함하고 있어 중복 원을 만들면
 * union 비용만 늘어난다.
 */
export function interpolate(from: GeoPoint, to: GeoPoint, stepMeters: number): GeoPoint[] {
  // 간격이 유효하지 않으면(0 이하·NaN) 나눗셈이 NaN 이 되어 샘플이 통째로 사라진다.
  // "채울 수 없음"과 "채울 필요 없음"을 갈라서, 전자에서도 최소한 끝점은 남긴다.
  if (!Number.isFinite(stepMeters) || stepMeters <= 0) return [to];

  const total = distanceMeters(from, to);
  if (!Number.isFinite(total) || total <= stepMeters) return [to];

  const steps = Math.ceil(total / stepMeters);
  return Array.from({ length: steps }, (_, index) => {
    const ratio = (index + 1) / steps;
    return {
      latitude: from.latitude + (to.latitude - from.latitude) * ratio,
      longitude: from.longitude + (to.longitude - from.longitude) * ratio,
    };
  });
}
