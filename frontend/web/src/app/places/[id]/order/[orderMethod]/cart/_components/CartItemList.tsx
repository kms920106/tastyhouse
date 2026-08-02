import OrderCartItem from '@/components/orders/OrderCartItem'
import type { OrderProduct } from '@/domains/order'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { LiaPlusSolid } from 'react-icons/lia'

interface Props {
  cartItems: OrderProduct[]
  shopName: string
  selectedKeys: Set<string>
  onToggleSelect: (optionKey: string) => void
  onQuantityChange: (optionKey: string, quantity: number) => void
  onRemove: (optionKey: string) => void
}

export default function CartItemList({
  cartItems,
  shopName,
  selectedKeys,
  onToggleSelect,
  onQuantityChange,
  onRemove,
}: Props) {
  const router = useRouter()

  if (cartItems.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-20">
        <p className="text-base leading-[16px] text-[#aaaaaa]">장바구니가 비어있습니다.</p>
        <div className="mt-[15px]">
          <Link href={PAGE_PATHS.HOME} className="text-sm leading-[14px] text-main underline">
            메뉴 담으러 가기
          </Link>
        </div>
      </div>
    )
  }

  return (
    <>
      <div className="px-[15px] divide-y divide-[#f2f2f2]">
        <h2 className="py-5 text-base leading-[16px]">{shopName}</h2>
        {cartItems.map((product) => (
          <OrderCartItem
            key={product.optionKey}
            optionKey={product.optionKey}
            name={product.name}
            imageUrl={product.imageUrl}
            salePrice={product.salePrice}
            originalPrice={product.originalPrice}
            quantity={product.quantity}
            selected={selectedKeys.has(product.optionKey)}
            options={product.options}
            onToggleSelect={onToggleSelect}
            onQuantityChange={onQuantityChange}
            onRemove={onRemove}
          />
        ))}
      </div>
      <div className="py-[18px] border-t border-[#f2f2f2] box-border">
        <div
          className="flex items-center justify-center gap-2.5 text-main"
          onClick={() => router.back()}
        >
          {cartItems.length > 0 && <LiaPlusSolid size={20} />}
          <span className="text-sm leading-[14px]">메뉴 담으러 가기</span>
        </div>
      </div>
    </>
  )
}
