"use client";

import { PackageX } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import type { AvailabilityOptionGroup, AvailabilityOptionRow, OptionSelection } from "@/feature/product/domain";
import { formatPrice, formatSoldOutUntil } from "@/feature/product/format";
import { PRODUCT_AVAILABILITY_COPY, PRODUCT_MESSAGE } from "@/feature/product/message";
import { cn } from "@/lib/utils";

import { optionSelectionKey } from "./use-availability-mutation";

interface AvailabilityOptionListProps {
  groups?: AvailabilityOptionGroup[];
  errorMessage?: string;
  filtered: boolean;
  /** 키는 `optionType:optionId` — id 만으로는 일반/공통 옵션이 겹친다 */
  selected: ReadonlyMap<string, OptionSelection>;
  disabled?: boolean;
  onSelectionChange: (next: ReadonlyMap<string, OptionSelection>) => void;
  onReleaseRow: (row: AvailabilityOptionRow) => void;
  onChangePeriod: (row: AvailabilityOptionRow) => void;
}

export function AvailabilityOptionList({
  groups,
  errorMessage,
  filtered,
  selected,
  disabled,
  onSelectionChange,
  onReleaseRow,
  onChangePeriod,
}: AvailabilityOptionListProps) {
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

  const allRows = groups.flatMap((group) => group.options);

  if (allRows.length === 0) {
    return (
      <Empty>
        <EmptyHeader>
          <EmptyMedia variant="icon">
            <PackageX />
          </EmptyMedia>
          <EmptyTitle>
            {filtered ? PRODUCT_AVAILABILITY_COPY.EMPTY_FILTERED : PRODUCT_AVAILABILITY_COPY.EMPTY_OPTION}
          </EmptyTitle>
          {filtered && <EmptyDescription>{PRODUCT_AVAILABILITY_COPY.EMPTY_DESCRIPTION}</EmptyDescription>}
        </EmptyHeader>
      </Empty>
    );
  }

  function toggleRow(row: AvailabilityOptionRow, checked: boolean) {
    const next = new Map(selected);
    const key = optionSelectionKey(row);
    if (checked) next.set(key, { optionId: row.id, optionType: row.optionType });
    else next.delete(key);
    onSelectionChange(next);
  }

  function toggleGroup(rows: AvailabilityOptionRow[], checked: boolean) {
    const next = new Map(selected);
    for (const row of rows) {
      const key = optionSelectionKey(row);
      if (checked) next.set(key, { optionId: row.id, optionType: row.optionType });
      else next.delete(key);
    }
    onSelectionChange(next);
  }

  return (
    <div className="flex flex-col gap-8">
      {groups.map((group) => {
        const selectedInGroup = group.options.filter((row) => selected.has(optionSelectionKey(row))).length;
        const groupChecked =
          selectedInGroup === 0 ? false : selectedInGroup === group.options.length ? true : ("indeterminate" as const);
        // 일반/공통 옵션그룹은 id 시퀀스가 달라 key 에도 갈래를 섞는다.
        const groupKey = `${group.optionType}:${group.optionGroupId}`;

        return (
          <section key={groupKey} className="flex flex-col">
            <div className="flex flex-col gap-1 border-b pb-3">
              <div className="flex flex-wrap items-center gap-2">
                <Checkbox
                  id={`availability-option-group-${groupKey}`}
                  checked={groupChecked}
                  disabled={disabled === true || group.options.length === 0}
                  onCheckedChange={(checked) => toggleGroup(group.options, checked === true)}
                />
                <label htmlFor={`availability-option-group-${groupKey}`} className="text-sm font-medium">
                  {group.name}
                </label>
                {group.required && <Badge variant="destructive">{PRODUCT_AVAILABILITY_COPY.BADGE_REQUIRED}</Badge>}
                {group.optionType === "COMMON" && (
                  <Badge variant="outline">{PRODUCT_AVAILABILITY_COPY.BADGE_COMMON_OPTION}</Badge>
                )}
                <SelectRangeLabel minSelect={group.minSelect} maxSelect={group.maxSelect} />
              </div>

              {group.linkedProductNames.length > 0 && (
                <p className="text-muted-foreground truncate text-sm">
                  {PRODUCT_AVAILABILITY_COPY.LINKED_PRODUCTS} {group.linkedProductNames.join(", ")}
                </p>
              )}
            </div>

            {group.options.map((row) => (
              <OptionRow
                key={optionSelectionKey(row)}
                row={row}
                checked={selected.has(optionSelectionKey(row))}
                disabled={disabled}
                onCheckedChange={(checked) => toggleRow(row, checked)}
                onRelease={() => onReleaseRow(row)}
                onChangePeriod={() => onChangePeriod(row)}
              />
            ))}
          </section>
        );
      })}
    </div>
  );
}

function SelectRangeLabel({ minSelect, maxSelect }: { minSelect: number | null; maxSelect: number | null }) {
  if (minSelect === null && maxSelect === null) return null;

  const parts: string[] = [];
  if (minSelect !== null) {
    parts.push(
      `${PRODUCT_AVAILABILITY_COPY.OPTION_SELECT_RANGE_MIN} ${minSelect}${PRODUCT_AVAILABILITY_COPY.OPTION_SELECT_RANGE_UNIT}`,
    );
  }
  if (maxSelect !== null) {
    parts.push(
      `${PRODUCT_AVAILABILITY_COPY.OPTION_SELECT_RANGE_MAX} ${maxSelect}${PRODUCT_AVAILABILITY_COPY.OPTION_SELECT_RANGE_UNIT}`,
    );
  }

  return <span className="text-muted-foreground text-sm">{parts.join(" · ")}</span>;
}

interface OptionRowProps {
  row: AvailabilityOptionRow;
  checked: boolean;
  disabled?: boolean;
  onCheckedChange: (checked: boolean) => void;
  onRelease: () => void;
  onChangePeriod: () => void;
}

function OptionRow({ row, checked, disabled, onCheckedChange, onRelease, onChangePeriod }: OptionRowProps) {
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

      <div className="flex min-w-0 flex-1 flex-col gap-1">
        <div className="flex flex-wrap items-center gap-2">
          {/* 숨김 행은 흐리게 — 판매중 행과 한눈에 구분되게 한다 */}
          <span className={cn("text-sm font-medium", !row.visible && "text-muted-foreground")}>{row.name}</span>
          {row.soldOut && <Badge variant="destructive">{PRODUCT_AVAILABILITY_COPY.BADGE_SOLD_OUT}</Badge>}
          {!row.visible && <Badge variant="secondary">{PRODUCT_AVAILABILITY_COPY.BADGE_HIDDEN}</Badge>}
        </div>

        <span className="text-muted-foreground text-sm">
          {PRODUCT_AVAILABILITY_COPY.ADDITIONAL_PRICE_PREFIX}
          {formatPrice(row.additionalPrice)}
        </span>

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
