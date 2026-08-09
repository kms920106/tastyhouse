"use client";

import * as React from "react";

import { DELIVERY_AREA_DRAFT_SAVE_DEBOUNCE_MS } from "@/feature/shop/constants";
import type { GeoRing } from "@/feature/shop/domain";
import { getLocalStorageValue, setLocalStorageValue } from "@/lib/local-storage.client";

import type { DeliveryAreaDraftState } from "./use-delivery-area-draft";

/**
 * 편집 중인 draft 를 localStorage 에 임시 저장하고 재진입 시 복원한다.
 *
 * 저장 전에 탭이 닫히거나 실수로 뒤로 가면 지도에 칠한 내용이 통째로 날아간다. 반경·트리는
 * 다시 고르면 그만이지만 브러시로 다듬은 도형은 복원할 방법이 없어 손실이 크다.
 *
 * 가게마다 키를 나눈다 — 가게를 바꿔 들어왔을 때 남의 draft 를 이어받으면 안 된다.
 */

const DRAFT_KEY_PREFIX = "delivery-area-draft";

/** localStorage 에는 Set 을 담을 수 없어 배열로 눕혀 저장한다 */
interface StoredDraft {
  rings: GeoRing[];
  adminDongIds: number[];
}

function storageKey(shopId: number): string {
  return `${DRAFT_KEY_PREFIX}:${shopId}`;
}

export interface DeliveryAreaDraftStorage {
  /** 저장된 draft. 없거나 깨졌으면 null */
  restored: DeliveryAreaDraftState | null;
  /** 복원 여부를 물었고 답을 받았음을 기록한다 — 이후 다시 묻지 않는다 */
  dismiss: () => void;
  /** 저장 성공·이탈 확정 시 임시 저장분을 지운다 */
  clear: () => void;
}

export function useDeliveryAreaDraftStorage(
  shopId: number,
  state: DeliveryAreaDraftState,
  isDirty: boolean,
): DeliveryAreaDraftStorage {
  // 최초 마운트 시점에 한 번만 읽는다. 이후에는 우리가 쓴 값이라 다시 읽을 이유가 없다.
  const [restored, setRestored] = React.useState<DeliveryAreaDraftState | null>(() => {
    if (typeof window === "undefined") return null;

    const raw = getLocalStorageValue(storageKey(shopId));
    if (!raw) return null;

    try {
      const parsed = JSON.parse(raw) as StoredDraft;
      // 형태가 조금이라도 어긋나면 복원하지 않는다 — 깨진 draft 로 편집을 시작하는 편이 더 나쁘다.
      if (!Array.isArray(parsed.rings) || !Array.isArray(parsed.adminDongIds)) return null;
      return { rings: parsed.rings, adminDongIds: new Set(parsed.adminDongIds) };
    } catch {
      return null;
    }
  });

  const clear = React.useCallback(() => {
    setLocalStorageValue(storageKey(shopId), "");
    setRestored(null);
  }, [shopId]);

  const dismiss = React.useCallback(() => setRestored(null), []);

  // 조작이 멈춘 뒤에만 쓴다. 브러시 한 획마다 직렬화하면 정점 수천 개를 매번 문자열로 만든다.
  React.useEffect(() => {
    if (!isDirty) return;

    const timer = setTimeout(() => {
      const payload: StoredDraft = { rings: state.rings, adminDongIds: [...state.adminDongIds] };
      setLocalStorageValue(storageKey(shopId), JSON.stringify(payload));
    }, DELIVERY_AREA_DRAFT_SAVE_DEBOUNCE_MS);

    return () => clearTimeout(timer);
  }, [shopId, state, isDirty]);

  return { restored, dismiss, clear };
}
