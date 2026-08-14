"use client";

import * as React from "react";

import { AspectRatio } from "@/components/ui/aspect-ratio";
import { Carousel, CarouselContent, CarouselItem, CarouselNext, CarouselPrevious } from "@/components/ui/carousel";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { SHOP_REVIEW_COPY } from "@/feature/shop-review/message";
import { cn } from "@/lib/utils";

/**
 * 리뷰 사진 썸네일 + 확대 Dialog.
 *
 * `next/image` 를 쓰지 않는다 — 외부 호스트에 `remotePatterns` 가 설정돼 있지 않다
 * (`shop-image-preview.tsx` 와 같은 판단). 로드 실패가 정상 시나리오에 포함되므로
 * `onError` 폴백을 **`src` 를 키로** 관리한다. 인덱스를 키로 쓰면 목록이 재정렬될 때
 * 엉뚱한 사진이 실패 상태로 표시된다.
 */

interface ReviewImageDialogProps {
  imageUrls: string[];
  /** 썸네일 그리드에 적용할 클래스 */
  className?: string;
}

function useFailedImages() {
  const [failedSrcs, setFailedSrcs] = React.useState<ReadonlySet<string>>(() => new Set());

  const markFailed = React.useCallback((src: string) => {
    setFailedSrcs((previous) => {
      if (previous.has(src)) return previous;
      const next = new Set(previous);
      next.add(src);
      return next;
    });
  }, []);

  return { failedSrcs, markFailed };
}

export function ReviewImageDialog({ imageUrls, className }: ReviewImageDialogProps) {
  const [openedIndex, setOpenedIndex] = React.useState<number | null>(null);
  const { failedSrcs, markFailed } = useFailedImages();

  if (imageUrls.length === 0) return null;

  return (
    <>
      <ul className={cn("flex flex-wrap gap-2", className)}>
        {imageUrls.map((url, index) => (
          <li key={`${url}-${index}`} className="w-20">
            <button
              type="button"
              className="block w-full cursor-pointer rounded-md focus-visible:outline-2 focus-visible:outline-ring focus-visible:outline-offset-2"
              onClick={() => setOpenedIndex(index)}
            >
              <AspectRatio ratio={1} className="overflow-hidden rounded-md border bg-muted">
                {failedSrcs.has(url) ? (
                  <span className="flex size-full items-center justify-center px-1 text-center text-[10px] text-muted-foreground leading-tight">
                    {SHOP_REVIEW_COPY.IMAGE_LOAD_FAILED}
                  </span>
                ) : (
                  // biome-ignore lint/performance/noImgElement: 외부 호스트 이미지, remotePatterns 미설정
                  <img src={url} alt="" className="size-full object-cover" onError={() => markFailed(url)} />
                )}
              </AspectRatio>
            </button>
          </li>
        ))}
      </ul>

      <Dialog open={openedIndex !== null} onOpenChange={(open) => !open && setOpenedIndex(null)}>
        <DialogContent className="sm:max-w-2xl">
          <DialogHeader>
            <DialogTitle>
              {SHOP_REVIEW_COPY.IMAGE_DIALOG_TITLE}
              {imageUrls.length > 1 && openedIndex !== null && (
                <span className="ml-2 font-normal text-muted-foreground text-sm tabular-nums">
                  {openedIndex + 1}
                  {SHOP_REVIEW_COPY.IMAGE_COUNT_SEPARATOR}
                  {imageUrls.length}
                </span>
              )}
            </DialogTitle>
          </DialogHeader>

          {/* 캐러셀은 여러 장일 때만 컨트롤을 붙인다 — 한 장짜리에 좌우 버튼은 혼란만 준다 */}
          <Carousel opts={{ startIndex: openedIndex ?? 0 }} className="w-full">
            <CarouselContent>
              {imageUrls.map((url, index) => (
                <CarouselItem key={`${url}-${index}`}>
                  <AspectRatio ratio={1} className="overflow-hidden rounded-md border bg-muted">
                    {failedSrcs.has(url) ? (
                      <span className="flex size-full items-center justify-center text-muted-foreground text-sm">
                        {SHOP_REVIEW_COPY.IMAGE_LOAD_FAILED}
                      </span>
                    ) : (
                      // biome-ignore lint/performance/noImgElement: 외부 호스트 이미지, remotePatterns 미설정
                      <img src={url} alt="" className="size-full object-contain" onError={() => markFailed(url)} />
                    )}
                  </AspectRatio>
                </CarouselItem>
              ))}
            </CarouselContent>
            {imageUrls.length > 1 && (
              <>
                <CarouselPrevious />
                <CarouselNext />
              </>
            )}
          </Carousel>
        </DialogContent>
      </Dialog>
    </>
  );
}
