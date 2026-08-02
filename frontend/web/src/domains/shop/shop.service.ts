import 'server-only'

import { getShopAmenityCodeName, getShopFoodTypeCodeName } from './shop.constants'
import { shopRepository } from './shop.repository'

export const shopService = {
  async getShopFoodTypes() {
    const response = await shopRepository.getShopFoodTypes()
    if (response.data) {
      response.data = response.data.map((item) => ({
        ...item,
        name: getShopFoodTypeCodeName(item.code),
      }))
    }
    return response
  },

  async getShopAmenities() {
    const response = await shopRepository.getShopAmenities()
    if (response.data) {
      response.data = response.data.map((item) => ({
        ...item,
        name: getShopAmenityCodeName(item.code),
      }))
    }
    return response
  },
}
