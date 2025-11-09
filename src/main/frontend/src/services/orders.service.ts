import { BeerOrderService } from '../lib/api'
import { normalizeError } from '../lib/axios'

export type Page<T> = {
  content: T[]
  number: number
  size: number
  totalElements: number
  totalPages: number
}

export type BeerOrderDTO = {
  id?: number
  version?: number
  customerId: number
  status?: string
  createdDate?: string
  updateDate?: string
  lines?: Array<{ beerId: number; quantity: number }>
}

export type ListOrdersParams = {
  page: number
  size: number
  status?: string
  customerId?: number
}

export async function listOrders(params: ListOrdersParams): Promise<Page<BeerOrderDTO>> {
  try {
    const res = await BeerOrderService.listOrders(params as any)
    return {
      content: (res as any).content ?? [],
      number: (res as any).number ?? 0,
      size: (res as any).size ?? params.size,
      totalElements: (res as any).totalElements ?? 0,
      totalPages: (res as any).totalPages ?? 0,
    }
  } catch (e) {
    throw normalizeError(e)
  }
}

export async function getOrder(id: number): Promise<BeerOrderDTO> {
  try {
    return (await BeerOrderService.getOrder({ id })) as any
  } catch (e) {
    throw normalizeError(e)
  }
}

export async function createOrder(payload: BeerOrderDTO): Promise<BeerOrderDTO> {
  try {
    return (await BeerOrderService.createOrder({ requestBody: payload as any })) as any
  } catch (e) {
    throw normalizeError(e)
  }
}

export async function updateOrder(id: number, payload: BeerOrderDTO): Promise<BeerOrderDTO> {
  try {
    return (await BeerOrderService.updateOrder({ id, requestBody: payload as any })) as any
  } catch (e) {
    throw normalizeError(e)
  }
}
