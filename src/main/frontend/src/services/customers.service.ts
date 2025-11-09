import { CustomerService } from '../lib/api'
import { normalizeError } from '../lib/axios'

export type Page<T> = {
  content: T[]
  number: number
  size: number
  totalElements: number
  totalPages: number
}

export type CustomerDTO = {
  id?: number
  version?: number
  customerName: string
  email?: string
  phone?: string
  createdDate?: string
  updateDate?: string
}

export type ListCustomersParams = {
  page: number
  size: number
  customerName?: string
}

export async function listCustomers(params: ListCustomersParams): Promise<Page<CustomerDTO>> {
  try {
    const res = await CustomerService.listCustomers(params as any)
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

export async function getCustomer(id: number): Promise<CustomerDTO> {
  try {
    return (await CustomerService.getCustomer({ id })) as any
  } catch (e) {
    throw normalizeError(e)
  }
}

export async function createCustomer(payload: CustomerDTO): Promise<CustomerDTO> {
  try {
    return (await CustomerService.createCustomer({ requestBody: payload as any })) as any
  } catch (e) {
    throw normalizeError(e)
  }
}

export async function updateCustomer(id: number, payload: CustomerDTO): Promise<CustomerDTO> {
  try {
    return (await CustomerService.updateCustomer({ id, requestBody: payload as any })) as any
  } catch (e) {
    throw normalizeError(e)
  }
}

export async function deleteCustomer(id: number): Promise<void> {
  try {
    await CustomerService.deleteCustomer({ id })
  } catch (e) {
    throw normalizeError(e)
  }
}
