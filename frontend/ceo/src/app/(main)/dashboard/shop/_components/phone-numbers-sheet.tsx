"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
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
  createPhoneNumberAction,
  deletePhoneNumberAction,
  fetchPhoneNumbersAction,
  setPrimaryPhoneNumberAction,
} from "@/feature/shop/actions";
import { PHONE_NUMBER_MAX_COUNT } from "@/feature/shop/constants";
import type { PhoneNumber } from "@/feature/shop/domain";
import { SHOP_BASIC_COPY, SHOP_MESSAGE } from "@/feature/shop/message";
import { type PhoneNumberFormValues, phoneNumberSchema } from "@/feature/shop/schema";

interface PhoneNumbersSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  phoneNumbers: PhoneNumber[];
}

const EMPTY_PHONE_NUMBER: PhoneNumberFormValues = {
  phoneNumber: "",
  virtual: false,
};

export function PhoneNumbersSheet({ open, onOpenChange, shopId, phoneNumbers }: PhoneNumbersSheetProps) {
  const [items, setItems] = React.useState<PhoneNumber[]>(phoneNumbers);
  const [isPending, startTransition] = React.useTransition();

  const form = useForm<PhoneNumberFormValues>({
    resolver: zodResolver(phoneNumberSchema),
    defaultValues: EMPTY_PHONE_NUMBER,
  });

  React.useEffect(() => {
    if (open) {
      setItems(phoneNumbers);
      form.reset(EMPTY_PHONE_NUMBER);
    }
  }, [open, phoneNumbers, form]);

  const reload = React.useCallback(() => {
    startTransition(async () => {
      const { success, data } = await fetchPhoneNumbersAction(shopId);
      if (success && data) setItems(data);
    });
  }, [shopId]);

  const isMaxReached = items.length >= PHONE_NUMBER_MAX_COUNT;

  const onSubmit = (values: PhoneNumberFormValues) => {
    if (isMaxReached) {
      toast.error(SHOP_MESSAGE.PHONE_NUMBER_MAX_REACHED);
      return;
    }

    startTransition(async () => {
      // 첫 등록 번호는 서버가 자동으로 대표번호로 지정하므로 클라이언트에서 대표 여부를 보내지 않는다.
      const { success, message } = await createPhoneNumberAction(shopId, values);
      if (success) {
        toast.success(SHOP_MESSAGE.PHONE_NUMBER_CREATE_SUCCESS);
        form.reset(EMPTY_PHONE_NUMBER);
        reload();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  };

  function handleSetPrimary(target: PhoneNumber) {
    if (target.primary) return;

    startTransition(async () => {
      const { success, message } = await setPrimaryPhoneNumberAction(target.id);
      if (success) {
        toast.success(SHOP_MESSAGE.PHONE_NUMBER_PRIMARY_SUCCESS);
        reload();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  function handleDelete(target: PhoneNumber) {
    startTransition(async () => {
      const { success, message } = await deletePhoneNumberAction(target.id);
      if (success) {
        toast.success(SHOP_MESSAGE.PHONE_NUMBER_DELETE_SUCCESS);
        reload();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  const primaryId = items.find((item) => item.primary)?.id;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{SHOP_BASIC_COPY.PHONE_NUMBER_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_BASIC_COPY.PHONE_NUMBER_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-4 overflow-y-auto px-4">
          {items.length > 0 ? (
            <RadioGroup
              // 대표번호는 항상 문자열 값을 유지한다 — 미지정이면 빈 문자열.
              value={primaryId === undefined ? "" : String(primaryId)}
              onValueChange={(value) => {
                const target = items.find((item) => String(item.id) === value);
                if (target) handleSetPrimary(target);
              }}
              disabled={isPending}
              className="gap-2"
            >
              {items.map((item) => (
                <div key={item.id} className="flex items-center gap-3 rounded-md border px-3 py-2">
                  <RadioGroupItem id={`phone-primary-${item.id}`} value={String(item.id)} />
                  <FieldLabel htmlFor={`phone-primary-${item.id}`} className="flex-1 font-normal">
                    <span className="tabular-nums">{item.phoneNumber}</span>
                    {item.virtual && (
                      <Badge variant="outline" className="ml-2">
                        가상번호
                      </Badge>
                    )}
                    {item.primary && (
                      <Badge variant="secondary" className="ml-1">
                        대표
                      </Badge>
                    )}
                  </FieldLabel>
                  <Button
                    type="button"
                    size="sm"
                    variant="ghost"
                    className="text-destructive"
                    disabled={isPending}
                    onClick={() => handleDelete(item)}
                  >
                    삭제
                  </Button>
                </div>
              ))}
            </RadioGroup>
          ) : (
            <p className="text-muted-foreground text-sm">등록된 전화번호가 없습니다.</p>
          )}

          <Separator />

          <form id="phone-number-form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
            <FieldGroup className="gap-4">
              <Controller
                control={form.control}
                name="phoneNumber"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="phone-number-input">전화번호 추가</FieldLabel>
                    <Input
                      {...field}
                      id="phone-number-input"
                      inputMode="numeric"
                      placeholder="숫자만 입력 (예: 0212345678)"
                      maxLength={13}
                      disabled={isPending || isMaxReached}
                      aria-invalid={fieldState.invalid}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="virtual"
                render={({ field }) => (
                  <Field orientation="horizontal" className="gap-3">
                    <Checkbox
                      id="phone-number-virtual"
                      checked={field.value}
                      disabled={isPending || isMaxReached}
                      onCheckedChange={(checked) => field.onChange(checked === true)}
                    />
                    <FieldLabel htmlFor="phone-number-virtual" className="font-normal">
                      가상번호 사용
                    </FieldLabel>
                  </Field>
                )}
              />

              {isMaxReached && <p className="text-destructive text-sm">{SHOP_MESSAGE.PHONE_NUMBER_MAX_REACHED}</p>}
            </FieldGroup>
          </form>
        </div>

        <SheetFooter>
          <Button type="submit" form="phone-number-form" disabled={isPending || isMaxReached}>
            {isPending ? "처리 중..." : "번호 추가"}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={isPending}>
              닫기
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
