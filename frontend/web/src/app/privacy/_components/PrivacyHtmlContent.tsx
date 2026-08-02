import FetchErrorState from '@/components/ui/FetchErrorState'
import HtmlContent from '@/components/ui/HtmlContent'
import { policiesRepository } from '@/domains/policies/policies.repository'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'

export default async function PrivacyHtmlContent() {
  const { error, status, data } = await policiesRepository.getLatestPrivacyPolicy()

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('개인정보처리방침')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const { content } = data

  return <HtmlContent content={content} />
}
