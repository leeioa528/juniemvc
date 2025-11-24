import { BeerService } from '../lib/api'
import { normalizeError } from '../lib/axios'

export type Page<T> = {
  content: T[]
  number: number
  size: number
  totalElements: number
  totalPages: number
}

export type BeerDTO = {
  id?: number
  version?: number
  beerName: string
  beerStyle: string
  upc: string
  quantityOnHand: number
  price: number
  description?: string
  createdDate?: string
  updateDate?: string
}

export type ListBeersParams = {
  page: number
  size: number
  beanName?: string
  beerStyle?: string
}

export async function listBeers(params: ListBeersParams): Promise<Page<BeerDTO>> {
  try {
    const res = await BeerService.listBeers(params as any)
    return {
      content: res.content ?? [],
      number: res.number ?? 0,
      size: res.size ?? params.size,
      totalElements: res.totalElements ?? 0,
      totalPages: res.totalPages ?? 0,
    }
  } catch (e) {
    throw normalizeError(e)
  }
}

export async function getBeer(id: number): Promise<BeerDTO> {
  try {
    return (await BeerService.getBeerById({ id })) as any
  } catch (e) {
    throw normalizeError(e)
  }
}

export async function createBeer(payload: BeerDTO): Promise<BeerDTO> {
  try {
    return await BeerService.createBeer({ requestBody: payload as any }) as any
  } catch (e) {
    throw normalizeError(e)
  }
}

export async function updateBeer(id: number, payload: BeerDTO): Promise<BeerDTO> {
  try {
    return await BeerService.updateBeer({ id, requestBody: payload as any }) as any
  } catch (e) {
    throw normalizeError(e)
  }
}

export async function deleteBeer(id: number): Promise<void> {
  try {
    await BeerService.deleteBeer({ id })
  } catch (e) {
    throw normalizeError(e)
  }
}
