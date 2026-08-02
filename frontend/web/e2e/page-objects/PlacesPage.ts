import { type Page, type Locator, expect } from '@playwright/test'

/**
 * PlacesPage Page Object Model
 *
 * 매장 목록 페이지의 UI 요소와 액션을 캡슐화합니다.
 */
export class PlacesPage {
  readonly page: Page

  readonly placeCards: Locator
  readonly filterButton: Locator
  readonly searchInput: Locator

  constructor(page: Page) {
    this.page = page
    this.placeCards = page.getByRole('article')
    this.filterButton = page.getByRole('button', { name: /필터|filter/i })
    this.searchInput = page.getByRole('searchbox')
  }

  async goto() {
    await this.page.goto('/places')
  }

  async expectPlaceCardsLoaded(minCount = 1) {
    await expect(this.placeCards.first()).toBeVisible()
    const count = await this.placeCards.count()
    expect(count).toBeGreaterThanOrEqual(minCount)
  }

  async clickPlaceCard(index = 0) {
    await this.placeCards.nth(index).click()
  }

  async expectNavigatedToPlaceDetail() {
    await expect(this.page).toHaveURL(/\/places\/\d+/)
  }
}
