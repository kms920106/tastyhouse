import { productService } from "@/api/product/product.service";
import { shopService } from "@/api/shop/shop.service";
import type { ApprovalStatus } from "@/feature/product/domain";
import { PRODUCT_APPROVAL_MESSAGE } from "@/feature/product/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { ProductApprovals } from "./_components/product-approvals";

const MAX_PAGE_SIZE = 100;

const APPROVAL_STATUSES: readonly ApprovalStatus[] = ["PENDING", "APPROVED", "REJECTED", "CANCELED"];

const TABS = ["image", "vegetarian", "menuCollection", "representative"] as const;

type Tab = (typeof TABS)[number];

/** 알 수 없는 tab 값은 기본 탭(메뉴 이미지)으로 떨어뜨린다. */
function parseTab(value: string | string[] | undefined): Tab {
  const raw = parseSearchString(value);
  return TABS.find((tab) => tab === raw) ?? "image";
}

/** 화면이 알 수 없는 status 를 그대로 서버에 넘겨 400 을 받지 않도록, enum 밖의 값은 전체 조회로 떨어뜨린다. */
function parseApprovalStatus(value: string | string[] | undefined): ApprovalStatus | undefined {
  const raw = parseSearchString(value);
  return APPROVAL_STATUSES.find((status) => status === raw);
}

export default async function Page({ searchParams }: PageProps<"/dashboard/product-approvals">) {
  const { tab: tabParam, status: statusParam, page: pageParam, size: sizeParam } = await searchParams;

  const tab = parseTab(tabParam);
  const status = parseApprovalStatus(statusParam);
  const page = parseNonNegativeInt(pageParam, 0);
  const size = Math.min(parseNonNegativeInt(sizeParam, 10), MAX_PAGE_SIZE);
  const pageRequest = { page, size };

  // 탭마다 호출 API 가 다르므로 보이는 탭만 조회한다.
  if (tab === "vegetarian") {
    const { error, data, pagination } = await productService.getVegetarianRequests({ status }, pageRequest);
    if (error || !data || !pagination) {
      logger.error({ reason: error, tab, data, pagination }, "메뉴 채식 설정 요청 목록 조회 실패");
      throw new Error(PRODUCT_APPROVAL_MESSAGE.LOAD_FAILED);
    }
    return <ProductApprovals tab="vegetarian" requests={data} pagination={pagination} initialStatus={status} />;
  }

  // 메뉴모음컷은 가게(shop) 리소스라 productService 가 아닌 shopService 를 탄다.
  if (tab === "menuCollection") {
    const { error, data, pagination } = await shopService.getMenuCollectionImageRequests({ status }, pageRequest);
    if (error || !data || !pagination) {
      logger.error({ reason: error, tab, data, pagination }, "메뉴모음컷 승인요청 목록 조회 실패");
      throw new Error(PRODUCT_APPROVAL_MESSAGE.LOAD_FAILED);
    }
    return <ProductApprovals tab="menuCollection" requests={data} pagination={pagination} initialStatus={status} />;
  }

  if (tab === "representative") {
    const { error, data, pagination } = await productService.getRepresentativeRequests({ status }, pageRequest);
    if (error || !data || !pagination) {
      logger.error({ reason: error, tab, data, pagination }, "사장님 추천 지정 요청 목록 조회 실패");
      throw new Error(PRODUCT_APPROVAL_MESSAGE.LOAD_FAILED);
    }
    return <ProductApprovals tab="representative" requests={data} pagination={pagination} initialStatus={status} />;
  }

  const { error, data, pagination } = await productService.getImageChangeRequests({ status }, pageRequest);
  if (error || !data || !pagination) {
    logger.error({ reason: error, tab, data, pagination }, "메뉴 이미지 변경 요청 목록 조회 실패");
    throw new Error(PRODUCT_APPROVAL_MESSAGE.LOAD_FAILED);
  }
  return <ProductApprovals tab="image" requests={data} pagination={pagination} initialStatus={status} />;
}
