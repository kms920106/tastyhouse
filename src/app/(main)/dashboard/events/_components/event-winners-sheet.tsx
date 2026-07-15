"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Trash2 } from "lucide-react";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
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
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { createEventWinnerAction, deleteEventWinnerAction, fetchEventWinnersAction } from "@/feature/event/actions";
import { WINNER_NAME_MAX } from "@/feature/event/constants";
import type { EventListItem, EventWinner } from "@/feature/event/domain";
import { EVENT_MESSAGE } from "@/feature/event/message";
import { type WinnerFormValues, winnerFormSchema } from "@/feature/event/schema";
import { formatDateTime } from "@/lib/date";

interface EventWinnersSheetProps {
  /** 당첨자 관리 대상 이벤트. null 이면 닫힌 상태. */
  event: Pick<EventListItem, "id" | "name"> | null;
  onOpenChange: (open: boolean) => void;
}

const EMPTY_VALUES: WinnerFormValues = {
  rankNo: undefined as unknown as number,
  winnerName: "",
  phoneNumber: "",
  announcedAt: "",
};

export function EventWinnersSheet({ event, onOpenChange }: EventWinnersSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [winners, setWinners] = React.useState<EventWinner[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [deletingId, setDeletingId] = React.useState<number | null>(null);

  const form = useForm<WinnerFormValues>({
    resolver: zodResolver(winnerFormSchema),
    defaultValues: EMPTY_VALUES,
  });

  const eventId = event?.id ?? null;

  const loadWinners = React.useCallback(() => {
    if (eventId == null) return;

    let active = true;
    setIsLoading(true);
    setError(null);

    void fetchEventWinnersAction(eventId).then((result) => {
      if (!active) return;
      if (result.success && result.data) {
        setWinners(result.data);
      } else {
        setError(result.message ?? EVENT_MESSAGE.WINNERS_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [eventId]);

  // 시트가 열릴 때 폼 초기화 + 당첨자 목록 로드
  React.useEffect(() => {
    if (eventId == null) return;
    form.reset(EMPTY_VALUES);
    setWinners([]);
    setError(null);
    const cleanup = loadWinners();
    return cleanup;
  }, [eventId, form.reset, loadWinners]);

  const onSubmit = (values: WinnerFormValues) => {
    if (eventId == null) return;
    startTransition(async () => {
      const { success, message } = await createEventWinnerAction(eventId, values);
      if (success) {
        toast.success(EVENT_MESSAGE.WINNER_CREATE_SUCCESS);
        form.reset(EMPTY_VALUES);
        loadWinners();
      } else {
        toast.error(message ?? EVENT_MESSAGE.WINNER_SAVE_FAILED);
      }
    });
  };

  function handleDelete(winnerId: number) {
    if (eventId == null) return;
    setDeletingId(winnerId);
    void deleteEventWinnerAction(eventId, winnerId).then((result) => {
      setDeletingId(null);
      if (result.success) {
        toast.success(EVENT_MESSAGE.WINNER_DELETE_SUCCESS);
        loadWinners();
      } else {
        toast.error(result.message ?? EVENT_MESSAGE.WINNER_DELETE_FAILED);
      }
    });
  }

  return (
    <Sheet open={event != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>당첨자 관리</SheetTitle>
          <SheetDescription>{event ? `"${event.name}" 이벤트의 당첨자를 등록하고 관리합니다.` : ""}</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-6 overflow-y-auto px-4">
          {/* 당첨자 등록 폼 */}
          <form id="winner-form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
            <FieldGroup className="gap-3">
              <div className="flex gap-2">
                <Controller
                  control={form.control}
                  name="rankNo"
                  render={({ field, fieldState }) => (
                    <Field className="w-24 gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="winner-rank-no">순위</FieldLabel>
                      <Input
                        id="winner-rank-no"
                        type="number"
                        min={1}
                        placeholder="1"
                        value={field.value ?? ""}
                        onChange={(e) => field.onChange(e.target.value === "" ? undefined : Number(e.target.value))}
                        aria-invalid={fieldState.invalid}
                        disabled={isPending}
                      />
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />
                <Controller
                  control={form.control}
                  name="winnerName"
                  render={({ field, fieldState }) => (
                    <Field className="flex-1 gap-1.5" data-invalid={fieldState.invalid}>
                      <FieldLabel htmlFor="winner-name">당첨자 이름</FieldLabel>
                      <Input
                        {...field}
                        id="winner-name"
                        placeholder="홍길동"
                        maxLength={WINNER_NAME_MAX}
                        aria-invalid={fieldState.invalid}
                        disabled={isPending}
                      />
                      {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                  )}
                />
              </div>

              <Controller
                control={form.control}
                name="phoneNumber"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="winner-phone-number">휴대폰 번호</FieldLabel>
                    <Input
                      {...field}
                      id="winner-phone-number"
                      inputMode="numeric"
                      placeholder="01012345678 (숫자 11자리)"
                      maxLength={11}
                      aria-invalid={fieldState.invalid}
                      disabled={isPending}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Controller
                control={form.control}
                name="announcedAt"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="winner-announced-at">발표 일시</FieldLabel>
                    <Input
                      {...field}
                      value={field.value ?? ""}
                      id="winner-announced-at"
                      type="datetime-local"
                      step={1}
                      aria-invalid={fieldState.invalid}
                      disabled={isPending}
                    />
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Button type="submit" disabled={isPending}>
                {isPending ? "등록 중..." : "당첨자 등록"}
              </Button>
            </FieldGroup>
          </form>

          {/* 당첨자 목록 */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <h4 className="font-medium text-sm">당첨자 목록</h4>
              <span className="text-muted-foreground text-xs">총 {winners.length}명</span>
            </div>

            {error ? (
              <p className="text-destructive text-sm">{error}</p>
            ) : (
              <div className="rounded-md border">
                <Table className="**:data-[slot='table-cell']:px-3 **:data-[slot='table-head']:px-3">
                  <TableHeader>
                    <TableRow>
                      <TableHead className="font-normal">순위</TableHead>
                      <TableHead className="font-normal">이름</TableHead>
                      <TableHead className="font-normal">휴대폰</TableHead>
                      <TableHead className="font-normal">발표일시</TableHead>
                      <TableHead className="font-normal text-right">삭제</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {isLoading ? (
                      Array.from({ length: 3 }, (_, index) => `winner-skeleton-${index}`).map((key) => (
                        <TableRow key={key}>
                          {Array.from({ length: 5 }, (_, i) => `${key}-c${i}`).map((cellKey) => (
                            <TableCell key={cellKey} className="py-3">
                              <Skeleton className="h-4 w-full" />
                            </TableCell>
                          ))}
                        </TableRow>
                      ))
                    ) : winners.length ? (
                      winners.map((winner) => (
                        <TableRow key={winner.id}>
                          <TableCell className="tabular-nums">{winner.rankNo}</TableCell>
                          <TableCell>{winner.winnerName}</TableCell>
                          <TableCell className="tabular-nums">{winner.phoneNumber}</TableCell>
                          <TableCell className="whitespace-nowrap tabular-nums text-xs">
                            {formatDateTime(winner.announcedAt)}
                          </TableCell>
                          <TableCell className="text-right">
                            <Button
                              type="button"
                              variant="ghost"
                              size="icon"
                              className="size-8"
                              aria-label="당첨자 삭제"
                              disabled={deletingId === winner.id}
                              onClick={() => handleDelete(winner.id)}
                            >
                              <Trash2 className="size-4" />
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))
                    ) : (
                      <TableRow>
                        <TableCell colSpan={5} className="h-20 text-center text-muted-foreground text-sm">
                          등록된 당첨자가 없습니다.
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </div>
            )}
          </div>
        </div>

        <SheetFooter>
          <SheetClose asChild>
            <Button variant="outline">닫기</Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
