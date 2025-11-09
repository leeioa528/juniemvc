import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getCustomer, type CustomerDTO } from '../services/customers.service'

export default function CustomerDetails() {
  const { id } = useParams()
  const [customer, setCustomer] = useState<CustomerDTO | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const customerId = Number(id)
    if (!Number.isFinite(customerId)) return
    getCustomer(customerId)
      .then(setCustomer)
      .catch((e) => setError(e.message ?? 'Failed to load customer'))
  }, [id])

  if (error) return <div className="text-red-600">{error}</div>
  if (!customer) return <div>Loading…</div>

  return (
    <div className="space-y-2">
      <h1 className="text-2xl font-bold">{customer.customerName}</h1>
      <dl className="grid grid-cols-2 gap-2 max-w-lg">
        <dt className="text-muted-foreground">Email</dt><dd>{customer.email}</dd>
        <dt className="text-muted-foreground">Phone</dt><dd>{customer.phone}</dd>
      </dl>
      <Link to="/customers" className="underline">Back to list</Link>
    </div>
  )
}
