import { test, expect } from '../../fixtures/test'

/**
 * 오늘의 할인 페이지 - URL 기반 뷰/정렬 상태 유지 E2E
 *
 * 인증된 세션(storageState)을 사용합니다.
 */

test.describe('오늘의 할인 - 뷰/정렬 URL 상태 유지', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/products/today-discount')
  })

  test('그리드 뷰 전환 후 상세 이동 → 뒤로가기 시 그리드 유지', async ({ page }) => {
    await page.getByRole('button', { name: '그리드 뷰로 전환' }).click()
    await expect(page).toHaveURL(/view=grid/)

    // PRODUCT_DETAIL 경로: /products/{id} (숫자 ID)
    // 상품 카드는 가격(원)을 포함하므로 hasText로 today-discount 링크와 구분
    const firstProduct = page
      .locator('a[href^="/products/"]')
      .filter({ hasText: /원$/ })
      .first()
    await firstProduct.click()
    await page.waitForURL(/\/products\/\d+(\?|$)/)

    await page.goBack()

    await expect(page).toHaveURL(/view=grid/)
    await expect(page.getByRole('button', { name: '리스트 뷰로 전환' })).toBeVisible()
  })

  test('정렬 변경 후 뒤로가기 시 정렬 유지', async ({ page }) => {
    await page.getByRole('button', { name: '정렬 선택' }).click()
    await page.getByRole('button', { name: '할인율 높은순' }).click()
    await expect(page).toHaveURL(/sort=DISCOUNT_RATE/)

    const firstProduct = page
      .locator('a[href^="/products/"]')
      .filter({ hasText: /원$/ })
      .first()
    await firstProduct.click()
    await page.waitForURL(/\/products\/\d+(\?|$)/)
    await page.goBack()

    await expect(page).toHaveURL(/sort=DISCOUNT_RATE/)
  })

  test('기본값 복원 시 URL param 자동 제거 (clearOnDefault)', async ({ page }) => {
    await page.goto('/products/today-discount?view=grid')
    await page.getByRole('button', { name: '리스트 뷰로 전환' }).click()
    await expect(page).not.toHaveURL(/view=/)
  })

  test('잘못된 searchParams 값은 기본값으로 fallback', async ({ page }) => {
    // nuqs는 잘못된 값을 URL에서 자동 제거하지 않음 → URL은 ?view=invalid 유지되지만
    // 컴포넌트는 parseAsStringLiteral의 fallback으로 'list' 모드 렌더링
    await page.goto('/products/today-discount?view=invalid')
    await expect(page.getByRole('button', { name: '그리드 뷰로 전환' })).toBeVisible()
  })
})
