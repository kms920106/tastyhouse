import { RANK_TYPE_VALUES } from "@/api/rank/rank.dto";
import { rankService } from "@/api/rank/rank.service";
import { RANK_MEMBER_DEFAULT_LIMIT } from "@/feature/rank/constants";
import { RANK_MESSAGE } from "@/feature/rank/message";
import logger from "@/lib/logger";

import { Ranks } from "./_components/ranks";

function parseRankType(value: string | string[] | undefined) {
  const raw = Array.isArray(value) ? value[0] : value;
  return (RANK_TYPE_VALUES as readonly string[]).includes(raw ?? "")
    ? (raw as (typeof RANK_TYPE_VALUES)[number])
    : "ALL";
}

function parsePositiveInt(value: string | string[] | undefined, fallback: number): number {
  const raw = Array.isArray(value) ? value[0] : value;
  const parsed = Number(raw);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/ranks">) {
  const { type: typeParam, limit: limitParam } = await searchParams;

  const type = parseRankType(typeParam);
  const limit = parsePositiveInt(limitParam, RANK_MEMBER_DEFAULT_LIMIT);

  const [membersRes, periodsRes] = await Promise.all([
    rankService.getMembers({ type, limit }),
    rankService.getPeriods(),
  ]);

  if (membersRes.error || !membersRes.data) {
    logger.error({ reason: membersRes.error }, "회원 랭킹 조회 실패");
    throw new Error(RANK_MESSAGE.MEMBERS_LOAD_FAILED);
  }

  if (periodsRes.error || !periodsRes.data) {
    logger.error({ reason: periodsRes.error }, "랭킹 기간 목록 조회 실패");
    throw new Error(RANK_MESSAGE.PERIODS_LOAD_FAILED);
  }

  return <Ranks members={membersRes.data} periods={periodsRes.data} initialType={type} initialLimit={limit} />;
}
