"use client";

import * as React from "react";

import { Separator } from "@/components/ui/separator";
import {
  CLOSED_DAY_TYPE_LABEL,
  type ClosedDayTypeOption,
  DAY_TYPE_LABEL,
  MIN_ORDER_AMOUNT_UNSET,
  WEEKDAY_OPTIONS,
  type WeekdayOption,
} from "@/feature/shop/constants";
import type { ShopOperationInfo } from "@/feature/shop/domain";
import { SHOP_OPERATION_COPY } from "@/feature/shop/message";
import { formatTimeLabel } from "@/feature/shop/time";

import { BusinessHoursSheet } from "./business-hours-sheet";
import { ClosedDaysSheet } from "./closed-days-sheet";
import { HygieneInfoCard } from "./hygiene-info-card";
import { MinOrderAmountSheet } from "./min-order-amount-sheet";
import { SettingRow } from "./setting-row";

interface OperationInfoTabProps {
  shopId: number;
  operationInfo: ShopOperationInfo;
  /** 가게 상세(basicInfo)에서 전달받는 최소주문금액. 0이면 미설정(제한 없음) */
  minOrderAmount?: number;
}

export function OperationInfoTab({ shopId, operationInfo, minOrderAmount }: OperationInfoTabProps) {
  const [editingDay, setEditingDay] = React.useState<WeekdayOption | null>(null);
  const [closedDaysOpen, setClosedDaysOpen] = React.useState(false);
  const [minOrderAmountOpen, setMinOrderAmountOpen] = React.useState(false);

  const currentMinOrderAmount = minOrderAmount ?? MIN_ORDER_AMOUNT_UNSET;

  const businessHourByDay = React.useMemo(
    () => new Map(operationInfo.businessHours.map((item) => [item.dayType, item])),
    [operationInfo.businessHours],
  );
  const breakTimeByDay = React.useMemo(
    () => new Map(operationInfo.breakTimes.map((item) => [item.dayType, item])),
    [operationInfo.breakTimes],
  );

  const { closedOnPublicHolidays, regularClosedDays, temporaryClosures } = operationInfo.closedDays;

  const closedDaySummaryParts: string[] = [];
  if (closedOnPublicHolidays) closedDaySummaryParts.push(SHOP_OPERATION_COPY.HOLIDAY_CLOSED_ON);
  if (regularClosedDays.length > 0) {
    closedDaySummaryParts.push(
      regularClosedDays
        .map((item) => item.description || CLOSED_DAY_TYPE_LABEL[item.closedDayType as ClosedDayTypeOption])
        .join(", "),
    );
  }
  if (temporaryClosures.length > 0) closedDaySummaryParts.push(`임시휴무 ${temporaryClosures.length}건`);

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col">
        <div className="flex flex-col gap-1 pb-2">
          <span className="font-medium text-sm">{SHOP_OPERATION_COPY.BUSINESS_HOURS_TITLE}</span>
          <span className="text-muted-foreground text-xs leading-snug">
            {SHOP_OPERATION_COPY.BUSINESS_HOURS_DESCRIPTION}
          </span>
        </div>

        {WEEKDAY_OPTIONS.map((day) => {
          const businessHour = businessHourByDay.get(day);
          const breakTime = breakTimeByDay.get(day);

          const summaryParts: string[] = [];
          if (!businessHour) {
            summaryParts.push(SHOP_OPERATION_COPY.NOT_REGISTERED);
          } else if (businessHour.isClosed) {
            summaryParts.push(SHOP_OPERATION_COPY.CLOSED);
          } else if (businessHour.is24Hours) {
            summaryParts.push(SHOP_OPERATION_COPY.ALL_DAY);
          } else {
            summaryParts.push(`${formatTimeLabel(businessHour.openTime)} ~ ${formatTimeLabel(businessHour.closeTime)}`);
          }
          if (breakTime) {
            summaryParts.push(`휴게 ${formatTimeLabel(breakTime.startTime)} ~ ${formatTimeLabel(breakTime.endTime)}`);
          }

          return (
            <SettingRow
              key={day}
              title={DAY_TYPE_LABEL[day]}
              summary={summaryParts.join(" · ")}
              onAction={() => setEditingDay(day)}
            />
          );
        })}
      </div>

      <Separator />

      <SettingRow
        title={SHOP_OPERATION_COPY.CLOSED_DAYS_TITLE}
        description={SHOP_OPERATION_COPY.CLOSED_DAYS_DESCRIPTION}
        summary={closedDaySummaryParts.length > 0 ? closedDaySummaryParts.join(" · ") : undefined}
        onAction={() => setClosedDaysOpen(true)}
      />

      <Separator />

      <SettingRow
        title={SHOP_OPERATION_COPY.MIN_ORDER_AMOUNT_TITLE}
        description={SHOP_OPERATION_COPY.MIN_ORDER_AMOUNT_DESCRIPTION}
        summary={
          currentMinOrderAmount > MIN_ORDER_AMOUNT_UNSET
            ? `${currentMinOrderAmount.toLocaleString("ko-KR")}원`
            : SHOP_OPERATION_COPY.MIN_ORDER_AMOUNT_UNSET_LABEL
        }
        onAction={() => setMinOrderAmountOpen(true)}
      />

      <Separator />

      <HygieneInfoCard badges={operationInfo.hygieneBadges} />

      {editingDay && (
        <BusinessHoursSheet
          open
          onOpenChange={() => setEditingDay(null)}
          shopId={shopId}
          dayType={editingDay}
          businessHour={businessHourByDay.get(editingDay)}
          breakTime={breakTimeByDay.get(editingDay)}
          businessHourByDay={businessHourByDay}
          breakTimeByDay={breakTimeByDay}
        />
      )}

      <ClosedDaysSheet
        open={closedDaysOpen}
        onOpenChange={setClosedDaysOpen}
        shopId={shopId}
        closedDays={operationInfo.closedDays}
      />

      <MinOrderAmountSheet
        open={minOrderAmountOpen}
        onOpenChange={setMinOrderAmountOpen}
        shopId={shopId}
        minOrderAmount={currentMinOrderAmount}
      />
    </div>
  );
}
