"use client";

import * as React from "react";

import { PowerOff, Store } from "lucide-react";
import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import { Switch } from "@/components/ui/switch";
import { ORDER_METHOD_LABEL, SUSPENSION_REASON_LABEL } from "@/feature/shop/constants";
import type { Suspension } from "@/feature/shop/domain";
import { SHOP_MESSAGE, SHOP_STATUS_PAGE_COPY } from "@/feature/shop/message";
import { formatDateTime } from "@/lib/date";

import { ResumeSuspensionDialog } from "./resume-suspension-dialog";
import { SuspensionSheet } from "./suspension-sheet";

export interface ShopStatusRow {
  shopId: number;
  shopName: string;
  permanentlyClosed: boolean;
  /** 가게별 임시중지 조회가 실패한 경우 — 스위치를 조작하지 못하게 막고 사유를 노출한다. */
  loadFailed: boolean;
  /** releasedAt === null 인 건만 담긴다. 주문유형별로 여러 건일 수 있다. */
  activeSuspensions: Suspension[];
}

interface ShopStatusOverviewProps {
  rows: ShopStatusRow[];
}

/** 활성 임시중지 건들을 "사유 · 주문유형 · 종료일시" 한 줄 요약으로 합친다. */
function summarizeSuspensions(suspensions: Suspension[]): string {
  const reasons = [...new Set(suspensions.map((item) => SUSPENSION_REASON_LABEL[item.reason]))].join(", ");
  // orderMethod 가 null 이면 전체 주문유형 대상으로 등록된 건이다.
  const orderMethods = [
    ...new Set(
      suspensions.map((item) =>
        item.orderMethod ? ORDER_METHOD_LABEL[item.orderMethod] : SHOP_STATUS_PAGE_COPY.ALL_ORDER_METHODS,
      ),
    ),
  ].join(", ");
  // 여러 건이면 가장 늦게 끝나는 시점을 기준으로 보여준다.
  const latestEndAt = suspensions.reduce<string | null>(
    (latest, item) => (latest === null || item.endAt > latest ? item.endAt : latest),
    null,
  );

  return `${reasons} · ${orderMethods} · ${formatDateTime(latestEndAt)}까지`;
}

export function ShopStatusOverview({ rows }: ShopStatusOverviewProps) {
  const [selectedIds, setSelectedIds] = React.useState<number[]>([]);
  const [suspensionTarget, setSuspensionTarget] = React.useState<{ shopIds: number[]; label: string } | null>(null);
  const [resumeTarget, setResumeTarget] = React.useState<{
    shopId: number;
    suspensionId: number;
    shopName: string;
  } | null>(null);

  function toggleSelected(shopId: number, checked: boolean) {
    setSelectedIds((previous) => (checked ? [...previous, shopId] : previous.filter((id) => id !== shopId)));
  }

  function handleSwitchChange(row: ShopStatusRow, checked: boolean) {
    if (checked) {
      setSuspensionTarget({ shopIds: [row.shopId], label: row.shopName });
      return;
    }

    // 주문유형별로 여러 건이 걸려 있을 수 있어, 해제는 가장 먼저 조회된 활성 건을 대상으로 한다.
    const target = row.activeSuspensions[0];
    if (target) {
      setResumeTarget({ shopId: row.shopId, suspensionId: target.id, shopName: row.shopName });
    } else {
      toast.error(SHOP_MESSAGE.SUSPENSION_RESUME_UNAVAILABLE);
    }
  }

  function openBulkSuspension() {
    const targetIds = selectedIds.length > 0 ? selectedIds : rows.map((row) => row.shopId);
    const label = selectedIds.length > 0 ? `선택한 ${selectedIds.length}개 가게` : "전체 가게";
    setSuspensionTarget({ shopIds: targetIds, label });
  }

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{SHOP_STATUS_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{SHOP_STATUS_PAGE_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Button size="sm" variant="destructive" disabled={rows.length === 0} onClick={openBulkSuspension}>
            <PowerOff />
            {selectedIds.length > 0 ? `선택 ${selectedIds.length}개 임시중지` : SHOP_STATUS_PAGE_COPY.BULK_SUSPEND_ALL}
          </Button>
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-2">
        {rows.length === 0 ? (
          <Empty>
            <EmptyHeader>
              <EmptyMedia variant="icon">
                <Store />
              </EmptyMedia>
              <EmptyTitle>{SHOP_STATUS_PAGE_COPY.EMPTY_TITLE}</EmptyTitle>
              <EmptyDescription>{SHOP_STATUS_PAGE_COPY.EMPTY_DESCRIPTION}</EmptyDescription>
            </EmptyHeader>
          </Empty>
        ) : (
          rows.map((row) => {
            const isSuspended = row.activeSuspensions.length > 0;

            return (
              <div key={row.shopId} className="flex items-center justify-between gap-4 rounded-md border px-4 py-3">
                <div className="flex min-w-0 flex-1 items-center gap-3">
                  <Checkbox
                    id={`shop-select-${row.shopId}`}
                    checked={selectedIds.includes(row.shopId)}
                    onCheckedChange={(checked) => toggleSelected(row.shopId, checked === true)}
                    aria-label={`${row.shopName} 선택`}
                  />
                  <div className="flex min-w-0 flex-1 flex-col gap-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="truncate font-medium text-sm">{row.shopName}</span>
                      <Badge variant={isSuspended ? "destructive" : "secondary"}>
                        {isSuspended ? SHOP_STATUS_PAGE_COPY.SUSPENDED_BADGE : SHOP_STATUS_PAGE_COPY.OPERATING_BADGE}
                      </Badge>
                      {row.permanentlyClosed && (
                        <Badge variant="destructive">{SHOP_STATUS_PAGE_COPY.PERMANENTLY_CLOSED_BADGE}</Badge>
                      )}
                    </div>
                    {row.loadFailed ? (
                      <span className="text-destructive text-xs">{SHOP_STATUS_PAGE_COPY.SUSPENSION_LOAD_FAILED}</span>
                    ) : (
                      isSuspended && (
                        <span className="text-muted-foreground text-xs">
                          {summarizeSuspensions(row.activeSuspensions)}
                        </span>
                      )
                    )}
                  </div>
                </div>
                <Switch
                  checked={isSuspended}
                  disabled={row.loadFailed}
                  onCheckedChange={(checked) => handleSwitchChange(row, checked)}
                  aria-label={`${row.shopName} 임시중지`}
                />
              </div>
            );
          })
        )}
      </CardContent>

      <SuspensionSheet
        open={suspensionTarget !== null}
        onOpenChange={(open) => !open && setSuspensionTarget(null)}
        shopIds={suspensionTarget?.shopIds ?? []}
        targetLabel={suspensionTarget?.label ?? ""}
      />
      <ResumeSuspensionDialog target={resumeTarget} onOpenChange={(open) => !open && setResumeTarget(null)} />
    </Card>
  );
}
