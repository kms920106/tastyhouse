page.tsx, Page 파일 분리

error, data return 일관성 통일
if (error) {
return ErrorStateSection
}

if (!data) {
return FetchErrorState
}

COMMON_ERROR_MESSAGES 문구 정리
