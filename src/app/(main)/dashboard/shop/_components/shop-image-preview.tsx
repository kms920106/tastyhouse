"use client";

import * as React from "react";

import { AspectRatio } from "@/components/ui/aspect-ratio";
import { SHOP_BASIC_COPY } from "@/feature/shop/message";
import { cn } from "@/lib/utils";

interface ShopImagePreviewProps {
  /** blob: 미리보기 URL 또는 resolveFileUrl 결과. null 이면 미등록 상태로 렌더한다. */
  src: string | null;
  alt: string;
  className?: string;
  fit?: "cover" | "contain";
}

/**
 * fileId → URL 규칙이 아직 백엔드와 확정되지 않아 로드 실패가 정상 시나리오에 포함된다.
 * 깨진 이미지 아이콘 대신 안내 문구를 노출하기 위해 onError 를 항상 처리한다.
 */
export function ShopImagePreview({ src, alt, className, fit = "cover" }: ShopImagePreviewProps) {
  // src 가 바뀌면 이전 실패 상태를 버리고 새 이미지에 다시 기회를 준다.
  // key 로 상태를 초기화하면 effect 없이 렌더 중에 처리되므로 깜빡임이 없다.
  const [failedSrc, setFailedSrc] = React.useState<string | null>(null);
  const failed = src !== null && failedSrc === src;

  return (
    <AspectRatio ratio={1} className={cn("overflow-hidden rounded-md border bg-muted", className)}>
      {src && !failed ? (
        // blob: 미리보기와 외부 호스트 경로를 함께 다루고 remotePatterns 설정도 없어 next/image 를 쓰지 않는다.
        // biome-ignore lint/performance/noImgElement: blob/외부 호스트 이미지
        <img
          src={src}
          alt={alt}
          className={cn("size-full", fit === "cover" ? "object-cover" : "object-contain")}
          onError={() => setFailedSrc(src)}
        />
      ) : (
        <div className="flex size-full items-center justify-center px-2 text-center text-muted-foreground text-xs">
          {src && failed ? SHOP_BASIC_COPY.IMAGE_LOAD_FAILED : SHOP_BASIC_COPY.NOT_REGISTERED}
        </div>
      )}
    </AspectRatio>
  );
}
