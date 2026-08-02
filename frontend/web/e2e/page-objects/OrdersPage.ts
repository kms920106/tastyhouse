import { type Page, type Locator, expect } from '@playwright/test'

/**
 * OrdersPage Page Object Model
 *
 * 주문 내역 페이지의 UI 요소와 액션을 캡슐화합니다.
 */
export class OrdersPage {
  readonly page: Page

  readonly orderProducts: Locator
  readonly emptyState: Locator
  readonly loadingSpinner: Locator

  constructor(page: Page) {
    this.page = page
    this.orderProducts = page.getByRole('listitem')
    this.emptyState = page.getByText(/주문 내역이 없|no orders/i)
    this.loadingSpinner = page.getByRole('status')
  }

  async goto() {
    await this.page.goto('/orders')
  }

  async expectPageLoaded() {
    await expect(this.page).toHaveURL('/orders')
  }

  async expectOrdersVisible() {
    await expect(this.orderProducts.first()).toBeVisible()
  }

  async expectEmptyState() {
    await expect(this.emptyState).toBeVisible()
  }
}
