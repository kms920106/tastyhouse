"use client";

import * as React from "react";

import { toast } from "sonner";

import { StatusBadge } from "@/components/status-badge";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Textarea } from "@/components/ui/textarea";
import { cancelShopRequestAction, createShopRequestCommentAction } from "@/feature/shop/actions";
import type { ShopRequestComment, ShopRequestDetail } from "@/feature/shop/domain";
import { SHOP_REQUEST_COPY } from "@/feature/shop/message";
import { formatDateTime } from "@/lib/date";

import { ShopImagePreview } from "../../_components/shop-image-preview";

/** 서버 `@Size(max = 1000)` 과 같은 값 — 액션에서도 다시 검사한다 */
const COMMENT_MAX_LENGTH = 1000;

/** 취소는 대기중일 때만 가능하다(`docs/tasks/backend.md` 2-3) */
const CANCELABLE_STATUS = "PENDING";

interface ShopRequestDetailSheetProps {
  shopId: number;
  detail: ShopRequestDetail;
  comments: ShopRequestComment[];
  /** 문의 스레드만 실패했을 때의 안내 */
  commentsFailed?: boolean;
  onClose: () => void;
}

/** 라벨-값 한 쌍. 호출부가 값 없음을 null 로 넘기면 `VALUE_ABSENT` 로 채운다 */
function DetailRow({ label, value }: { label: string; value: React.ReactNode | null }) {
  return (
    <div className="flex flex-col gap-1">
      <dt className="text-muted-foreground text-xs">{label}</dt>
      <dd className="text-sm">{value ?? SHOP_REQUEST_COPY.VALUE_ABSENT}</dd>
    </div>
  );
}

export function ShopRequestDetailSheet({
  shopId,
  detail,
  comments,
  commentsFailed = false,
  onClose,
}: ShopRequestDetailSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [content, setContent] = React.useState("");

  const trimmedContent = content.trim();
  const isCommentSubmittable = trimmedContent.length > 0 && trimmedContent.length <= COMMENT_MAX_LENGTH;

  function handleCancel() {
    startTransition(async () => {
      const { success, message } = await cancelShopRequestAction(shopId, detail.requestId);
      if (success) {
        toast.success(SHOP_REQUEST_COPY.CANCEL_SUCCESS);
      } else {
        toast.error(message ?? SHOP_REQUEST_COPY.CANCEL_FAILED);
      }
    });
  }

  function handleSubmitComment() {
    startTransition(async () => {
      const { success, message } = await createShopRequestCommentAction(shopId, detail.requestId, content);
      if (success) {
        // 입력만 비우고 시트는 열어 둔다 — 등록한 문의가 스레드에 붙는 것을 바로 확인해야 한다.
        setContent("");
        toast.success(SHOP_REQUEST_COPY.COMMENT_SUCCESS);
      } else {
        toast.error(message ?? SHOP_REQUEST_COPY.COMMENT_FAILED);
      }
    });
  }

  return (
    <Sheet open onOpenChange={(open) => !open && onClose()}>
      <SheetContent className="flex flex-col gap-0 overflow-y-auto">
        <SheetHeader>
          <SheetTitle className="flex flex-wrap items-center gap-2">
            <span className="min-w-0 flex-1">{detail.summary}</span>
            <StatusBadge status={detail.status} label={detail.statusDescription} />
          </SheetTitle>
          <SheetDescription>{detail.requestTypeDescription}</SheetDescription>
        </SheetHeader>

        <div className="flex flex-col gap-6 px-4 pb-6">
          {/* ===== 1. 공통 정보 ===== */}
          <dl className="flex flex-col gap-3">
            <DetailRow label={SHOP_REQUEST_COPY.REQUESTED_AT} value={formatDateTime(detail.requestedAt)} />
            <DetailRow
              label={SHOP_REQUEST_COPY.PROCESSED_AT}
              value={detail.processedAt ? formatDateTime(detail.processedAt) : null}
            />
            {detail.rejectReason && (
              <div className="flex flex-col gap-1">
                <dt className="text-muted-foreground text-xs">{SHOP_REQUEST_COPY.REJECT_REASON}</dt>
                {/* 관리자가 자유 입력한 사유라 줄바꿈을 살린다. */}
                <dd className="whitespace-pre-line text-destructive text-sm">{detail.rejectReason}</dd>
              </div>
            )}
            <DetailRow
              label={SHOP_REQUEST_COPY.CONTRACT_AMENDING}
              value={
                detail.contractAmending
                  ? SHOP_REQUEST_COPY.CONTRACT_AMENDING_YES
                  : SHOP_REQUEST_COPY.CONTRACT_AMENDING_NO
              }
            />
          </dl>

          {/* ===== 2. 첨부 ===== */}
          {detail.attachmentUrl && (
            <>
              <Separator />
              <section className="flex flex-col gap-2">
                <span className="font-medium text-sm">{SHOP_REQUEST_COPY.ATTACHMENT}</span>
                <a
                  href={detail.attachmentUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-primary text-sm underline underline-offset-4"
                >
                  {detail.attachmentLabel ?? SHOP_REQUEST_COPY.ATTACHMENT}
                </a>
              </section>
            </>
          )}

          {/* ===== 3. 유형별 블록 — 서버가 한쪽만 채워 내려준다 ===== */}
          {detail.imageChange && (
            <>
              <Separator />
              <section className="flex flex-col gap-2">
                <span className="font-medium text-sm">{detail.imageChange.imageTypeDescription}</span>
                {/* remotePatterns 설정이 없어 next/image 대신 공용 미리보기를 쓴다(로드 실패도 정상 시나리오). */}
                <ShopImagePreview
                  src={detail.imageChange.imageUrl}
                  alt={detail.imageChange.imageTypeDescription}
                  className="w-40"
                  fit="contain"
                />
              </section>
            </>
          )}

          {detail.deliveryAreaAdjustment && (
            <>
              <Separator />
              <section className="flex flex-col gap-2">
                <span className="font-medium text-sm">{SHOP_REQUEST_COPY.ADJUSTMENT_TITLE}</span>
                <dl className="flex flex-col gap-3">
                  <DetailRow
                    label={SHOP_REQUEST_COPY.ADJUSTMENT_COUNTERPART_SHOP}
                    value={detail.deliveryAreaAdjustment.counterpartShopName}
                  />
                  <DetailRow
                    label={SHOP_REQUEST_COPY.ADJUSTMENT_BUSINESS_NUMBER}
                    value={detail.deliveryAreaAdjustment.counterpartBusinessNumber}
                  />
                  <DetailRow
                    label={SHOP_REQUEST_COPY.ADJUSTMENT_FRANCHISE}
                    value={detail.deliveryAreaAdjustment.franchiseName}
                  />
                  <div className="flex flex-col gap-1">
                    <dt className="text-muted-foreground text-xs">{SHOP_REQUEST_COPY.ADJUSTMENT_REASON}</dt>
                    <dd className="whitespace-pre-line text-sm">{detail.deliveryAreaAdjustment.reason}</dd>
                  </div>
                </dl>
              </section>
            </>
          )}

          {/* ===== 4. 취소 — 대기중일 때만 ===== */}
          {detail.status === CANCELABLE_STATUS && (
            <>
              <Separator />
              <AlertDialog>
                <AlertDialogTrigger asChild>
                  <Button type="button" variant="destructive" disabled={isPending}>
                    {SHOP_REQUEST_COPY.CANCEL_ACTION}
                  </Button>
                </AlertDialogTrigger>
                <AlertDialogContent>
                  <AlertDialogHeader>
                    <AlertDialogTitle>{SHOP_REQUEST_COPY.CANCEL_CONFIRM_TITLE}</AlertDialogTitle>
                    <AlertDialogDescription>{SHOP_REQUEST_COPY.CANCEL_CONFIRM_DESCRIPTION}</AlertDialogDescription>
                  </AlertDialogHeader>
                  <AlertDialogFooter>
                    <AlertDialogCancel>{SHOP_REQUEST_COPY.CANCEL_CONFIRM_DISMISS}</AlertDialogCancel>
                    <AlertDialogAction onClick={handleCancel}>
                      {SHOP_REQUEST_COPY.CANCEL_CONFIRM_ACTION}
                    </AlertDialogAction>
                  </AlertDialogFooter>
                </AlertDialogContent>
              </AlertDialog>
            </>
          )}

          {/* ===== 5. 문의 스레드 ===== */}
          <Separator />
          <section className="flex flex-col gap-3">
            <span className="font-medium text-sm">{SHOP_REQUEST_COPY.COMMENT_SECTION_TITLE}</span>

            {commentsFailed ? (
              <p className="text-destructive text-sm">{SHOP_REQUEST_COPY.COMMENT_LOAD_FAILED}</p>
            ) : comments.length === 0 ? (
              <p className="rounded-md border border-dashed p-4 text-center text-muted-foreground text-sm">
                {SHOP_REQUEST_COPY.COMMENT_EMPTY}
              </p>
            ) : (
              <ul className="flex flex-col gap-2">
                {comments.map((comment) => (
                  <li key={comment.commentId} className="flex flex-col gap-1 rounded-md border p-3">
                    <div className="flex items-center gap-2">
                      {/* 작성자 실명은 서버가 내려주지 않는다 — 유형 라벨로만 구성한다. */}
                      <Badge variant={comment.authorType === "ADMIN" ? "secondary" : "outline"}>
                        {comment.authorTypeDescription}
                      </Badge>
                      <span className="ml-auto text-muted-foreground text-xs">{formatDateTime(comment.createdAt)}</span>
                    </div>
                    <p className="whitespace-pre-line text-sm">{comment.content}</p>
                  </li>
                ))}
              </ul>
            )}

            <div className="flex flex-col gap-2">
              <Textarea
                value={content}
                onChange={(event) => setContent(event.target.value)}
                placeholder={SHOP_REQUEST_COPY.COMMENT_PLACEHOLDER}
                maxLength={COMMENT_MAX_LENGTH}
                disabled={isPending}
                rows={3}
              />
              <Button
                type="button"
                className="self-end"
                onClick={handleSubmitComment}
                disabled={isPending || !isCommentSubmittable}
              >
                {SHOP_REQUEST_COPY.COMMENT_SUBMIT}
              </Button>
            </div>
          </section>
        </div>
      </SheetContent>
    </Sheet>
  );
}
