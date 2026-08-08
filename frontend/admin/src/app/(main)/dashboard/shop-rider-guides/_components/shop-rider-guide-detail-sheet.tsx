"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
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
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldGroup, FieldLabel, FieldLegend, FieldSet } from "@/components/ui/field";
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
import { Skeleton } from "@/components/ui/skeleton";
import { Textarea } from "@/components/ui/textarea";
import {
  deleteShopRiderVisitGuideAction,
  fetchShopRiderGuideDetailAction,
  requestShopRiderVisitGuideRevisionAction,
  updateShopRiderPickupLocationAction,
} from "@/feature/shop/actions";
import { RIDER_GUIDE_ACTION_TYPE_LABEL, SHOP_RIDER_GUIDE_REASON_MAX } from "@/feature/shop/constants";
import type { RiderGuideActionType, ShopRiderGuideDetail } from "@/feature/shop/domain";
import { SHOP_MESSAGE, SHOP_RIDER_GUIDE_ADMIN_COPY, SHOP_RIDER_GUIDE_MESSAGE } from "@/feature/shop/message";
import {
  type RiderGuideReasonFormValues,
  type RiderPickupLocationFormValues,
  riderGuideReasonSchema,
  riderPickupLocationSchema,
} from "@/feature/shop/schema";
import { formatDateTime } from "@/lib/date";

const ACTION_TYPE_BADGE_VARIANT: Record<RiderGuideActionType, "secondary" | "outline" | "destructive"> = {
  UPDATE: "secondary",
  REVISION_REQUEST: "outline",
  DELETION: "destructive",
};

/** 확인 다이얼로그가 뜬 조치. null 이면 닫힌 상태 */
type PendingAction = "REVISION_REQUEST" | "DELETION" | null;

interface Props {
  /** null 이면 시트가 닫힌 상태 */
  shopId: number | null;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

export function ShopRiderGuideDetailSheet({ shopId, onOpenChange, onSuccess }: Props) {
  const [isPending, startTransition] = React.useTransition();
  const [detail, setDetail] = React.useState<ShopRiderGuideDetail | null>(null);
  const [pendingAction, setPendingAction] = React.useState<PendingAction>(null);
  const [pickupEditing, setPickupEditing] = React.useState(false);

  const open = shopId !== null;

  const reasonForm = useForm<RiderGuideReasonFormValues>({
    resolver: zodResolver(riderGuideReasonSchema),
    defaultValues: { reason: "" },
  });

  const pickupForm = useForm<RiderPickupLocationFormValues>({
    resolver: zodResolver(riderPickupLocationSchema),
    defaultValues: { roadAddress: "", lotAddress: "", detailAddress: "", latitude: 0, longitude: 0 },
  });

  // 상세(문구·픽업 위치·변경 이력)는 목록에 없는 값이라 시트를 열 때마다 조회한다.
  React.useEffect(() => {
    if (shopId === null) {
      setDetail(null);
      setPickupEditing(false);
      setPendingAction(null);
      return;
    }

    reasonForm.reset({ reason: "" });

    startTransition(async () => {
      const { success, message, data } = await fetchShopRiderGuideDetailAction(shopId);
      if (!success || !data) {
        toast.error(message ?? SHOP_RIDER_GUIDE_MESSAGE.DETAIL_LOAD_FAILED);
        return;
      }
      setDetail(data);
      pickupForm.reset({
        roadAddress: data.pickupLocation?.roadAddress ?? data.shopRoadAddress,
        lotAddress: data.pickupLocation?.lotAddress ?? "",
        detailAddress: data.pickupLocation?.detailAddress ?? "",
        latitude: data.pickupLocation?.latitude ?? 0,
        longitude: data.pickupLocation?.longitude ?? 0,
      });
    });
  }, [shopId, reasonForm, pickupForm]);

  const reasonValue = reasonForm.watch("reason");
  // zodResolver 로도 막지만, 버튼 비활성이 '사유 필수'라는 의도를 더 분명히 보여준다.
  const isReasonEmpty = reasonValue.trim().length === 0;
  const hasVisitGuide = detail?.visitGuide !== null && detail?.visitGuide !== undefined;

  function handleConfirmAction() {
    if (shopId === null || pendingAction === null) return;
    const action = pendingAction;

    startTransition(async () => {
      const values = reasonForm.getValues();
      const { success, message } =
        action === "DELETION"
          ? await deleteShopRiderVisitGuideAction(shopId, values)
          : await requestShopRiderVisitGuideRevisionAction(shopId, values);

      setPendingAction(null);

      if (!success) {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
        return;
      }

      toast.success(
        action === "DELETION"
          ? SHOP_RIDER_GUIDE_MESSAGE.DELETE_SUCCESS
          : SHOP_RIDER_GUIDE_MESSAGE.REVISION_REQUEST_SUCCESS,
      );
      onSuccess();
      onOpenChange(false);
    });
  }

  const onPickupSubmit = (values: RiderPickupLocationFormValues) => {
    if (shopId === null) return;

    startTransition(async () => {
      const { success, message } = await updateShopRiderPickupLocationAction(shopId, values);
      if (!success) {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
        return;
      }

      toast.success(SHOP_RIDER_GUIDE_MESSAGE.PICKUP_UPDATE_SUCCESS);
      setPickupEditing(false);
      onSuccess();
      onOpenChange(false);
    });
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>{detail?.shopName ?? SHOP_RIDER_GUIDE_ADMIN_COPY.PAGE_TITLE}</SheetTitle>
          <SheetDescription>{detail?.shopRoadAddress ?? SHOP_RIDER_GUIDE_ADMIN_COPY.PAGE_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-5 overflow-y-auto px-4">
          {detail === null ? (
            <div className="space-y-3 pt-2">
              <Skeleton className="h-5 w-32" />
              <Skeleton className="h-20 w-full" />
              <Skeleton className="h-5 w-32" />
              <Skeleton className="h-16 w-full" />
            </div>
          ) : (
            <>
              <FieldSet>
                <FieldLegend variant="label">{SHOP_RIDER_GUIDE_ADMIN_COPY.DETAIL_VISIT_GUIDE_LEGEND}</FieldLegend>
                {hasVisitGuide ? (
                  <p className="whitespace-pre-wrap rounded-md border p-3 text-sm leading-relaxed">
                    {detail.visitGuide}
                  </p>
                ) : (
                  <p className="text-muted-foreground text-sm">{SHOP_RIDER_GUIDE_ADMIN_COPY.VISIT_GUIDE_EMPTY}</p>
                )}
              </FieldSet>

              <Separator />

              <FieldSet>
                <FieldLegend variant="label">{SHOP_RIDER_GUIDE_ADMIN_COPY.DETAIL_PICKUP_LEGEND}</FieldLegend>
                {pickupEditing ? (
                  <form
                    id="shop-rider-pickup-location-form"
                    noValidate
                    onSubmit={pickupForm.handleSubmit(onPickupSubmit)}
                  >
                    <p className="mb-3 text-muted-foreground text-xs leading-snug">
                      {SHOP_RIDER_GUIDE_ADMIN_COPY.PICKUP_EDIT_GUIDE}
                    </p>
                    <FieldGroup className="gap-3">
                      <Controller
                        control={pickupForm.control}
                        name="roadAddress"
                        render={({ field, fieldState }) => (
                          <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor="admin-pickup-road-address">
                              {SHOP_RIDER_GUIDE_ADMIN_COPY.PICKUP_ROAD_ADDRESS_LABEL}
                            </FieldLabel>
                            <Input
                              {...field}
                              id="admin-pickup-road-address"
                              disabled={isPending}
                              aria-invalid={fieldState.invalid}
                            />
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                          </Field>
                        )}
                      />
                      <Controller
                        control={pickupForm.control}
                        name="lotAddress"
                        render={({ field, fieldState }) => (
                          <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor="admin-pickup-lot-address">
                              {SHOP_RIDER_GUIDE_ADMIN_COPY.PICKUP_LOT_ADDRESS_LABEL}
                            </FieldLabel>
                            <Input
                              {...field}
                              id="admin-pickup-lot-address"
                              disabled={isPending}
                              aria-invalid={fieldState.invalid}
                            />
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                          </Field>
                        )}
                      />
                      <Controller
                        control={pickupForm.control}
                        name="detailAddress"
                        render={({ field, fieldState }) => (
                          <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor="admin-pickup-detail-address">
                              {SHOP_RIDER_GUIDE_ADMIN_COPY.PICKUP_DETAIL_ADDRESS_LABEL}
                            </FieldLabel>
                            <Input
                              {...field}
                              id="admin-pickup-detail-address"
                              disabled={isPending}
                              aria-invalid={fieldState.invalid}
                            />
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                          </Field>
                        )}
                      />
                      <Controller
                        control={pickupForm.control}
                        name="latitude"
                        render={({ field, fieldState }) => (
                          <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor="admin-pickup-latitude">
                              {SHOP_RIDER_GUIDE_ADMIN_COPY.PICKUP_LATITUDE_LABEL}
                            </FieldLabel>
                            <Input
                              id="admin-pickup-latitude"
                              type="number"
                              step="0.000001"
                              // valueAsNumber 는 빈 입력에서 NaN 을 주므로, 표시값은 NaN 을 빈 문자열로 되돌린다.
                              value={Number.isNaN(field.value) ? "" : field.value}
                              onChange={(event) => field.onChange(event.target.valueAsNumber)}
                              onBlur={field.onBlur}
                              disabled={isPending}
                              aria-invalid={fieldState.invalid}
                            />
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                          </Field>
                        )}
                      />
                      <Controller
                        control={pickupForm.control}
                        name="longitude"
                        render={({ field, fieldState }) => (
                          <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor="admin-pickup-longitude">
                              {SHOP_RIDER_GUIDE_ADMIN_COPY.PICKUP_LONGITUDE_LABEL}
                            </FieldLabel>
                            <Input
                              id="admin-pickup-longitude"
                              type="number"
                              step="0.000001"
                              value={Number.isNaN(field.value) ? "" : field.value}
                              onChange={(event) => field.onChange(event.target.valueAsNumber)}
                              onBlur={field.onBlur}
                              disabled={isPending}
                              aria-invalid={fieldState.invalid}
                            />
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                          </Field>
                        )}
                      />

                      <div className="flex gap-2">
                        <Button type="submit" size="sm" disabled={isPending}>
                          {isPending ? "저장 중..." : "적용"}
                        </Button>
                        <Button
                          type="button"
                          size="sm"
                          variant="outline"
                          disabled={isPending}
                          onClick={() => setPickupEditing(false)}
                        >
                          취소
                        </Button>
                      </div>
                    </FieldGroup>
                  </form>
                ) : (
                  <div className="flex flex-col items-start gap-2">
                    {detail.pickupLocation ? (
                      <div className="text-sm leading-relaxed">
                        <p>
                          {[detail.pickupLocation.roadAddress, detail.pickupLocation.detailAddress]
                            .filter(Boolean)
                            .join(" ")}
                        </p>
                        <p className="text-muted-foreground text-xs tabular-nums">
                          {detail.pickupLocation.latitude}, {detail.pickupLocation.longitude}
                        </p>
                      </div>
                    ) : (
                      <p className="text-muted-foreground text-sm">
                        {SHOP_RIDER_GUIDE_ADMIN_COPY.PICKUP_FALLBACK_LABEL} · {detail.shopRoadAddress}
                      </p>
                    )}
                    <Button type="button" size="sm" variant="outline" onClick={() => setPickupEditing(true)}>
                      {SHOP_RIDER_GUIDE_ADMIN_COPY.PICKUP_EDIT_ACTION}
                    </Button>
                  </div>
                )}
              </FieldSet>

              <Separator />

              <FieldSet>
                <FieldLegend variant="label">{SHOP_RIDER_GUIDE_ADMIN_COPY.DETAIL_HISTORY_LEGEND}</FieldLegend>
                {detail.histories.length > 0 ? (
                  <ul className="flex flex-col gap-3">
                    {detail.histories.map((history) => (
                      <li key={history.id} className="rounded-md border p-3 text-sm">
                        <div className="flex items-center justify-between gap-2">
                          <Badge variant={ACTION_TYPE_BADGE_VARIANT[history.actionType]}>
                            {RIDER_GUIDE_ACTION_TYPE_LABEL[history.actionType]}
                          </Badge>
                          <span className="text-muted-foreground text-xs tabular-nums">
                            {formatDateTime(history.createdAt)}
                          </span>
                        </div>
                        {history.previousVisitGuide !== null && (
                          <p className="mt-2 whitespace-pre-wrap text-muted-foreground text-xs leading-snug">
                            변경 전: {history.previousVisitGuide}
                          </p>
                        )}
                        {history.newVisitGuide !== null && (
                          <p className="mt-1 whitespace-pre-wrap text-xs leading-snug">
                            변경 후: {history.newVisitGuide}
                          </p>
                        )}
                        {history.reason !== null && (
                          <p className="mt-1 whitespace-pre-wrap text-xs leading-snug">사유: {history.reason}</p>
                        )}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="text-muted-foreground text-sm">{SHOP_RIDER_GUIDE_ADMIN_COPY.HISTORY_EMPTY}</p>
                )}
              </FieldSet>

              <Separator />

              <Controller
                control={reasonForm.control}
                name="reason"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="rider-guide-reason">
                      {SHOP_RIDER_GUIDE_ADMIN_COPY.REVISION_REQUEST_REASON_LABEL}
                    </FieldLabel>
                    <Textarea
                      {...field}
                      id="rider-guide-reason"
                      placeholder={SHOP_RIDER_GUIDE_ADMIN_COPY.REVISION_REQUEST_REASON_PLACEHOLDER}
                      maxLength={SHOP_RIDER_GUIDE_REASON_MAX}
                      rows={3}
                      disabled={isPending}
                      aria-invalid={fieldState.invalid}
                    />
                    <span className="text-muted-foreground text-xs">
                      {reasonValue.length} / {SHOP_RIDER_GUIDE_REASON_MAX}
                    </span>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />
            </>
          )}
        </div>

        <SheetFooter>
          <div className="flex gap-2">
            <Button
              type="button"
              variant="outline"
              disabled={isPending || detail === null || isReasonEmpty}
              onClick={() => setPendingAction("REVISION_REQUEST")}
            >
              {SHOP_RIDER_GUIDE_ADMIN_COPY.REVISION_REQUEST_ACTION}
            </Button>
            <Button
              type="button"
              variant="destructive"
              disabled={isPending || detail === null || isReasonEmpty || !hasVisitGuide}
              onClick={() => setPendingAction("DELETION")}
            >
              {SHOP_RIDER_GUIDE_ADMIN_COPY.DELETE_ACTION}
            </Button>
          </div>
          <SheetClose asChild>
            <Button variant="ghost" disabled={isPending}>
              닫기
            </Button>
          </SheetClose>
        </SheetFooter>

        <AlertDialog open={pendingAction !== null} onOpenChange={(next) => !next && setPendingAction(null)}>
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>
                {pendingAction === "DELETION"
                  ? SHOP_RIDER_GUIDE_ADMIN_COPY.DELETE_CONFIRM_TITLE
                  : SHOP_RIDER_GUIDE_ADMIN_COPY.REVISION_REQUEST_CONFIRM_TITLE}
              </AlertDialogTitle>
              <AlertDialogDescription>
                {pendingAction === "DELETION"
                  ? SHOP_RIDER_GUIDE_ADMIN_COPY.DELETE_CONFIRM_DESCRIPTION
                  : SHOP_RIDER_GUIDE_ADMIN_COPY.REVISION_REQUEST_CONFIRM_DESCRIPTION}
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
              <AlertDialogAction disabled={isPending} onClick={handleConfirmAction}>
                확인
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </SheetContent>
    </Sheet>
  );
}
