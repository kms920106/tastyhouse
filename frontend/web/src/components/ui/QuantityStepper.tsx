import { cn } from '@/lib/utils'

interface Props {
  value: number
  onChange: (next: number) => void
  min?: number
  max?: number
  decrementLabel?: string
  incrementLabel?: string
  className?: string
}

export default function QuantityStepper({
  value,
  onChange,
  min = 1,
  max = Infinity,
  decrementLabel = '수량 감소',
  incrementLabel = '수량 증가',
  className,
}: Props) {
  return (
    <div className={cn('flex items-center border border-[#cccccc]', className)}>
      <button
        type="button"
        onClick={() => onChange(Math.max(min, value - 1))}
        className="w-[30px] h-[30px] flex items-center justify-center text-sm leading-[14px] text-[#999999] cursor-pointer"
        aria-label={decrementLabel}
      >
        −
      </button>
      <span className="w-[30px] h-[30px] flex items-center justify-center text-xs leading-[12px] border-x border-[#cccccc] box-border">
        {value}
      </span>
      <button
        type="button"
        onClick={() => onChange(Math.min(max, value + 1))}
        className="w-[30px] h-[30px] flex items-center justify-center text-sm leading-[14px] text-[#999999] cursor-pointer"
        aria-label={incrementLabel}
      >
        +
      </button>
    </div>
  )
}
