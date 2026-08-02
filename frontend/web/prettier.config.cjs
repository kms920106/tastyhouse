/** @type {import('prettier').Config} */
module.exports = {
  semi: false, // 문장 끝에 세미콜론 제거 (ESLint의 semi: never와 일치)
  singleQuote: true, // 문자열에 ' ' 사용
  trailingComma: 'all', // 가능하면 항상 마지막에 쉼표
  printWidth: 100, // 한 줄 최대 길이
  tabWidth: 2, // 들여쓰기 2칸
  useTabs: false, // 탭 대신 스페이스
  bracketSpacing: true, // { foo: bar } 중괄호 안에 공백
  arrowParens: 'always', // 화살표 함수 파라미터 항상 괄호
  endOfLine: 'lf', // 줄바꿈 방식 (LF)
  jsxSingleQuote: false, // JSX에서는 " " 사용
  proseWrap: 'preserve', // 마크다운 자동 줄바꿈 안함
  htmlWhitespaceSensitivity: 'css', // HTML 공백 CSS 규칙에 맞춤
  quoteProps: 'as-needed', // 객체 키에 필요할 때만 따옴표
  bracketSameLine: false, // JSX 닫는 꺾쇠는 새 줄
  overrides: [
    {
      files: ['*.ts', '*.tsx'],
      options: {
        parser: 'typescript',
        semi: false, // TypeScript 파일에서도 세미콜론 사용 안 함
      },
    },
  ],
}
