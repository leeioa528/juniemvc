import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import CustomersList from './CustomersList'
import * as Api from '../lib/api'

describe('CustomersList', () => {
  it('renders customers from API', async () => {
    const spy = vi.spyOn(Api.CustomerService, 'listCustomers').mockResolvedValue({
      content: [
        { id: 1, customerName: 'Alice', email: 'a@example.com', phone: '123' } as any,
        { id: 2, customerName: 'Bob', email: 'b@example.com', phone: '456' } as any
      ],
      number: 0,
      size: 10,
      totalElements: 2,
      totalPages: 1
    } as any)

    render(
      <MemoryRouter>
        <CustomersList />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getByText('Alice')).toBeInTheDocument()
      expect(screen.getByText('Bob')).toBeInTheDocument()
    })

    spy.mockRestore()
  })

  it('shows an error when API fails', async () => {
    const spy = vi.spyOn(Api.CustomerService, 'listCustomers').mockRejectedValue(new Error('Boom'))

    render(
      <MemoryRouter>
        <CustomersList />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getByText(/failed to load customers/i)).toBeInTheDocument()
    })

    spy.mockRestore()
  })
})
