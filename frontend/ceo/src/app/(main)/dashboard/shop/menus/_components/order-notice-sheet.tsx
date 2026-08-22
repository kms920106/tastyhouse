"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { ChevronRight } from "lucide-react";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
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
import { loadOrderNoticeAction, updateOrderNoticeAction } from "@/feature/shop/actions";
import { ORDER_NOTICE_CONTENT_MAX } from "@/feature/shop/constants";
import type { ShopOrderNotice } from "@/feature/shop/domain";
import { SHOP_ORDER_NOTICE_COPY, SHOP_ORDER_NOTICE_MESSAGE } from "@/feature/shop/message";
import { type OrderNoticeFormValues, orderNoticeSchema } from "@/feature/shop/schema";

interface OrderNoticeSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  /** 서버에서 받은 초기 값. 시트가 열리면 곧바로 재조회해 확정한다 */
  initialNotice?: ShopOrderNotice;
}

export function OrderNoticeSheet({ open, onOpenChange, shopId, initialNotice }: OrderNoticeSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [notice, setNotice] = React.useState<ShopOrderNotice | null>(initialNotice ?? null);

  const form = useForm<OrderNoticeFormValues>({
    resolver: zodResolver(orderNoticeSchema),
    defaultValues: { content: initialNotice?.content ?? "" },
  });

  /**
   * 시트가 **열리는 순간에만** 서버 값으로 폼을 되돌린다.
   *
   * `notice` 를 의존성에 두면 저장 후 `revalidatePath` 로 값이 갱신될 때 이 effect 가 다시 돌아
   * 입력 중이던 본문이 날아간다(`notice-sheet.tsx` 가 세운 패턴).
   */
  const wasOpen = React.useRef(false);
  React.useEffect(() => {
    if (open && !wasOpen.current) {
      startTransition(async () => {
        const { success, message, data } = await loadOrderNoticeAction(shopId);
        if (!success || !data) {
          toast.error(message ?? SHOP_ORDER_NOTICE_COPY.LOAD_FAILED);
          return;
        }
        setNotice(data);
        form.reset({ content: data.content ?? "" });
      });
    }
    wasOpen.current = open;
  }, [open, shopId, form]);

  const onSubmit = (values: OrderNoticeFormValues) => {
    startTransition(async () => {
      // 게시중단(`hidden`) 상태에서도 저장을 막지 않는다 — 문구를 고쳐 다시 게시를 요청하는 것이
      // 점주가 할 수 있는 유일한 조치이므로, 저장 자체를 잠그면 빠져나갈 길이 없다.
      const { success, message } = await updateOrderNoticeAction(shopId, values.content);
      if (!success) {
        toast.error(message ?? SHOP_ORDER_NOTICE_COPY.SAVE_FAILED);
        return;
      }
      toast.success(SHOP_ORDER_NOTICE_MESSAGE.SAVE_SUCCESS);
    });
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{SHOP_ORDER_NOTICE_COPY.SHEET_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_ORDER_NOTICE_COPY.SHEET_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex flex-1 flex-col gap-4 overflow-y-auto px-4">
          {/* 게시중단은 손님 화면에서 문구가 사라진 상태다 — 사유를 함께 보여줘야 무엇을 고칠지 알 수 있다. */}
          {notice?.hidden === true && (
            <Alert variant="destructive">
              <AlertTitle>{SHOP_ORDER_NOTICE_COPY.HIDDEN_TITLE}</AlertTitle>
              <AlertDescription>
                <span>{SHOP_ORDER_NOTICE_MESSAGE.HIDDEN_NOTICE}</span>
                {notice.hiddenReason !== null && (
                  <span>{`${SHOP_ORDER_NOTICE_COPY.HIDDEN_REASON_PREFIX}${notice.hiddenReason}`}</span>
                )}
              </AlertDescription>
            </Alert>
          )}

          <form id="order-notice-form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
            <FieldGroup className="gap-4">
              <Controller
                control={form.control}
                name="content"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="order-notice-content">{SHOP_ORDER_NOTICE_COPY.CONTENT_LABEL}</FieldLabel>
                    <Textarea
                      {...field}
                      id="order-notice-content"
                      placeholder={SHOP_ORDER_NOTICE_COPY.CONTENT_PLACEHOLDER}
                      rows={6}
                      disabled={isPending}
                      aria-invalid={fieldState.invalid}
                    />
                    <span className="text-muted-foreground text-xs">
                      {field.value.length} / {ORDER_NOTICE_CONTENT_MAX}
                    </span>
                    <FieldDescription>{SHOP_ORDER_NOTICE_COPY.WRITING_HELP}</FieldDescription>
                    <FieldDescription>{SHOP_ORDER_NOTICE_COPY.WRITING_EXAMPLE}</FieldDescription>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />
            </FieldGroup>
          </form>

          <Collapsible className="group/prohibited rounded-md border px-3 py-2">
            <CollapsibleTrigger className="flex w-full items-center justify-between gap-2 text-left font-medium text-sm">
              {SHOP_ORDER_NOTICE_COPY.PROHIBITED_TITLE}
              <ChevronRight className="size-4 transition-transform duration-200 group-data-[state=open]/prohibited:rotate-90" />
            </CollapsibleTrigger>
            <CollapsibleContent>
              <p className="mt-2 text-muted-foreground text-xs leading-snug">
                {SHOP_ORDER_NOTICE_COPY.PROHIBITED_LEAD}
              </p>
              <ul className="mt-1 list-disc space-y-1 pl-4 text-muted-foreground text-xs leading-snug">
                {SHOP_ORDER_NOTICE_COPY.PROHIBITED_ITEMS.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </CollapsibleContent>
          </Collapsible>
        </div>

        <SheetFooter>
          <Button type="submit" form="order-notice-form" disabled={isPending}>
            {isPending ? SHOP_ORDER_NOTICE_COPY.ACTION_PENDING : SHOP_ORDER_NOTICE_COPY.ACTION_SUBMIT}
          </Button>
          <SheetClose asChild>
            <Button variant="outline" disabled={isPending}>
              {SHOP_ORDER_NOTICE_COPY.ACTION_CLOSE}
            </Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
