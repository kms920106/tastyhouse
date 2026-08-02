import type { ProductOption } from '@/domains/product'
import { formatNumber } from '@/lib/number'
import { cn } from '@/lib/utils'
import { IoIosCheckbox, IoIosCheckboxOutline } from 'react-icons/io'
import { RiRadioButtonFill } from 'react-icons/ri'

interface Props {
  option: ProductOption
  isMultiple: boolean
  isSelected: boolean
  onSelect: () => void
}

export default function ShopOrderMenuDetailOptionItem({
  option,
  isMultiple,
  isSelected,
  onSelect,
}: Props) {
  const isDisabled = option.soldOut

  return (
    <button
      type="button"
      onClick={onSelect}
      disabled={isDisabled}
      className={cn(
        'flex items-center gap-2.5 w-full text-left cursor-pointer',
        isDisabled && 'opacity-50 cursor-not-allowed',
      )}
    >
      {isMultiple ? (
        isSelected ? (
          <IoIosCheckbox size={28} className="text-main flex-shrink-0" />
        ) : (
          <IoIosCheckboxOutline size={28} className="text-[#dddddd] flex-shrink-0" />
        )
      ) : isSelected ? (
        <RiRadioButtonFill size={28} className="text-main flex-shrink-0" />
      ) : (
        <RiRadioButtonFill size={28} className="text-[#dddddd] flex-shrink-0" />
      )}
      <span className="flex-1 text-sm leading-[14px]">
        {option.name}
        {option.soldOut && <span className="text-[#aaaaaa] ml-2">(품절)</span>}
      </span>
      {option.additionalPrice > 0 && (
        <span className="text-sm leading-[14px]">+{formatNumber(option.additionalPrice)}원</span>
      )}
    </button>
  )
}
