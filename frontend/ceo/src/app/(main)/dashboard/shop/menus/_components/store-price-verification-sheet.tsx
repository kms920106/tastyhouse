"use client";

import * as React from "react";

import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Field, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
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
  requestStorePriceVerificationAction,
} from "@/feature/product/actions";
import { STORE_PRICE_BULK_STEP } from "@/feature/product/constants";
import type { MenuBoardGroup, StorePriceVerification } from "@/feature/product/domain";
import { formatPrice } from "@/feature/product/format";
import {
  PRODUCT_DETAIL_SCREEN_COPY,
  STORE_PRICE_VERIFICATION_COPY,
  STORE_PRICE_VERIFICATION_MESSAGE,
} from "@/feature/product/message";

interface StorePriceVerificationSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  /** 인증 대상 후보. 메뉴판이 이미 받아 둔 그룹을 그대로 쓴다(별도 목록 API 를 두지 않는다) */
  groups?: MenuBoardGroup[];
}

/** 선택한 메뉴의 입력 상태. `priceId` 는 열 때 가격 조회로 채운다 */
interface TargetRow {
  productId: number;
  productName: string;
  deliveryPrice: number;
  /** 첫 가격 행(`sort=0`)의 id. 가격 조회 실패 시 null 이고 그 메뉴는 요청에서 제외된다 */
  priceId: number | null;
  storePrice: string;
}

/**
 * 매장 가격 인증 요청.
 *
 * PDF STEP 흐름(대상 선택 → 매장가 입력 → 일괄 변경 → 픽업 동일 → 가격표 업로드 → 요청)을
 * 한 Sheet 에 담는다. 단계를 화면으로 쪼개지 않는 이유는 점주가 일괄 변경 결과를 보며 개별
 * 값을 다시 만지는 왕복이 잦아, 한 화면에서 전부 보이는 편이 낫기 때문이다.
 *
 * **가격표 이미지 규격 판정은 서버가 한다** — 브라우저에서 잰 치수와 서버 판정이 어긋나면
 * "올렸는데 통과 못 하는" 상태를 설명할 수 없어서다. 화면은 파일을 골랐는지만 본다.
 */
export function StorePriceVerificationSheet({
  open,
  onOpenChange,
  shopId,
  groups = [],
}: StorePriceVerificationSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [verification, setVerification] = React.useState<StorePriceVerification | null>(null);
  const [rows, setRows] = React.useState<TargetRow[]>([]);
  const [selectedIds, setSelectedIds] = React.useState<Set<number>>(new Set());
  const [applyPickupSamePrice, setApplyPickupSamePrice] = React.useState(false);
  const [bulkAmount, setBulkAmount] = React.useState(String(STORE_PRICE_BULK_STEP));
  const [file, setFile] = React.useState<File | null>(null);
  const fileInputRef = React.useRef<HTMLInputElement>(null);

  const candidates = React.useMemo(
    () =>
      groups.flatMap((group) =>
        group.products.map((product) => ({ id: product.id, name: product.name, originalPrice: product.originalPrice })),
      ),
    [groups],
  );

  // 열리는 순간에만 인증 상태를 읽는다. 후보 목록은 부모가 이미 갖고 있어 재조회하지 않는다.
  const wasOpen = React.useRef(false);
  React.useEffect(() => {
    if (open && !wasOpen.current) {
      setRows(
        candidates.map((candidate) => ({
          productId: candidate.id,
          productName: candidate.name,
          deliveryPrice: candidate.originalPrice,
          priceId: null,
          storePrice: "",
        })),
      );
      setSelectedIds(new Set());
      setApplyPickupSamePrice(false);
      setFile(null);
      if (fileInputRef.current !== null) fileInputRef.current.value = "";

      startTransition(async () => {
        const result = await loadStorePriceVerificationAction(shopId);
        if (!result.success || !result.data) {
          toast.error(result.message ?? STORE_PRICE_VERIFICATION_MESSAGE.LOAD_FAILED);
          return;
        }
        setVerification(result.data);
      });
    }
    wasOpen.current = open;
  }, [open, shopId, candidates]);

  /**
   * 메뉴를 선택하면 그 메뉴의 `priceId` 를 조회해 채운다.
   *
   * 인증 요청은 가격 **행** 단위라 `priceId` 가 반드시 있어야 한다. 후보 목록(메뉴판 응답)에는
   * 가격 행 정보가 없어, 선택 시점에 그 메뉴만 조회한다 — 전체를 미리 조회하면 메뉴가 많은
   * 가게에서 N+1 이 된다.
   */
  const toggleTarget = (productId: number) => {
    const next = new Set(selectedIds);
    if (next.has(productId)) {
      next.delete(productId);
      setSelectedIds(next);
      return;
    }

    next.add(productId);
    setSelectedIds(next);

    const row = rows.find((item) => item.productId === productId);
    if (row === undefined || row.priceId !== null) return;

    startTransition(async () => {
      const result = await loadProductPricesAction(productId, shopId);
      if (!result.success || !result.data || result.data.length === 0) {
        toast.error(result.message ?? STORE_PRICE_VERIFICATION_MESSAGE.LOAD_FAILED);
        return;
      }

      // 첫 행(`sort=0`)이 기본 가격이다. 가격명이 여러 개인 메뉴의 나머지 행은 이 화면에서
      // 다루지 않는다 — PDF 의 인증 흐름도 메뉴 단위 대표 가격만 받는다.
      const [firstPrice] = [...result.data].sort((a, b) => a.sort - b.sort);
      setRows((prev) =>
        prev.map((item) =>
          item.productId === productId
            ? { ...item, priceId: firstPrice.id, deliveryPrice: firstPrice.deliveryPrice }
            : item,
        ),
      );
    });
  };

  const updateStorePrice = (productId: number, value: string) => {
    setRows((prev) => prev.map((row) => (row.productId === productId ? { ...row, storePrice: value } : row)));
  };

  /**
   * 매장가격 일괄 변경.
   *
   * 선택한 메뉴 전체를 100원 단위로 올리거나 내린다. 값이 비어 있는 행은 배달가를 기준으로
   * 시작한다 — 아무것도 없는 칸에 증감만 적용하면 무엇을 올린 것인지 알 수 없다.
   * 0 아래로는 내려가지 않는다.
   */
  const applyBulk = (direction: 1 | -1) => {
    const amount = Number(bulkAmount);
    if (!Number.isInteger(amount) || amount <= 0) return;

    setRows((prev) =>
      prev.map((row) => {
        if (!selectedIds.has(row.productId)) return row;
        const base = row.storePrice.trim() === "" ? row.deliveryPrice : Number(row.storePrice);
        const next = Math.max(0, base + direction * amount);
        return { ...row, storePrice: String(next) };
      }),
    );
  };

  const selectedRows = rows.filter((row) => selectedIds.has(row.productId));

  /**
   * 요청 불가 상황.
   *
   * 검수 중 재요청은 서버도 막지만(`SHOP_STORE_PRICE_VERIFICATION_IN_PROGRESS`), 버튼을 눌러
   * 실패를 겪게 하는 대신 사유를 미리 보여준다. 할인 진행 여부는 이 화면이 알 수 없어
   * 서버 거절 문구로만 드러난다.
   */
  const reviewInProgress = verification?.status === "PENDING" || verification?.status === "IN_PROGRESS";

  const handleSubmit = () => {
    if (selectedRows.length === 0) {
      toast.error(STORE_PRICE_VERIFICATION_MESSAGE.TARGET_EMPTY);
      return;
    }
    if (file === null) {
      toast.error(STORE_PRICE_VERIFICATION_MESSAGE.IMAGE_REQUIRED);
      return;
    }
    // `priceId` 를 못 받은 행은 요청에 실을 수 없다 — 서버가 어느 가격 행인지 알 수 없다.
    if (selectedRows.some((row) => row.priceId === null || row.storePrice.trim() === "")) {
      toast.error(STORE_PRICE_VERIFICATION_MESSAGE.STORE_PRICE_REQUIRED);
      return;
    }

    startTransition(async () => {
      const { success, message } = await requestStorePriceVerificationAction(
        shopId,
        {
          items: selectedRows.map((row) => ({
            productId: row.productId,
            // 위에서 null 을 걸러냈다
            priceId: row.priceId as number,
            storePrice: row.storePrice,
            applyPickupSamePrice,
          })),
        },
        file,
      );

      if (!success) {
        toast.error(message ?? STORE_PRICE_VERIFICATION_MESSAGE.REQUEST_FAILED);
        return;
      }

      toast.success(STORE_PRICE_VERIFICATION_MESSAGE.REQUEST_SUCCESS);
      onOpenChange(false);
    });
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-xl">
        <SheetHeader>
          <SheetTitle>{STORE_PRICE_VERIFICATION_COPY.SHEET_TITLE}</SheetTitle>
          <SheetDescription>{STORE_PRICE_VERIFICATION_COPY.SHEET_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex flex-1 flex-col gap-5 overflow-y-auto px-4">
          {/* 인증 상태 — ON/OFF 와 미인증 사유 */}
          <section className="flex flex-col gap-2">
            <div className="flex items-center gap-2">
              <span className="font-medium text-sm">{STORE_PRICE_VERIFICATION_COPY.STATUS_TITLE}</span>
              <Badge variant={verification?.verified === true ? "default" : "secondary"}>
                {verification?.verified === true
                  ? STORE_PRICE_VERIFICATION_COPY.STATUS_ON
                  : STORE_PRICE_VERIFICATION_COPY.STATUS_OFF}
              </Badge>
            </div>
            <p className="text-muted-foreground text-sm">
              {verification?.verified === true
                ? STORE_PRICE_VERIFICATION_MESSAGE.VERIFIED_ON
                : STORE_PRICE_VERIFICATION_MESSAGE.VERIFIED_OFF}
            </p>

            {verification?.rejectReason != null && (
              <p className="text-destructive text-sm">
                {`${STORE_PRICE_VERIFICATION_COPY.REJECT_REASON_PREFIX}${verification.rejectReason}`}
              </p>
            )}

            {verification !== null && verification.unverifiedItems.length > 0 && (
              <div className="flex flex-col gap-1 rounded-md bg-muted px-3 py-2">
                <span className="text-sm">{STORE_PRICE_VERIFICATION_COPY.UNVERIFIED_LIST_TITLE}</span>
                {verification.unverifiedItems.map((item) => (
                  <span key={item.productId} className="text-muted-foreground text-xs">
                    {`${item.productName} — ${STORE_PRICE_VERIFICATION_COPY.REASON_LABEL[item.reason]}`}
                  </span>
                ))}
              </div>
            )}

            {reviewInProgress && (
              <div className="flex flex-col gap-1 rounded-md bg-muted px-3 py-2">
                <span className="font-medium text-sm">{STORE_PRICE_VERIFICATION_COPY.BLOCKED_TITLE}</span>
                <span className="text-muted-foreground text-xs">{STORE_PRICE_VERIFICATION_MESSAGE.IN_PROGRESS}</span>
                <span className="text-muted-foreground text-xs">
                  {STORE_PRICE_VERIFICATION_MESSAGE.DISCOUNT_IN_PROGRESS}
                </span>
              </div>
            )}
          </section>

          <Separator />

          {/* 1·2단계 — 대상 선택과 매장가 입력을 한 행에서 함께 한다 */}
          <section className="flex flex-col gap-2">
            <span className="font-medium text-sm">{STORE_PRICE_VERIFICATION_COPY.STEP_TARGET_TITLE}</span>
            {rows.length === 0 ? (
              <p className="text-muted-foreground text-sm">{STORE_PRICE_VERIFICATION_COPY.EMPTY_MENU}</p>
            ) : (
              <div className="flex flex-col gap-2">
                {rows.map((row) => {
                  const checked = selectedIds.has(row.productId);
                  return (
                    <div key={row.productId} className="flex items-center gap-3 rounded-md border px-3 py-2">
                      <Checkbox
                        id={`store-price-target-${row.productId}`}
                        checked={checked}
                        disabled={isPending || reviewInProgress}
                        onCheckedChange={() => toggleTarget(row.productId)}
                      />
                      <label
                        htmlFor={`store-price-target-${row.productId}`}
                        className="min-w-0 flex-1 truncate text-sm"
                      >
                        {row.productName}
                      </label>
                      <span className="text-muted-foreground text-xs">
                        {`${STORE_PRICE_VERIFICATION_COPY.COLUMN_DELIVERY_PRICE} ${formatPrice(row.deliveryPrice)}`}
                      </span>
                      <Input
                        aria-label={`${row.productName} ${STORE_PRICE_VERIFICATION_COPY.COLUMN_STORE_PRICE}`}
                        className="w-28"
                        inputMode="numeric"
                        value={row.storePrice}
                        disabled={!checked || isPending || reviewInProgress}
                        onChange={(event) => updateStorePrice(row.productId, event.target.value)}
                      />
                    </div>
                  );
                })}
              </div>
            )}
          </section>

          {/* 3단계 — 일괄 변경 */}
          <section className="flex flex-col gap-2">
            <span className="font-medium text-sm">{STORE_PRICE_VERIFICATION_COPY.STEP_BULK_TITLE}</span>
            <p className="text-muted-foreground text-xs">{STORE_PRICE_VERIFICATION_COPY.BULK_HELP}</p>
            <div className="flex items-end gap-2">
              <Field className="gap-1.5">
                <FieldLabel htmlFor="store-price-bulk-amount">
                  {STORE_PRICE_VERIFICATION_COPY.BULK_AMOUNT_LABEL}
                </FieldLabel>
                <Input
                  id="store-price-bulk-amount"
                  className="w-32"
                  inputMode="numeric"
                  step={STORE_PRICE_BULK_STEP}
                  value={bulkAmount}
                  disabled={isPending || reviewInProgress}
                  onChange={(event) => setBulkAmount(event.target.value)}
                />
              </Field>
              <Button
                type="button"
                variant="outline"
                disabled={isPending || reviewInProgress || selectedRows.length === 0}
                onClick={() => applyBulk(-1)}
              >
                {STORE_PRICE_VERIFICATION_COPY.ACTION_BULK_DECREASE}
              </Button>
              <Button
                type="button"
                variant="outline"
                disabled={isPending || reviewInProgress || selectedRows.length === 0}
                onClick={() => applyBulk(1)}
              >
                {STORE_PRICE_VERIFICATION_COPY.ACTION_BULK_INCREASE}
              </Button>
            </div>
          </section>

          {/* 4단계 — 픽업 동일 설정 */}
          <section className="flex flex-col gap-2">
            <span className="font-medium text-sm">{STORE_PRICE_VERIFICATION_COPY.STEP_PICKUP_TITLE}</span>
            <div className="flex items-center gap-2">
              <Checkbox
                id="store-price-pickup-same"
                checked={applyPickupSamePrice}
                disabled={isPending || reviewInProgress}
                onCheckedChange={(checked) => setApplyPickupSamePrice(checked === true)}
              />
              <label htmlFor="store-price-pickup-same" className="text-sm">
                {STORE_PRICE_VERIFICATION_COPY.PICKUP_SAME_LABEL}
              </label>
            </div>
            <p className="text-muted-foreground text-xs">{STORE_PRICE_VERIFICATION_COPY.PICKUP_SAME_HELP}</p>
          </section>

          {/* 5단계 — 가격표 업로드 */}
          <section className="flex flex-col gap-2">
            <span className="font-medium text-sm">{STORE_PRICE_VERIFICATION_COPY.STEP_IMAGE_TITLE}</span>
            <ul className="flex flex-col gap-1">
              {STORE_PRICE_VERIFICATION_COPY.IMAGE_GUIDE.map((guide) => (
                <li key={guide} className="text-muted-foreground text-xs leading-snug">
                  {`· ${guide}`}
                </li>
              ))}
            </ul>
            <Input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png"
              disabled={isPending || reviewInProgress}
              aria-label={STORE_PRICE_VERIFICATION_COPY.IMAGE_SELECT}
              onChange={(event) => setFile(event.target.files?.[0] ?? null)}
            />
          </section>
        </div>

        <SheetFooter>
          <Button type="button" disabled={isPending || reviewInProgress} onClick={handleSubmit}>
            {isPending ? STORE_PRICE_VERIFICATION_COPY.ACTION_PENDING : STORE_PRICE_VERIFICATION_COPY.ACTION_SUBMIT}
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
