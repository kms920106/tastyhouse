import dayjs from 'dayjs'
import duration from 'dayjs/plugin/duration'
import 'dayjs/locale/ko'

/**
 * 프로젝트 전역에서 사용하는 dayjs 인스턴스.
 *
 * - duration 플러그인: 두 시점 간 차이를 일/시/분/초 단위로 분해 (getTimeDifference 등)
 * - 한국어 로케일: 요일·월 등 한국어 출력
 *
 * 날짜를 다루는 모든 코드는 `new Date(...)`를 직접 쓰지 않고 이 모듈을 통해 dayjs를 사용한다.
 */
dayjs.extend(duration)
dayjs.locale('ko')

export default dayjs
