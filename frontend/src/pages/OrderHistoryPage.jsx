import { useEffect, useState } from 'react';
import orderApi from '../api/orderApi';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';

export default function OrderHistoryPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [orderDetails, setOrderDetails] = useState({});
  const [expandedOrderId, setExpandedOrderId] = useState(null);
  const [loadingOrderId, setLoadingOrderId] = useState(null);
  const [detailError, setDetailError] = useState('');

  const fetchOrders = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await orderApi.getMyOrders();
      setOrders(data || []);
    } catch (err) {
      setError(err?.response?.data?.message || 'Unable to load your order history.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const toggleOrderDetails = async (orderId) => {
    if (expandedOrderId === orderId) {
      setExpandedOrderId(null);
      return;
    }

    setDetailError('');
    if (!orderDetails[orderId]) {
      try {
        setLoadingOrderId(orderId);
        const details = await orderApi.getOrderById(orderId);
        setOrderDetails((current) => ({ ...current, [orderId]: details }));
      } catch (err) {
        setDetailError(err?.response?.data?.message || 'Unable to load order details.');
        return;
      } finally {
        setLoadingOrderId(null);
      }
    }

    setExpandedOrderId(orderId);
  };

  if (loading) return <LoadingSpinner message="Loading your orders..." />;
  if (error) return <ErrorMessage message={error} onRetry={fetchOrders} />;

  if (!orders.length) {
    return (
      <div className="container section-block">
        <EmptyState title="No orders yet" message="Your order history will appear here after checkout." />
      </div>
    );
  }

  return (
    <div className="container section-block">
      <div className="section-header">
        <div>
          <span className="eyebrow">Orders</span>
          <h2>Order history</h2>
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
            <button
              className="secondary-btn small"
              onClick={() => toggleOrderDetails(order.id)}
              disabled={loadingOrderId === order.id}
            >
              {loadingOrderId === order.id ? 'Loading details...' : expandedOrderId === order.id ? 'Hide details' : 'View details'}
            </button>
            {detailError && <p className="error-banner">{detailError}</p>}
            {expandedOrderId === order.id && orderDetails[order.id] && (
              <div className="order-details">
                <p>Placed {orderDetails[order.id].createdAt ? new Date(orderDetails[order.id].createdAt).toLocaleString() : 'date unavailable'}</p>
                <ul>
                  {orderDetails[order.id].items?.map((item) => (
                    <li key={item.id}>{item.productName} × {item.quantity} at ₹{Number(item.price || 0).toLocaleString('en-IN')} each</li>
                  ))}
                </ul>
              </div>
            )}
          </article>
        ))}
      </div>
    </div>
  );
}
