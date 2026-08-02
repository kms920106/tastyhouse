"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
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
import { updateShopIntroductionAction, validateShopIntroductionAction } from "@/feature/shop/actions";
import { SHOP_INTRODUCTION_MAX } from "@/feature/shop/constants";
import { SHOP_BASIC_COPY, SHOP_MESSAGE } from "@/feature/shop/message";
import { type ShopIntroductionFormValues, shopIntroductionSchema } from "@/feature/shop/schema";

interface IntroductionSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  introduction: string;
}

export function IntroductionSheet({ open, onOpenChange, shopId, introduction }: IntroductionSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  // 서버 검증(금지어 등)에서 돌아온 위반 사유는 폼 필드 에러와 별도로 목록으로 노출한다.
  const [violations, setViolations] = React.useState<string[]>([]);

  const form = useForm<ShopIntroductionFormValues>({
    resolver: zodResolver(shopIntroductionSchema),
    defaultValues: { message: "" },
  });

  React.useEffect(() => {
    if (open) {
      form.reset({ message: introduction });
      setViolations([]);
    }
  }, [open, introduction, form]);

  const currentLength = form.watch("message").length;

  const onSubmit = (values: ShopIntroductionFormValues) => {
    startTransition(async () => {
      setViolations([]);

      // 저장 전에 서버 검증을 먼저 통과시켜, 반려 사유를 저장 실패 토스트가 아닌 인라인으로 보여준다.
      const validation = await validateShopIntroductionAction(shopId, values);
      if (!validation.success || !validation.data) {
        toast.error(validation.message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
        return;
      }
      if (!validation.data.valid) {
        setViolations(validation.data.violations);
        return;
      }

      const { success, message } = await updateShopIntroductionAction(shopId, values);
      if (success) {
        toast.success(SHOP_MESSAGE.INTRODUCTION_UPDATE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{SHOP_BASIC_COPY.INTRODUCTION_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_BASIC_COPY.INTRODUCTION_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <form
          id="shop-introduction-form"
          noValidate
          onSubmit={form.handleSubmit(onSubmit)}
          className="flex-1 overflow-y-auto px-4"
        >
          <FieldGroup className="gap-4">
            <Controller
              control={form.control}
              name="message"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="shop-introduction">{SHOP_BASIC_COPY.INTRODUCTION_TITLE}</FieldLabel>
                  <Textarea
                    {...field}
                    id="shop-introduction"
                    placeholder="가게를 소개하는 문구를 입력하세요"
                    maxLength={SHOP_INTRODUCTION_MAX}
                    rows={10}
                    disabled={isPending}
                    aria-invalid={fieldState.invalid}
                  />
                  <span className="text-muted-foreground text-xs">
                    {currentLength} / {SHOP_INTRODUCTION_MAX}
                  </span>
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />

            {violations.length > 0 && (
              <ul className="list-disc space-y-1 pl-4 text-destructive text-sm">
                {violations.map((violation) => (
                  <li key={violation}>{violation}</li>
                ))}
              </ul>
            )}
          </FieldGroup>
        </form>

        <SheetFooter>
          <Button type="submit" form="shop-introduction-form" disabled={isPending}>
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
