"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE_BYTES } from "@/api/file/file.dto";
import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
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
import {
  createShopAction,
  fetchCeosAction,
  fetchShopAction,
  fetchStationsAction,
  updateShopAction,
  uploadShopImageAction,
} from "@/feature/shop/actions";
import { ADDRESS_MAX, CEO_STATUS_LABEL, SHOP_NAME_MAX } from "@/feature/shop/constants";
import type { Ceo, ShopListItem, Station } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import { type ShopFormValues, shopFormSchema } from "@/feature/shop/schema";

interface ShopFormSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shop?: Pick<ShopListItem, "id"> | null;
}

const EMPTY_VALUES: ShopFormValues = {
  stationId: undefined as unknown as number,
  name: "",
  latitude: undefined as unknown as number,
  longitude: undefined as unknown as number,
  roadAddress: "",
  lotAddress: "",
  phoneNumber: undefined,
  thumbnailImageFileId: undefined,
  ceoId: undefined,
};

/** 숫자 input onChange: 빈 값이면 undefined, 아니면 Number */
function parseOptionalNumber(value: string): number | undefined {
  return value.trim() === "" ? undefined : Number(value);
}

export function ShopFormSheet({ open, onOpenChange, shop }: ShopFormSheetProps) {
  const isEdit = Boolean(shop);
  const [isPending, startTransition] = React.useTransition();
  const [isLoadingDetail, setIsLoadingDetail] = React.useState(false);
  const [isUploading, setIsUploading] = React.useState(false);
  const [previewUrl, setPreviewUrl] = React.useState<string | undefined>(undefined);
  const [stations, setStations] = React.useState<Station[]>([]);
  const [ceos, setCeos] = React.useState<Ceo[]>([]);

  const form = useForm<ShopFormValues>({
    resolver: zodResolver(shopFormSchema),
    defaultValues: EMPTY_VALUES,
  });

  const resetPreview = React.useCallback(() => {
    setPreviewUrl((prev) => {
      if (prev?.startsWith("blob:")) URL.revokeObjectURL(prev);
      return undefined;
    });
  }, []);

  React.useEffect(() => {
    if (!open) return;

    if (!shop) {
      form.reset(EMPTY_VALUES);
      resetPreview();
      return;
    }

    let active = true;
    setIsLoadingDetail(true);

    void fetchShopAction(shop.id).then((result) => {
      if (!active) return;
      setIsLoadingDetail(false);

      if (!result.success || !result.data) {
        toast.error(result.message ?? SHOP_MESSAGE.DETAIL_LOAD_FAILED);
        onOpenChange(false);
        return;
      }

      const detail = result.data;
      form.reset({
        stationId: detail.stationId,
        name: detail.name,
        latitude: detail.latitude,
        longitude: detail.longitude,
        roadAddress: detail.roadAddress,
        lotAddress: detail.lotAddress,
        phoneNumber: detail.phoneNumber ?? undefined,
        thumbnailImageFileId: undefined,
      });
      setPreviewUrl(detail.thumbnailImageUrl ?? undefined);
    });

    return () => {
      active = false;
    };
  }, [open, shop, form.reset, onOpenChange, resetPreview]);

  // 시트가 열릴 때 지하철역 목록 로드 (드롭다운용)
  React.useEffect(() => {
    if (!open || stations.length > 0) return;

    let active = true;
    void fetchStationsAction().then((result) => {
      if (!active) return;
      if (result.success && result.data) {
        setStations(result.data);
      } else {
        toast.error(result.message ?? SHOP_MESSAGE.STATIONS_LOAD_FAILED);
      }
    });

    return () => {
      active = false;
    };
  }, [open, stations.length]);

  // 시트가 열릴 때(등록 모드에서만) 점주 목록 로드 (드롭다운용)
  React.useEffect(() => {
    if (!open || isEdit || ceos.length > 0) return;

    let active = true;
    void fetchCeosAction().then((result) => {
      if (!active) return;
      if (result.success && result.data) {
        setCeos(result.data);
      } else {
        toast.error(result.message ?? SHOP_MESSAGE.CEOS_LOAD_FAILED);
      }
    });

    return () => {
      active = false;
    };
  }, [open, isEdit, ceos.length]);

  // 언마운트 시 objectURL 해제
  React.useEffect(() => {
    return () => {
      setPreviewUrl((prev) => {
        if (prev?.startsWith("blob:")) URL.revokeObjectURL(prev);
        return undefined;
      });
    };
  }, []);

  async function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!(ALLOWED_IMAGE_TYPES as readonly string[]).includes(file.type)) {
      toast.error(SHOP_MESSAGE.IMAGE_TYPE_INVALID);
      event.target.value = "";
      return;
    }
    if (file.size > MAX_IMAGE_SIZE_BYTES) {
      toast.error(SHOP_MESSAGE.IMAGE_SIZE_EXCEEDED);
      event.target.value = "";
      return;
    }

    setIsUploading(true);
    const formData = new FormData();
    formData.append("file", file);

    const result = await uploadShopImageAction(formData);
    setIsUploading(false);
    event.target.value = "";

    if (!result.success || result.fileId === undefined) {
      toast.error(result.message ?? SHOP_MESSAGE.IMAGE_UPLOAD_FAILED);
      return;
    }

    form.setValue("thumbnailImageFileId", result.fileId, { shouldValidate: true });
    setPreviewUrl((prev) => {
      if (prev?.startsWith("blob:")) URL.revokeObjectURL(prev);
      return URL.createObjectURL(file);
    });
  }

  const onSubmit = (values: ShopFormValues) => {
    startTransition(async () => {
      const { success, message } = shop ? await updateShopAction(shop.id, values) : await createShopAction(values);

      if (success) {
        toast.success(isEdit ? SHOP_MESSAGE.UPDATE_SUCCESS : SHOP_MESSAGE.CREATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  const busy = Boolean(isPending) || Boolean(isLoadingDetail) || Boolean(isUploading);

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{isEdit ? "가게 수정" : "가게 등록"}</SheetTitle>
          <SheetDescription>{isEdit ? "가게 정보를 수정합니다." : "새로운 가게를 등록합니다."}</SheetDescription>
        </SheetHeader>

        {isLoadingDetail ? (
          <div className="flex-1 space-y-3 px-4">
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
          </div>
        ) : (
          <form
            id="shop-form"
            noValidate
            onSubmit={form.handleSubmit(onSubmit)}
            className="flex-1 overflow-y-auto px-4"
          >
            <FieldGroup className="gap-4">
              <Controller
                control={form.control}
                name="stationId"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="shop-station-id">지하철역</FieldLabel>
                    <Select
                      value={field.value == null ? "" : String(field.value)}
                      onValueChange={(value) => field.onChange(Number(value))}
                      disabled={busy || stations.length === 0}
                    >
                      <SelectTrigger id="shop-station-id" className="w-full" aria-invalid={fieldState.invalid}>
                        <SelectValue
                          placeholder={stations.length === 0 ? "지하철역 불러오는 중..." : "지하철역 선택"}
                        />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectGroup>
                          {stations.map((station) => (
                            <SelectItem key={station.id} value={String(station.id)}>
                              {station.stationName}
                            </SelectItem>
                          ))}
                        </SelectGroup>
                      </SelectContent>
                    </Select>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              {!isEdit ? (
                <Controller
                  control={form.control}
                  name="ceoId"
                  render={({ field, fieldState }) => (
                    <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="shop-ceo-id">소유 점주 (선택)</FieldLabel>
                      <Select
                        value={field.value == null ? "none" : String(field.value)}
                        onValueChange={(value) => field.onChange(value === "none" ? undefined : Number(value))}
                        disabled={busy || ceos.length === 0}
                      >
                        <SelectTrigger id="shop-ceo-id" className="w-full" aria-invalid={fieldState.invalid}>
                          <SelectValue placeholder={ceos.length === 0 ? "점주 불러오는 중..." : "선택 안 함"} />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectGroup>
                            <SelectItem value="none">선택 안 함 (미배정으로 등록)</SelectItem>
                            {ceos.map((ceo) => (
                              <SelectItem key={ceo.id} value={String(ceo.id)}>
                                {ceo.name} ({ceo.businessRegistrationNumber})
                                {ceo.status === "INACTIVE" ? ` (${CEO_STATUS_LABEL.INACTIVE})` : ""}
                              </SelectItem>
                            ))}
                          </SelectGroup>
                        </SelectContent>
                      </Select>
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />
              ) : null}

              <Controller
                control={form.control}
                name="name"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="shop-name">가게 이름</FieldLabel>
                    <Input
                      {...field}
                      id="shop-name"
                      placeholder="가게 이름을 입력하세요"
                      maxLength={SHOP_NAME_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
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
                    <FieldLabel htmlFor="shop-latitude">위도</FieldLabel>
                    <Input
                      id="shop-latitude"
                      type="number"
                      step="0.000001"
                      value={field.value ?? ""}
                      onChange={(e) => field.onChange(parseOptionalNumber(e.target.value))}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
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
                    <FieldLabel htmlFor="shop-longitude">경도</FieldLabel>
                    <Input
                      id="shop-longitude"
                      type="number"
                      step="0.000001"
                      value={field.value ?? ""}
                      onChange={(e) => field.onChange(parseOptionalNumber(e.target.value))}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="roadAddress"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="shop-road-address">도로명 주소</FieldLabel>
                    <Input
                      {...field}
                      id="shop-road-address"
                      placeholder="도로명 주소를 입력하세요"
                      maxLength={ADDRESS_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="lotAddress"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="shop-lot-address">지번 주소</FieldLabel>
                    <Input
                      {...field}
                      id="shop-lot-address"
                      placeholder="지번 주소를 입력하세요"
                      maxLength={ADDRESS_MAX}
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="phoneNumber"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="shop-phone-number">전화번호 (선택)</FieldLabel>
                    <Input
                      {...field}
                      value={field.value ?? ""}
                      id="shop-phone-number"
                      placeholder="02-1234-5678"
                      aria-invalid={fieldState.invalid}
                      disabled={busy}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="thumbnailImageFileId"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="shop-thumbnail-file">
                      썸네일 이미지 (선택){isEdit ? " — 유지하려면 재업로드하지 마세요" : ""}
                    </FieldLabel>
                    {previewUrl ? (
                      // biome-ignore lint/performance/noImgElement: 업로드 직후 blob URL 미리보기
                      <img
                        src={previewUrl}
                        alt="썸네일 미리보기"
                        className="h-32 w-full rounded-md border object-cover"
                      />
                    ) : null}
                    <Input
                      id="shop-thumbnail-file"
                      type="file"
                      accept="image/*"
                      onChange={handleFileChange}
                      disabled={busy}
                    />
                    {isUploading && <p className="text-muted-foreground text-sm">업로드 중...</p>}
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    <input type="hidden" value={field.value ?? ""} readOnly />
                  </Field>
                )}
              />
            </FieldGroup>
          </form>
        )}

        <SheetFooter>
          <Button type="submit" form="shop-form" disabled={busy}>
            {isPending ? "저장 중..." : isEdit ? "수정" : "등록"}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={busy}>
              취소
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
