"use client";

import * as React from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
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
import { fetchMemberAction } from "@/feature/member/actions";
import type { MemberDetail } from "@/feature/member/domain";
import {
  formatBirthDate,
  genderLabel,
  memberGradeLabel,
  memberStatusBadgeVariant,
  memberStatusLabel,
} from "@/feature/member/format";
import { MEMBER_MESSAGE } from "@/feature/member/message";
import { formatDateTime } from "@/lib/date";

interface MemberDetailSheetProps {
  /** 조회할 회원 ID. null 이면 닫힌 상태. */
  memberId: number | null;
  onOpenChange: (open: boolean) => void;
}

export function MemberDetailSheet({ memberId, onOpenChange }: MemberDetailSheetProps) {
  const [detail, setDetail] = React.useState<MemberDetail | null>(null);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (memberId == null) {
      return;
    }

    let active = true;
    setIsLoading(true);
    setError(null);
    setDetail(null);

    void fetchMemberAction(memberId).then((result) => {
      const { success, message, data } = result;

      if (!active) return;
      if (success && data) {
        setDetail(data);
      } else {
        setError(message ?? MEMBER_MESSAGE.DETAIL_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [memberId]);

  return (
    <Sheet open={memberId != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>회원 상세</SheetTitle>
          <SheetDescription>회원의 상세 정보를 확인합니다.</SheetDescription>
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
                <div className="flex items-center gap-3">
                  {detail.profileImageUrl ? (
                    // biome-ignore lint/performance/noImgElement: CDN 프로필 이미지 미리보기
                    <img
                      src={detail.profileImageUrl}
                      alt={detail.nickname}
                      className="size-12 rounded-full border object-cover"
                    />
                  ) : null}
                  <div>
                    <h3 className="font-semibold text-lg leading-snug">{detail.nickname}</h3>
                    <p className="text-muted-foreground text-sm">{detail.username}</p>
                  </div>
                </div>
                <Badge variant={memberStatusBadgeVariant(detail.memberStatus)}>
                  {memberStatusLabel(detail.memberStatus)}
                </Badge>
              </div>

              {detail.statusMessage ? (
                <p className="whitespace-pre-wrap break-words text-muted-foreground text-sm leading-relaxed">
                  {detail.statusMessage}
                </p>
              ) : null}

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">ID</dt>
                <dd className="tabular-nums">{detail.id}</dd>
                <dt className="text-muted-foreground">실명</dt>
                <dd>{detail.fullName}</dd>
                <dt className="text-muted-foreground">휴대폰번호</dt>
                <dd className="tabular-nums">{detail.phoneNumber}</dd>
                <dt className="text-muted-foreground">성별</dt>
                <dd>{genderLabel(detail.gender)}</dd>
                <dt className="text-muted-foreground">생년월일</dt>
                <dd className="tabular-nums">{formatBirthDate(detail.birthDate)}</dd>
                <dt className="text-muted-foreground">등급</dt>
                <dd>{memberGradeLabel(detail.memberGrade)}</dd>
              </dl>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">푸시 알림 동의</dt>
                <dd>
                  <Badge variant={detail.pushNotificationEnabled ? "default" : "secondary"}>
                    {detail.pushNotificationEnabled ? "동의" : "미동의"}
                  </Badge>
                </dd>
                <dt className="text-muted-foreground">마케팅 정보 수신 동의</dt>
                <dd>
                  <Badge variant={detail.marketingInfoEnabled ? "default" : "secondary"}>
                    {detail.marketingInfoEnabled ? "동의" : "미동의"}
                  </Badge>
                </dd>
                <dt className="text-muted-foreground">이벤트 정보 수신 동의</dt>
                <dd>
                  <Badge variant={detail.eventInfoEnabled ? "default" : "secondary"}>
                    {detail.eventInfoEnabled ? "동의" : "미동의"}
                  </Badge>
                </dd>
              </dl>

              <Separator />

              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <dt className="text-muted-foreground">가입일시</dt>
                <dd className="tabular-nums">{formatDateTime(detail.createdAt)}</dd>
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
