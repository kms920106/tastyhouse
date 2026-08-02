export const COMMON_ERROR_MESSAGES = {
  API_FETCH_ERROR: '일시적인 오류가 발생했습니다.\n잠시 후 다시 시도해 주세요.',
  FETCH_ERROR: (subject: string) =>
    `${subject} 정보를 불러오지 발생했습니다.\n잠시 후 다시 시도해 주세요.`,
  RENDER_ERROR: '오류가 발생했습니다.\n(RENDER)',
  MUTATION_ERROR: '오류가 발생했습니다. 다시 시도해 주세요.\n(MUTATION)',
  UNKNOWN_ERROR: '예기치 않은 오류가 발생했습니다.',
}
