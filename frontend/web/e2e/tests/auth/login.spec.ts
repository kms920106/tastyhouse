import { test, expect } from '../../fixtures/test'

/**
 * 로그인 E2E 테스트
 *
 * 이 테스트들은 unauthenticated 프로젝트에서 실행됩니다.
 * (storageState 없이 실행 — playwright.config.ts 참고)
 */

test.describe('로그인 페이지', () => {
  test.beforeEach(async ({ loginPage }) => {
    await loginPage.goto()
  })

  test('로그인 페이지가 정상적으로 렌더링된다', async ({ loginPage }) => {
    await expect(loginPage.usernameInput).toBeVisible()
    await expect(loginPage.passwordInput).toBeVisible()
    await expect(loginPage.submitButton).toBeVisible()
  })

  test('올바른 자격증명으로 로그인하면 홈으로 이동한다', async ({ loginPage }) => {
    await loginPage.login(
      process.env.E2E_USERNAME ?? 'testuser',
      process.env.E2E_PASSWORD ?? 'testpass1234',
    )
    await loginPage.expectLoginSuccess()
  })

  test('잘못된 비밀번호로 로그인하면 오류 메시지가 표시된다', async ({ loginPage }) => {
    await loginPage.login('testuser', 'wrongpassword')
    await loginPage.expectLoginError()
  })

  test('아이디 미입력 시 유효성 검사 오류가 표시된다', async ({ loginPage }) => {
    await loginPage.passwordInput.fill('password123')
    await loginPage.submitButton.click()
    /* HTML5 required 속성 또는 서버 유효성 검사 오류 확인 */
    const isInvalid = await loginPage.usernameInput.evaluate((el: HTMLInputElement) => !el.validity.valid)
    expect(isInvalid).toBe(true)
  })

  test('비밀번호 6자 미만 입력 시 유효성 검사 오류가 표시된다', async ({ loginPage }) => {
    await loginPage.login('testuser', 'abc')
    await loginPage.expectLoginError('비밀번호는 최소 6글자')
  })
})
