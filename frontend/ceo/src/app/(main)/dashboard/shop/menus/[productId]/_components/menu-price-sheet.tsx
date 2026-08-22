"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { useFieldArray, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import {
  loadProductPricesAction,
  loadStorePriceVerificationAction,
  updateProductPricesAction,
} from "@/feature/product/actions";
import type { MenuPrice } from "@/feature/product/domain";
import { PRODUCT_DETAIL_SCREEN_COPY, PRODUCT_PRICE_COPY, PRODUCT_PRICE_MESSAGE } from "@/feature/product/message";
import { type ProductPricesFormValues, productPricesSchema } from "@/feature/product/schema";

import { MenuPriceRows } from "./menu-price-rows";

const FORM_ID = "menu-price-form";

/** 가격이 하나도 없는 메뉴는 있을 수 없지만, 조회 실패 시 편집할 행 하나는 있어야 한다 */
const EMPTY_ROW = { priceName: "", deliveryPrice: "", storePrice: "", pickupPrice: "" };

/** 금액 null 은 미설정이므로 빈 문자열로 편다 — 0("무료")과 구분되어야 한다 */
function toFormValues(prices: MenuPrice[]): ProductPricesFormValues {
  if (prices.length === 0) return { prices: [EMPTY_ROW] };

  return {
    prices: prices.map((price) => ({
      id: price.id,
      priceName: price.priceName ?? "",
      deliveryPrice: String(price.deliveryPrice),
      storePrice: price.storePrice === null ? "" : String(price.storePrice),
      pickupPrice: price.pickupPrice === null ? "" : String(price.pickupPrice),
    })),
  };
}

interface MenuPriceSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  productId: number;
  shopId: number;
  /**
   * 할인 대기·진행 중 여부.
   *
   * 할인 중인 메뉴는 가격을 바꿀 수 없다(PDF 명시) — 상세가 이미 아는 값이라 다시 조회하지 않는다.
   */
  discountInProgress: boolean;
  /** 상세 행 요약을 갱신한다 */
  onChanged: (prices: MenuPrice[]) => void;
  /** 매장 가격 인증 화면(메뉴판)으로 보내는 진입점 */
  onNavigateVerification?: () => void;
}

/**
 * 메뉴 가격 설정.
 *
 * 기존 단일 가격(`originalPrice`·`discountPrice`)을 편집하던 자리를 **가격 행 목록**으로 바꾼다.
 * 저장은 전체 교체(PUT)라 보내지 않은 행이 삭제되므로, 조회 실패 시 폼을 비우지 않는다 —
 * 빈 폼으로 저장하면 기존 가격이 사라진다.
 *
 * 매장가·픽업가는 **매장가격 인증이 승인된 가게만** 설정할 수 있어, 열 때 인증 상태를 함께 읽어
 * 입력란 활성 여부를 정한다.
 */
export function MenuPriceSheet({
  open,
  onOpenChange,
  productId,
  shopId,
  discountInProgress,
  onChanged,
  onNavigateVerification,
}: MenuPriceSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [storePriceVerified, setStorePriceVerified] = React.useState(false);

  const form = useForm<ProductPricesFormValues>({
    resolver: zodResolver(productPricesSchema),
    defaultValues: { prices: [EMPTY_ROW] },
  });

  const fieldArray = useFieldArray({ control: form.control, name: "prices" });

  // 열리는 순간에만 서버 값으로 되돌린다. 조회 실패 시 폼을 건드리지 않는다(전체 교체라 위험하다).
  const wasOpen = React.useRef(false);
  React.useEffect(() => {
    if (open && !wasOpen.current) {
      startTransition(async () => {
        const [priceResult, verificationResult] = await Promise.all([
          loadProductPricesAction(productId, shopId),
          loadStorePriceVerificationAction(shopId),
        ]);

        // 인증 상태 조회 실패는 저장을 막지 않는다 — 배달가는 인증과 무관하게 상시 수정 가능하다.
        // 다만 실패 시 매장가·픽업가는 잠근 채로 둔다(서버가 어차피 거절한다).
        setStorePriceVerified(verificationResult.success && (verificationResult.data?.verified ?? false));

        if (!priceResult.success || !priceResult.data) {
          toast.error(priceResult.message ?? PRODUCT_PRICE_MESSAGE.LOAD_FAILED);
          return;
        }

        form.reset(toFormValues(priceResult.data));
      });
    }
    wasOpen.current = open;
  }, [open, productId, shopId, form]);

  const onSubmit = (values: ProductPricesFormValues) => {
    startTransition(async () => {
      const { success, message } = await updateProductPricesAction(productId, shopId, values);
      if (!success) {
        toast.error(message ?? PRODUCT_PRICE_MESSAGE.SAVE_FAILED);
        return;
      }

      toast.success(PRODUCT_PRICE_MESSAGE.SAVE_SUCCESS);
      // 서버가 부여한 행 id 를 받아야 다음 저장이 기존 행을 갱신한다(id 없으면 새 행으로 추가된다).
      const { data } = await loadProductPricesAction(productId, shopId);
      if (data) {
        form.reset(toFormValues(data));
        onChanged(data);
      }
      onOpenChange(false);
    });
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>{PRODUCT_PRICE_COPY.SECTION_TITLE}</SheetTitle>
          <SheetDescription>{PRODUCT_PRICE_COPY.CHANNEL_GUIDE_FOOTNOTE}</SheetDescription>
        </SheetHeader>

        <form id={FORM_ID} noValidate onSubmit={form.handleSubmit(onSubmit)} className="flex-1 overflow-y-auto px-4">
          <MenuPriceRows
            control={form.control}
            fieldArray={fieldArray}
            errors={form.formState.errors}
            pending={isPending}
            storePriceVerified={storePriceVerified}
            discountInProgress={discountInProgress}
            onNavigateVerification={onNavigateVerification}
          />
        </form>

        <SheetFooter>
          <Button type="submit" form={FORM_ID} disabled={isPending || discountInProgress}>
            {isPending ? PRODUCT_PRICE_COPY.ACTION_PENDING : PRODUCT_PRICE_COPY.ACTION_SUBMIT}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={isPending}>
              {PRODUCT_DETAIL_SCREEN_COPY.BUTTON_CANCEL}
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
