"use client";

import * as React from "react";

import { toast } from "sonner";

import {
  changeOptionsSoldOutUntilAction,
  changeProductsSoldOutUntilAction,
  hideOptionsAction,
  hideProductsAction,
  markOptionsSoldOutAction,
  markProductsSoldOutAction,
  releaseOptionsAction,
  releaseProductsAction,
} from "@/feature/product/actions";
import type {
  AvailabilityChangeOutcome,
  AvailabilityOptionRow,
  OptionSelection,
  ProductReleaseTarget,
} from "@/feature/product/domain";
import { PRODUCT_MESSAGE } from "@/feature/product/message";

/**
 * 옵션 선택 키.
 *
 * **id 만으로 관리하면 안 된다** — 일반 옵션(`PRODUCT_OPTION`)과 공통 옵션(`PRODUCT_COMMON_OPTION`)은
 * 다른 테이블·다른 id 시퀀스라 id 가 겹치고, 겹치면 잘못된 대상이 품절 처리된다.
 */
export function optionSelectionKey(row: Pick<AvailabilityOptionRow, "id" | "optionType">): string {
  return `${row.optionType}:${row.id}`;
}

type MutationKind = "soldOut" | "hide" | "release" | "changeSoldOutUntil";

interface MenuMutationInput {
  shopId?: number;
  productIds: number[];
  target?: ProductReleaseTarget;
  soldOutUntil?: string;
}

interface OptionMutationInput {
  shopId?: number;
  options: OptionSelection[];
  target?: ProductReleaseTarget;
  soldOutUntil?: string;
}

const SUCCESS_MESSAGE: Record<MutationKind, string> = {
  soldOut: PRODUCT_MESSAGE.SOLD_OUT_SUCCESS,
  hide: PRODUCT_MESSAGE.HIDE_SUCCESS,
  release: PRODUCT_MESSAGE.RELEASE_SUCCESS,
  changeSoldOutUntil: PRODUCT_MESSAGE.PERIOD_CHANGE_SUCCESS,
};

interface UseAvailabilityMutationOptions {
  /** 부분실패 목록을 화면에 남기기 위해 호출부에 전달한다 */
  onOutcome: (outcome: AvailabilityChangeOutcome) => void;
  /**
   * 처리가 끝나 선택을 비워야 할 때.
   *
   * `keepFailures` 를 주면 선택만 비우고 `onOutcome` 으로 세팅한 실패 목록은 남긴다 —
   * 부분실패에서 필요하다.
   */
  onCleared: (options?: { keepFailures?: boolean }) => void;
}

/**
 * 품절·숨김 일괄 처리 공통 훅.
 *
 * **낙관적 업데이트를 하지 않는다.** 품절·숨김은 주문을 막거나 노출을 끊으므로
 * `useTransition` + 서버 revalidation 만 쓴다(`dashboard/AGENTS.md` 의 임시중지 Switch 선례).
 * 부분실패가 있는 이 화면에서는 특히 중요하다 — 낙관적으로 반영하면 서버가 거부한 항목까지
 * 처리된 것처럼 보인다.
 */
export function useAvailabilityMutation({ onOutcome, onCleared }: UseAvailabilityMutationOptions) {
  const [isMutating, startTransition] = React.useTransition();

  /**
   * 결과 안내.
   *
   * `toast.success` 하나로 끝내지 않는다 — 부분실패는 HTTP 200 이라 성공으로 보이지만
   * 실제로는 일부만 적용됐기 때문이다. 실패 목록은 토스트에 묻지 않고 화면에 남긴다.
   */
  const report = React.useCallback(
    (outcome: AvailabilityChangeOutcome, kind: MutationKind) => {
      const succeeded = outcome.succeededIds.length;
      const failed = outcome.failed.length;

      onOutcome(outcome);

      if (failed === 0) {
        toast.success(SUCCESS_MESSAGE[kind]);
        onCleared();
        return;
      }

      if (succeeded > 0) {
        toast.warning(PRODUCT_MESSAGE.PARTIAL_FAILURE_SUMMARY(succeeded, failed), {
          description: PRODUCT_MESSAGE.PARTIAL_FAILURE,
        });
        // 일부라도 반영됐으면 선택을 비운다 — 이미 처리된 항목을 다시 누르지 않게 한다.
        // **실패 목록은 반드시 남긴다**(`keepFailures`) — 인자 없이 부르면 방금 `onOutcome` 이
        // 세팅한 실패 안내까지 지워져, 일부만 적용된 결과가 전건 성공처럼 보인다.
        onCleared({ keepFailures: true });
        return;
      }

      // 전건 실패는 선택을 남긴다 — 사용자가 조건을 고쳐 그대로 재시도할 수 있게 한다.
      // (`onCleared` 를 인자 없이 부르면 실패 목록도 함께 지워지므로 여기서는 부르지 않는다.)
      toast.error(PRODUCT_MESSAGE.CHANGE_FAILED);
    },
    [onCleared, onOutcome],
  );

  const runMenuMutation = React.useCallback(
    (kind: MutationKind, input: MenuMutationInput) => {
      const { shopId, productIds, target, soldOutUntil } = input;
      if (shopId === undefined) return;
      if (productIds.length === 0) {
        toast.error(PRODUCT_MESSAGE.TARGET_REQUIRED);
        return;
      }

      startTransition(async () => {
        const result =
          kind === "soldOut"
            ? await markProductsSoldOutAction(shopId, productIds, soldOutUntil)
            : kind === "hide"
              ? await hideProductsAction(shopId, productIds)
              : kind === "release"
                ? await releaseProductsAction(shopId, productIds, target ?? "ALL")
                : await changeProductsSoldOutUntilAction(shopId, productIds, soldOutUntil ?? "");

        // 요청 전체 거부(400)는 서버가 내려준 문구를 그대로 노출한다 — 경계값의 최종 판정은 서버다.
        if (!result.success || !result.data) {
          toast.error(result.message ?? PRODUCT_MESSAGE.CHANGE_FAILED);
          return;
        }

        report(result.data, kind);
      });
    },
    [report],
  );

  const runOptionMutation = React.useCallback(
    (kind: MutationKind, input: OptionMutationInput) => {
      const { shopId, options, target, soldOutUntil } = input;
      if (shopId === undefined) return;
      if (options.length === 0) {
        toast.error(PRODUCT_MESSAGE.TARGET_REQUIRED);
        return;
      }

      startTransition(async () => {
        const result =
          kind === "soldOut"
            ? await markOptionsSoldOutAction(shopId, options, soldOutUntil)
            : kind === "hide"
              ? await hideOptionsAction(shopId, options)
              : kind === "release"
                ? await releaseOptionsAction(shopId, options, target ?? "ALL")
                : await changeOptionsSoldOutUntilAction(shopId, options, soldOutUntil ?? "");

        if (!result.success || !result.data) {
          toast.error(result.message ?? PRODUCT_MESSAGE.CHANGE_FAILED);
          return;
        }

        report(result.data, kind);
      });
    },
    [report],
  );

  return { isMutating, runMenuMutation, runOptionMutation };
}
