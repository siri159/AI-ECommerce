import { useEffect, useState } from 'react';
import orderApi from '../api/orderApi';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';

export default function AdminPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchOrders = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await orderApi.getAllOrders();
      setOrders(data || []);
    } catch (err) {
      setError(err?.response?.data?.message || 'Unable to load admin orders.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  if (loading) return <LoadingSpinner message="Loading admin dashboard..." />;
  if (error) return <ErrorMessage message={error} onRetry={fetchOrders} />;

  return (
    <div className="container section-block">
      <div className="section-header">
        <div>
          <span className="eyebrow">Admin</span>
          <h2>Orders overview</h2>
        </div>
      </div>

      <div className="order-list">
        {orders.map((order) => (
          <article className="order-card" key={order.id}>
            <div className="order-card-header">
              <div>
                <small>Order #</small>
                <strong>{order.id}</strong>
              </div>
              <span className="badge badge-green">{order.status}</span>
            </div>
            <p className="shipping-address">{order.shippingAddress}</p>
            <ul>
              {order.items?.map((item) => (
                <li key={item.id}>{item.productName} × {item.quantity}</li>
              ))}
            </ul>
            <div className="summary-row">
              <span>Total</span>
              <strong>₹{Number(order.totalAmount || 0).toLocaleString('en-IN')}</strong>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
