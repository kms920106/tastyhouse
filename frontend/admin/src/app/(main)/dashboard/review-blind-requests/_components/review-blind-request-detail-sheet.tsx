"use client";

import * as React from "react";

import { FileText, Star } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
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
import { REVIEW_BLIND_REASON_LABEL, REVIEW_BLIND_REQUEST_STATUS_LABEL } from "@/feature/review-blind-request/constants";
import type { ReviewBlindRequestDetail } from "@/feature/review-blind-request/domain";
import { REVIEW_BLIND_REQUEST_DETAIL_COPY, REVIEW_BLIND_REQUEST_MESSAGE } from "@/feature/review-blind-request/message";
import { formatDateTime } from "@/lib/date";

interface ReviewBlindRequestDetailSheetProps {
  /** ?requestId= 로 열린 요청 ID. null 이면 닫힌 상태. */
  requestId: number | null;
  /** page.tsx 가 서버에서 조회해 내려준 상세 (repository 가 server-only 라 클라이언트 fetch 불가). */
  detail?: ReviewBlindRequestDetail;
  /** 상세 조회 실패 문구. */
  error?: string;
  onOpenChange: (open: boolean) => void;
}

export function ReviewBlindRequestDetailSheet({
  requestId,
  detail,
  error,
  onOpenChange,
}: ReviewBlindRequestDetailSheetProps) {
  const [failedImageUrls, setFailedImageUrls] = React.useState<string[]>([]);

  return (
    <Sheet open={requestId != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>{REVIEW_BLIND_REQUEST_DETAIL_COPY.TITLE}</SheetTitle>
          <SheetDescription>{REVIEW_BLIND_REQUEST_DETAIL_COPY.DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-5 overflow-y-auto px-4">
          {error ? (
            <p className="text-destructive text-sm">{error}</p>
          ) : detail ? (
            <>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h3 className="font-semibold text-lg leading-snug">{detail.shopName}</h3>
                  <p className="text-muted-foreground text-sm tabular-nums">{formatDateTime(detail.createdAt)} 요청</p>
                </div>
                <Badge variant="outline">
                  {detail.statusDescription || REVIEW_BLIND_REQUEST_STATUS_LABEL[detail.status]}
                </Badge>
              </div>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">요청 사유</dt>
                <dd>{detail.reasonDescription || REVIEW_BLIND_REASON_LABEL[detail.reason]}</dd>
                <dt className="text-muted-foreground">상세 사유</dt>
                <dd className="whitespace-pre-wrap">{detail.detailReason ?? "-"}</dd>
                <dt className="text-muted-foreground">반려 사유</dt>
                <dd className="whitespace-pre-wrap">{detail.rejectReason ?? "-"}</dd>
                {/* 기한은 승인 상태에서만 의미가 있다 — 그 외 상태에서는 서버가 null 을 준다. */}
                {detail.status === "APPROVED" && detail.blindUntil ? (
                  <>
                    <dt className="text-muted-foreground">{REVIEW_BLIND_REQUEST_DETAIL_COPY.BLIND_UNTIL_LABEL}</dt>
                    <dd className="tabular-nums">{formatDateTime(detail.blindUntil)}</dd>
                  </>
                ) : null}
              </dl>

              <Separator />

              {/* 증빙 서류는 PDF 가 섞여 있어 이미지로 렌더할 수 없다 — 새 탭 링크로만 연다. */}
              <div className="space-y-2">
                <h4 className="font-medium text-sm">{REVIEW_BLIND_REQUEST_DETAIL_COPY.ATTACHMENT_TITLE}</h4>
                {detail.attachmentUrls.length ? (
                  <ul className="space-y-1">
                    {detail.attachmentUrls.map((url, index) => (
                      <li key={url}>
                        <a
                          href={url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="inline-flex items-center gap-1.5 text-primary text-sm underline underline-offset-4"
                        >
                          <FileText className="size-4 shrink-0" />
                          {`${REVIEW_BLIND_REQUEST_DETAIL_COPY.ATTACHMENT_ITEM_PREFIX} ${index + 1}`}
                        </a>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="text-muted-foreground text-sm">{REVIEW_BLIND_REQUEST_DETAIL_COPY.NO_ATTACHMENT}</p>
                )}
              </div>

              <Separator />

              <div className="space-y-3">
                <div className="flex items-center gap-2">
                  <span className="flex items-center gap-1 font-medium tabular-nums">
                    <Star className="size-4 fill-current text-amber-500" />
                    {detail.reviewTotalRating.toFixed(1)}
                  </span>
                  <span className="text-muted-foreground text-sm">{detail.reviewMemberNickname}</span>
                  {detail.reviewHidden ? <Badge variant="secondary">숨김</Badge> : null}
                </div>
                <p className="whitespace-pre-wrap text-sm">{detail.reviewContent}</p>
                <p className="text-muted-foreground text-xs tabular-nums">
                  {formatDateTime(detail.reviewCreatedAt)} 작성
                </p>

                {detail.reviewImageUrls.length ? (
                  <div className="flex flex-wrap gap-2">
                    {detail.reviewImageUrls.map((url) =>
                      failedImageUrls.includes(url) ? (
                        <div
                          key={url}
                          className="flex size-24 items-center justify-center rounded-md border p-2 text-center text-muted-foreground text-xs"
                        >
                          {REVIEW_BLIND_REQUEST_DETAIL_COPY.IMAGE_LOAD_FAILED}
                        </div>
                      ) : (
                        <a key={url} href={url} target="_blank" rel="noreferrer" className="block size-24">
                          {/* biome-ignore lint/performance/noImgElement: 외부 호스트 이미지, remotePatterns 미설정 */}
                          <img
                            src={url}
                            alt="리뷰 사진"
                            className="size-24 rounded-md border object-cover"
                            onError={() => setFailedImageUrls((previous) => [...previous, url])}
                          />
                        </a>
                      ),
                    )}
                  </div>
                ) : (
                  <p className="text-muted-foreground text-sm">{REVIEW_BLIND_REQUEST_DETAIL_COPY.NO_IMAGE}</p>
                )}
              </div>
            </>
          ) : (
            <p className="text-muted-foreground text-sm">{REVIEW_BLIND_REQUEST_MESSAGE.DETAIL_LOAD_FAILED}</p>
          )}
        </div>

        <SheetFooter>
          <SheetClose asChild>
            <Button variant="outline">닫기</Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
