'use client'

import CircleCheckbox from '@/components/ui/CircleCheckbox'
import FormCheckbox from '@/components/ui/FormCheckbox'
import Icon from '@/components/ui/Icon'

export const ORDER_TERMS_LIST = [
  { key: 'agreedTerms', label: '이용약관 동의 (필수)', required: true, href: '/terms' },
  {
    key: 'agreedPrivacy',
    label: '개인정보처리방침 동의 (필수)',
    required: true,
    href: '/privacy',
  },
  {
    key: 'agreedFinance',
    label: '전자금융거래 이용약관 동의 (필수)',
    required: true,
    href: '/terms/finance',
  },
  {
    key: 'agreedAge',
    label: '만 14세 이상 이용 동의 (필수)',
    required: true,
    href: '/terms/age-verification',
  },
  { key: 'agreedEvent', label: '이벤트 정보 수신 동의 (선택)', required: false, href: null },
] as const

export type OrderTermsKey = (typeof ORDER_TERMS_LIST)[number]['key']

interface Props {
  agreedAll: boolean
  agreedTerms: Record<OrderTermsKey, boolean>
  onAgreedAllChange: (checked: boolean) => void
  onTermChange: (key: OrderTermsKey, checked: boolean) => void
  onOpenTermsDialog: (key: OrderTermsKey, label: string) => void
}

export default function OrderTermsSection({
  agreedAll,
  agreedTerms,
  onAgreedAllChange,
  onTermChange,
  onOpenTermsDialog,
}: Props) {
  return (
    <div className="px-[15px] py-6">
      <label className="flex items-center gap-2.5 pb-2.5 border-b border-line cursor-pointer">
        <CircleCheckbox checked={agreedAll} onChange={onAgreedAllChange} />
        <span className="text-sm leading-[14px]">약관에 모두 동의합니다.</span>
      </label>
      <div className="flex flex-col gap-[13px] px-[5px] py-5">
        {ORDER_TERMS_LIST.map(({ key, label, href }) => (
          <div key={key} className="flex items-center justify-between">
            <label className="flex items-center gap-[15px] cursor-pointer">
              <FormCheckbox
                name={key}
                checked={agreedTerms[key]}
                onChange={(checked) => onTermChange(key, checked)}
              />
              <span className="text-[13px] leading-[13px] font-light">{label}</span>
            </label>
            {href && (
              <button
                type="button"
                onClick={() => onOpenTermsDialog(key, label)}
                className="shrink-0 cursor-pointer"
              >
                <Icon name="nav-right" alt="약관 보기" />
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
