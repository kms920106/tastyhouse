"use client";

import * as React from "react";

import { Button } from "@/components/ui/button";
import type { MenuBoardGroup } from "@/feature/product/domain";
import { PRODUCT_REPRESENTATIVE_COPY, STORE_PRICE_VERIFICATION_COPY } from "@/feature/product/message";
import type { MenuCollectionImage, ShopOrderNotice, ShopOrigin } from "@/feature/shop/domain";
import { SHOP_MENU_COLLECTION_COPY, SHOP_ORDER_NOTICE_COPY, SHOP_ORIGIN_COPY } from "@/feature/shop/message";

import { MenuCollectionSheet } from "./menu-collection-sheet";
import { OrderNoticeSheet } from "./order-notice-sheet";
import { RepresentativeMenuSheet } from "./representative-menu-sheet";
import { ShopOriginSheet } from "./shop-origin-sheet";
import { StorePriceVerificationSheet } from "./store-price-verification-sheet";

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
  /** 원산지 초기값. 미설정 가게도 기본값 객체가 내려오므로 조회 실패일 때만 undefined 다 */
  origin?: ShopOrigin;
}

/**
 * 메뉴판 상단 진입점 버튼 줄.
 *
 * `가게 메뉴판 편집.pdf` 의 화면 구조대로 홍보 3종과 원산지를 한 줄에 모은다.
 *
 * 원산지는 메뉴 하나가 아니라 **가게 전체** 설정이라 메뉴 상세가 아니라 여기에 둔다
 * (출처 PDF 와 같은 배치). 홍보 3종과 달리 검수 대상이 아니다 — 사실 정보라 관리자가
 * 검증할 근거가 없다.
 */
export function MenuBoardTopActions({
  shopId,
  disabled,
  menuCollectionImages,
  orderNotice,
  groups,
  origin,
}: MenuBoardTopActionsProps) {
  const [openSheet, setOpenSheet] = React.useState<
    "collection" | "representative" | "notice" | "origin" | "storePrice" | null
  >(null);

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
      <Button type="button" variant="outline" disabled={disabled} onClick={() => setOpenSheet("origin")}>
        {SHOP_ORIGIN_COPY.SHEET_TITLE}
      </Button>
      <Button type="button" variant="outline" disabled={disabled} onClick={() => setOpenSheet("storePrice")}>
        {STORE_PRICE_VERIFICATION_COPY.SHEET_TITLE}
      </Button>

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
      <ShopOriginSheet
        open={openSheet === "origin"}
        onOpenChange={(next) => setOpenSheet(next ? "origin" : null)}
        shopId={shopId}
        initialOrigin={origin}
      />
      <StorePriceVerificationSheet
        open={openSheet === "storePrice"}
        onOpenChange={(next) => setOpenSheet(next ? "storePrice" : null)}
        shopId={shopId}
        groups={groups}
      />
    </div>
  );
}
