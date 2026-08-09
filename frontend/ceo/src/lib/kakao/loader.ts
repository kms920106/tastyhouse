import { getKakaoMapAppKey } from "@/lib/env";

import type { KakaoMaps } from "./types";

/**
 * 카카오맵 SDK 단일 로드 보장.
 *
 * `next/script` 의 `<Script onReady>` 대신 명령형 로더를 쓰는 이유는 "지도 인스턴스 생성 →
 * 경계 fetch → 오버레이 attach" 라는 순차 의존을 `await` 로 직렬화하기 위해서다. 콜백 기반으로
 * 두면 그 순서를 컴포넌트 상태로 흉내내야 한다.
 *
 * 모듈 스코프 Promise 싱글턴이라 여러 컴포넌트가 동시에 호출해도 `<script>` 는 한 번만 꽂힌다.
 */

const SDK_SCRIPT_ATTRIBUTE = "data-kakao-sdk";

/**
 * `?libraries=services` 는 붙이지 않는다.
 *
 * 가게 좌표는 서버가 `ShopBasicInfo.latitude/longitude` 로 이미 내려주므로 지오코딩이 필요 없고,
 * 안 쓰는 라이브러리를 붙이면 번들만 커진다.
 */
function buildSdkUrl(appKey: string): string {
  return `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${encodeURIComponent(appKey)}&autoload=false`;
}

let sdkPromise: Promise<KakaoMaps> | null = null;

/** 이미 초기화가 끝난 SDK 가 전역에 있으면 그것을 쓴다 */
function getLoadedMaps(): KakaoMaps | null {
  const maps = window.kakao?.maps;
  // `LatLng` 이 있어야 `kakao.maps.load` 가 끝난 상태다 — 네임스페이스만 있는 중간 상태와 구분한다.
  return maps?.LatLng ? maps : null;
}

/** `<script>` 를 꽂거나 이미 꽂힌 것을 기다린 뒤 `kakao.maps.load` 까지 마친다 */
function injectSdk(appKey: string): Promise<KakaoMaps> {
  return new Promise((resolve, reject) => {
    const onScriptLoad = () => {
      const maps = window.kakao?.maps;
      if (!maps) {
        reject(new Error("카카오맵 SDK 를 초기화하지 못했습니다."));
        return;
      }
      // autoload=false 로 받았으므로 여기서 명시적으로 초기화한다.
      maps.load(() => {
        const loaded = getLoadedMaps();
        if (loaded) resolve(loaded);
        else reject(new Error("카카오맵 SDK 를 초기화하지 못했습니다."));
      });
    };

    const existing = document.querySelector<HTMLScriptElement>(`script[${SDK_SCRIPT_ATTRIBUTE}]`);
    if (existing) {
      // 다른 곳이 이미 태그를 꽂았다면 새로 만들지 않고 그 로드 완료에 편승한다.
      existing.addEventListener("load", onScriptLoad, { once: true });
      existing.addEventListener("error", () => reject(new Error("카카오맵 SDK 를 불러오지 못했습니다.")), {
        once: true,
      });
      return;
    }

    const script = document.createElement("script");
    script.src = buildSdkUrl(appKey);
    script.async = true;
    script.setAttribute(SDK_SCRIPT_ATTRIBUTE, "");
    script.addEventListener("load", onScriptLoad, { once: true });
    script.addEventListener("error", () => reject(new Error("카카오맵 SDK 를 불러오지 못했습니다.")), { once: true });
    document.head.appendChild(script);
  });
}

/**
 * 카카오맵 SDK 를 로드하고 `kakao.maps` 네임스페이스를 돌려준다.
 *
 * 지도 키가 없으면 reject 한다 — 호출부는 이 실패를 잡아 지도 대신 폴백 UI 를 렌더한다.
 * 실패한 Promise 는 캐시하지 않으므로 재시도가 가능하다.
 */
export function loadKakaoMaps(): Promise<KakaoMaps> {
  if (typeof window === "undefined") {
    return Promise.reject(new Error("카카오맵 SDK 는 브라우저에서만 로드할 수 있습니다."));
  }

  const alreadyLoaded = getLoadedMaps();
  if (alreadyLoaded) return Promise.resolve(alreadyLoaded);

  // 이미 로드가 진행 중이면 그 Promise 를 공유한다 — `<script>` 를 두 번 꽂지 않는다.
  if (sdkPromise !== null) return sdkPromise;

  const appKey = getKakaoMapAppKey();
  if (!appKey) return Promise.reject(new Error("카카오맵 키가 설정되지 않았습니다."));

  sdkPromise = injectSdk(appKey).catch((error: unknown) => {
    // 실패를 캐시하면 새로고침 전까지 영영 재시도할 수 없다.
    sdkPromise = null;
    throw error;
  });

  return sdkPromise;
}
