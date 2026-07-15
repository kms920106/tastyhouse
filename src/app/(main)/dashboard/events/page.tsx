import { eventService } from "@/api/events/event.service";
import type { EventStatus } from "@/feature/event/domain";
import { EVENT_MESSAGE } from "@/feature/event/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { Events } from "./_components/events";

function parseEventStatus(value: string | string[] | undefined): EventStatus | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  return raw === "SCHEDULED" || raw === "ACTIVE" || raw === "ENDED" ? raw : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/events">) {
  const { page: pageParam, size: sizeParam, name: nameParam, status: statusParam } = await searchParams;

  const name = parseSearchString(nameParam);
  const status = parseEventStatus(statusParam);
  const page = parseNonNegativeInt(pageParam, 0);
  const size = parseNonNegativeInt(sizeParam, 10);

  const { error, data, pagination } = await eventService.getEvents({ name, status }, { page, size });

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "이벤트 목록 조회 실패");
    throw new Error(EVENT_MESSAGE.LIST_LOAD_FAILED);
  }

  return <Events events={data} pagination={pagination} initialName={name} initialStatus={status} />;
}
