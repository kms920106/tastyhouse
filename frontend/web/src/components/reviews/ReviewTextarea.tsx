import AppTextarea from '@/components/ui/AppTextarea'

interface Props {
  value: string
  onChange: (val: string) => void
  error?: boolean
  className?: string
}

export default function ReviewTextarea({ value, onChange, error, className }: Props) {
  return (
    <div className="relative">
      <AppTextarea
        id="content"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        maxLength={500}
        rows={10}
        error={error}
        className={className}
        placeholder="내용을 입력해 주세요."
      />
      <span className="absolute bottom-[15px] right-[15px] text-sm leading-[14px] text-[#cccccc]">
        {value.length} / 500
      </span>
    </div>
  )
}
