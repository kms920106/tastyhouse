"use client";

import { TriangleAlert } from "lucide-react";

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import type { AvailabilityChangeOutcome } from "@/feature/product/domain";
import { PRODUCT_AVAILABILITY_COPY } from "@/feature/product/message";

interface MenuBoardFailureNoticeProps {
  failures: AvailabilityChangeOutcome["failed"];
  onDismiss: () => void;
}

/**
 * 삭제 부분실패 안내.
 *
 * 실패 목록을 토스트에 묻지 않고 **화면에 남긴다** — 항목이 여러 개면 토스트로는 다 읽을 수 없고,
 * 사라진 뒤에는 어떤 메뉴가 왜 남았는지 확인할 방법이 없다.
 * 사유(`PRODUCT_LAST_VISIBLE_CANNOT_HIDE` 등)는 서버가 내려준 한국어 `message` 를 그대로 나열한다.
 */
export function MenuBoardFailureNotice({ failures, onDismiss }: MenuBoardFailureNoticeProps) {
  if (failures.length === 0) return null;

  return (
    <Alert variant="destructive">
      <TriangleAlert />
      <AlertTitle>
        {PRODUCT_AVAILABILITY_COPY.FAILURE_NOTICE_TITLE} {failures.length}
        {PRODUCT_AVAILABILITY_COPY.COUNT_UNIT}
      </AlertTitle>
      <AlertDescription className="flex flex-col gap-2">
        <p>{PRODUCT_AVAILABILITY_COPY.FAILURE_NOTICE_DESCRIPTION}</p>
        <ul className="flex list-disc flex-col gap-1 pl-4">
          {failures.map((failure) => (
            <li key={`${failure.id}-${failure.errorCode}`}>
              <span className="font-medium">{failure.name}</span> — {failure.message}
            </li>
          ))}
        </ul>
        <div>
          <Button type="button" size="sm" variant="outline" onClick={onDismiss}>
            {PRODUCT_AVAILABILITY_COPY.FAILURE_NOTICE_DISMISS}
          </Button>
        </div>
      </AlertDescription>
    </Alert>
  );
}
