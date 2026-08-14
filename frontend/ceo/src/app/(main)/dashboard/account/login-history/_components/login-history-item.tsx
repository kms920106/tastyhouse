"use client";

import { StatusBadge } from "@/components/status-badge";
import { AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import type { CeoLoginHistoryItem as CeoLoginHistoryItemModel } from "@/feature/ceo/domain";
import { CEO_LOGIN_HISTORY_COPY } from "@/feature/ceo/message";
import { formatDateTime } from "@/lib/date";

interface LoginHistoryItemProps {
  item: CeoLoginHistoryItemModel;
}

export function LoginHistoryItem({ item }: LoginHistoryItemProps) {
  return (
    <AccordionItem value={String(item.id)}>
      <AccordionTrigger className="text-sm">
        <div className="flex min-w-0 flex-1 flex-wrap items-center gap-2">
          {/* 라벨은 서버가 준 한글(`resultName`)을 그대로 넘긴다 — 프론트에서 매핑하지 않는다. */}
          <StatusBadge status={item.result} label={item.resultName} />
          {item.failureReasonName && <span className="text-muted-foreground text-xs">{item.failureReasonName}</span>}
          <span className="ml-auto text-muted-foreground text-xs">{formatDateTime(item.loggedInAt)}</span>
        </div>
      </AccordionTrigger>
      <AccordionContent>
        <dl className="flex flex-col gap-3">
          <div className="flex flex-col gap-1">
            <dt className="text-muted-foreground text-xs">{CEO_LOGIN_HISTORY_COPY.IP_ADDRESS}</dt>
            <dd className="text-sm">{item.ipAddress ?? CEO_LOGIN_HISTORY_COPY.VALUE_ABSENT}</dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-muted-foreground text-xs">{CEO_LOGIN_HISTORY_COPY.USER_AGENT}</dt>
            {/* 서버가 500자로 절단해 저장한 원문 — 파싱하지 않고 그대로 보여주되 긴 문자열이 넘치지 않게 감싼다. */}
            <dd className="break-all text-sm">{item.userAgent ?? CEO_LOGIN_HISTORY_COPY.VALUE_ABSENT}</dd>
          </div>
          {item.failureReasonName && (
            <div className="flex flex-col gap-1">
              <dt className="text-muted-foreground text-xs">{CEO_LOGIN_HISTORY_COPY.FAILURE_REASON}</dt>
              <dd className="text-sm">{item.failureReasonName}</dd>
            </div>
          )}
        </dl>
      </AccordionContent>
    </AccordionItem>
  );
}
