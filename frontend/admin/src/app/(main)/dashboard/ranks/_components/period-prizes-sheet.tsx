"use client";

import * as React from "react";

import { MoreHorizontal, Plus } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Skeleton } from "@/components/ui/skeleton";
import { fetchPrizesAction } from "@/feature/rank/actions";
import type { RankPeriod, RankPrize } from "@/feature/rank/domain";
import { RANK_MESSAGE } from "@/feature/rank/message";

import { DeletePrizeDialog } from "./delete-prize-dialog";
import { PrizeFormSheet } from "./prize-form-sheet";

interface PeriodPrizesSheetProps {
  /** 경품 관리 대상 기간. null 이면 닫힌 상태. */
  period: Pick<RankPeriod, "id"> | null;
  onOpenChange: (open: boolean) => void;
}

export function PeriodPrizesSheet({ period, onOpenChange }: PeriodPrizesSheetProps) {
  const [prizes, setPrizes] = React.useState<RankPrize[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [formOpen, setFormOpen] = React.useState(false);
  const [editing, setEditing] = React.useState<RankPrize | null>(null);
  const [deleteTarget, setDeleteTarget] = React.useState<RankPrize | null>(null);

  const periodId = period?.id ?? null;

  const loadPrizes = React.useCallback(() => {
    if (periodId == null) return;

    let active = true;
    setIsLoading(true);
    setError(null);

    void fetchPrizesAction(periodId).then((result) => {
      if (!active) return;
      if (result.success && result.data) {
        setPrizes(result.data);
      } else {
        setError(result.message ?? RANK_MESSAGE.PRIZES_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [periodId]);

  React.useEffect(() => {
    if (periodId == null) return;
    setPrizes([]);
    setError(null);
    return loadPrizes();
  }, [periodId, loadPrizes]);

  function openCreate() {
    setEditing(null);
    setFormOpen(true);
  }

  function openEdit(prize: RankPrize) {
    setEditing(prize);
    setFormOpen(true);
  }

  return (
    <Sheet open={period != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>경품 관리</SheetTitle>
          <SheetDescription>이 랭킹 기간의 경품을 등수 순으로 관리합니다.</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-4 overflow-y-auto px-4">
          <Button type="button" size="sm" onClick={openCreate} disabled={periodId == null}>
            <Plus /> 경품 추가
          </Button>

          {error ? (
            <p className="text-destructive text-sm">{error}</p>
          ) : isLoading ? (
            <div className="space-y-2">
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
            </div>
          ) : prizes.length ? (
            <ul className="space-y-2">
              {prizes.map((prize) => (
                <li key={prize.id} className="flex items-center gap-3 rounded-md border p-3">
                  {prize.image ? (
                    // biome-ignore lint/performance/noImgElement: CDN URL 썸네일
                    <img
                      src={prize.image.url}
                      alt={prize.name}
                      className="size-12 shrink-0 rounded-md border object-cover"
                    />
                  ) : (
                    <div className="flex size-12 shrink-0 items-center justify-center rounded-md border bg-muted text-muted-foreground text-xs">
                      No Image
                    </div>
                  )}
                  <div className="min-w-0 flex-1">
                    <p className="text-muted-foreground text-xs">{prize.prizeRank}등</p>
                    <p className="line-clamp-1 font-medium text-sm">{prize.name}</p>
                    <p className="line-clamp-1 text-muted-foreground text-xs">{prize.brand}</p>
                  </div>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="icon" className="size-8 shrink-0" aria-label="경품 작업 메뉴">
                        <MoreHorizontal className="size-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem onSelect={() => openEdit(prize)}>수정</DropdownMenuItem>
                      <DropdownMenuItem variant="destructive" onSelect={() => setDeleteTarget(prize)}>
                        삭제
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-muted-foreground text-sm">등록된 경품이 없습니다.</p>
          )}
        </div>

        <SheetFooter>
          <SheetClose asChild>
            <Button variant="outline">닫기</Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>

      <PrizeFormSheet
        open={formOpen}
        onOpenChange={setFormOpen}
        periodId={periodId}
        prize={editing}
        existingPrizes={prizes}
        onSaved={loadPrizes}
      />
      <DeletePrizeDialog
        prize={deleteTarget}
        onOpenChange={(open) => !open && setDeleteTarget(null)}
        onDeleted={loadPrizes}
      />
    </Sheet>
  );
}
