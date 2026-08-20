"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
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
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import {
  clearMenuVegetarianAction,
  loadMenuVegetarianAction,
  requestMenuVegetarianAction,
} from "@/feature/product/actions";
import { VEGETARIAN_TYPE_OPTIONS } from "@/feature/product/constants";
import type { MenuVegetarian } from "@/feature/product/domain";
import { PRODUCT_DETAIL_COPY, PRODUCT_DETAIL_SCREEN_COPY, PRODUCT_MENU_MESSAGE } from "@/feature/product/message";
import { type VegetarianFormValues, vegetarianFormSchema } from "@/feature/product/schema";

const FORM_ID = "menu-vegetarian-form";

const DEFAULT_VALUES: VegetarianFormValues = { vegetarianType: "", ingredients: "", description: "" };

interface MenuVegetarianSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  productId: number;
  shopId: number;
  /** 상세 행의 요약을 갱신하기 위해 부모에 알린다 */
  onChanged: (vegetarian: MenuVegetarian | null) => void;
}

export function MenuVegetarianSheet({ open, onOpenChange, productId, shopId, onChanged }: MenuVegetarianSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [isLoading, setIsLoading] = React.useState(false);
  const [vegetarian, setVegetarian] = React.useState<MenuVegetarian | null>(null);
  const [enabled, setEnabled] = React.useState(false);

  const form = useForm<VegetarianFormValues>({
    resolver: zodResolver(vegetarianFormSchema),
    defaultValues: DEFAULT_VALUES,
  });

  const reload = React.useCallback(async () => {
    setIsLoading(true);
    const { success, message, data } = await loadMenuVegetarianAction(productId, shopId);
    setIsLoading(false);

    if (!success || !data) {
      toast.error(message ?? PRODUCT_MENU_MESSAGE.VEGETARIAN_LOAD_FAILED);
      return;
    }

    setVegetarian(data);
    // 검수 대기·반려 중인 요청이 있으면 그 내용을, 없으면 승인되어 반영된 값을 채운다.
    const latest = data.pendingRequest;
    setEnabled((latest?.vegetarianType ?? data.vegetarianType) !== null);
    form.reset({
      vegetarianType: latest?.vegetarianType ?? data.vegetarianType ?? "",
      ingredients: latest?.ingredients ?? "",
      description: latest?.description ?? "",
    });
    onChanged(data);
  }, [productId, shopId, form, onChanged]);

  React.useEffect(() => {
    if (open) void reload();
  }, [open, reload]);

  // 채식 불가 카테고리(돈까스/회·고기·찜탕·족발보쌈·피자·치킨·중식·야식)의 가게는 요청 자체가
  // `PRODUCT_VEGETARIAN_CATEGORY_NOT_ALLOWED`(400)로 거부된다. 서버 판정(`changeable`)을 그대로
  // 받아 Switch 를 잠그고 사유를 적어 준다 — PDF 도 "'변경' 버튼이 클릭되지 않습니다"로 명시한다.
  const changeable = vegetarian?.changeable ?? false;
  const pendingRequest = vegetarian?.pendingRequest ?? null;
  const hasPendingRequest = pendingRequest?.status === "PENDING";
  const rejectedRequest = pendingRequest?.status === "REJECTED" ? pendingRequest : null;
  // 검수 대기 중이거나 채식 불가 카테고리면 입력 자체를 잠근다.
  const locked = hasPendingRequest || !changeable;
  const busy = isPending || isLoading;
  const disabled = busy || locked;

  const onSubmit = (values: VegetarianFormValues) => {
    startTransition(async () => {
      const { success, message } = await requestMenuVegetarianAction(productId, shopId, values);
      if (!success) {
        toast.error(message ?? PRODUCT_MENU_MESSAGE.VEGETARIAN_REQUEST_FAILED);
        return;
      }
      toast.success(PRODUCT_MENU_MESSAGE.VEGETARIAN_REQUEST_SUCCESS);
      await reload();
      onOpenChange(false);
    });
  };

  function handleClear() {
    startTransition(async () => {
      const { success, message } = await clearMenuVegetarianAction(productId, shopId);
      if (!success) {
        toast.error(message ?? PRODUCT_MENU_MESSAGE.VEGETARIAN_CLEAR_FAILED);
        return;
      }
      toast.success(PRODUCT_MENU_MESSAGE.VEGETARIAN_CLEAR_SUCCESS);
      await reload();
    });
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle className="flex items-center gap-2">
            {PRODUCT_DETAIL_COPY.SHEET_VEGETARIAN_TITLE}
            {hasPendingRequest && <Badge variant="secondary">{PRODUCT_DETAIL_COPY.BADGE_PENDING}</Badge>}
            {rejectedRequest !== null && <Badge variant="destructive">{PRODUCT_DETAIL_COPY.BADGE_REJECTED}</Badge>}
          </SheetTitle>
          <SheetDescription>{PRODUCT_DETAIL_COPY.VEGETARIAN_APPROVAL_NOTICE}</SheetDescription>
        </SheetHeader>

        <div className="flex flex-1 flex-col gap-4 overflow-y-auto px-4">
          {isLoading ? (
            <>
              <Skeleton className="h-9 w-full" />
              <Skeleton className="h-24 w-full" />
            </>
          ) : (
            <>
              {!changeable && (
                <Alert>
                  <AlertTitle>{PRODUCT_DETAIL_COPY.ROW_VEGETARIAN}</AlertTitle>
                  <AlertDescription>{PRODUCT_DETAIL_COPY.VEGETARIAN_NOT_ALLOWED}</AlertDescription>
                </Alert>
              )}

              {rejectedRequest !== null && rejectedRequest.rejectReason !== null && (
                <Alert variant="destructive">
                  <AlertTitle>{PRODUCT_DETAIL_COPY.BADGE_REJECTED}</AlertTitle>
                  <AlertDescription>
                    {`${PRODUCT_DETAIL_COPY.REJECT_REASON_PREFIX}${rejectedRequest.rejectReason}`}
                  </AlertDescription>
                </Alert>
              )}

              <Field orientation="horizontal" className="gap-3">
                <div className="flex min-w-0 flex-1 flex-col gap-1">
                  <FieldLabel htmlFor="menu-vegetarian-enabled">
                    {PRODUCT_DETAIL_COPY.VEGETARIAN_ENABLE_LABEL}
                  </FieldLabel>
                  <FieldDescription>{PRODUCT_DETAIL_COPY.VEGETARIAN_APPROVAL_NOTICE}</FieldDescription>
                </div>
                <Switch
                  id="menu-vegetarian-enabled"
                  checked={enabled}
                  onCheckedChange={setEnabled}
                  disabled={disabled}
                />
              </Field>

              {enabled && (
                <>
                  <Separator />
                  <form id={FORM_ID} noValidate onSubmit={form.handleSubmit(onSubmit)}>
                    <FieldGroup className="gap-4">
                      <Controller
                        control={form.control}
                        name="vegetarianType"
                        render={({ field, fieldState }) => (
                          <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor="menu-vegetarian-type">
                              {PRODUCT_DETAIL_COPY.VEGETARIAN_TYPE_LABEL}
                            </FieldLabel>
                            {/* Radix Select 의 value 는 lifetime 내내 문자열이어야 한다 —
                                undefined 로 뒤집히면 uncontrolled → controlled 경고가 난다. */}
                            <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={disabled}>
                              <SelectTrigger id="menu-vegetarian-type" aria-invalid={fieldState.invalid}>
                                <SelectValue />
                              </SelectTrigger>
                              <SelectContent position="popper">
                                <SelectGroup>
                                  {VEGETARIAN_TYPE_OPTIONS.map((option) => (
                                    <SelectItem key={option.value} value={option.value}>
                                      {option.label}
                                    </SelectItem>
                                  ))}
                                </SelectGroup>
                              </SelectContent>
                            </Select>
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                          </Field>
                        )}
                      />

                      <Controller
                        control={form.control}
                        name="ingredients"
                        render={({ field, fieldState }) => (
                          <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor="menu-vegetarian-ingredients">
                              {PRODUCT_DETAIL_COPY.VEGETARIAN_INGREDIENTS_LABEL}
                            </FieldLabel>
                            <Textarea
                              id="menu-vegetarian-ingredients"
                              rows={4}
                              value={field.value}
                              onChange={field.onChange}
                              aria-invalid={fieldState.invalid}
                              disabled={disabled}
                            />
                            <FieldDescription>{PRODUCT_DETAIL_COPY.VEGETARIAN_INGREDIENTS_HELP}</FieldDescription>
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                          </Field>
                        )}
                      />

                      <Controller
                        control={form.control}
                        name="description"
                        render={({ field, fieldState }) => (
                          <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor="menu-vegetarian-description">
                              {PRODUCT_DETAIL_COPY.VEGETARIAN_DESCRIPTION_LABEL}
                            </FieldLabel>
                            <Textarea
                              id="menu-vegetarian-description"
                              rows={4}
                              value={field.value}
                              onChange={field.onChange}
                              aria-invalid={fieldState.invalid}
                              disabled={disabled}
                            />
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                          </Field>
                        )}
                      />
                    </FieldGroup>
                  </form>
                </>
              )}

              {/* 해제도 검수 대상이라 즉시 반영되지 않는다 — 이미 설정된 경우에만 노출한다. */}
              {vegetarian?.vegetarianType != null && (
                <Button type="button" variant="outline" className="w-fit" disabled={disabled} onClick={handleClear}>
                  {PRODUCT_DETAIL_COPY.VEGETARIAN_CLEAR}
                </Button>
              )}
            </>
          )}
        </div>

        <SheetFooter>
          <Button type="submit" form={FORM_ID} disabled={disabled || !enabled}>
            {isPending ? PRODUCT_DETAIL_SCREEN_COPY.BUTTON_SAVING : PRODUCT_DETAIL_COPY.VEGETARIAN_SUBMIT}
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
