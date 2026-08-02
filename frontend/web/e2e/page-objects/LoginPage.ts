import { type Page, type Locator, expect } from '@playwright/test'

/**
 * LoginPage Page Object Model
 *
 * 로그인 페이지의 UI 요소와 액션을 캡슐화합니다.
 * 테스트 코드가 셀렉터에 직접 의존하지 않도록 분리합니다.
 */
export class LoginPage {
  readonly page: Page

  readonly usernameInput: Locator
  readonly passwordInput: Locator
  readonly submitButton: Locator
  readonly errorAlert: Locator

  constructor(page: Page) {
    this.page = page
    this.usernameInput = page.getByLabel('아이디')
    this.passwordInput = page.getByLabel('비밀번호')
    this.submitButton = page.getByRole('button', { name: '로그인' })
    this.errorAlert = page.getByRole('alert')
  }

  async goto() {
    await this.page.goto('/login')
  }

  async login(username: string, password: string) {
    await this.usernameInput.fill(username)
    await this.passwordInput.fill(password)
    await this.submitButton.click()
  }

  async expectLoginSuccess() {
    await expect(this.page).toHaveURL('/')
  }

  async expectLoginError(message?: string) {
    await expect(this.errorAlert).toBeVisible()
    if (message) {
      await expect(this.errorAlert).toContainText(message)
    }
  }

  async expectValidationError(message: string) {
    await expect(this.errorAlert).toContainText(message)
  }
}
