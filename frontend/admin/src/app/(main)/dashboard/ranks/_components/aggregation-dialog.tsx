"use client";

import * as React from "react";

import { useRouter } from "next/navigation";

import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Field, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { runAggregationAction } from "@/feature/rank/actions";
import { AGGREGATION_DEFAULT_LIMIT, RANK_TYPE_LABELS } from "@/feature/rank/constants";
import { RANK_MESSAGE } from "@/feature/rank/message";

interface AggregationDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function AggregationDialog({ open, onOpenChange }: AggregationDialogProps) {
  const router = useRouter();
  const [isPending, startTransition] = React.useTransition();
  const [typeInput, setTypeInput] = React.useState<string>("all");
  const [baseDate, setBaseDate] = React.useState("");
  const [limit, setLimit] = React.useState(String(AGGREGATION_DEFAULT_LIMIT));

  React.useEffect(() => {
    if (!open) return;
    setTypeInput("all");
    setBaseDate("");
    setLimit(String(AGGREGATION_DEFAULT_LIMIT));
  }, [open]);

  const typeSpecified = typeInput !== "all";

  function handleRun() {
    startTransition(async () => {
      const { success, message } = await runAggregationAction({
        type: typeSpecified ? (typeInput as "ALL" | "MONTHLY" | "WEEKLY") : undefined,
        baseDate: typeSpecified && baseDate.trim() !== "" ? baseDate : undefined,
        limit: typeSpecified && limit.trim() !== "" ? Number(limit) : undefined,
      });

      if (success) {
        toast.success(RANK_MESSAGE.AGGREGATION_SUCCESS);
        onOpenChange(false);
        router.refresh();
      } else {
        toast.error(message ?? RANK_MESSAGE.AGGREGATION_FAILED);
      }
    });
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>랭킹 집계 실행</DialogTitle>
          <DialogDescription>
            타입을 지정하지 않으면 전체 타입(ALL/MONTHLY/WEEKLY)을 오늘 기준으로 재집계합니다.
          </DialogDescription>
        </DialogHeader>

        <div className="flex flex-col gap-4">
          <Field className="gap-1.5">
            <FieldLabel htmlFor="aggregation-type">타입</FieldLabel>
            <Select value={typeInput} onValueChange={setTypeInput} disabled={isPending}>
              <SelectTrigger id="aggregation-type" className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectGroup>
                  <SelectItem value="all">미지정 (전체 재집계)</SelectItem>
                  {Object.entries(RANK_TYPE_LABELS).map(([value, label]) => (
                    <SelectItem key={value} value={value}>
                      {label}
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
          </Field>

          <Field className="gap-1.5">
            <FieldLabel htmlFor="aggregation-base-date">기준일 (미입력=오늘)</FieldLabel>
            <Input
              id="aggregation-base-date"
              type="date"
              value={baseDate}
              onChange={(e) => setBaseDate(e.target.value)}
              disabled={isPending || !typeSpecified}
            />
          </Field>

          <Field className="gap-1.5">
            <FieldLabel htmlFor="aggregation-limit">상위 N명 (미입력=10)</FieldLabel>
            <Input
              id="aggregation-limit"
              type="number"
              min={1}
              value={limit}
              onChange={(e) => setLimit(e.target.value)}
              disabled={isPending || !typeSpecified}
            />
          </Field>
        </div>

        <DialogFooter>
          <Button onClick={handleRun} disabled={isPending}>
            {isPending ? "실행 중..." : "집계 실행"}
          </Button>
          <DialogClose asChild>
            <Button variant="outline" disabled={isPending}>
              취소
            </Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
