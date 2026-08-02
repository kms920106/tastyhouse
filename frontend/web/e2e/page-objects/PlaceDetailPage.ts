import { type Page, type Locator, expect } from '@playwright/test'

/**
 * PlaceDetailPage Page Object Model
 *
 * 매장 상세 페이지의 UI 요소와 액션을 캡슐화합니다.
 */
export class PlaceDetailPage {
  readonly page: Page

  readonly infoTab: Locator
  readonly menuTab: Locator
  readonly photoTab: Locator
  readonly orderButton: Locator

  constructor(page: Page) {
    this.page = page
    this.infoTab = page.getByRole('tab', { name: /정보|info/i })
    this.menuTab = page.getByRole('tab', { name: /메뉴|menu/i })
    this.photoTab = page.getByRole('tab', { name: /사진|photo/i })
    this.orderButton = page.getByRole('button', { name: /주문|order/i })
  }

  async goto(placeId: number) {
    await this.page.goto(`/places/${placeId}`)
  }

  async clickMenuTab() {
    await this.menuTab.click()
  }

  async clickPhotoTab() {
    await this.photoTab.click()
  }

  async expectPageLoaded() {
    await expect(this.page).toHaveURL(/\/places\/\d+/)
    await expect(this.infoTab).toBeVisible()
  }

  async expectMenuTabActive() {
    await expect(this.page).toHaveURL(/tab=menu/)
  }
}
