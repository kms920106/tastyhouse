import { partnershipRequestService } from "@/api/partnership-request/partnership-request.service";
import type { PartnershipStatus } from "@/feature/partnership-request/domain";
import { PARTNERSHIP_MESSAGE } from "@/feature/partnership-request/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { PartnershipRequests } from "./_components/partnership-requests";

const PARTNERSHIP_STATUSES: readonly PartnershipStatus[] = ["PENDING", "IN_PROGRESS", "COMPLETED"];

const MAX_PAGE_SIZE = 100;

function parsePartnershipStatus(value: string | string[] | undefined): PartnershipStatus | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  return PARTNERSHIP_STATUSES.includes(raw as PartnershipStatus) ? (raw as PartnershipStatus) : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/partnership-requests">) {
  const {
    page: pageParam,
    size: sizeParam,
    businessName: businessNameParam,
    contactName: contactNameParam,
    contactPhone: contactPhoneParam,
    status: statusParam,
    startDate: startDateParam,
    endDate: endDateParam,
  } = await searchParams;

  const businessName = parseSearchString(businessNameParam);
  const contactName = parseSearchString(contactNameParam);
  const contactPhone = parseSearchString(contactPhoneParam);
  const status = parsePartnershipStatus(statusParam);
  const startDate = parseSearchString(startDateParam);
  const endDate = parseSearchString(endDateParam);
  const page = parseNonNegativeInt(pageParam, 0);
  const size = Math.min(parseNonNegativeInt(sizeParam, 10), MAX_PAGE_SIZE);

  const { error, data, pagination } = await partnershipRequestService.getList(
    { businessName, contactName, contactPhone, status, startDate, endDate },
    { page, size },
  );

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "제휴 신청 목록 조회 실패");
    throw new Error(PARTNERSHIP_MESSAGE.LIST_LOAD_FAILED);
  }

  return (
    <PartnershipRequests
      partnershipRequests={data}
      pagination={pagination}
      initialBusinessName={businessName}
      initialContactName={contactName}
      initialContactPhone={contactPhone}
      initialStatus={status}
      initialStartDate={startDate}
      initialEndDate={endDate}
    />
  );
}
