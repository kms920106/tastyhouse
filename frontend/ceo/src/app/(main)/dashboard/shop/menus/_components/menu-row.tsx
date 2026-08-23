"use client";

import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { GripVertical, X } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import type { MenuBoardRow } from "@/feature/product/domain";
import { formatPrice } from "@/feature/product/format";
import { PRODUCT_AVAILABILITY_COPY, PRODUCT_EXCLUDE_COPY, PRODUCT_MENU_COPY } from "@/feature/product/message";
import { cn } from "@/lib/utils";

import { toMenuDragId } from "./use-menu-sort";

interface MenuRowProps {
  row: MenuBoardRow;
  checked: boolean;
  disabled?: boolean;
  onCheckedChange: (checked: boolean) => void;
  onOpenDetail: () => void;
  /** 메뉴판에서 제외(링크 해제). 삭제와 다른 동작이다 */
  onExclude: () => void;
}

/**
 * 메뉴 한 줄.
 *
 * 드래그 손잡이(`≡`)에만 `listeners` 를 붙인다 — 행 전체를 드래그 대상으로 만들면 체크박스·버튼을
 * 누를 때마다 드래그가 시작돼 다중 선택이 사실상 불가능해진다.
 */
export function MenuRow({ row, checked, disabled, onCheckedChange, onOpenDetail, onExclude }: MenuRowProps) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: toMenuDragId(row.id),
    disabled,
  });

  return (
    <div
      ref={setNodeRef}
      style={{ transform: CSS.Translate.toString(transform), transition }}
      className={cn(
        "bg-background flex items-start gap-3 border-b py-3 last:border-b-0",
        // 끌고 있는 행은 반투명하게 띄워 놓은 자리를 가늠할 수 있게 한다.
        isDragging && "relative z-10 opacity-60 shadow-sm",
      )}
    >
      <button
        type="button"
        className="text-muted-foreground hover:text-foreground mt-1 cursor-grab touch-none disabled:cursor-not-allowed disabled:opacity-50"
        aria-label={PRODUCT_MENU_COPY.DRAG_HANDLE_LABEL}
        disabled={disabled}
        {...attributes}
        {...listeners}
      >
        <GripVertical className="size-4" />
      </button>

      <Checkbox
        className="mt-1"
        checked={checked}
        disabled={disabled}
        aria-label={row.name}
        onCheckedChange={(next) => onCheckedChange(next === true)}
      />

      {/* 서버가 완성해 내려준 절대 URL 을 그대로 쓴다(응답에 fileId 를 노출하지 않는 규칙).
          외부 호스트 경로이고 `remotePatterns` 설정이 없어 next/image 를 쓰지 않는다 —
          `availability-menu-list.tsx` 와 같은 판단이다. */}
      {row.imageUrl ? (
        // biome-ignore lint/performance/noImgElement: 외부 호스트 이미지
        <img src={row.imageUrl} alt="" className="size-12 shrink-0 rounded-md border object-cover" />
      ) : (
        <div className="bg-muted size-12 shrink-0 rounded-md border" />
      )}

      <div className="flex min-w-0 flex-1 flex-col gap-1">
        <div className="flex flex-wrap items-center gap-2">
          {/* 숨김 행은 흐리게 — 판매중 행과 한눈에 구분되게 한다 */}
          <span className={cn("text-sm font-medium", !row.visible && "text-muted-foreground")}>{row.name}</span>
          {row.soldOut && <Badge variant="destructive">{PRODUCT_AVAILABILITY_COPY.BADGE_SOLD_OUT}</Badge>}
          {!row.visible && <Badge variant="secondary">{PRODUCT_AVAILABILITY_COPY.BADGE_HIDDEN}</Badge>}
          {row.representative && <Badge variant="outline">{PRODUCT_AVAILABILITY_COPY.BADGE_REPRESENTATIVE}</Badge>}
        </div>

        <div className="text-muted-foreground flex items-center gap-2 text-sm">
          {row.discountPrice !== null ? (
            <>
              <span className="line-through">{formatPrice(row.originalPrice)}</span>
              <span className="text-foreground">{formatPrice(row.discountPrice)}</span>
            </>
          ) : (
            <span>{formatPrice(row.originalPrice)}</span>
          )}
        </div>
      </div>

      <Button type="button" size="sm" variant="outline" disabled={disabled} onClick={onOpenDetail}>
        {PRODUCT_MENU_COPY.BUTTON_DETAIL}
      </Button>

      {/* 메뉴판에서 제외 — 링크만 끊는다. 소프트 삭제(일괄 삭제)와 다른 동작이라
          `destructive` 가 아닌 `ghost` 로 두어 무게를 구분한다. */}
      <Button
        type="button"
        size="sm"
        variant="ghost"
        disabled={disabled}
        aria-label={PRODUCT_EXCLUDE_COPY.ACTION}
        title={PRODUCT_EXCLUDE_COPY.ACTION}
        onClick={onExclude}
      >
        <X className="size-4" />
      </Button>
    </div>
  );
}
