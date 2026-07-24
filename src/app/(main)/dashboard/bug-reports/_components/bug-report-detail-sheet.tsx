"use client";

import * as React from "react";

import { useRouter } from "next/navigation";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
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
import { Textarea } from "@/components/ui/textarea";
import {
  assignBugReportAction,
  classifyBugReportAction,
  fetchBugReportAction,
  updateBugReportStatusAction,
} from "@/feature/bug-report/actions";
import {
  BUG_CATEGORY,
  BUG_CATEGORY_LABEL,
  BUG_PLATFORM_LABEL,
  BUG_PRIORITY,
  BUG_PRIORITY_BADGE_VARIANT,
  BUG_PRIORITY_LABEL,
  BUG_STATUS_BADGE_VARIANT,
  BUG_STATUS_LABEL,
  BUG_STATUS_TRANSITIONS,
} from "@/feature/bug-report/constants";
import type { BugReportDetail } from "@/feature/bug-report/domain";
import { BUG_REPORT_MESSAGE } from "@/feature/bug-report/message";
import {
  type AssignValues,
  assignSchema,
  BUG_ANSWER_MAX,
  type ClassifyValues,
  classifySchema,
  type StatusUpdateValues,
  statusUpdateSchema,
} from "@/feature/bug-report/schema";
import { formatDateTime } from "@/lib/date";

interface BugReportDetailSheetProps {
  /** 조회할 버그 제보 ID. null 이면 닫힌 상태. */
  bugReportId: number | null;
  onOpenChange: (open: boolean) => void;
}

export function BugReportDetailSheet({ bugReportId, onOpenChange }: BugReportDetailSheetProps) {
  const router = useRouter();
  const [detail, setDetail] = React.useState<BugReportDetail | null>(null);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  const load = React.useCallback((id: number) => {
    let active = true;
    setIsLoading(true);
    setError(null);

    void fetchBugReportAction(id).then((result) => {
      if (!active) return;
      const { success, message, data } = result;
      if (success && data) {
        setDetail(data);
      } else {
        setError(message ?? BUG_REPORT_MESSAGE.DETAIL_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, []);

  React.useEffect(() => {
    if (bugReportId == null) return;
    setDetail(null);
    const cleanup = load(bugReportId);
    return cleanup;
  }, [bugReportId, load]);

  // 변경 성공 후 상세 재조회 + 목록 갱신
  const reload = React.useCallback(() => {
    if (bugReportId != null) load(bugReportId);
    router.refresh();
  }, [bugReportId, load, router]);

  return (
    <Sheet open={bugReportId != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-lg">
        <SheetHeader>
          <SheetTitle>버그 제보 상세</SheetTitle>
          <SheetDescription>버그 제보 내용을 확인하고 처리합니다.</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-5 overflow-y-auto px-4">
          {isLoading ? (
            <div className="space-y-3">
              <Skeleton className="h-6 w-3/4" />
              <Skeleton className="h-4 w-1/3" />
              <Skeleton className="h-32 w-full" />
            </div>
          ) : error ? (
            <p className="text-destructive text-sm">{error}</p>
          ) : detail ? (
            <>
              <div className="flex items-start justify-between gap-3">
                <h3 className="font-semibold text-lg leading-snug">{detail.title}</h3>
                <div className="flex shrink-0 flex-wrap justify-end gap-1.5">
                  <Badge variant={BUG_STATUS_BADGE_VARIANT[detail.status]}>{BUG_STATUS_LABEL[detail.status]}</Badge>
                  {detail.category && <Badge variant="outline">{BUG_CATEGORY_LABEL[detail.category]}</Badge>}
                  {detail.priority && (
                    <Badge variant={BUG_PRIORITY_BADGE_VARIANT[detail.priority]}>
                      {BUG_PRIORITY_LABEL[detail.priority]}
                    </Badge>
                  )}
                </div>
              </div>

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">회원</dt>
                <dd>{detail.member ? `${detail.member.nickname} (ID: ${detail.member.id})` : "-"}</dd>
                <dt className="text-muted-foreground">기기</dt>
                <dd>{detail.device}</dd>
                <dt className="text-muted-foreground">플랫폼</dt>
                <dd>
                  {BUG_PLATFORM_LABEL[detail.platform] ?? detail.platform} · OS {detail.osVersion} · 앱{" "}
                  {detail.appVersion}
                </dd>
              </dl>

              <Separator />

              <div className="whitespace-pre-wrap break-words text-sm leading-relaxed">{detail.content}</div>

              <Separator />

              <div className="space-y-2">
                <p className="font-medium text-sm">첨부 이미지</p>
                {detail.images.length ? (
                  <div className="space-y-3">
                    {detail.images.map((image) => (
                      <div key={image.id} className="space-y-1">
                        {/* biome-ignore lint/performance/noImgElement: CDN 원본 URL, next/image remotePatterns 미설정 */}
                        <img src={image.url} alt={image.name} className="w-full rounded-md border object-cover" />
                        <p className="text-muted-foreground text-xs">{image.name}</p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-muted-foreground text-sm">첨부 이미지 없음</p>
                )}
              </div>

              <Separator />

              <StatusUpdateSection detail={detail} onSuccess={reload} />
              <Separator />
              <ClassifySection detail={detail} onSuccess={reload} />
              <Separator />
              <AssignSection detail={detail} onSuccess={reload} />

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">담당 관리자 ID</dt>
                <dd className="tabular-nums">{detail.assigneeAdminId ?? "-"}</dd>
                <dt className="text-muted-foreground">처리 결과</dt>
                <dd className="whitespace-pre-wrap break-words">{detail.adminAnswer ?? "-"}</dd>
                <dt className="text-muted-foreground">처리 완료</dt>
                <dd className="tabular-nums">{detail.resolvedAt ? formatDateTime(detail.resolvedAt) : "-"}</dd>
                <dt className="text-muted-foreground">ID</dt>
                <dd className="tabular-nums">{detail.id}</dd>
                <dt className="text-muted-foreground">등록일시</dt>
                <dd className="tabular-nums">{formatDateTime(detail.createdAt)}</dd>
                <dt className="text-muted-foreground">수정일시</dt>
                <dd className="tabular-nums">{formatDateTime(detail.updatedAt)}</dd>
              </dl>
            </>
          ) : null}
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

interface SectionProps {
  detail: BugReportDetail;
  onSuccess: () => void;
}

// 처리 상태 변경
function StatusUpdateSection({ detail, onSuccess }: SectionProps) {
  const [isPending, startTransition] = React.useTransition();
  const form = useForm<StatusUpdateValues>({
    resolver: zodResolver(statusUpdateSchema),
    defaultValues: {
      status: BUG_STATUS_TRANSITIONS.includes(detail.status as (typeof BUG_STATUS_TRANSITIONS)[number])
        ? (detail.status as (typeof BUG_STATUS_TRANSITIONS)[number])
        : "IN_PROGRESS",
      answer: detail.adminAnswer ?? "",
    },
  });
  const status = form.watch("status");
  const showAnswer = status === "RESOLVED" || status === "REJECTED";

  const onSubmit = (values: StatusUpdateValues) => {
    startTransition(async () => {
      const { success, message } = await updateBugReportStatusAction(detail.id, values);
      if (success) {
        toast.success(BUG_REPORT_MESSAGE.STATUS_UPDATE_SUCCESS);
        onSuccess();
      } else {
        toast.error(message ?? BUG_REPORT_MESSAGE.STATUS_UPDATE_FAILED);
      }
    });
  };

  return (
    <form noValidate onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
      <p className="font-medium text-sm">처리 상태 변경</p>
      <Controller
        control={form.control}
        name="status"
        render={({ field, fieldState }) => (
          <Field className="gap-1.5" data-invalid={fieldState.invalid}>
            <FieldLabel htmlFor="bug-status">상태</FieldLabel>
            <Select value={field.value} onValueChange={field.onChange} disabled={isPending}>
              <SelectTrigger id="bug-status" aria-invalid={fieldState.invalid}>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectGroup>
                  {BUG_STATUS_TRANSITIONS.map((value) => (
                    <SelectItem key={value} value={value}>
                      {BUG_STATUS_LABEL[value]}
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
          </Field>
        )}
      />
      {showAnswer && (
        <Controller
          control={form.control}
          name="answer"
          render={({ field, fieldState }) => (
            <Field className="gap-1.5" data-invalid={fieldState.invalid}>
              <FieldLabel htmlFor="bug-answer">처리 결과 / 반려 사유</FieldLabel>
              <Textarea
                {...field}
                value={field.value ?? ""}
                id="bug-answer"
                placeholder="처리 결과 또는 반려 사유를 입력하세요"
                maxLength={BUG_ANSWER_MAX}
                rows={3}
                aria-invalid={fieldState.invalid}
              />
              {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
            </Field>
          )}
        />
      )}
      <Button type="submit" size="sm" disabled={isPending}>
        {isPending ? "저장 중..." : "상태 변경"}
      </Button>
    </form>
  );
}

// 분류/우선순위 지정
function ClassifySection({ detail, onSuccess }: SectionProps) {
  const [isPending, startTransition] = React.useTransition();
  const form = useForm<ClassifyValues>({
    resolver: zodResolver(classifySchema),
    defaultValues: {
      category: detail.category ?? undefined,
      priority: detail.priority ?? undefined,
    },
  });

  const onSubmit = (values: ClassifyValues) => {
    startTransition(async () => {
      const { success, message } = await classifyBugReportAction(detail.id, values);
      if (success) {
        toast.success(BUG_REPORT_MESSAGE.CLASSIFY_SUCCESS);
        onSuccess();
      } else {
        toast.error(message ?? BUG_REPORT_MESSAGE.CLASSIFY_FAILED);
      }
    });
  };

  return (
    <form noValidate onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
      <p className="font-medium text-sm">분류 / 우선순위</p>
      <div className="flex flex-wrap gap-3">
        <Controller
          control={form.control}
          name="category"
          render={({ field, fieldState }) => (
            <Field className="min-w-32 flex-1 gap-1.5" data-invalid={fieldState.invalid}>
              <FieldLabel htmlFor="bug-category">분류</FieldLabel>
              <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={isPending}>
                <SelectTrigger id="bug-category" aria-invalid={fieldState.invalid}>
                  <SelectValue placeholder="선택" />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    {BUG_CATEGORY.map((value) => (
                      <SelectItem key={value} value={value}>
                        {BUG_CATEGORY_LABEL[value]}
                      </SelectItem>
                    ))}
                  </SelectGroup>
                </SelectContent>
              </Select>
              {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
            </Field>
          )}
        />
        <Controller
          control={form.control}
          name="priority"
          render={({ field, fieldState }) => (
            <Field className="min-w-32 flex-1 gap-1.5" data-invalid={fieldState.invalid}>
              <FieldLabel htmlFor="bug-priority">우선순위</FieldLabel>
              <Select value={field.value ?? ""} onValueChange={field.onChange} disabled={isPending}>
                <SelectTrigger id="bug-priority" aria-invalid={fieldState.invalid}>
                  <SelectValue placeholder="선택" />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    {BUG_PRIORITY.map((value) => (
                      <SelectItem key={value} value={value}>
                        {BUG_PRIORITY_LABEL[value]}
                      </SelectItem>
                    ))}
                  </SelectGroup>
                </SelectContent>
              </Select>
              {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
            </Field>
          )}
        />
      </div>
      <Button type="submit" size="sm" disabled={isPending}>
        {isPending ? "저장 중..." : "분류 저장"}
      </Button>
    </form>
  );
}

// 담당자 배정
function AssignSection({ detail, onSuccess }: SectionProps) {
  const [isPending, startTransition] = React.useTransition();
  const form = useForm<AssignValues>({
    resolver: zodResolver(assignSchema),
    defaultValues: {
      assigneeAdminId: detail.assigneeAdminId ?? undefined,
    },
  });

  const onSubmit = (values: AssignValues) => {
    startTransition(async () => {
      const { success, message } = await assignBugReportAction(detail.id, values);
      if (success) {
        toast.success(BUG_REPORT_MESSAGE.ASSIGN_SUCCESS);
        onSuccess();
      } else {
        toast.error(message ?? BUG_REPORT_MESSAGE.ASSIGN_FAILED);
      }
    });
  };

  return (
    <form noValidate onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
      <p className="font-medium text-sm">담당자 배정</p>
      <Controller
        control={form.control}
        name="assigneeAdminId"
        render={({ field, fieldState }) => (
          <Field className="gap-1.5" data-invalid={fieldState.invalid}>
            <FieldLabel htmlFor="bug-assignee">담당 관리자 ID</FieldLabel>
            <Input
              id="bug-assignee"
              inputMode="numeric"
              placeholder="담당 관리자 ID"
              value={field.value ?? ""}
              onChange={(e) => {
                const digits = e.target.value.replace(/[^0-9]/g, "");
                field.onChange(digits === "" ? undefined : Number(digits));
              }}
              disabled={isPending}
              aria-invalid={fieldState.invalid}
            />
            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
          </Field>
        )}
      />
      <Button type="submit" size="sm" disabled={isPending}>
        {isPending ? "저장 중..." : "담당자 배정"}
      </Button>
    </form>
  );
}
