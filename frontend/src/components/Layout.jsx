import { useEffect, useState } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import cartApi from '../api/cartApi';
import { useAuth } from '../context/AuthContext';

export default function Layout() {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const [cartCount, setCartCount] = useState(0);

  useEffect(() => {
    const fetchCartCount = async () => {
      if (!isAuthenticated) {
        setCartCount(0);
        return;
      }

      try {
        const cart = await cartApi.getCart();
        const count = (cart?.items || []).reduce((sum, item) => sum + Number(item.quantity || 0), 0);
        setCartCount(count);
      } catch (error) {
        setCartCount(0);
      }
    };

    fetchCartCount();
  }, [isAuthenticated, user?.userId]);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <>
      <header className="topbar">
        <div className="container nav-bar">
          <Link to="/" className="brand" aria-label="maasho home">
            <span className="brand-mark">M</span>
            <span>maasho</span>
          </Link>

          <nav className="main-nav">
            <NavLink to="/">Home</NavLink>
            <NavLink to="/products">Shop</NavLink>
            <NavLink to="/orders">Orders</NavLink>
            {user?.role === 'ADMIN' && <NavLink to="/admin">Admin</NavLink>}
          </nav>

          <div className="nav-actions">
            <Link to="/cart" className="nav-icon-link">
              Cart
              {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
            </Link>
            {isAuthenticated ? (
              <>
                <span className="user-pill">{user?.name || 'User'}</span>
                <button className="secondary-btn small" onClick={handleLogout}>Logout</button>
              </>
            ) : (
              <>
                <Link to="/login" className="secondary-btn small">Login</Link>
                <Link to="/register" className="primary-btn small">Register</Link>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="page-shell">
        <Outlet />
      </main>

      <footer className="site-footer">
        <div className="container footer-inner">
          <div>
            <div className="brand footer-brand">
              <span className="brand-mark">M</span>
              <span>maasho</span>
            </div>
            <p>Premium essentials for modern living.</p>
          </div>
          <div>
            <h4>Shop</h4>
            <ul>
              <li>Electronics</li>
              <li>Home</li>
              <li>Fashion</li>
              <li>Wellness</li>
            </ul>
          </div>
          <div>
            <h4>Support</h4>
            <ul>
              <li>Shipping</li>
              <li>Returns</li>
              <li>FAQs</li>
              <li>Contact</li>
            </ul>
          </div>
        </div>
      </footer>
    </>
  );
}
