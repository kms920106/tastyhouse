"use client";

import * as React from "react";

import { toast } from "sonner";

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { deletePeriodAction, fetchPrizesAction } from "@/feature/rank/actions";
import type { RankPeriod } from "@/feature/rank/domain";
import { RANK_MESSAGE } from "@/feature/rank/message";

interface DeletePeriodDialogProps {
  period: RankPeriod | null;
  onOpenChange: (open: boolean) => void;
}

export function DeletePeriodDialog({ period, onOpenChange }: DeletePeriodDialogProps) {
  const [isPending, startTransition] = React.useTransition();
  const [prizeCount, setPrizeCount] = React.useState<number | null>(null);
  const [isLoadingCount, setIsLoadingCount] = React.useState(false);

  const periodId = period?.id ?? null;

  React.useEffect(() => {
    if (periodId == null) {
      setPrizeCount(null);
      return;
    }

    let active = true;
    setIsLoadingCount(true);

    void fetchPrizesAction(periodId).then((result) => {
      if (!active) return;
      setIsLoadingCount(false);
      setPrizeCount(result.success && result.data ? result.data.length : 0);
    });

    return () => {
      active = false;
    };
  }, [periodId]);

  function handleDelete() {
    if (!period) return;
    startTransition(async () => {
      const { success, message } = await deletePeriodAction(period.id);
      if (success) {
        toast.success(RANK_MESSAGE.PERIOD_DELETE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? RANK_MESSAGE.PERIOD_DELETE_FAILED);
      }
    });
  }

  const busy = isPending || isLoadingCount;

  return (
    <AlertDialog open={period != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>랭킹 기간을 삭제하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {isLoadingCount ? (
              "경품 정보를 확인하는 중..."
            ) : prizeCount != null && prizeCount > 0 ? (
              <>
                {"이 기간의 경품 "}
                <strong>{prizeCount}개</strong>
                {"도 함께 삭제됩니다. 이 작업은 되돌릴 수 없습니다."}
              </>
            ) : (
              "이 작업은 되돌릴 수 없습니다."
            )}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
          <AlertDialogAction
            onClick={(event) => {
              event.preventDefault();
              handleDelete();
            }}
            disabled={busy}
          >
            {isPending ? "삭제 중..." : "삭제"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
