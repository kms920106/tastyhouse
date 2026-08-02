import { FlatCompat } from '@eslint/eslintrc'
import unusedImports from 'eslint-plugin-unused-imports'
import { dirname } from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const compat = new FlatCompat({
  baseDirectory: __dirname,
})

const eslintConfig = [
  ...compat.extends('next/core-web-vitals', 'next/typescript', 'prettier'),
  {
    plugins: {
      'unused-imports': unusedImports,
    },
    rules: {
      // 사용하지 않는 import 자동 제거
      'unused-imports/no-unused-imports': 'error',
      // 사용하지 않는 변수 경고
      'unused-imports/no-unused-vars': [
        'warn',
        {
          vars: 'all',
          varsIgnorePattern: '^_',
          args: 'after-used',
          argsIgnorePattern: '^_',
        },
      ],
      // 세미콜론 관련 규칙 (Prettier와 통합)
      // eslint-config-prettier가 Prettier와 충돌하는 규칙을 비활성화함
      // Prettier의 semi: false 설정과 일치하도록 유지
      'no-extra-semi': 'error',
      // 쉼표 관련 규칙
      'comma-dangle': ['error', 'always-multiline'],
      // 공백 관련 규칙
      'no-trailing-spaces': 'error',
      'eol-last': 'error',
      // 자동 수정 가능한 규칙들
      'prefer-const': 'error',
      'no-var': 'error',
      'object-shorthand': 'error',
      'prefer-template': 'error',
    },
  },
]

export default eslintConfig
