"use client";
"use no memo";

import * as React from "react";

import { useRouter, useSearchParams } from "next/navigation";

import { Play } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { RANK_TYPE_LABELS } from "@/feature/rank/constants";
import type { RankMember, RankPeriod, RankType } from "@/feature/rank/domain";
import { RANK_PAGE_COPY } from "@/feature/rank/message";

import { AggregationDialog } from "./aggregation-dialog";
import { DeletePeriodDialog } from "./delete-period-dialog";
import { PeriodDetailSheet } from "./period-detail-sheet";
import { PeriodFormSheet } from "./period-form-sheet";
import { PeriodPrizesSheet } from "./period-prizes-sheet";
import { RankMembersTable } from "./rank-members-table";
import { RankPeriodsTable } from "./rank-periods-table";

interface RanksProps {
  members: RankMember[];
  periods: RankPeriod[];
  initialType: RankType;
  initialLimit: number;
}

export function Ranks({ members, periods, initialType, initialLimit }: RanksProps) {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [isPending, startTransition] = React.useTransition();
  const [typeInput, setTypeInput] = React.useState<string>(initialType);
  const [limitInput, setLimitInput] = React.useState(String(initialLimit));
  const [aggregationOpen, setAggregationOpen] = React.useState(false);

  const [periodFormOpen, setPeriodFormOpen] = React.useState(false);
  const [editingPeriod, setEditingPeriod] = React.useState<RankPeriod | null>(null);
  const [detailPeriodId, setDetailPeriodId] = React.useState<number | null>(null);
  const [prizesTarget, setPrizesTarget] = React.useState<RankPeriod | null>(null);
  const [deleteTarget, setDeleteTarget] = React.useState<RankPeriod | null>(null);

  function pushMemberParams(next: { type?: string; limit?: string }) {
    const params = new URLSearchParams(searchParams.toString());
    if (next.type !== undefined) params.set("type", next.type);
    if (next.limit !== undefined) {
      if (next.limit.trim() === "") params.delete("limit");
      else params.set("limit", next.limit);
    }
    startTransition(() => {
      router.push(`?${params.toString()}`);
    });
  }

  function openCreatePeriod() {
    setEditingPeriod(null);
    setPeriodFormOpen(true);
  }

  function openEditPeriod(period: RankPeriod) {
    setEditingPeriod(period);
    setPeriodFormOpen(true);
  }

  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{RANK_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{RANK_PAGE_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Button size="sm" onClick={() => setAggregationOpen(true)}>
            <Play /> 집계 실행
          </Button>
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-4 px-0">
        <Tabs defaultValue="members" className="px-4">
          <TabsList>
            <TabsTrigger value="members">회원 랭킹</TabsTrigger>
            <TabsTrigger value="periods">기간·경품 관리</TabsTrigger>
          </TabsList>

          <TabsContent value="members" className="flex flex-col gap-4">
            <div className="flex flex-wrap items-center gap-2 pt-2">
              <Select
                value={typeInput}
                onValueChange={(value) => {
                  setTypeInput(value);
                  pushMemberParams({ type: value });
                }}
                disabled={isPending}
              >
                <SelectTrigger size="sm">
                  <span className="text-muted-foreground">타입:</span>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent position="popper" align="start">
                  <SelectGroup>
                    {Object.entries(RANK_TYPE_LABELS).map(([value, label]) => (
                      <SelectItem key={value} value={value}>
                        {label}
                      </SelectItem>
                    ))}
                  </SelectGroup>
                </SelectContent>
              </Select>
              <Input
                className="w-28"
                type="number"
                min={1}
                placeholder="조회 개수"
                value={limitInput}
                onChange={(e) => setLimitInput(e.target.value)}
                onBlur={() => pushMemberParams({ limit: limitInput })}
                disabled={isPending}
              />
            </div>
            <RankMembersTable members={members} isPending={isPending} />
          </TabsContent>

          <TabsContent value="periods" className="flex flex-col gap-4">
            <div className="flex justify-end pt-2">
              <Button size="sm" onClick={openCreatePeriod}>
                기간 등록
              </Button>
            </div>
            <RankPeriodsTable
              periods={periods}
              meta={{
                onView: (period) => setDetailPeriodId(period.id),
                onEdit: (period) => openEditPeriod(period),
                onManagePrizes: (period) => setPrizesTarget(period),
                onDelete: (period) => setDeleteTarget(period),
              }}
            />
          </TabsContent>
        </Tabs>
      </CardContent>

      <AggregationDialog open={aggregationOpen} onOpenChange={setAggregationOpen} />
      <PeriodFormSheet open={periodFormOpen} onOpenChange={setPeriodFormOpen} period={editingPeriod} />
      <PeriodDetailSheet periodId={detailPeriodId} onOpenChange={(open) => !open && setDetailPeriodId(null)} />
      <PeriodPrizesSheet period={prizesTarget} onOpenChange={(open) => !open && setPrizesTarget(null)} />
      <DeletePeriodDialog period={deleteTarget} onOpenChange={(open) => !open && setDeleteTarget(null)} />
    </Card>
  );
}
