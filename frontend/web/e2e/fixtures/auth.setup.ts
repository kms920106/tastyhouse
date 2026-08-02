import { test as setup, expect } from '@playwright/test'

/**
 * 인증 셋업 픽스처
 *
 * 테스트 실행 전 1회만 수행되며, 인증 상태(쿠키)를 e2e/.auth/user.json에 저장합니다.
 * 이후 테스트들은 이 파일을 storageState로 재사용하여 매번 로그인하지 않아도 됩니다.
 *
 * @see https://playwright.dev/docs/auth#basic-shared-account-in-all-workers
 */

const AUTH_FILE = 'e2e/.auth/user.json'

setup('인증 세션 생성', async ({ page }) => {
  await page.goto('/login')

  await page.getByLabel('아이디').fill(process.env.E2E_USERNAME ?? 'testuser')
  await page.getByLabel('비밀번호').fill(process.env.E2E_PASSWORD ?? 'testpass1234')
  await page.getByRole('button', { name: '로그인' }).click()

  /* 로그인 성공 후 홈 또는 리다이렉트 확인 */
  await expect(page).toHaveURL('/')

  /* 인증 상태 저장 */
  await page.context().storageState({ path: AUTH_FILE })
})
