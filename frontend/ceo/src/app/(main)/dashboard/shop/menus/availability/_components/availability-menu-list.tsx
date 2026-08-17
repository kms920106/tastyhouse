"use client";

import { PackageX } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import type { AvailabilityMenuGroup, AvailabilityMenuRow } from "@/feature/product/domain";
import { formatPrice, formatSoldOutUntil } from "@/feature/product/format";
import { PRODUCT_AVAILABILITY_COPY, PRODUCT_MESSAGE } from "@/feature/product/message";
import { cn } from "@/lib/utils";

interface AvailabilityMenuListProps {
  /** 조회 실패 시 undefined — 필터바는 살아있어야 하므로 목록만 안내로 대체한다 */
  groups?: AvailabilityMenuGroup[];
  errorMessage?: string;
  /** 검색·필터가 걸린 상태인지. 빈 목록의 문구를 가른다 */
  filtered: boolean;
  selectedIds: ReadonlySet<number>;
  disabled?: boolean;
  onSelectionChange: (next: ReadonlySet<number>) => void;
  onReleaseRow: (row: AvailabilityMenuRow) => void;
  onChangePeriod: (productId: number) => void;
}

export function AvailabilityMenuList({
  groups,
  errorMessage,
  filtered,
  selectedIds,
  disabled,
  onSelectionChange,
  onReleaseRow,
  onChangePeriod,
}: AvailabilityMenuListProps) {
  if (groups === undefined) {
    return (
      <Empty>
        <EmptyHeader>
          <EmptyMedia variant="icon">
            <PackageX />
          </EmptyMedia>
          <EmptyTitle>{PRODUCT_MESSAGE.LOAD_FAILED}</EmptyTitle>
          {errorMessage && <EmptyDescription>{errorMessage}</EmptyDescription>}
        </EmptyHeader>
      </Empty>
    );
  }

  const allRows = groups.flatMap((group) => group.products);

  if (allRows.length === 0) {
    return (
      <Empty>
        <EmptyHeader>
          <EmptyMedia variant="icon">
            <PackageX />
          </EmptyMedia>
          <EmptyTitle>
            {filtered ? PRODUCT_AVAILABILITY_COPY.EMPTY_FILTERED : PRODUCT_AVAILABILITY_COPY.EMPTY}
          </EmptyTitle>
          {filtered && <EmptyDescription>{PRODUCT_AVAILABILITY_COPY.EMPTY_DESCRIPTION}</EmptyDescription>}
        </EmptyHeader>
      </Empty>
    );
  }

  function toggleRow(id: number, checked: boolean) {
    const next = new Set(selectedIds);
    if (checked) next.add(id);
    else next.delete(id);
    onSelectionChange(next);
  }

  /** 그룹 헤더 체크박스는 하위 전체를 한 번에 선택/해제한다(원문 PDF) */
  function toggleGroup(rows: AvailabilityMenuRow[], checked: boolean) {
    const next = new Set(selectedIds);
    for (const row of rows) {
      if (checked) next.add(row.id);
      else next.delete(row.id);
    }
    onSelectionChange(next);
  }

  return (
    <div className="flex flex-col gap-8">
      {groups.map((group) => {
        const selectedInGroup = group.products.filter((row) => selectedIds.has(row.id)).length;
        // 하위가 일부만 선택된 상태는 indeterminate 로 표시한다.
        const groupChecked =
          selectedInGroup === 0 ? false : selectedInGroup === group.products.length ? true : ("indeterminate" as const);
        const groupKey = group.categoryId ?? `uncategorized-${group.sort}`;

        return (
          <section key={groupKey} className="flex flex-col">
            <div className="flex items-center gap-2 border-b pb-3">
              <Checkbox
                id={`availability-menu-group-${groupKey}`}
                checked={groupChecked}
                disabled={disabled === true || group.products.length === 0}
                onCheckedChange={(checked) => toggleGroup(group.products, checked === true)}
              />
              <label htmlFor={`availability-menu-group-${groupKey}`} className="text-sm font-medium">
                {group.categoryName ?? PRODUCT_AVAILABILITY_COPY.NO_CATEGORY}
              </label>
              <span className="text-muted-foreground text-sm">
                {group.products.length}
                {PRODUCT_AVAILABILITY_COPY.COUNT_UNIT}
              </span>
            </div>

            {group.products.map((row) => (
              <MenuRow
                key={row.id}
                row={row}
                checked={selectedIds.has(row.id)}
                disabled={disabled}
                onCheckedChange={(checked) => toggleRow(row.id, checked)}
                onRelease={() => onReleaseRow(row)}
                onChangePeriod={() => onChangePeriod(row.id)}
              />
            ))}
          </section>
        );
      })}
    </div>
  );
}

interface MenuRowProps {
  row: AvailabilityMenuRow;
  checked: boolean;
  disabled?: boolean;
  onCheckedChange: (checked: boolean) => void;
  onRelease: () => void;
  onChangePeriod: () => void;
}

function MenuRow({ row, checked, disabled, onCheckedChange, onRelease, onChangePeriod }: MenuRowProps) {
  const soldOutUntilLabel = formatSoldOutUntil(row.soldOutUntil);
  const isReleasable = row.soldOut || !row.visible;

  return (
    <div className="flex items-start gap-3 border-b py-3 last:border-b-0">
      <Checkbox
        className="mt-1"
        checked={checked}
        disabled={disabled}
        aria-label={row.name}
        onCheckedChange={(next) => onCheckedChange(next === true)}
      />

      {/* 서버가 완성해 내려준 절대 URL 을 그대로 쓴다(응답에 fileId 를 노출하지 않는 규칙).
          외부 호스트 경로이고 `remotePatterns` 설정이 없어 next/image 를 쓰지 않는다 —
          `shop-image-preview.tsx` 와 같은 판단이다. */}
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

        {row.soldOut && (
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-muted-foreground text-sm">
              {soldOutUntilLabel ?? PRODUCT_AVAILABILITY_COPY.SOLD_OUT_INDEFINITE}
            </span>
            <Button type="button" size="sm" variant="outline" disabled={disabled} onClick={onChangePeriod}>
              {soldOutUntilLabel
                ? PRODUCT_AVAILABILITY_COPY.BUTTON_CHANGE_PERIOD
                : PRODUCT_AVAILABILITY_COPY.BUTTON_SET_PERIOD}
            </Button>
          </div>
        )}
      </div>

      {isReleasable && (
        <Button type="button" size="sm" variant="outline" disabled={disabled} onClick={onRelease}>
          {PRODUCT_AVAILABILITY_COPY.BUTTON_RELEASE}
        </Button>
      )}
    </div>
  );
}
