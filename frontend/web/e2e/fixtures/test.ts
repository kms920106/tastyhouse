import { test as base } from '@playwright/test'
import { LoginPage } from '../page-objects/LoginPage'
import { PlacesPage } from '../page-objects/PlacesPage'
import { PlaceDetailPage } from '../page-objects/PlaceDetailPage'
import { OrdersPage } from '../page-objects/OrdersPage'

/**
 * 커스텀 fixtures
 *
 * Page Object Model을 테스트에 주입하여 코드 재사용성을 높입니다.
 * @see https://playwright.dev/docs/test-fixtures
 */
type CustomFixtures = {
  loginPage: LoginPage
  placesPage: PlacesPage
  placeDetailPage: PlaceDetailPage
  ordersPage: OrdersPage
}

export const test = base.extend<CustomFixtures>({
  loginPage: async ({ page }, use) => {
    await use(new LoginPage(page))
  },
  placesPage: async ({ page }, use) => {
    await use(new PlacesPage(page))
  },
  placeDetailPage: async ({ page }, use) => {
    await use(new PlaceDetailPage(page))
  },
  ordersPage: async ({ page }, use) => {
    await use(new OrdersPage(page))
  },
})

export { expect } from '@playwright/test'
