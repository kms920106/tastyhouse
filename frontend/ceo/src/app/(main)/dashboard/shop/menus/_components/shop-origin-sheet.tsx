"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { ChevronRight } from "lucide-react";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Textarea } from "@/components/ui/textarea";
import { loadShopOriginAction, updateShopOriginAction } from "@/feature/shop/actions";
import { SHOP_ORIGIN_CONTENT_MAX } from "@/feature/shop/constants";
import type { ShopOrigin } from "@/feature/shop/domain";
import { SHOP_ORIGIN_COPY, SHOP_ORIGIN_MESSAGE } from "@/feature/shop/message";
import { type ShopOriginFormValues, shopOriginSchema } from "@/feature/shop/schema";

const FORM_ID = "shop-origin-form";

interface ShopOriginSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  /** 서버에서 받은 초기 값. 시트가 열리면 곧바로 재조회해 확정한다 */
  initialOrigin?: ShopOrigin;
}

/** 도메인 값 → 폼 값. 반대편 필드는 서버가 null 로 정리하므로 빈 문자열로 편다 */
function toFormValues(origin: ShopOrigin | null): ShopOriginFormValues {
  return {
    sourceType: origin?.sourceType ?? "DIRECT",
    content: origin?.content ?? "",
    url: origin?.url ?? "",
  };
}

export function ShopOriginSheet({ open, onOpenChange, shopId, initialOrigin }: ShopOriginSheetProps) {
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<ShopOriginFormValues>({
    resolver: zodResolver(shopOriginSchema),
    defaultValues: toFormValues(initialOrigin ?? null),
  });

  /**
   * 시트가 **열리는 순간에만** 서버 값으로 폼을 되돌린다.
   *
   * 조회 실패 시 폼을 빈 값으로 덮지 않는다 — 전체 교체(PUT)라 빈 폼으로 저장하면 기존
   * 원산지가 지워진다(`frontend.md` §예외 처리).
   */
  const wasOpen = React.useRef(false);
  React.useEffect(() => {
    if (open && !wasOpen.current) {
      startTransition(async () => {
        const { success, message, data } = await loadShopOriginAction(shopId);
        if (!success || !data) {
          toast.error(message ?? SHOP_ORIGIN_MESSAGE.LOAD_FAILED);
          return;
        }
        form.reset(toFormValues(data));
      });
    }
    wasOpen.current = open;
  }, [open, shopId, form]);

  const sourceType = form.watch("sourceType");

  const onSubmit = (values: ShopOriginFormValues) => {
    startTransition(async () => {
      const { success, message } = await updateShopOriginAction(shopId, values);
      if (!success) {
        toast.error(message ?? SHOP_ORIGIN_MESSAGE.SAVE_FAILED);
        return;
      }
      toast.success(SHOP_ORIGIN_MESSAGE.SAVE_SUCCESS);
    });
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{SHOP_ORIGIN_COPY.SHEET_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_ORIGIN_COPY.SHEET_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex flex-1 flex-col gap-4 overflow-y-auto px-4">
          <form id={FORM_ID} noValidate onSubmit={form.handleSubmit(onSubmit)}>
            <FieldGroup className="gap-4">
              <Controller
                control={form.control}
                name="sourceType"
                render={({ field }) => (
                  <Field className="gap-1.5">
                    <FieldLabel htmlFor="shop-origin-source-direct">{SHOP_ORIGIN_COPY.SOURCE_TYPE_LABEL}</FieldLabel>
                    {/* 방식을 고르면 반대편 입력란을 숨긴다 — 둘 다 보이면 어느 쪽이 저장되는지 모호하다 */}
                    <RadioGroup
                      value={field.value}
                      onValueChange={field.onChange}
                      disabled={isPending}
                      className="gap-2"
                    >
                      <div className="flex items-center gap-2">
                        <RadioGroupItem value="DIRECT" id="shop-origin-source-direct" />
                        <FieldLabel htmlFor="shop-origin-source-direct" className="font-normal">
                          {SHOP_ORIGIN_COPY.SOURCE_TYPE_DIRECT}
                        </FieldLabel>
                      </div>
                      <div className="flex items-center gap-2">
                        <RadioGroupItem value="FRANCHISE_URL" id="shop-origin-source-franchise" />
                        <FieldLabel htmlFor="shop-origin-source-franchise" className="font-normal">
                          {SHOP_ORIGIN_COPY.SOURCE_TYPE_FRANCHISE_URL}
                        </FieldLabel>
                      </div>
                    </RadioGroup>
                  </Field>
                )}
              />

              {sourceType === "DIRECT" && (
                <Controller
                  control={form.control}
                  name="content"
                  render={({ field, fieldState }) => (
                    <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="shop-origin-content">{SHOP_ORIGIN_COPY.CONTENT_LABEL}</FieldLabel>
                      <Textarea
                        {...field}
                        id="shop-origin-content"
                        placeholder={SHOP_ORIGIN_COPY.CONTENT_PLACEHOLDER}
                        rows={8}
                        disabled={isPending}
                        aria-invalid={fieldState.invalid}
                      />
                      <span className="text-muted-foreground text-xs">
                        {field.value.length} / {SHOP_ORIGIN_CONTENT_MAX}
                      </span>
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />
              )}

              {sourceType === "FRANCHISE_URL" && (
                <Controller
                  control={form.control}
                  name="url"
                  render={({ field, fieldState }) => (
                    <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="shop-origin-url">{SHOP_ORIGIN_COPY.URL_LABEL}</FieldLabel>
                      <Input
                        {...field}
                        id="shop-origin-url"
                        type="url"
                        inputMode="url"
                        placeholder={SHOP_ORIGIN_COPY.URL_PLACEHOLDER}
                        disabled={isPending}
                        aria-invalid={fieldState.invalid}
                      />
                      <FieldDescription>{SHOP_ORIGIN_COPY.URL_FRANCHISE_NOTICE}</FieldDescription>
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />
              )}
            </FieldGroup>
          </form>

          {/* 원산지는 법정 필수 표시라 잘못 쓰면 과태료 대상이다 — 작성 기준을 손닿는 곳에 둔다 */}
          <Collapsible className="group/guide rounded-md border px-3 py-2">
            <CollapsibleTrigger className="flex w-full items-center justify-between gap-2 text-left font-medium text-sm">
              {SHOP_ORIGIN_COPY.GUIDE_TITLE}
              <ChevronRight className="size-4 transition-transform duration-200 group-data-[state=open]/guide:rotate-90" />
            </CollapsibleTrigger>
            <CollapsibleContent>
              <p className="mt-2 text-muted-foreground text-xs leading-snug">{SHOP_ORIGIN_COPY.GUIDE_LEAD}</p>
              <ol className="mt-1 list-decimal space-y-1 pl-4 text-muted-foreground text-xs leading-snug">
                {SHOP_ORIGIN_COPY.GUIDE_ITEMS.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ol>
            </CollapsibleContent>
          </Collapsible>

          <Collapsible className="group/items rounded-md border px-3 py-2">
            <CollapsibleTrigger className="flex w-full items-center justify-between gap-2 text-left font-medium text-sm">
              {SHOP_ORIGIN_COPY.ITEMS_TITLE}
              <ChevronRight className="size-4 transition-transform duration-200 group-data-[state=open]/items:rotate-90" />
            </CollapsibleTrigger>
            <CollapsibleContent>
              <Alert className="mt-2">
                <AlertDescription className="flex flex-col gap-2 text-xs leading-snug">
                  <span>
                    <span className="font-medium">{SHOP_ORIGIN_COPY.ITEMS_LIVESTOCK_LABEL}</span>
                    <span className="text-muted-foreground"> — {SHOP_ORIGIN_COPY.ITEMS_LIVESTOCK}</span>
                  </span>
                  <span>
                    <span className="font-medium">{SHOP_ORIGIN_COPY.ITEMS_SEAFOOD_LABEL}</span>
                    <span className="text-muted-foreground"> — {SHOP_ORIGIN_COPY.ITEMS_SEAFOOD}</span>
                  </span>
                </AlertDescription>
              </Alert>
            </CollapsibleContent>
          </Collapsible>
        </div>

        <SheetFooter>
          <Button type="submit" form={FORM_ID} disabled={isPending}>
            {isPending ? SHOP_ORIGIN_COPY.ACTION_PENDING : SHOP_ORIGIN_COPY.ACTION_SUBMIT}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={isPending}>
              {SHOP_ORIGIN_COPY.ACTION_CLOSE}
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
