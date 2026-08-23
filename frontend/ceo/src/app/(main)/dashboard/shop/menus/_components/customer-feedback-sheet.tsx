"use client";

import * as React from "react";

import { useRouter } from "next/navigation";

import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Skeleton } from "@/components/ui/skeleton";
import { loadCustomerFeedbacksAction, readCustomerFeedbacksAction } from "@/feature/product/actions";
import type { CustomerFeedback } from "@/feature/product/domain";
import { CUSTOMER_FEEDBACK_COPY, CUSTOMER_FEEDBACK_TYPE_LABEL } from "@/feature/product/message";

interface CustomerFeedbackSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  /** 확인 처리가 끝났음을 부모에게 알려 빨간 점을 끄게 한다 */
  onRead: () => void;
}

/**
 * 고객 제보 목록.
 *
 * **여는 시점에 확인 처리 API 를 부른다** — 점주가 목록을 봤다는 사실이 곧 확인이므로 별도
 * 버튼을 두지 않는다. 확인 처리가 실패해도 목록은 그대로 보여준다(점이 남을 뿐이다).
 *
 * 메뉴명을 누르면 **메뉴 상세로 이동해 바로 고칠 수 있다**(PDF 명시). 제보를 확인만 하고
 * 고치러 갈 길이 없으면 화면의 목적이 반쯤 사라진다.
 *
 * **제보자 정보는 응답에 없다** — 화면도 표시하지 않는다.
 */
export function CustomerFeedbackSheet({ open, onOpenChange, shopId, onRead }: CustomerFeedbackSheetProps) {
  const router = useRouter();
  const [isLoading, setIsLoading] = React.useState(false);
  const [feedbacks, setFeedbacks] = React.useState<CustomerFeedback[]>([]);

  React.useEffect(() => {
    if (!open) return;

    let alive = true;
    setIsLoading(true);

    void (async () => {
      const { success, message, data } = await loadCustomerFeedbacksAction(shopId);
      if (!alive) return;

      setIsLoading(false);
      if (!success || !data) {
        toast.error(message ?? CUSTOMER_FEEDBACK_COPY.LOAD_FAILED);
        return;
      }
      setFeedbacks(data);

      // 목록을 띄운 뒤에 확인 처리한다. 실패해도 목록은 유지하고 점만 남긴다.
      const read = await readCustomerFeedbacksAction(shopId);
      if (alive && read.success) onRead();
    })();

    return () => {
      alive = false;
    };
  }, [open, shopId, onRead]);

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-full overflow-y-auto sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>{CUSTOMER_FEEDBACK_COPY.TITLE}</SheetTitle>
          <SheetDescription>{CUSTOMER_FEEDBACK_COPY.PERIOD_NOTICE}</SheetDescription>
        </SheetHeader>

        <div className="px-4 pb-6">
          {isLoading ? (
            <div className="flex flex-col gap-3">
              <Skeleton className="h-14 w-full" />
              <Skeleton className="h-14 w-full" />
              <Skeleton className="h-14 w-full" />
            </div>
          ) : feedbacks.length === 0 ? (
            <p className="text-muted-foreground py-10 text-center text-sm">{CUSTOMER_FEEDBACK_COPY.EMPTY}</p>
          ) : (
            <ul className="flex flex-col">
              {feedbacks.map((feedback) => (
                <li
                  key={`${feedback.productId}-${feedback.feedbackType}`}
                  className="flex flex-col gap-2 border-b py-3 last:border-b-0"
                >
                  <div className="flex items-center justify-between gap-3">
                    <div className="flex min-w-0 flex-col gap-1">
                      {/* 메뉴명 클릭 → 메뉴 상세로 이동해 바로 수정 */}
                      <button
                        type="button"
                        className="truncate text-left text-sm font-medium underline-offset-4 hover:underline"
                        onClick={() => {
                          onOpenChange(false);
                          router.push(`/dashboard/shop/menus/${feedback.productId}?shopId=${shopId}`);
                        }}
                      >
                        {feedback.productName}
                      </button>
                      <span className="text-muted-foreground text-xs">
                        {CUSTOMER_FEEDBACK_TYPE_LABEL[feedback.feedbackType]}
                      </span>
                    </div>
                    <Badge variant="secondary" className="shrink-0">
                      {`${feedback.count}${CUSTOMER_FEEDBACK_COPY.COUNT_SUFFIX}`}
                    </Badge>
                  </div>

                  {/* 기타 의견은 유형만으로 무엇을 고칠지 알 수 없어 서술을 함께 보여준다 */}
                  {feedback.contents.length > 0 && (
                    <ul className="bg-muted/50 flex flex-col gap-1 rounded-md px-3 py-2">
                      {feedback.contents.map((content, index) => (
                        <li
                          // 서술 내용은 중복될 수 있어 인덱스를 함께 키로 쓴다
                          key={`${content}-${index}`}
                          className="text-muted-foreground text-xs leading-relaxed"
                        >
                          {content}
                        </li>
                      ))}
                    </ul>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
}
