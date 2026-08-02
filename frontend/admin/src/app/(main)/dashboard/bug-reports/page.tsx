import { bugReportService } from "@/api/bug-report/bug-report.service";
import { BUG_CATEGORY, BUG_PRIORITY, BUG_STATUS } from "@/feature/bug-report/constants";
import { BUG_REPORT_MESSAGE } from "@/feature/bug-report/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { BugReports } from "./_components/bug-reports";

/** 값이 허용된 enum 중 하나일 때만 반환, 아니면 undefined. (잘못된 URL 방어) */
function parseEnumParam<T extends string>(value: string | string[] | undefined, allowed: readonly T[]): T | undefined {
  const raw = parseSearchString(value);
  return raw && (allowed as readonly string[]).includes(raw) ? (raw as T) : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/bug-reports">) {
  const {
    page: pageParam,
    size: sizeParam,
    title: titleParam,
    content: contentParam,
    memberId: memberIdParam,
    status: statusParam,
    category: categoryParam,
    priority: priorityParam,
  } = await searchParams;

  const title = parseSearchString(titleParam);
  const content = parseSearchString(contentParam);
  const memberIdRaw = parseSearchString(memberIdParam);
  const memberId = memberIdRaw !== undefined && Number.isInteger(Number(memberIdRaw)) ? Number(memberIdRaw) : undefined;
  const status = parseEnumParam(statusParam, BUG_STATUS);
  const category = parseEnumParam(categoryParam, BUG_CATEGORY);
  const priority = parseEnumParam(priorityParam, BUG_PRIORITY);
  const page = parseNonNegativeInt(pageParam, 0);
  const size = parseNonNegativeInt(sizeParam, 10);

  const { error, data, pagination } = await bugReportService.getBugReports(
    { title, content, memberId, status, category, priority },
    { page, size },
  );

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "버그 제보 목록 조회 실패");
    throw new Error(BUG_REPORT_MESSAGE.LIST_LOAD_FAILED);
  }

  return (
    <BugReports
      bugReports={data}
      pagination={pagination}
      initialTitle={title}
      initialContent={content}
      initialMemberId={memberId}
      initialStatus={status}
      initialCategory={category}
      initialPriority={priority}
    />
  );
}
