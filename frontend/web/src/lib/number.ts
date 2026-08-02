/**
 * 숫자의 소수점을 지정된 자리수로 내림(버림)합니다.
 *
 * @param value - 변환할 숫자
 * @param decimalPlaces - 유지할 소수점 자리수 (기본값: 0)
 * @returns 내림된 숫자
 *
 * @example
 * ```ts
 * formatDecimal(12.345, 2) // 12.34
 * formatDecimal(12.345, 1) // 12.3
 * formatDecimal(12.345)    // 12
 * formatDecimal(12.999, 0) // 12
 * ```
 */
export function formatDecimal(value: number, decimalPlaces: number = 0): number {
  const multiplier = Math.pow(10, decimalPlaces)
  return Math.floor(value * multiplier) / multiplier
}

/**
 * 숫자의 소수점을 지정된 자리수로 내림(버림)하여 문자열로 반환합니다.
 * 지정된 자리수만큼 0으로 패딩됩니다.
 *
 * @param value - 변환할 숫자
 * @param decimalPlaces - 유지할 소수점 자리수 (기본값: 0)
 * @returns 내림되고 포맷팅된 문자열
 *
 * @example
 * ```ts
 * formatDecimalString(12.3, 2)    // "12.30"
 * formatDecimalString(12.345, 2)  // "12.34"
 * formatDecimalString(12, 2)      // "12.00"
 * formatDecimalString(12.999, 0)  // "12"
 * ```
 */
export function formatDecimalString(value: number, decimalPlaces: number = 0): string {
  const truncated = formatDecimal(value, decimalPlaces)
  return truncated.toFixed(decimalPlaces)
}

/**
 * 숫자에 천 단위 구분 기호(콤마)를 추가하여 문자열로 반환합니다.
 *
 * @param value - 포맷팅할 숫자
 * @returns 콤마가 추가된 문자열
 *
 * @example
 * ```ts
 * formatNumber(1000)      // "1,000"
 * formatNumber(1234567)   // "1,234,567"
 * formatNumber(100)       // "100"
 * formatNumber(0)         // "0"
 * ```
 */
export function formatNumber(value: number): string {
  return value.toLocaleString('ko-KR')
}
