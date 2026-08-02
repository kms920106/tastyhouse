import { test, expect } from '../fixtures/test'
import AxeBuilder from '@axe-core/playwright'

/**
 * 접근성(a11y) E2E 테스트
 *
 * axe-core를 통해 WCAG 기준 위반 사항을 자동 감지합니다.
 *
 * 설치: npm install -D @axe-core/playwright
 * @see https://playwright.dev/docs/accessibility-testing
 */

test.describe('접근성 검사', () => {
  test('로그인 페이지 접근성 위반이 없다', async ({ page }) => {
    await page.goto('/login')
    const results = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'])
      .analyze()

    expect(results.violations).toEqual([])
  })
})
