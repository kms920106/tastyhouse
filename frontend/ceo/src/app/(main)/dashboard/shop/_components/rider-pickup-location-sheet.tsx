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
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
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
import { clearShopRiderPickupLocationAction, updateShopRiderPickupLocationAction } from "@/feature/shop/actions";
import { SHOP_RIDER_PICKUP_DETAIL_ADDRESS_MAX } from "@/feature/shop/constants";
import type { ShopRiderGuide } from "@/feature/shop/domain";
import { SHOP_MESSAGE, SHOP_RIDER_COPY } from "@/feature/shop/message";
import { type ShopRiderPickupLocationFormValues, shopRiderPickupLocationSchema } from "@/feature/shop/schema";

interface RiderPickupLocationSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  riderGuide: ShopRiderGuide;
}

export function RiderPickupLocationSheet({ open, onOpenChange, shopId, riderGuide }: RiderPickupLocationSheetProps) {
  const [isPending, startTransition] = React.useTransition();

  const { pickupLocation, shopLatitude, shopLongitude, shopLotAddress, shopRoadAddress } = riderGuide;
  const hasPickupLocation = pickupLocation !== null;

  const form = useForm<ShopRiderPickupLocationFormValues>({
    resolver: zodResolver(shopRiderPickupLocationSchema),
    defaultValues: {
      roadAddress: "",
      lotAddress: "",
      detailAddress: "",
      latitude: 0,
      longitude: 0,
    },
  });

  React.useEffect(() => {
    if (!open) return;

    // 미설정 상태에서는 가게 실주소·실좌표를 초기값으로 채워, 점주가 그 자리에서 미세 조정만 하면 되게 한다.
    form.reset({
      roadAddress: pickupLocation?.roadAddress ?? shopRoadAddress,
      lotAddress: pickupLocation?.lotAddress ?? shopLotAddress ?? "",
      detailAddress: pickupLocation?.detailAddress ?? "",
      latitude: pickupLocation?.latitude ?? shopLatitude,
      longitude: pickupLocation?.longitude ?? shopLongitude,
    });
  }, [open, pickupLocation, shopRoadAddress, shopLotAddress, shopLatitude, shopLongitude, form]);

  const onSubmit = (values: ShopRiderPickupLocationFormValues) => {
    startTransition(async () => {
      const { success, message } = await updateShopRiderPickupLocationAction(shopId, values);
      if (success) {
        toast.success(SHOP_MESSAGE.RIDER_PICKUP_LOCATION_UPDATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  // 실주소 값을 픽업 위치로 '복사'해 저장한다 — 초기화와 달리, 이후 가게 주소가 바뀌어도 이 값은 유지된다.
  function handleCopyShopAddress() {
    form.setValue("roadAddress", shopRoadAddress, { shouldValidate: true });
    form.setValue("lotAddress", shopLotAddress ?? "");
    // 가게 실주소에는 픽업 동선 정보가 없으므로 상세주소는 비운다.
    form.setValue("detailAddress", "");
    form.setValue("latitude", shopLatitude, { shouldValidate: true });
    form.setValue("longitude", shopLongitude, { shouldValidate: true });
  }

  function handleClear() {
    startTransition(async () => {
      const { success, message } = await clearShopRiderPickupLocationAction(shopId);
      if (success) {
        toast.success(SHOP_MESSAGE.RIDER_PICKUP_LOCATION_CLEAR_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{SHOP_RIDER_COPY.PICKUP_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_RIDER_COPY.PICKUP_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <form
          id="shop-rider-pickup-location-form"
          noValidate
          onSubmit={form.handleSubmit(onSubmit)}
          className="flex-1 overflow-y-auto px-4"
        >
          <FieldGroup className="gap-4">
            <div className="flex flex-col gap-2">
              <p className="text-muted-foreground text-xs leading-snug">
                {SHOP_RIDER_COPY.PICKUP_SHOP_ADDRESS_PREFIX} {shopRoadAddress || "-"}
              </p>
              <Button
                type="button"
                variant="outline"
                size="sm"
                className="self-start"
                disabled={isPending || shopRoadAddress.length === 0}
                onClick={handleCopyShopAddress}
              >
                {SHOP_RIDER_COPY.PICKUP_COPY_SHOP_ADDRESS}
              </Button>
              <FieldDescription>{SHOP_RIDER_COPY.PICKUP_COPY_GUIDE}</FieldDescription>
            </div>

            <Separator />

            <Controller
              control={form.control}
              name="roadAddress"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="pickup-road-address">{SHOP_RIDER_COPY.PICKUP_ROAD_ADDRESS_LABEL}</FieldLabel>
                  <Input {...field} id="pickup-road-address" disabled={isPending} aria-invalid={fieldState.invalid} />
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="lotAddress"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="pickup-lot-address">{SHOP_RIDER_COPY.PICKUP_LOT_ADDRESS_LABEL}</FieldLabel>
                  <Input {...field} id="pickup-lot-address" disabled={isPending} aria-invalid={fieldState.invalid} />
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="detailAddress"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="pickup-detail-address">{SHOP_RIDER_COPY.PICKUP_DETAIL_ADDRESS_LABEL}</FieldLabel>
                  <Input
                    {...field}
                    id="pickup-detail-address"
                    placeholder={SHOP_RIDER_COPY.PICKUP_DETAIL_ADDRESS_PLACEHOLDER}
                    maxLength={SHOP_RIDER_PICKUP_DETAIL_ADDRESS_MAX}
                    disabled={isPending}
                    aria-invalid={fieldState.invalid}
                  />
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />

            <Controller
              control={form.control}
              name="latitude"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="pickup-latitude">{SHOP_RIDER_COPY.PICKUP_LATITUDE_LABEL}</FieldLabel>
                  <Input
                    id="pickup-latitude"
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
              control={form.control}
              name="longitude"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="pickup-longitude">{SHOP_RIDER_COPY.PICKUP_LONGITUDE_LABEL}</FieldLabel>
                  <Input
                    id="pickup-longitude"
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

            <Separator />

            <div className="flex flex-col gap-1">
              <AlertDialog>
                <AlertDialogTrigger asChild>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    className="self-start"
                    disabled={isPending || !hasPickupLocation}
                  >
                    {SHOP_RIDER_COPY.PICKUP_CLEAR_ACTION}
                  </Button>
                </AlertDialogTrigger>
                <AlertDialogContent>
                  <AlertDialogHeader>
                    <AlertDialogTitle>{SHOP_RIDER_COPY.PICKUP_CLEAR_CONFIRM_TITLE}</AlertDialogTitle>
                    <AlertDialogDescription>{SHOP_RIDER_COPY.PICKUP_CLEAR_CONFIRM_DESCRIPTION}</AlertDialogDescription>
                  </AlertDialogHeader>
                  <AlertDialogFooter>
                    <AlertDialogCancel>취소</AlertDialogCancel>
                    <AlertDialogAction onClick={handleClear}>{SHOP_RIDER_COPY.PICKUP_CLEAR_ACTION}</AlertDialogAction>
                  </AlertDialogFooter>
                </AlertDialogContent>
              </AlertDialog>
            </div>
          </FieldGroup>
        </form>

        <SheetFooter>
          <Button type="submit" form="shop-rider-pickup-location-form" disabled={isPending}>
            {isPending ? "저장 중..." : "적용"}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={isPending}>
              취소
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
