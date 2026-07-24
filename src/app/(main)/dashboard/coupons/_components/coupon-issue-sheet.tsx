"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import type { ApiPagination } from "@/api/shared/types";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldLabel } from "@/components/ui/field";
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
import { fetchCouponIssuesAction, issueCouponAction } from "@/feature/coupon/actions";
import type { CouponListItem, MemberCouponItem } from "@/feature/coupon/domain";
import { COUPON_MESSAGE } from "@/feature/coupon/message";
import { type CouponIssueFormValues, couponIssueSchema } from "@/feature/coupon/schema";
import { formatDateTime } from "@/lib/date";

interface CouponIssueSheetProps {
  /** 발급 관리 대상 쿠폰. null 이면 닫힌 상태. */
  coupon: Pick<CouponListItem, "id" | "name"> | null;
  onOpenChange: (open: boolean) => void;
}

const ISSUES_PAGE_SIZE = 10;

export function CouponIssueSheet({ coupon, onOpenChange }: CouponIssueSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [issues, setIssues] = React.useState<MemberCouponItem[]>([]);
  const [pagination, setPagination] = React.useState<ApiPagination | null>(null);
  const [page, setPage] = React.useState(0);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  const form = useForm<CouponIssueFormValues>({
    resolver: zodResolver(couponIssueSchema),
    defaultValues: { memberId: undefined as unknown as number },
  });

  const couponId = coupon?.id ?? null;

  const loadIssues = React.useCallback(
    (targetPage: number) => {
      if (couponId == null) return;

      let active = true;
      setIsLoading(true);
      setError(null);

      void fetchCouponIssuesAction(couponId, targetPage, ISSUES_PAGE_SIZE).then((result) => {
        if (!active) return;
        if (result.success && result.data && result.pagination) {
          setIssues(result.data);
          setPagination(result.pagination);
          setPage(result.pagination.page);
        } else {
          setError(result.message ?? COUPON_MESSAGE.ISSUES_LOAD_FAILED);
        }
        setIsLoading(false);
      });

      return () => {
        active = false;
      };
    },
    [couponId],
  );

  // 시트가 열릴 때 폼 초기화 + 첫 페이지 발급 현황 로드
  React.useEffect(() => {
    if (couponId == null) return;
    form.reset({ memberId: undefined as unknown as number });
    setIssues([]);
    setPagination(null);
    setError(null);
    const cleanup = loadIssues(0);
    return cleanup;
  }, [couponId, form.reset, loadIssues]);

  const onSubmit = (values: CouponIssueFormValues) => {
    if (couponId == null) return;
    startTransition(async () => {
      const { success, message } = await issueCouponAction(couponId, values);
      if (success) {
        toast.success(COUPON_MESSAGE.ISSUE_SUCCESS);
        form.reset({ memberId: undefined as unknown as number });
        loadIssues(0);
      } else {
        toast.error(message ?? COUPON_MESSAGE.ISSUE_FAILED);
      }
    });
  };

  const pageCount = Math.max(pagination?.totalPages ?? 1, 1);
  const currentPage = page + 1;
  const canPrev = page > 0 && !isLoading;
  const canNext = page + 1 < pageCount && !isLoading;

  return (
    <Sheet open={coupon != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>쿠폰 발급 관리</SheetTitle>
          <SheetDescription>
            {coupon ? `"${coupon.name}" 쿠폰을 회원에게 발급하고 발급 현황을 확인합니다.` : ""}
          </SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-6 overflow-y-auto px-4">
          {/* 회원 발급 폼 */}
          <form
            id="coupon-issue-form"
            noValidate
            onSubmit={form.handleSubmit(onSubmit)}
            className="flex items-end gap-2"
          >
            <Controller
              control={form.control}
              name="memberId"
              render={({ field, fieldState }) => (
                <Field className="flex-1 gap-1.5" data-invalid={fieldState.invalid}>
                  <FieldLabel htmlFor="coupon-issue-member-id">회원 ID</FieldLabel>
                  <Input
                    id="coupon-issue-member-id"
                    type="number"
                    min={1}
                    placeholder="발급 대상 회원 ID"
                    value={field.value ?? ""}
                    onChange={(e) => field.onChange(e.target.value === "" ? undefined : Number(e.target.value))}
                    aria-invalid={fieldState.invalid}
                    disabled={isPending}
                  />
                  {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                </Field>
              )}
            />
            <Button type="submit" disabled={isPending}>
              {isPending ? "발급 중..." : "발급"}
            </Button>
          </form>

          {/* 발급 현황 목록 */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <h4 className="font-medium text-sm">발급 현황</h4>
              {pagination ? (
                <span className="text-muted-foreground text-xs">총 {pagination.totalElements}건</span>
              ) : null}
            </div>

            {error ? (
              <p className="text-destructive text-sm">{error}</p>
            ) : (
              <div className="rounded-md border">
                <Table className="**:data-[slot='table-cell']:px-3 **:data-[slot='table-head']:px-3">
                  <TableHeader>
                    <TableRow>
                      <TableHead className="font-normal">발급ID</TableHead>
                      <TableHead className="font-normal">회원ID</TableHead>
                      <TableHead className="font-normal">사용</TableHead>
                      <TableHead className="font-normal">만료일시</TableHead>
                      <TableHead className="font-normal">발급일시</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {isLoading ? (
                      Array.from({ length: 5 }, (_, index) => `issue-skeleton-${index}`).map((key) => (
                        <TableRow key={key}>
                          {Array.from({ length: 5 }, (_, i) => `${key}-c${i}`).map((cellKey) => (
                            <TableCell key={cellKey} className="py-3">
                              <Skeleton className="h-4 w-full" />
                            </TableCell>
                          ))}
                        </TableRow>
                      ))
                    ) : issues.length ? (
                      issues.map((issue) => (
                        <TableRow key={issue.id}>
                          <TableCell className="tabular-nums">{issue.id}</TableCell>
                          <TableCell className="tabular-nums">{issue.memberId}</TableCell>
                          <TableCell>
                            <Badge variant={issue.used ? "default" : "secondary"}>
                              {issue.used ? "사용" : "미사용"}
                            </Badge>
                          </TableCell>
                          <TableCell className="whitespace-nowrap tabular-nums text-xs">
                            {formatDateTime(issue.expiredAt)}
                          </TableCell>
                          <TableCell className="whitespace-nowrap tabular-nums text-xs">
                            {formatDateTime(issue.issuedAt)}
                          </TableCell>
                        </TableRow>
                      ))
                    ) : (
                      <TableRow>
                        <TableCell colSpan={5} className="h-20 text-center text-muted-foreground text-sm">
                          발급된 내역이 없습니다.
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </div>
            )}

            <div className="flex items-center justify-between pt-1">
              <span className="text-muted-foreground text-xs tabular-nums">
                {currentPage} / {pageCount} 페이지
              </span>
              <div className="flex gap-2">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  disabled={!canPrev}
                  onClick={() => loadIssues(page - 1)}
                >
                  이전
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  disabled={!canNext}
                  onClick={() => loadIssues(page + 1)}
                >
                  다음
                </Button>
              </div>
            </div>
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
