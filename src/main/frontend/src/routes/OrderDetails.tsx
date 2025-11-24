import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getOrder, type BeerOrderDTO } from '../services/orders.service'

export default function OrderDetails() {
  const { id } = useParams()
  const [order, setOrder] = useState<BeerOrderDTO | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const oid = Number(id)
    if (!Number.isFinite(oid)) return
    getOrder(oid)
      .then(setOrder)
      .catch((e) => setError(e.message ?? 'Failed to load order'))
  }, [id])

  if (error) return <div className="text-red-600">{error}</div>
  if (!order) return <div>Loading…</div>

  return (
    <div className="space-y-2">
      <h1 className="text-2xl font-bold">Order #{order.id}</h1>
      <dl className="grid grid-cols-2 gap-2 max-w-lg">
        <dt className="text-muted-foreground">Customer</dt><dd>{order.customerId}</dd>
        <dt className="text-muted-foreground">Status</dt><dd>{order.status}</dd>
        <dt className="text-muted-foreground">Created</dt><dd>{order.createdDate}</dd>
      </dl>
      {order.lines && order.lines.length > 0 && (
        <div>
          <h2 className="font-semibold mt-4">Line Items</h2>
          <ul className="list-disc pl-5 text-sm">
            {order.lines.map((l, idx) => (
              <li key={idx}>Beer {l.beerId} — Qty {l.quantity}</li>
            ))}
          </ul>
        </div>
      )}
      <Link to="/orders" className="underline">Back to list</Link>
    </div>
  )
}
