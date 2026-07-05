import { noticeRepository } from "@/api/notice/notice.repository";
import { NOTICE_MESSAGE } from "@/feature/notice/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseOptionalBoolean, parseSearchString } from "@/lib/utils";

import { Notices } from "./_components/notices";

export default async function Page({ searchParams }: PageProps<"/dashboard/notices">) {
  const {
    page: pageParam,
    size: sizeParam,
    title: titleParam,
    content: contentParam,
    visible: visibleParam,
  } = await searchParams;

  const title = parseSearchString(titleParam);
  const content = parseSearchString(contentParam);
  const visible = parseOptionalBoolean(visibleParam);
  const page = parseNonNegativeInt(pageParam, 0);
  const size = parseNonNegativeInt(sizeParam, 10);

  const { error, data, pagination } = await noticeRepository.getList({ title, content, visible }, { page, size });

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "공지사항 목록 조회 실패");
    throw new Error(NOTICE_MESSAGE.LIST_LOAD_FAILED);
  }

  return (
    <Notices
      notices={data}
      pagination={pagination}
      initialTitle={title}
      initialContent={content}
      initialVisible={visible}
    />
  );
}
