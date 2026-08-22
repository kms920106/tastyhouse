"use client";

import { Badge } from "@/components/ui/badge";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import type { OptionGroupMergePreview, OptionGroupMergePreviewItem } from "@/feature/product/domain";
import { formatPrice } from "@/feature/product/format";
import { OPTION_GROUP_MERGE_COPY, OPTION_GROUP_SCREEN_COPY } from "@/feature/product/message";

interface MergeDiffSheetProps {
  open: boolean;
  /** 조회 실패·미조회 시 undefined — 그때는 Sheet 를 열지 않는다 */
  preview?: OptionGroupMergePreview;
  onOpenChange: (open: boolean) => void;
}

interface MergeDiffItemProps {
  item: OptionGroupMergePreviewItem;
  /** 기준 그룹은 diff 대상이 아니라 비교의 축이므로 강조 표시를 하지 않는다 */
  isBase: boolean;
  base?: OptionGroupMergePreviewItem;
}

/** 기준과 다른 필드에 붙는 강조 배지 */
function DiffMark({ differs }: { differs: boolean }) {
  if (!differs) return null;
  return <Badge variant="destructive">{OPTION_GROUP_MERGE_COPY.DIFF_FIELD_DIFFERS}</Badge>;
}

function MergeDiffItem({ item, isBase, base }: MergeDiffItemProps) {
  return (
    <li className="flex flex-col gap-2 rounded-md border p-3">
      <div className="flex flex-wrap items-center gap-2">
        <Badge variant={isBase ? "default" : "secondary"}>
          {isBase ? OPTION_GROUP_MERGE_COPY.DIFF_BASE_LABEL : OPTION_GROUP_MERGE_COPY.DIFF_CANDIDATE_LABEL}
        </Badge>
        <span className="font-medium text-sm">{item.name}</span>
        {!isBase && <DiffMark differs={item.nameDiffers} />}
      </div>

      <div className="flex flex-wrap items-center gap-2 text-muted-foreground text-xs">
        <span>{OPTION_GROUP_SCREEN_COPY.SELECT_RANGE(item.minSelect, item.maxSelect)}</span>
        {/* 최소/최대는 한 줄에 함께 보여주므로 어느 쪽이 다른지 배지 두 개로 가른다. */}
        {!isBase && <DiffMark differs={item.minSelectDiffers} />}
        {!isBase && <DiffMark differs={item.maxSelectDiffers} />}
      </div>

      {item.linkedProductNames.length > 0 && (
        <p className="text-muted-foreground text-xs">{item.linkedProductNames.join(", ")}</p>
      )}

      <ul className="flex flex-col gap-1">
        {item.options.map((option) => {
          // 가격이 다른 옵션은 양쪽 금액을 나란히 보여줘야 어느 쪽이 남는지 판단할 수 있다.
          //
          // 짝을 이름으로 찾는 것은 서버 응답에 기준 가격이 없어서다. **동명 옵션이 2개 이상이면
          // 어느 쪽과 비교해야 하는지 알 수 없으므로 비교를 생략한다** — 첫 매칭을 쓰면 틀린 금액을
          // 확신 있게 보여주게 되고, 그것이 비교를 안 보여주는 것보다 나쁘다.
          const baseMatches = base?.options.filter((candidate) => candidate.name === option.name) ?? [];
          const basePrice = baseMatches.length === 1 ? baseMatches[0].additionalPrice : undefined;
          const showPriceCompare = option.diffType === "PRICE_DIFFERS" && basePrice !== undefined;

          return (
            <li key={option.id} className="flex flex-wrap items-center gap-2 text-sm">
              <span className="min-w-0 truncate">{option.name}</span>
              <span className="text-muted-foreground text-xs">
                {showPriceCompare
                  ? OPTION_GROUP_MERGE_COPY.DIFF_PRICE_COMPARE(
                      formatPrice(basePrice),
                      formatPrice(option.additionalPrice),
                    )
                  : formatPrice(option.additionalPrice)}
              </span>
              {/* `ONLY_IN_CANDIDATE`("합치면 사라짐")가 이 화면의 핵심 경고라 destructive 로 띄운다. */}
              {option.diffType !== "SAME" && (
                <Badge variant={option.diffType === "ONLY_IN_CANDIDATE" ? "destructive" : "outline"}>
                  {OPTION_GROUP_MERGE_COPY.DIFF_TYPE_LABEL[option.diffType]}
                </Badge>
              )}
            </li>
          );
        })}
      </ul>
    </li>
  );
}

/**
 * 기준 vs 후보 diff 상세보기.
 *
 * PDF STEP 4 의 [상세보기]에 대응한다 — 흡수될 그룹에만 있는 옵션이 사라지는 것을 합치기 **전에**
 * 알려야 하므로, 필드 단위(`*Differs`)와 옵션 단위(`diffType`)를 함께 보여준다.
 */
export function MergeDiffSheet({ open, preview, onOpenChange }: MergeDiffSheetProps) {
  return (
    <Sheet open={open && preview !== undefined} onOpenChange={onOpenChange}>
      <SheetContent side="right" className="flex w-full flex-col gap-4 overflow-y-auto sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>{OPTION_GROUP_MERGE_COPY.DIFF_SHEET_TITLE}</SheetTitle>
          <SheetDescription>{OPTION_GROUP_MERGE_COPY.DIFF_SHEET_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        {preview !== undefined && (
          <div className="flex flex-col gap-3 px-4 pb-4">
            <p className="rounded-md border border-dashed p-3 text-muted-foreground text-xs">
              {OPTION_GROUP_MERGE_COPY.DIFF_VANISHING_NOTICE}
            </p>

            <ul className="flex flex-col gap-3">
              <MergeDiffItem item={preview.base} isBase />
              {preview.candidates.map((candidate) => (
                <MergeDiffItem key={candidate.id} item={candidate} isBase={false} base={preview.base} />
              ))}
            </ul>
          </div>
        )}
      </SheetContent>
    </Sheet>
  );
}
