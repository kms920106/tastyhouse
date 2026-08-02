import { test, expect } from '../../fixtures/test'

/**
 * 주문 내역 E2E 테스트
 *
 * 인증된 세션(storageState)을 사용합니다.
 */

test.describe('주문 내역 페이지', () => {
  test.beforeEach(async ({ ordersPage }) => {
    await ordersPage.goto()
  })

  test('주문 내역 페이지로 이동된다', async ({ ordersPage }) => {
    await ordersPage.expectPageLoaded()
  })

  test('주문 내역 또는 빈 상태가 표시된다', async ({ page }) => {
    /* 주문이 있거나 없거나 둘 중 하나는 표시되어야 함 */
    const hasOrders = await page.getByRole('listitem').count()
    const hasEmpty = await page.getByText(/주문 내역이 없|no orders/i).isVisible()

    expect(hasOrders > 0 || hasEmpty).toBe(true)
  })
})
