"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
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
import { updateShopRiderVisitGuideAction, validateShopRiderVisitGuideAction } from "@/feature/shop/actions";
import { SHOP_RIDER_VISIT_GUIDE_MAX } from "@/feature/shop/constants";
import { SHOP_MESSAGE, SHOP_RIDER_COPY } from "@/feature/shop/message";
import { type ShopRiderVisitGuideFormValues, shopRiderVisitGuideSchema } from "@/feature/shop/schema";

// 이 시트에서만 쓰이는 카피이므로 공용 카피 파일이 아닌 컴포넌트 상단 상수로 둔다(conveniences-sheet 의 DIRECTIONS_GUIDE 선례).
const VISIT_GUIDE_GOOD_EXAMPLES = [
  "주소로 찾기 어려운 위치는 주변 건물·간판을 기준으로 설명해 주세요. 예) OO 약국 상가 왼쪽 문으로 들어오시면 됩니다.",
  "실제 간판명이 가게명과 다르면 함께 적어 주세요. 예) 상호명은 'OO네 밥상'이지만 간판은 배민이네 분식집입니다.",
  "주차·출입 동선을 안내해 주세요. 예) 가게 뒷문 앞에 오토바이를 세워주시고 앞문으로 방문해주세요.",
  "자리를 비울 때의 픽업 방법을 적어 주세요. 예) 가게 앞 배달통에 픽업된 음식이 있는지 확인 부탁드립니다.",
];

const VISIT_GUIDE_BAD_EXAMPLES = [
  "가게 실주소를 다시 적는 문구는 등록할 수 없습니다. 예) 서울시 송파구 위례성대로 OO",
  "배차나 이동수단을 특정하는 문구는 등록할 수 없습니다. 예) 자동차 라이더만 수행 부탁드립니다 / 보온가방 없으신 분은 배차 자제 부탁드립니다",
  "가게 방문과 관련 없는 문구는 등록할 수 없습니다. 예) 마스크 미착용시 출입 금지",
];

interface RiderVisitGuideSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  visitGuide: string | null;
}

export function RiderVisitGuideSheet({ open, onOpenChange, shopId, visitGuide }: RiderVisitGuideSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  // 서버 사전 검수(금칙어·실주소·배차 특정)에서 돌아온 위반 사유는 폼 필드 에러와 별도로 목록으로 노출한다.
  const [violations, setViolations] = React.useState<string[]>([]);

  const form = useForm<ShopRiderVisitGuideFormValues>({
    resolver: zodResolver(shopRiderVisitGuideSchema),
    defaultValues: { visitGuide: "" },
  });

  React.useEffect(() => {
    if (open) {
      form.reset({ visitGuide: visitGuide ?? "" });
      setViolations([]);
    }
  }, [open, visitGuide, form]);

  const currentLength = form.watch("visitGuide").length;

  const onSubmit = (values: ShopRiderVisitGuideFormValues) => {
    startTransition(async () => {
      setViolations([]);

      const isDeletion = values.visitGuide.trim().length === 0;

      // 빈 값은 삭제 의도이므로 사전 검수를 건너뛴다 — 빈 문자열 검수는 항상 통과라 불필요한 왕복이다.
      if (!isDeletion) {
        // 저장 전에 서버 검증을 먼저 통과시켜, 위반 사유를 저장 실패 토스트가 아닌 인라인으로 보여준다.
        const validation = await validateShopRiderVisitGuideAction(shopId, values);
        if (!validation.success || !validation.data) {
          toast.error(validation.message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
          return;
        }
        if (!validation.data.valid) {
          setViolations(validation.data.violations);
          return;
        }
      }

      const { success, message } = await updateShopRiderVisitGuideAction(shopId, values);
      if (success) {
        toast.success(
          isDeletion ? SHOP_MESSAGE.RIDER_VISIT_GUIDE_DELETE_SUCCESS : SHOP_MESSAGE.RIDER_VISIT_GUIDE_UPDATE_SUCCESS,
        );
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
          <SheetTitle>{SHOP_RIDER_COPY.VISIT_GUIDE_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_RIDER_COPY.VISIT_GUIDE_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <form
          id="shop-rider-visit-guide-form"
          noValidate
          onSubmit={form.handleSubmit(onSubmit)}
          className="flex-1 overflow-y-auto px-4"
        >
          <FieldGroup className="gap-4">
            <p className="rounded-md bg-muted px-3 py-2 text-muted-foreground text-xs leading-snug">
              {SHOP_RIDER_COPY.VISIT_GUIDE_RIDER_ONLY_NOTICE}
            </p>

            <Controller
              control={form.control}
              name="visitGuide"
              render={({ field, fieldState }) => (
                <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="shop-rider-visit-guide">{SHOP_RIDER_COPY.VISIT_GUIDE_TITLE}</FieldLabel>
                  <Textarea
                    {...field}
                    id="shop-rider-visit-guide"
                    placeholder={SHOP_RIDER_COPY.VISIT_GUIDE_PLACEHOLDER}
                    maxLength={SHOP_RIDER_VISIT_GUIDE_MAX}
                    rows={6}
                    disabled={isPending}
                    aria-invalid={fieldState.invalid}
                  />
                  <span className="text-muted-foreground text-xs">
                    {currentLength} / {SHOP_RIDER_VISIT_GUIDE_MAX}
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

            <Accordion type="single" collapsible>
              <AccordionItem value="good-examples">
                <AccordionTrigger className="text-sm">{SHOP_RIDER_COPY.GOOD_EXAMPLES_TITLE}</AccordionTrigger>
                <AccordionContent>
                  <ul className="list-disc space-y-1 pl-4 text-muted-foreground text-xs">
                    {VISIT_GUIDE_GOOD_EXAMPLES.map((example) => (
                      <li key={example}>{example}</li>
                    ))}
                  </ul>
                </AccordionContent>
              </AccordionItem>
              <AccordionItem value="bad-examples">
                <AccordionTrigger className="text-sm">{SHOP_RIDER_COPY.BAD_EXAMPLES_TITLE}</AccordionTrigger>
                <AccordionContent>
                  <ul className="list-disc space-y-1 pl-4 text-muted-foreground text-xs">
                    {VISIT_GUIDE_BAD_EXAMPLES.map((example) => (
                      <li key={example}>{example}</li>
                    ))}
                  </ul>
                  <p className="mt-2 text-muted-foreground text-xs">{SHOP_RIDER_COPY.MODERATION_NOTICE}</p>
                </AccordionContent>
              </AccordionItem>
            </Accordion>
          </FieldGroup>
        </form>

        <SheetFooter>
          <Button type="submit" form="shop-rider-visit-guide-form" disabled={isPending}>
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
