import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import cartApi from '../api/cartApi';
import paymentApi from '../api/paymentApi';
import ProductImage from '../components/ProductImage';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';

export default function CheckoutPage() {
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [address, setAddress] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('CARD');
  const [processing, setProcessing] = useState(false);
  const [paymentSession, setPaymentSession] = useState(null);
  const [paymentResult, setPaymentResult] = useState('');

  const fetchCart = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await cartApi.getCart();
      setCart(data);
    } catch (err) {
      setError(err?.response?.data?.message || 'Unable to load your cart for checkout.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, []);

  const subtotal = useMemo(
    () => Number(cart?.totalAmount || 0),
    [cart]
  );
  const shipping = subtotal > 0 ? 199 : 0;
  const total = subtotal + shipping;

  const handleInitiatePayment = async () => {
    if (!address.trim()) {
      setError('Please enter your shipping address.');
      return;
    }

    try {
      setProcessing(true);
      setError('');
      setPaymentResult('');
      const session = await paymentApi.initiate({ shippingAddress: address, paymentMethod });
      setPaymentSession(session);
      setPaymentResult('Sandbox payment ready. Choose a test result below.');
    } catch (err) {
      setError(err?.response?.data?.message || 'Unable to start payment.');
    } finally {
      setProcessing(false);
    }
  };

  const handleSandboxPayment = async (status) => {
    if (!paymentSession?.id) {
      setError('Start the payment session before testing the gateway result.');
      return;
    }

    try {
      setProcessing(true);
      setError('');

      let paymentResponse;
      if (status === 'SUCCESS') {
        paymentResponse = await paymentApi.complete(paymentSession.id);
      } else if (status === 'FAILED') {
        paymentResponse = await paymentApi.fail(paymentSession.id);
      } else {
        paymentResponse = await paymentApi.cancel(paymentSession.id);
      }

      if (paymentResponse.paymentStatus === 'SUCCESS') {
        navigate(`/order-confirmation?orderId=${paymentResponse.orderId}`);
        return;
      }

      setPaymentResult(`Payment ${paymentResponse.paymentStatus.toLowerCase()}. Please retry or update the order.`);
    } catch (err) {
      setError(err?.response?.data?.message || 'Unable to complete the test payment.');
    } finally {
      setProcessing(false);
    }
  };

  if (loading) return <LoadingSpinner message="Loading checkout..." />;
  if (error && !cart) return <ErrorMessage message={error} onRetry={fetchCart} />;

  if (!cart || !cart.items || cart.items.length === 0) {
    return (
      <div className="container section-block">
        <EmptyState
          title="Your cart is empty"
          message="Add items before you start checkout."
          actionLabel="Browse products"
          onAction={() => navigate('/products')}
        />
      </div>
    );
  }

  return (
    <div className="container section-block checkout-shell">
      <div className="section-header">
        <div>
          <span className="eyebrow">Checkout</span>
          <h2>Finalize your order</h2>
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}

      <div className="checkout-grid">
        <section className="checkout-section">
          <div className="checkout-card">
            <h3>Customer details</h3>
            <label>
              Shipping address
              <textarea
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                rows="4"
                placeholder="123 Main Street, Bengaluru, Karnataka"
              />
            </label>

            <label>
              Payment method
              <select value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value)}>
                <option value="CARD">Card</option>
                <option value="UPI">UPI</option>
                <option value="COD">Cash on delivery</option>
              </select>
            </label>
          </div>

          <div className="checkout-card">
            <h3>Items</h3>
            <div className="checkout-items">
              {cart.items.map((item) => (
                <div className="checkout-item" key={item.id}>
                  <ProductImage
                    src={item.productImageUrl}
                    alt={item.productName}
                    className="checkout-thumb"
                  />
                  <div className="checkout-item-meta">
                    <strong>{item.productName}</strong>
                    <span>Qty: {item.quantity}</span>
                  </div>
                  <strong>₹{Number(item.subtotal || 0).toLocaleString('en-IN')}</strong>
                </div>
              ))}
            </div>
          </div>
        </section>

        <aside className="checkout-panel checkout-summary">
          <h3>Order summary</h3>
          <div className="summary-row"><span>Subtotal</span><strong>₹{subtotal.toLocaleString('en-IN')}</strong></div>
          <div className="summary-row"><span>Shipping</span><strong>₹{shipping.toLocaleString('en-IN')}</strong></div>
          <div className="summary-row total-row"><span>Total</span><strong>₹{total.toLocaleString('en-IN')}</strong></div>

          <button className="primary-btn" onClick={handleInitiatePayment} disabled={processing}>
            {processing ? 'Preparing payment...' : 'Start test payment'}
          </button>

          {paymentSession && (
            <div className="payment-test-box">
              <p><strong>Payment ID:</strong> {paymentSession.id}</p>
              <p><strong>Gateway status:</strong> {paymentSession.paymentStatus}</p>
              {paymentResult && <div className="success-banner">{paymentResult}</div>}

              <div className="sandbox-actions">
                <button className="primary-btn small" onClick={() => handleSandboxPayment('SUCCESS')} disabled={processing}>
                  {processing ? 'Processing...' : 'Successful payment'}
                </button>
                <button className="secondary-btn small" onClick={() => handleSandboxPayment('FAILED')} disabled={processing}>
                  Failed payment
                </button>
                <button className="secondary-btn small" onClick={() => handleSandboxPayment('CANCELLED')} disabled={processing}>
                  Cancel payment
                </button>
              </div>
            </div>
          )}

          <Link to="/cart" className="secondary-btn full-width">Back to cart</Link>
        </aside>
      </div>
    </div>
  );
}
