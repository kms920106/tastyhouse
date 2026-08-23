"use client";

import * as React from "react";

import { MessageSquareWarning } from "lucide-react";

import { Button } from "@/components/ui/button";
import { loadCustomerFeedbackUnreadAction } from "@/feature/product/actions";
import { CUSTOMER_FEEDBACK_COPY } from "@/feature/product/message";

import { CustomerFeedbackSheet } from "./customer-feedback-sheet";

interface CustomerFeedbackButtonProps {
  shopId: number;
  disabled: boolean;
}

/**
 * 메뉴판 상단 '고객 제보' 진입 버튼.
 *
 * **모바일은 아이콘만, PC 는 아이콘 + 텍스트**다(출처 PDF 의 화면 구조). 라벨은 지우지 않고
 * `sr-only` 로 남겨 스크린리더에서는 두 폭 모두 같은 이름으로 읽힌다.
 *
 * 미확인 제보가 있으면 아이콘 옆에 빨간 점이 붙는다. 판정은 목록보다 가벼운 전용 API 로 하며,
 * 조회에 실패하면 점을 띄우지 않는다 — 없는 제보를 있다고 알리는 쪽이 더 나쁘다.
 */
export function CustomerFeedbackButton({ shopId, disabled }: CustomerFeedbackButtonProps) {
  const [open, setOpen] = React.useState(false);
  const [hasUnread, setHasUnread] = React.useState(false);

  React.useEffect(() => {
    let alive = true;
    void loadCustomerFeedbackUnreadAction(shopId).then(({ data }) => {
      if (alive) setHasUnread(data === true);
    });
    return () => {
      alive = false;
    };
  }, [shopId]);

  return (
    <>
      <Button type="button" variant="outline" disabled={disabled} className="relative" onClick={() => setOpen(true)}>
        <MessageSquareWarning className="size-4" />
        <span className="sr-only sm:not-sr-only">{CUSTOMER_FEEDBACK_COPY.TITLE}</span>
        {hasUnread && (
          <>
            {/* 빨간 점은 순수 장식이라 스크린리더에서 감춘다. 같은 정보를 아래 텍스트가 읽어준다 —
                role 없는 span 의 `aria-label` 은 무시되므로 실제로 읽히지 않는다. */}
            <span aria-hidden className="bg-destructive absolute -top-1 -right-1 size-2 rounded-full" />
            <span className="sr-only">{CUSTOMER_FEEDBACK_COPY.UNREAD_BADGE_LABEL}</span>
          </>
        )}
      </Button>

      <CustomerFeedbackSheet
        open={open}
        onOpenChange={setOpen}
        shopId={shopId}
        // 시트가 확인 처리까지 끝내면 점을 끈다 — 다시 열어도 켜지지 않아야 한다.
        onRead={() => setHasUnread(false)}
      />
    </>
  );
}
