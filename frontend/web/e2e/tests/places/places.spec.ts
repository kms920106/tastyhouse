import { test, expect } from '../../fixtures/test'

/**
 * 매장 목록 E2E 테스트
 *
 * 인증된 세션(storageState)을 사용합니다.
 */

test.describe('매장 목록 페이지', () => {
  test.beforeEach(async ({ placesPage }) => {
    await placesPage.goto()
  })

  test('매장 목록이 표시된다', async ({ placesPage }) => {
    await placesPage.expectPlaceCardsLoaded()
  })

  test('매장 카드 클릭 시 상세 페이지로 이동한다', async ({ placesPage }) => {
    await placesPage.clickPlaceCard(0)
    await placesPage.expectNavigatedToPlaceDetail()
  })
})

test.describe('매장 상세 페이지', () => {
  const TEST_PLACE_ID = Number(process.env.E2E_TEST_PLACE_ID ?? '1')

  test.beforeEach(async ({ placeDetailPage }) => {
    await placeDetailPage.goto(TEST_PLACE_ID)
  })

  test('매장 상세 페이지가 정상적으로 로드된다', async ({ placeDetailPage }) => {
    await placeDetailPage.expectPageLoaded()
  })

  test('메뉴 탭 클릭 시 URL이 변경된다', async ({ placeDetailPage }) => {
    await placeDetailPage.clickMenuTab()
    await placeDetailPage.expectMenuTabActive()
  })

  test('사진 탭이 클릭 가능하다', async ({ placeDetailPage }) => {
    await expect(placeDetailPage.photoTab).toBeVisible()
    await placeDetailPage.clickPhotoTab()
  })
})
