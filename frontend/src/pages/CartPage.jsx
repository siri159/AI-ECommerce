import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import cartApi from '../api/cartApi';
import ProductImage from '../components/ProductImage';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';

export default function CartPage() {
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [address, setAddress] = useState('');

  const fetchCart = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await cartApi.getCart();
      setCart(data);
    } catch (err) {
      setError(err?.response?.data?.message || 'Unable to load your cart.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, []);

  const handleQuantityUpdate = async (itemId, quantity) => {
    if (quantity < 1) return;
    try {
      await cartApi.updateItem(itemId, { quantity });
      await fetchCart();
    } catch (err) {
      setError(err?.response?.data?.message || 'Unable to update quantity.');
    }
  };

  const handleRemoveItem = async (itemId) => {
    try {
      await cartApi.removeItem(itemId);
      await fetchCart();
    } catch (err) {
      setError(err?.response?.data?.message || 'Unable to remove item.');
    }
  };

  const handleClearCart = async () => {
    try {
      await cartApi.clearCart();
      await fetchCart();
    } catch (err) {
      setError(err?.response?.data?.message || 'Unable to clear the cart.');
    }
  };

  if (loading) return <LoadingSpinner message="Loading cart..." />;
  if (error) return <ErrorMessage message={error} onRetry={fetchCart} />;

  if (!cart || !cart.items || cart.items.length === 0) {
    return (
      <div className="container section-block">
        <EmptyState
          title="Your cart is empty"
          message="Add products to begin checkout."
          actionLabel="Browse products"
          onAction={() => navigate('/products')}
        />
      </div>
    );
  }

  return (
    <div className="container section-block cart-layout">
      <div className="cart-items-panel">
        <div className="section-header">
          <div>
            <span className="eyebrow">Shopping cart</span>
            <h2>Review your order</h2>
          </div>
          <button className="secondary-btn small" onClick={handleClearCart}>Clear cart</button>
        </div>

        {cart.items.map((item) => (
          <div className="cart-item" key={item.id}>
            <ProductImage src={item.productImageUrl} alt={item.productName} className="cart-thumb" />
            <div className="cart-item-info">
              <h3>{item.productName}</h3>
              <p>₹{Number(item.price || 0).toLocaleString('en-IN')} each</p>
              <div className="quantity-row">
                <button onClick={() => handleQuantityUpdate(item.id, item.quantity - 1)}>-</button>
                <span>{item.quantity}</span>
                <button onClick={() => handleQuantityUpdate(item.id, item.quantity + 1)}>+</button>
              </div>
            </div>
            <div className="cart-item-meta">
              <strong>₹{Number(item.subtotal || 0).toLocaleString('en-IN')}</strong>
              <button className="secondary-btn small" onClick={() => handleRemoveItem(item.id)}>Remove</button>
            </div>
          </div>
        ))}
      </div>

      <aside className="checkout-panel">
        <h3>Order summary</h3>
        <div className="summary-row"><span>Subtotal</span><strong>₹{Number(cart.totalAmount || 0).toLocaleString('en-IN')}</strong></div>
        <div className="summary-row"><span>Shipping</span><strong>₹199</strong></div>
        <div className="summary-row total-row"><span>Total</span><strong>₹{Number((Number(cart.totalAmount || 0) + 199)).toLocaleString('en-IN')}</strong></div>
        <Link to="/checkout" className="primary-btn">Proceed to checkout</Link>
        <Link to="/products" className="secondary-btn full-width">Continue shopping</Link>
      </aside>
    </div>
  );
}
