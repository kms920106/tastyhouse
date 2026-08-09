"use client";

import * as React from "react";

import { Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldDescription, FieldLabel } from "@/components/ui/field";
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
  createDeliveryAreaAction,
  deleteDeliveryAreaAction,
  getDeliveryAreasAction,
  searchAdminDongsAction,
} from "@/feature/shop/actions";
import { DELIVERY_AREA_MAX_COUNT } from "@/feature/shop/constants";
import type { AdminDong, ShopDeliveryArea } from "@/feature/shop/domain";
import { SHOP_MESSAGE, SHOP_OPERATION_COPY } from "@/feature/shop/message";

import { DeliveryAreaAdjustmentSheet } from "./delivery-area-adjustment-sheet";

interface DeliveryAreaSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  deliveryAreas: ShopDeliveryArea[];
}

/** 검색어 입력이 멈춘 뒤 조회하도록 하는 지연 시간(ms) */
const SEARCH_DEBOUNCE_MS = 300;

export function DeliveryAreaSheet({ open, onOpenChange, shopId, deliveryAreas }: DeliveryAreaSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [adjustmentOpen, setAdjustmentOpen] = React.useState(false);

  // props 는 시트가 열릴 때의 스냅샷이라, 등록/삭제 후에는 로컬 상태를 다시 조회해 목록을 갱신한다.
  const [areas, setAreas] = React.useState<ShopDeliveryArea[]>(deliveryAreas);
  const [keyword, setKeyword] = React.useState("");
  const [candidates, setCandidates] = React.useState<AdminDong[]>([]);
  const [isSearching, setIsSearching] = React.useState(false);

  React.useEffect(() => {
    if (open) {
      setAreas(deliveryAreas);
      setKeyword("");
      setCandidates([]);
    }
  }, [open, deliveryAreas]);

  // 검색어가 비면 후보를 비우고 요청도 보내지 않는다.
  React.useEffect(() => {
    if (!open) return;

    const trimmed = keyword.trim();
    if (!trimmed) {
      setCandidates([]);
      setIsSearching(false);
      return;
    }

    let active = true;
    setIsSearching(true);
    const timer = setTimeout(() => {
      void searchAdminDongsAction(trimmed).then(({ success, message, data }) => {
        if (!active) return;
        if (success) {
          setCandidates(data ?? []);
        } else {
          setCandidates([]);
          toast.error(message ?? SHOP_MESSAGE.ADMIN_DONG_SEARCH_FAILED);
        }
        setIsSearching(false);
      });
    }, SEARCH_DEBOUNCE_MS);

    return () => {
      active = false;
      clearTimeout(timer);
    };
  }, [open, keyword]);

  const reload = React.useCallback(() => {
    startTransition(async () => {
      const { success, data } = await getDeliveryAreasAction(shopId);
      if (success && data) setAreas(data);
    });
  }, [shopId]);

  const registeredAdminDongIds = areas.map((area) => area.adminDongId);
  const isMaxReached = areas.length >= DELIVERY_AREA_MAX_COUNT;

  function handleAdd(adminDongId: number) {
    if (isMaxReached) {
      toast.error(SHOP_MESSAGE.DELIVERY_AREA_MAX_REACHED);
      return;
    }

    startTransition(async () => {
      const { success, message } = await createDeliveryAreaAction(shopId, { adminDongId });
      if (success) {
        toast.success(SHOP_MESSAGE.DELIVERY_AREA_CREATE_SUCCESS);
        reload();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  // 지역별 배달팁이 걸린 행정동은 서버가 409 로 막는다. 클라이언트가 미리 판정하지 않고
  // 서버 메시지를 그대로 노출한다.
  function handleDelete(deliveryAreaId: number) {
    startTransition(async () => {
      const { success, message } = await deleteDeliveryAreaAction(deliveryAreaId);
      if (success) {
        toast.success(SHOP_MESSAGE.DELIVERY_AREA_DELETE_SUCCESS);
        reload();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <>
      <Sheet open={open} onOpenChange={onOpenChange}>
        <SheetContent className="flex w-full flex-col sm:max-w-lg">
          <SheetHeader>
            <SheetTitle>{SHOP_OPERATION_COPY.DELIVERY_AREA_TITLE}</SheetTitle>
            <SheetDescription>{SHOP_OPERATION_COPY.DELIVERY_AREA_DESCRIPTION}</SheetDescription>
          </SheetHeader>

          <div className="flex-1 space-y-6 overflow-y-auto px-4">
            {/* ===== 1. 등록된 배달가능지역 ===== */}
            <section className="space-y-3">
              <span className="font-medium text-sm">{SHOP_OPERATION_COPY.DELIVERY_AREA_LIST_LEGEND}</span>

              {areas.length > 0 ? (
                <ul className="space-y-2">
                  {areas.map((area) => (
                    <li key={area.id} className="flex items-center justify-between gap-3 rounded-md border p-3">
                      <span className="min-w-0 flex-1 truncate text-sm">{area.regionName}</span>
                      <Button
                        type="button"
                        size="icon"
                        variant="ghost"
                        onClick={() => handleDelete(area.id)}
                        disabled={isPending}
                        aria-label={`${area.regionName} 삭제`}
                      >
                        <Trash2 className="size-4" />
                      </Button>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="rounded-md border border-dashed p-4 text-center text-muted-foreground text-sm">
                  {SHOP_OPERATION_COPY.DELIVERY_AREA_LIST_EMPTY}
                </p>
              )}
            </section>

            <Separator />

            {/* ===== 2. 행정동 검색·추가 ===== */}
            <section className="space-y-3">
              <Field className="gap-1.5">
                <FieldLabel htmlFor="delivery-area-search">{SHOP_OPERATION_COPY.DELIVERY_AREA_SEARCH_LABEL}</FieldLabel>
                <Input
                  id="delivery-area-search"
                  value={keyword}
                  onChange={(event) => setKeyword(event.target.value)}
                  placeholder={SHOP_OPERATION_COPY.DELIVERY_AREA_SEARCH_PLACEHOLDER}
                  disabled={isPending}
                />
                <FieldDescription>{SHOP_OPERATION_COPY.DELIVERY_AREA_DESCRIPTION}</FieldDescription>
              </Field>

              {keyword.trim() &&
                (isSearching ? (
                  <p className="text-muted-foreground text-sm">검색 중...</p>
                ) : candidates.length > 0 ? (
                  <ul className="space-y-2">
                    {candidates.map((candidate) => {
                      const alreadyRegistered = registeredAdminDongIds.includes(candidate.id);
                      return (
                        <li
                          key={candidate.id}
                          className="flex items-center justify-between gap-3 rounded-md border p-3"
                        >
                          <span className="min-w-0 flex-1 truncate text-sm">{candidate.regionName}</span>
                          <Button
                            type="button"
                            size="sm"
                            variant="outline"
                            onClick={() => handleAdd(candidate.id)}
                            disabled={isPending || alreadyRegistered || isMaxReached}
                          >
                            <Plus className="size-4" />
                            {alreadyRegistered ? "등록됨" : "추가"}
                          </Button>
                        </li>
                      );
                    })}
                  </ul>
                ) : (
                  <p className="rounded-md border border-dashed p-4 text-center text-muted-foreground text-sm">
                    {SHOP_OPERATION_COPY.DELIVERY_AREA_SEARCH_EMPTY}
                  </p>
                ))}
            </section>

            <Separator />

            {/* ===== 3. 배달지역 조정 신청 진입 ===== */}
            <section className="flex flex-col gap-2">
              <span className="text-muted-foreground text-xs leading-snug">{SHOP_OPERATION_COPY.ADJUSTMENT_GUIDE}</span>
              <Button type="button" variant="outline" onClick={() => setAdjustmentOpen(true)} disabled={isPending}>
                {SHOP_OPERATION_COPY.ADJUSTMENT_TITLE}
              </Button>
            </section>
          </div>

          <SheetFooter>
            <SheetClose asChild>
              <Button variant="outline" disabled={isPending}>
                닫기
              </Button>
            </SheetClose>
          </SheetFooter>
        </SheetContent>
      </Sheet>

      <DeliveryAreaAdjustmentSheet open={adjustmentOpen} onOpenChange={setAdjustmentOpen} shopId={shopId} />
    </>
  );
}
