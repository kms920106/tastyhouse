"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
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
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import {
  createAmenityAction,
  deleteAmenityAction,
  fetchAmenityCategoriesAction,
  updateConvenienceInfoAction,
} from "@/feature/shop/actions";
import { SHOP_DIRECTIONS_MAX } from "@/feature/shop/constants";
import type { AmenityCategory, ShopAmenity, ShopConvenienceInfo } from "@/feature/shop/domain";
import { SHOP_BASIC_COPY, SHOP_MESSAGE } from "@/feature/shop/message";
import { type ConvenienceInfoFormValues, convenienceInfoSchema } from "@/feature/shop/schema";

const DIRECTIONS_GUIDE = [
  "가게 주변의 눈에 잘 띄는 건물이나 지형을 기준으로 설명해 주세요.",
  "지하철 출구 번호, 버스 정류장 이름처럼 확인 가능한 정보를 사용해 주세요.",
  "전화번호·URL·홍보 문구는 등록할 수 없습니다.",
];

interface ConveniencesSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  convenienceInfo: ShopConvenienceInfo;
  amenities: ShopAmenity[];
  roadAddress: string;
}

export function ConveniencesSheet({
  open,
  onOpenChange,
  shopId,
  convenienceInfo,
  amenities,
  roadAddress,
}: ConveniencesSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  // 편의시설은 개별 create/delete 로 즉시 반영되므로 폼과 분리해 로컬 상태로 관리한다.
  const [assignedCategoryIds, setAssignedCategoryIds] = React.useState<number[]>([]);
  const [categories, setCategories] = React.useState<AmenityCategory[]>([]);

  const form = useForm<ConvenienceInfoFormValues>({
    resolver: zodResolver(convenienceInfoSchema),
    defaultValues: {
      parkingAvailable: false,
      parkingPaid: false,
      valetAvailable: false,
      valetPaid: false,
      directionsGuide: "",
      displayLatitude: 0,
      displayLongitude: 0,
    },
  });

  React.useEffect(() => {
    if (!open) return;

    form.reset(convenienceInfo);
    setAssignedCategoryIds(amenities.map((item) => item.amenityCategoryId));

    // 편의시설 카탈로그는 서버가 관리하는 동적 목록이라 시트를 열 때마다 조회한다.
    startTransition(async () => {
      const { success, data } = await fetchAmenityCategoriesAction();
      if (success && data) setCategories(data);
    });
  }, [open, convenienceInfo, amenities, form]);

  const directionsLength = form.watch("directionsGuide").length;

  const onSubmit = (values: ConvenienceInfoFormValues) => {
    startTransition(async () => {
      const { success, message } = await updateConvenienceInfoAction(shopId, values);
      if (success) {
        toast.success(SHOP_MESSAGE.CONVENIENCE_UPDATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  function handleAmenityToggle(amenityCategoryId: number, checked: boolean) {
    startTransition(async () => {
      const { success, message } = checked
        ? await createAmenityAction(shopId, amenityCategoryId)
        : await deleteAmenityAction(shopId, amenityCategoryId);

      if (!success) {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
        return;
      }

      setAssignedCategoryIds((previous) =>
        checked ? [...previous, amenityCategoryId] : previous.filter((id) => id !== amenityCategoryId),
      );
      toast.success(checked ? SHOP_MESSAGE.AMENITY_CREATE_SUCCESS : SHOP_MESSAGE.AMENITY_DELETE_SUCCESS);
    });
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{SHOP_BASIC_COPY.CONVENIENCE_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_BASIC_COPY.CONVENIENCE_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-5 overflow-y-auto px-4">
          <form id="shop-convenience-form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
            <FieldGroup className="gap-5">
              <Controller
                control={form.control}
                name="parkingAvailable"
                render={({ field }) => (
                  <Field orientation="horizontal">
                    <FieldLabel htmlFor="parking-available">{SHOP_BASIC_COPY.PARKING_AVAILABLE}</FieldLabel>
                    <Switch
                      id="parking-available"
                      checked={field.value}
                      disabled={isPending}
                      onCheckedChange={(checked) => {
                        field.onChange(checked);
                        // 주차가 불가하면 유료 여부는 의미가 없으므로 함께 내린다.
                        if (!checked) form.setValue("parkingPaid", false);
                      }}
                    />
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="parkingPaid"
                render={({ field }) => (
                  <Field orientation="horizontal">
                    <FieldLabel htmlFor="parking-paid">{SHOP_BASIC_COPY.PARKING_PAID}</FieldLabel>
                    <Switch
                      id="parking-paid"
                      checked={field.value}
                      disabled={isPending || !form.watch("parkingAvailable")}
                      onCheckedChange={field.onChange}
                    />
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="valetAvailable"
                render={({ field }) => (
                  <Field orientation="horizontal">
                    <FieldLabel htmlFor="valet-available">{SHOP_BASIC_COPY.VALET_AVAILABLE}</FieldLabel>
                    <Switch
                      id="valet-available"
                      checked={field.value}
                      disabled={isPending}
                      onCheckedChange={(checked) => {
                        field.onChange(checked);
                        if (!checked) form.setValue("valetPaid", false);
                      }}
                    />
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="valetPaid"
                render={({ field }) => (
                  <Field orientation="horizontal">
                    <FieldLabel htmlFor="valet-paid">{SHOP_BASIC_COPY.VALET_PAID}</FieldLabel>
                    <Switch
                      id="valet-paid"
                      checked={field.value}
                      disabled={isPending || !form.watch("valetAvailable")}
                      onCheckedChange={field.onChange}
                    />
                  </Field>
                )}
              />

              <Separator />

              <Controller
                control={form.control}
                name="directionsGuide"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="shop-directions">찾아오는 길 안내</FieldLabel>
                    <Textarea
                      {...field}
                      id="shop-directions"
                      placeholder="예) 2호선 강남역 11번 출구에서 도보 3분, 편의점 옆 건물 2층"
                      maxLength={SHOP_DIRECTIONS_MAX}
                      rows={6}
                      disabled={isPending}
                      aria-invalid={fieldState.invalid}
                    />
                    <span className="text-muted-foreground text-xs">
                      {directionsLength} / {SHOP_DIRECTIONS_MAX}
                    </span>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Accordion type="single" collapsible>
                <AccordionItem value="directions-guide">
                  <AccordionTrigger className="text-sm">등록 기준 안내</AccordionTrigger>
                  <AccordionContent>
                    <ul className="list-disc space-y-1 pl-4 text-muted-foreground text-xs">
                      {DIRECTIONS_GUIDE.map((guide) => (
                        <li key={guide}>{guide}</li>
                      ))}
                    </ul>
                  </AccordionContent>
                </AccordionItem>
              </Accordion>

              <Separator />

              <FieldSet>
                <FieldLegend variant="label">노출 위치</FieldLegend>
                <p className="text-muted-foreground text-xs leading-snug">
                  지도에 표시되는 핀 위치만 조정됩니다. 현재 등록 주소: {roadAddress}
                </p>
                <div className="mt-2 flex flex-col gap-4">
                  <Controller
                    control={form.control}
                    name="displayLatitude"
                    render={({ field, fieldState }) => (
                      <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                        <FieldLabel htmlFor="display-latitude">위도</FieldLabel>
                        <Input
                          id="display-latitude"
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
                    name="displayLongitude"
                    render={({ field, fieldState }) => (
                      <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                        <FieldLabel htmlFor="display-longitude">경도</FieldLabel>
                        <Input
                          id="display-longitude"
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
                </div>
              </FieldSet>
            </FieldGroup>
          </form>

          <Separator />

          <FieldSet>
            <FieldLegend variant="label">{SHOP_BASIC_COPY.AMENITY_LEGEND}</FieldLegend>
            {categories.length > 0 ? (
              <div className="flex flex-col gap-3">
                {categories.map((category) => (
                  <Field key={category.id} orientation="horizontal" className="gap-3">
                    <Checkbox
                      id={`amenity-${category.id}`}
                      checked={assignedCategoryIds.includes(category.id)}
                      disabled={isPending}
                      onCheckedChange={(checked) => handleAmenityToggle(category.id, checked === true)}
                    />
                    <FieldLabel htmlFor={`amenity-${category.id}`} className="font-normal">
                      {category.displayName}
                    </FieldLabel>
                  </Field>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground text-sm">{SHOP_BASIC_COPY.AMENITY_EMPTY}</p>
            )}
          </FieldSet>
        </div>

        <SheetFooter>
          <Button type="submit" form="shop-convenience-form" disabled={isPending}>
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
