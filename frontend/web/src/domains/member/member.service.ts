import 'server-only'

import type { ApiResponse } from '@/lib/api'
import { getMemberGradeColor, getMemberGradeIcon, getMemberGradeName } from './member.constants'
import type { Member } from './member.model'
import { memberRepository } from './member.repository'

export const memberService = {
  async getMyProfile(): Promise<ApiResponse<Member>> {
    const response = await memberRepository.getMyProfile()
    if (!response.data) return { ...response, data: undefined }

    const grade = response.data.grade
    return {
      ...response,
      data: {
        id: response.data.id,
        nickname: response.data.nickname,
        grade,
        gradeName: getMemberGradeName(grade),
        gradeIcon: getMemberGradeIcon(grade),
        gradeColor: getMemberGradeColor(grade),
        statusMessage: response.data.statusMessage,
        profileImageUrl: response.data.profileImageUrl,
      },
    }
  },
}
