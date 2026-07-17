import { memberService } from "@/api/members/member.service";
import type { MemberGrade, MemberStatus } from "@/feature/member/domain";
import { MEMBER_MESSAGE } from "@/feature/member/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseSearchString } from "@/lib/utils";

import { Members } from "./_components/members";

const MEMBER_STATUSES: readonly MemberStatus[] = ["ACTIVE", "SUSPENDED", "DELETED"];
const MEMBER_GRADES: readonly MemberGrade[] = ["NEWCOMER", "ACTIVE", "INSIDER", "GOURMET", "TEHA"];

function parseMemberStatus(value: string | string[] | undefined): MemberStatus | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  return MEMBER_STATUSES.includes(raw as MemberStatus) ? (raw as MemberStatus) : undefined;
}

function parseMemberGrade(value: string | string[] | undefined): MemberGrade | undefined {
  const raw = Array.isArray(value) ? value[0] : value;
  return MEMBER_GRADES.includes(raw as MemberGrade) ? (raw as MemberGrade) : undefined;
}

export default async function Page({ searchParams }: PageProps<"/dashboard/members">) {
  const {
    page: pageParam,
    size: sizeParam,
    nickname: nicknameParam,
    username: usernameParam,
    phone: phoneParam,
    status: statusParam,
    grade: gradeParam,
  } = await searchParams;

  const nickname = parseSearchString(nicknameParam);
  const username = parseSearchString(usernameParam);
  const phone = parseSearchString(phoneParam);
  const status = parseMemberStatus(statusParam);
  const grade = parseMemberGrade(gradeParam);
  const page = parseNonNegativeInt(pageParam, 0);
  const size = parseNonNegativeInt(sizeParam, 10);

  const { error, data, pagination } = await memberService.getMembers(
    { nickname, username, phone, status, grade },
    { page, size },
  );

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "회원 목록 조회 실패");
    throw new Error(MEMBER_MESSAGE.LIST_LOAD_FAILED);
  }

  return (
    <Members
      members={data}
      pagination={pagination}
      initialNickname={nickname}
      initialUsername={username}
      initialPhone={phone}
      initialStatus={status}
      initialGrade={grade}
    />
  );
}
