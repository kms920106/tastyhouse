'use server'

import { policiesRepository } from '@/domains/policies/policies.repository'

export async function fetchTermsOfServiceContent(): Promise<string> {
  const { data } = await policiesRepository.getLatestTermsOfService()
  return data?.content ?? ''
}

export async function fetchPrivacyPolicyContent(): Promise<string> {
  const { data } = await policiesRepository.getLatestPrivacyPolicy()
  return data?.content ?? ''
}

export async function fetchElectronicFinancialTransactionsContent(): Promise<string> {
  const { data } = await policiesRepository.getLatestElectronicFinancialTransactions()
  return data?.content ?? ''
}

export async function fetchAgeVerificationContent(): Promise<string> {
  const { data } = await policiesRepository.getLatestAgeVerification()
  return data?.content ?? ''
}
