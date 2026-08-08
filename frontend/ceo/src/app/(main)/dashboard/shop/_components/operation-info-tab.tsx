"use client";

import * as React from "react";

import { Separator } from "@/components/ui/separator";
import {
  CLOSED_DAY_TYPE_LABEL,
  type ClosedDayTypeOption,
  DAY_TYPE_LABEL,
  DELIVERY_TIP_UNSET,
  MIN_ORDER_AMOUNT_UNSET,
  WEEKDAY_OPTIONS,
  type WeekdayOption,
} from "@/feature/shop/constants";
import type { ShopOperationInfo } from "@/feature/shop/domain";
import { SHOP_OPERATION_COPY, SHOP_RIDER_COPY } from "@/feature/shop/message";
import { formatTimeLabel } from "@/feature/shop/time";

import { BusinessHoursSheet } from "./business-hours-sheet";
import { ClosedDaysSheet } from "./closed-days-sheet";
import { DeliveryTipExtraSheet } from "./delivery-tip-extra-sheet";
import { DeliveryTipTiersSheet } from "./delivery-tip-tiers-sheet";
import { HygieneInfoCard } from "./hygiene-info-card";
import { MinOrderAmountSheet } from "./min-order-amount-sheet";
import { RiderPickupLocationSheet } from "./rider-pickup-location-sheet";
import { RiderVisitGuideSheet } from "./rider-visit-guide-sheet";
import { ScheduledOrderSheet } from "./scheduled-order-sheet";
import { SettingRow } from "./setting-row";

interface OperationInfoTabProps {
  shopId: number;
  operationInfo: ShopOperationInfo;
  /** 가게 상세(basicInfo)에서 전달받는 최소주문금액. 0이면 미설정(제한 없음) */
  minOrderAmount?: number;
  /** 가게 상세(basicInfo)에서 전달받는 예약주문 운영 여부 */
  scheduledOrderEnabled?: boolean;
}

export function OperationInfoTab({
  shopId,
  operationInfo,
  minOrderAmount,
  scheduledOrderEnabled,
}: OperationInfoTabProps) {
  const [editingDay, setEditingDay] = React.useState<WeekdayOption | null>(null);
  const [closedDaysOpen, setClosedDaysOpen] = React.useState(false);
  const [minOrderAmountOpen, setMinOrderAmountOpen] = React.useState(false);
  const [scheduledOrderOpen, setScheduledOrderOpen] = React.useState(false);
  const [deliveryTipTiersOpen, setDeliveryTipTiersOpen] = React.useState(false);
  const [deliveryTipExtraOpen, setDeliveryTipExtraOpen] = React.useState(false);
  const [riderVisitGuideOpen, setRiderVisitGuideOpen] = React.useState(false);
  const [riderPickupLocationOpen, setRiderPickupLocationOpen] = React.useState(false);

  const currentMinOrderAmount = minOrderAmount ?? MIN_ORDER_AMOUNT_UNSET;
  const currentScheduledOrderEnabled = scheduledOrderEnabled ?? false;

  const businessHourByDay = React.useMemo(
    () => new Map(operationInfo.businessHours.map((item) => [item.dayType, item])),
    [operationInfo.businessHours],
  );
  const breakTimeByDay = React.useMemo(
    () => new Map(operationInfo.breakTimes.map((item) => [item.dayType, item])),
    [operationInfo.breakTimes],
  );

  const { closedOnPublicHolidays, regularClosedDays, temporaryClosures } = operationInfo.closedDays;

  const { deliveryTip, deliveryAreas, riderGuide } = operationInfo;

  // SettingRow 의 summary 는 CSS 로 한 줄 truncate 되므로, JS 로 문자열을 잘라 "..." 를 붙이지 않는다.
  const visitGuideSummary = riderGuide.visitGuide ?? SHOP_RIDER_COPY.UNSET_LABEL;

  const pickupLocationSummary = riderGuide.pickupLocation
    ? [riderGuide.pickupLocation.roadAddress, riderGuide.pickupLocation.detailAddress].filter(Boolean).join(" ")
    : SHOP_RIDER_COPY.PICKUP_FALLBACK_LABEL;

  // 배달팁 요약 — 첫 구간(가장 낮은 주문금액) 배달팁과 구간 수를 함께 보여준다
  const deliveryTipSummary =
    deliveryTip.tiers.length > 0
      ? [`${deliveryTip.tiers[0].tipAmount.toLocaleString("ko-KR")}원`, `${deliveryTip.tiers.length}구간`].join(" · ")
      : SHOP_OPERATION_COPY.DELIVERY_TIP_UNSET_LABEL;

  const extraDeliveryTipParts: string[] = [];
  if (deliveryTip.extraTipType === "DISTANCE") extraDeliveryTipParts.push("거리별");
  if (deliveryTip.extraTipType === "REGION") {
    extraDeliveryTipParts.push(`지역별 ${deliveryTip.regions.length}건`);
  }
  if (deliveryTip.schedules.length > 0) extraDeliveryTipParts.push(`시간별 ${deliveryTip.schedules.length}건`);
  if (deliveryTip.holidayTipAmount > DELIVERY_TIP_UNSET) {
    extraDeliveryTipParts.push(`공휴일 ${deliveryTip.holidayTipAmount.toLocaleString("ko-KR")}원`);
  }
  const extraDeliveryTipSummary =
    extraDeliveryTipParts.length > 0 ? extraDeliveryTipParts.join(" · ") : SHOP_OPERATION_COPY.DELIVERY_TIP_UNSET_LABEL;

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

      <SettingRow
        title={SHOP_OPERATION_COPY.SCHEDULED_ORDER_TITLE}
        description={SHOP_OPERATION_COPY.SCHEDULED_ORDER_DESCRIPTION}
        summary={
          currentScheduledOrderEnabled
            ? SHOP_OPERATION_COPY.SCHEDULED_ORDER_ON_LABEL
            : SHOP_OPERATION_COPY.SCHEDULED_ORDER_OFF_LABEL
        }
        onAction={() => setScheduledOrderOpen(true)}
      />

      <Separator />

      <SettingRow
        title={SHOP_OPERATION_COPY.DELIVERY_TIP_TITLE}
        description={SHOP_OPERATION_COPY.DELIVERY_TIP_DESCRIPTION}
        summary={deliveryTipSummary}
        onAction={() => setDeliveryTipTiersOpen(true)}
      />

      <Separator />

      <SettingRow
        title={SHOP_OPERATION_COPY.EXTRA_DELIVERY_TIP_TITLE}
        description={SHOP_OPERATION_COPY.EXTRA_DELIVERY_TIP_DESCRIPTION}
        summary={extraDeliveryTipSummary}
        onAction={() => setDeliveryTipExtraOpen(true)}
      />

      <Separator />

      <SettingRow
        title={SHOP_RIDER_COPY.VISIT_GUIDE_TITLE}
        description={SHOP_RIDER_COPY.VISIT_GUIDE_DESCRIPTION}
        summary={visitGuideSummary}
        onAction={() => setRiderVisitGuideOpen(true)}
      />

      <Separator />

      <SettingRow
        title={SHOP_RIDER_COPY.PICKUP_TITLE}
        description={SHOP_RIDER_COPY.PICKUP_DESCRIPTION}
        summary={pickupLocationSummary}
        onAction={() => setRiderPickupLocationOpen(true)}
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

      <ScheduledOrderSheet
        open={scheduledOrderOpen}
        onOpenChange={setScheduledOrderOpen}
        shopId={shopId}
        enabled={currentScheduledOrderEnabled}
      />

      <DeliveryTipTiersSheet
        open={deliveryTipTiersOpen}
        onOpenChange={setDeliveryTipTiersOpen}
        shopId={shopId}
        tiers={deliveryTip.tiers}
      />

      <DeliveryTipExtraSheet
        open={deliveryTipExtraOpen}
        onOpenChange={setDeliveryTipExtraOpen}
        shopId={shopId}
        deliveryTip={deliveryTip}
        deliveryAreas={deliveryAreas}
      />

      <RiderVisitGuideSheet
        open={riderVisitGuideOpen}
        onOpenChange={setRiderVisitGuideOpen}
        shopId={shopId}
        visitGuide={riderGuide.visitGuide}
      />

      <RiderPickupLocationSheet
        open={riderPickupLocationOpen}
        onOpenChange={setRiderPickupLocationOpen}
        shopId={shopId}
        riderGuide={riderGuide}
      />
    </div>
  );
}
