export type ReviewType = 'ALL' | 'FOLLOWING'

export type ReviewSortType = 'recommended' | 'latest' | 'oldest'

/** 사장님이 선택한 게시중단 사유 코드 (`docs/tasks/backend.md` 4-1). */
export type ReviewBlindReason = 'ADVERTISEMENT' | 'PROFANITY' | 'IRRELEVANT' | 'PRIVACY' | 'ETC'
