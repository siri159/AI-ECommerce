import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import orderApi from '../api/orderApi';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';

export default function OrderConfirmationPage() {
  const [searchParams] = useSearchParams();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const orderId = searchParams.get('orderId');

  useEffect(() => {
    const fetchOrder = async () => {
      if (!orderId) {
        setError('Missing order ID.');
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        const data = await orderApi.getOrderById(Number(orderId));
        setOrder(data);
      } catch (err) {
        setError(err?.response?.data?.message || 'Unable to load your order confirmation.');
      } finally {
        setLoading(false);
      }
    };

    fetchOrder();
  }, [orderId]);

  if (loading) return <LoadingSpinner message="Confirming your order..." />;
  if (error) return <ErrorMessage message={error} />;
  if (!order) return null;

  return (
    <div className="container section-block">
      <div className="order-confirmation-card state-box">
        <div className="success-check">✓</div>
        <span className="eyebrow">Order placed successfully</span>
        <h2>Thank you for your order</h2>

        <div className="confirmation-grid">
          <div className="confirmation-item"><span>Order ID</span><strong>#{order.id}</strong></div>
          <div className="confirmation-item"><span>Total</span><strong>₹{Number(order.totalAmount || 0).toLocaleString('en-IN')}</strong></div>
          <div className="confirmation-item"><span>Payment status</span><strong>{order.paymentStatus || 'SUCCESS'}</strong></div>
          <div className="confirmation-item"><span>Order status</span><strong>{order.status || 'CONFIRMED'}</strong></div>
          <div className="confirmation-item full-span"><span>Date</span><strong>{new Date(order.createdAt).toLocaleString()}</strong></div>
        </div>

        <div className="confirmation-products">
          {order.items?.map((item) => (
            <div key={item.id} className="confirmation-product-row">
              <span>{item.productName} × {item.quantity}</span>
              <strong>₹{Number(item.subtotal || 0).toLocaleString('en-IN')}</strong>
            </div>
          ))}
        </div>

        <div className="confirmation-actions">
          <Link to="/orders" className="primary-btn">View Orders</Link>
          <Link to="/products" className="secondary-btn">Continue Shopping</Link>
        </div>
      </div>
    </div>
  );
}
