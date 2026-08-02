export interface ProductCategory {
  categoryName: string
  products: Product[]
}

export interface Product {
  id: number
  imageUrl: string
  spiciness: number | null
  name: string
  originalPrice: number
  discountPrice: number
  discountRate: number | null
  rating: number | null
  reviewCount: number | null
  representative: boolean | null
}

export interface ProductOptionGroup {
  id: number
  name: string
  description: string | null
  required: boolean
  multipleSelect: boolean
  minSelect: number
  maxSelect: number
  common: boolean
  options: ProductOption[]
}

export interface ProductOption {
  id: number
  name: string
  additionalPrice: number
  soldOut: boolean
}
