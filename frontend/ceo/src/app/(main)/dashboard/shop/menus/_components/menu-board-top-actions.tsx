"use client";

import * as React from "react";

import { Button } from "@/components/ui/button";
import type { MenuBoardGroup } from "@/feature/product/domain";
import { PRODUCT_REPRESENTATIVE_COPY } from "@/feature/product/message";
import type { MenuCollectionImage, ShopOrderNotice } from "@/feature/shop/domain";
import { SHOP_MENU_COLLECTION_COPY, SHOP_ORDER_NOTICE_COPY } from "@/feature/shop/message";

import { MenuCollectionSheet } from "./menu-collection-sheet";
import { OrderNoticeSheet } from "./order-notice-sheet";
import { RepresentativeMenuSheet } from "./representative-menu-sheet";

interface MenuBoardTopActionsProps {
  shopId: number;
  disabled: boolean;
  /** 서버에서 받은 초기 목록. 시트는 열릴 때 재조회하지만 첫 렌더는 이 값으로 채운다 */
  menuCollectionImages?: MenuCollectionImage[];
  orderNotice?: ShopOrderNotice;
  /**
   * 사장님 추천 선택 다이얼로그가 고를 후보.
   *
   * 메뉴 목록 전용 API 를 따로 두지 않고 메뉴판이 이미 받아 둔 그룹을 그대로 쓴다 —
   * 후보 판정에 필요한 `imageUrl`·`representative` 가 그 응답에 이미 들어 있다.
   */
  groups?: MenuBoardGroup[];
}

/**
 * 메뉴판 상단 진입점 버튼 줄.
 *
 * `가게 메뉴판 편집.pdf` 의 화면 구조대로 홍보 3종을 한 줄에 모은다.
 * 원산지 버튼(`menu-legal-info` 덩어리)도 같은 줄에 놓일 자리인데, 그 덩어리가 아직
 * 구현되지 않아 지금은 두지 않는다 — 구현되면 아래 주문안내 버튼 뒤에 추가한다.
 */
export function MenuBoardTopActions({
  shopId,
  disabled,
  menuCollectionImages,
  orderNotice,
  groups,
}: MenuBoardTopActionsProps) {
  const [openSheet, setOpenSheet] = React.useState<"collection" | "representative" | "notice" | null>(null);

  return (
    <div className="flex flex-wrap items-center gap-2">
      <Button type="button" variant="outline" disabled={disabled} onClick={() => setOpenSheet("collection")}>
        {SHOP_MENU_COLLECTION_COPY.SHEET_TITLE}
      </Button>
      <Button type="button" variant="outline" disabled={disabled} onClick={() => setOpenSheet("representative")}>
        {PRODUCT_REPRESENTATIVE_COPY.SHEET_TITLE}
      </Button>
      <Button type="button" variant="outline" disabled={disabled} onClick={() => setOpenSheet("notice")}>
        {SHOP_ORDER_NOTICE_COPY.SHEET_TITLE}
      </Button>
      {/* 원산지 버튼은 `menu-legal-info` 덩어리 구현 시 여기에 추가한다 */}

      <MenuCollectionSheet
        open={openSheet === "collection"}
        onOpenChange={(next) => setOpenSheet(next ? "collection" : null)}
        shopId={shopId}
        initialImages={menuCollectionImages}
      />
      <RepresentativeMenuSheet
        open={openSheet === "representative"}
        onOpenChange={(next) => setOpenSheet(next ? "representative" : null)}
        shopId={shopId}
        groups={groups}
      />
      <OrderNoticeSheet
        open={openSheet === "notice"}
        onOpenChange={(next) => setOpenSheet(next ? "notice" : null)}
        shopId={shopId}
        initialNotice={orderNotice}
      />
    </div>
  );
}
