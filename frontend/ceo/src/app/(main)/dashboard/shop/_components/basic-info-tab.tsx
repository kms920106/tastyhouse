"use client";

import * as React from "react";

import { Badge } from "@/components/ui/badge";
import { SHOP_STATUS_LABEL } from "@/feature/shop/constants";
import type { ShopBasicInfo } from "@/feature/shop/domain";
import { SHOP_BASIC_COPY } from "@/feature/shop/message";
import type { ShopNoticeItem } from "@/feature/shop-notice/domain";
import { SHOP_NOTICE_COPY } from "@/feature/shop-notice/message";

import { ContentBoardSheet } from "./content-board-sheet";
import { ConveniencesSheet } from "./conveniences-sheet";
import { IntroductionSheet } from "./introduction-sheet";
import { NoticeSheet } from "./notice-sheet";
import { PhoneNumbersSheet } from "./phone-numbers-sheet";
import { SettingRow } from "./setting-row";
import { ShopStatusSheet } from "./shop-status-sheet";
import { ThumbnailImageCard } from "./thumbnail-image-card";
import { TrademarkRequestSheet } from "./trademark-request-sheet";

type SheetKey = "trademark" | "introduction" | "contentBoard" | "notice" | "phoneNumbers" | "status" | "conveniences";

interface BasicInfoTabProps {
  shopId: number;
  basicInfo: ShopBasicInfo;
  /** 공지 목록. 조회 실패 시 빈 배열로 넘어와 시트는 열리되 목록만 비어 보인다 */
  notices: ShopNoticeItem[];
}

function truncate(value: string, max = 60): string {
  return value.length > max ? `${value.slice(0, max)}...` : value;
}

export function BasicInfoTab({ shopId, basicInfo, notices }: BasicInfoTabProps) {
  const [openSheet, setOpenSheet] = React.useState<SheetKey | null>(null);

  const closeSheet = () => setOpenSheet(null);

  const primaryPhone = basicInfo.phoneNumbers.find((item) => item.primary);
  // 게시중단된 공지는 켜져 있어도 고객에게 보이지 않으므로 설정행 요약에서 "노출중"으로 치지 않는다.
  const exposedNotice = notices.find((item) => item.exposed && !item.hidden);
  const hasTrademarkPending = basicInfo.trademarkStatus.requests.some((request) => request.status === "PENDING");
  // hidden 은 노출정지 상태를 뜻한다 — 스펙상 상태값은 OPEN/HIDDEN 두 가지뿐이다.
  const status = basicInfo.hidden ? "HIDDEN" : "OPEN";

  const convenienceSummaryParts = [
    `${SHOP_BASIC_COPY.PARKING_AVAILABLE} ${basicInfo.convenienceInfo.parkingAvailable ? "O" : "X"}`,
    `${SHOP_BASIC_COPY.VALET_AVAILABLE} ${basicInfo.convenienceInfo.valetAvailable ? "O" : "X"}`,
  ];
  if (basicInfo.amenities.length > 0) {
    convenienceSummaryParts.push(basicInfo.amenities.map((item) => item.displayName).join(", "));
  }

  return (
    <div className="flex flex-col">
      <ThumbnailImageCard
        shopId={shopId}
        thumbnailImageUrl={basicInfo.thumbnailImageUrl}
        thumbnailStatus={basicInfo.thumbnailStatus}
      />

      <SettingRow
        title={SHOP_BASIC_COPY.TRADEMARK_TITLE}
        description={SHOP_BASIC_COPY.TRADEMARK_DESCRIPTION}
        summary={
          hasTrademarkPending ? (
            <Badge variant="secondary">{SHOP_BASIC_COPY.IMAGE_PENDING_BADGE}</Badge>
          ) : (basicInfo.trademarkStatus.currentImageUrl ?? basicInfo.trademarkImageUrl) ? (
            <span>{SHOP_BASIC_COPY.IMAGE_REGISTERED}</span>
          ) : undefined
        }
        onAction={() => setOpenSheet("trademark")}
      />

      <SettingRow
        title={SHOP_BASIC_COPY.INTRODUCTION_TITLE}
        description={SHOP_BASIC_COPY.INTRODUCTION_DESCRIPTION}
        summary={basicInfo.introduction ? truncate(basicInfo.introduction) : undefined}
        onAction={() => setOpenSheet("introduction")}
      />

      <SettingRow
        title={SHOP_BASIC_COPY.CONTENT_BOARD_TITLE}
        description={SHOP_BASIC_COPY.CONTENT_BOARD_DESCRIPTION}
        summary={basicInfo.contentBoards.length > 0 ? `${basicInfo.contentBoards.length}건 등록` : undefined}
        onAction={() => setOpenSheet("contentBoard")}
      />

      <SettingRow
        title={SHOP_NOTICE_COPY.ENTRY_TITLE}
        description={SHOP_NOTICE_COPY.ENTRY_DESCRIPTION}
        summary={
          exposedNotice ? (
            <div className="flex flex-wrap items-center gap-2">
              <Badge variant="secondary">{SHOP_NOTICE_COPY.BADGE_EXPOSED}</Badge>
              <span>{truncate(exposedNotice.content)}</span>
            </div>
          ) : notices.length > 0 ? (
            `${notices.length}건 등록`
          ) : undefined
        }
        onAction={() => setOpenSheet("notice")}
      />

      <SettingRow
        title={SHOP_BASIC_COPY.PHONE_NUMBER_TITLE}
        description={SHOP_BASIC_COPY.PHONE_NUMBER_DESCRIPTION}
        summary={
          primaryPhone
            ? `${primaryPhone.phoneNumber} (대표) · 총 ${basicInfo.phoneNumbers.length}건`
            : basicInfo.phoneNumbers.length > 0
              ? `${basicInfo.phoneNumbers.length}건 등록 (대표번호 미지정)`
              : undefined
        }
        onAction={() => setOpenSheet("phoneNumbers")}
      />

      <SettingRow
        title={SHOP_BASIC_COPY.STATUS_TITLE}
        description={SHOP_BASIC_COPY.STATUS_DESCRIPTION}
        summary={
          <div className="flex flex-wrap items-center gap-2">
            <Badge variant={status === "OPEN" ? "secondary" : "destructive"}>{SHOP_STATUS_LABEL[status]}</Badge>
            {/* 폐업 여부는 서버가 관리하는 읽기 전용 값이라 표시만 한다. */}
            {basicInfo.permanentlyClosed && (
              <Badge variant="destructive">{SHOP_BASIC_COPY.PERMANENTLY_CLOSED_BADGE}</Badge>
            )}
          </div>
        }
        onAction={() => setOpenSheet("status")}
      />

      <SettingRow
        title={SHOP_BASIC_COPY.CONVENIENCE_TITLE}
        description={SHOP_BASIC_COPY.CONVENIENCE_DESCRIPTION}
        summary={convenienceSummaryParts.join(" · ")}
        onAction={() => setOpenSheet("conveniences")}
      />

      <TrademarkRequestSheet
        open={openSheet === "trademark"}
        onOpenChange={closeSheet}
        shopId={shopId}
        trademarkImageUrl={basicInfo.trademarkImageUrl}
        trademarkStatus={basicInfo.trademarkStatus}
      />
      <IntroductionSheet
        open={openSheet === "introduction"}
        onOpenChange={closeSheet}
        shopId={shopId}
        introduction={basicInfo.introduction}
      />
      <ContentBoardSheet
        open={openSheet === "contentBoard"}
        onOpenChange={closeSheet}
        shopId={shopId}
        contentBoards={basicInfo.contentBoards}
      />
      <NoticeSheet open={openSheet === "notice"} onOpenChange={closeSheet} shopId={shopId} notices={notices} />
      <PhoneNumbersSheet
        open={openSheet === "phoneNumbers"}
        onOpenChange={closeSheet}
        shopId={shopId}
        phoneNumbers={basicInfo.phoneNumbers}
      />
      <ShopStatusSheet open={openSheet === "status"} onOpenChange={closeSheet} shopId={shopId} status={status} />
      <ConveniencesSheet
        open={openSheet === "conveniences"}
        onOpenChange={closeSheet}
        shopId={shopId}
        convenienceInfo={basicInfo.convenienceInfo}
        amenities={basicInfo.amenities}
        roadAddress={basicInfo.roadAddress}
      />
    </div>
  );
}
