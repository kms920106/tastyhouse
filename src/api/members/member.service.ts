import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type { MemberDetail, MemberListItem } from "../../feature/member/domain";
import type { MemberListQueryRequest } from "./member.dto";
import { memberRepository } from "./member.repository";

export const memberService = {
  // 회원 목록 조회
  // 도메인 반환
  async getMembers(query: MemberListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<MemberListItem[]>> {
    const res = await memberRepository.getList(query, pageRequest);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        username: item.username,
        nickname: item.nickname,
        fullName: item.fullName,
        phoneNumber: item.phoneNumber,
        gender: item.gender,
        memberGrade: item.memberGrade,
        memberStatus: item.memberStatus,
        profileImageFilePath: item.profileImageFilePath,
        createdAt: item.createdAt,
      })),
    };
  },

  // 회원 상세 조회
  // 도메인 반환
  async getMember(id: number): Promise<ApiResponse<MemberDetail>> {
    const res = await memberRepository.getDetail(id);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        id: res.data.id,
        username: res.data.username,
        nickname: res.data.nickname,
        fullName: res.data.fullName,
        phoneNumber: res.data.phoneNumber,
        gender: res.data.gender,
        birthDate: res.data.birthDate,
        memberGrade: res.data.memberGrade,
        memberStatus: res.data.memberStatus,
        statusMessage: res.data.statusMessage,
        profileImageUrl: res.data.profileImageUrl,
        pushNotificationEnabled: res.data.pushNotificationEnabled,
        marketingInfoEnabled: res.data.marketingInfoEnabled,
        eventInfoEnabled: res.data.eventInfoEnabled,
        createdAt: res.data.createdAt,
      },
    };
  },
};
