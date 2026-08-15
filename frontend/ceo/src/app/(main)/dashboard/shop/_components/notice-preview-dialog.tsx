"use client";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { SHOP_NOTICE_COPY } from "@/feature/shop-notice/message";

interface NoticePreviewDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  content: string;
  /** 저장된 이미지 URL 또는 첨부 중인 파일의 `blob:` URL */
  imageUrls: string[];
}

/**
 * 고객 화면 미리보기.
 *
 * 저장 전 폼 상태를 그대로 렌더하므로 서버 API 를 쓰지 않는 순수 클라이언트 컴포넌트다.
 * 모바일 폭으로 제한해 web 가게 상세와 비슷하게 보이도록 한다.
 */
export function NoticePreviewDialog({ open, onOpenChange, content, imageUrls }: NoticePreviewDialogProps) {
  const trimmedContent = content.trim();

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-sm">
        <DialogHeader>
          <DialogTitle>{SHOP_NOTICE_COPY.PREVIEW_TITLE}</DialogTitle>
          <DialogDescription>{SHOP_NOTICE_COPY.PREVIEW_DESCRIPTION}</DialogDescription>
        </DialogHeader>

        <div className="relative rounded-md border bg-muted/40 px-4 py-5">
          <span className="absolute -top-3 left-3 rounded-full bg-primary px-3 py-1 text-primary-foreground text-xs">
            {SHOP_NOTICE_COPY.PREVIEW_BADGE}
          </span>
          {trimmedContent.length > 0 ? (
            <p className="whitespace-pre-wrap break-words text-sm">{trimmedContent}</p>
          ) : (
            <p className="text-muted-foreground text-sm">{SHOP_NOTICE_COPY.PREVIEW_EMPTY}</p>
          )}
          {imageUrls.length > 0 && (
            <div className="mt-3 space-y-2">
              {imageUrls.map((imageUrl) => (
                // blob: 미리보기와 외부 호스트 경로를 함께 다루고 remotePatterns 설정도 없어 next/image 를 쓰지 않는다.
                // biome-ignore lint/performance/noImgElement: blob/외부 호스트 이미지
                <img
                  key={imageUrl}
                  src={imageUrl}
                  alt={SHOP_NOTICE_COPY.PREVIEW_BADGE}
                  className="w-full rounded-md object-cover"
                />
              ))}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button type="button" onClick={() => onOpenChange(false)}>
            {SHOP_NOTICE_COPY.PREVIEW_CLOSE}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
