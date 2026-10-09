import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import aiApi from '../api/aiApi';
import productApi from '../api/productApi';
import ProductImage from '../components/ProductImage';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';

export default function HomePage() {
  const [products, setProducts] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [assistantMessage, setAssistantMessage] = useState('I need a laptop under ₹80,000.');
  const [assistantResponse, setAssistantResponse] = useState('');
  const [assistantLoading, setAssistantLoading] = useState(false);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError('');
      const [productsResponse, recommendationsResponse] = await Promise.all([
        productApi.getProducts({ page: 0, size: 4 }),
        aiApi.getRecommendations(),
      ]);
      setProducts(productsResponse?.content || productsResponse || []);
      setRecommendations(recommendationsResponse || []);
    } catch (err) {
      setError(err?.response?.data?.message || 'Unable to load the storefront right now.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleAssistant = async () => {
    try {
      setAssistantLoading(true);
      const response = await aiApi.chat({ message: assistantMessage });
      setAssistantResponse(response.answer || 'I can help with product suggestions.');
    } catch (err) {
      setAssistantResponse(err?.response?.data?.message || 'The assistant is unavailable right now.');
    } finally {
      setAssistantLoading(false);
    }
  };

  if (loading) return <LoadingSpinner message="Loading your storefront..." />;
  if (error) return <ErrorMessage message={error} onRetry={fetchData} />;

  return (
    <>
      <section className="hero container">
        <div className="hero-copy">
          <span className="eyebrow">Fresh arrivals</span>
          <h1>Find what you need.</h1>
          <p>Upgrade your everyday essentials with premium picks for home, tech, style and wellness.</p>
          <div className="hero-actions">
            <Link to="/products" className="primary-btn">Shop now</Link>
            <Link to="/products" className="secondary-btn">Browse categories</Link>
          </div>
          <ul className="hero-metrics">
            <li><strong>24h</strong><span>Fast dispatch</span></li>
            <li><strong>2k+</strong><span>Happy shoppers</span></li>
            <li><strong>4.8/5</strong><span>Average rating</span></li>
          </ul>
        </div>

        <div className="hero-visual">
          <div className="spotlight-card">
            <span className="badge badge-gold">Limited offer</span>
            <div className="spotlight-image" aria-hidden="true" />
            <div className="spotlight-row">
              <div>
                <p>Air Pro Max</p>
                <strong>₹24,999</strong>
              </div>
              <button className="primary-btn small">Add</button>
            </div>
          </div>
          <div className="floating-tag tag-left">Trending</div>
          <div className="floating-tag tag-right">Spring edit</div>
        </div>
      </section>

      <section className="container section-block">
        <div className="section-header">
          <div>
            <span className="eyebrow">Shop by category</span>
            <h2>Popular picks</h2>
          </div>
          <Link to="/products">View all</Link>
        </div>

        <div className="category-grid">
          {['Electronics', 'Home', 'Fashion', 'Wellness'].map((category, index) => (
            <article className="category-card" key={category}>
              <div className="category-icon">{['◫', '⌂', '✦', '≋'][index]}</div>
              <h3>{category}</h3>
              <p>Curated essentials</p>
            </article>
          ))}
        </div>
      </section>

      <section className="container section-block">
        <div className="section-header">
          <div>
            <span className="eyebrow">Featured collection</span>
            <h2>Best sellers</h2>
          </div>
          <Link to="/products">More products</Link>
        </div>

        {products.length ? (
          <div className="product-grid">
            {products.map((product) => (
              <article className="product-card" key={product.id}>
                <ProductImage src={product.imageUrl} alt={product.name} className="product-thumb" />
                <div className="product-body">
                  <span className="muted-label">{product.brand || product.categoryName || 'Premium'}</span>
                  <h3><Link to={`/products/${product.id}`}>{product.name}</Link></h3>
                  <div className="rating-row">★★★★★ <small>4.8</small></div>
                  <div className="price-row">
                    <strong>₹{Number(product.price || 0).toLocaleString('en-IN')}</strong>
                    <span className="badge badge-green">{product.stockQuantity > 0 ? 'In stock' : 'Sold out'}</span>
                  </div>
                  <div className="card-actions">
                    <Link className="primary-btn small" to={`/products/${product.id}`}>View</Link>
                  </div>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <EmptyState title="No products available" message="Products are not being returned by the backend yet." />
        )}
      </section>

      <section className="container section-block">
        <div className="deal-banner">
          <div>
            <span className="eyebrow eyebrow-light">Weekend savings</span>
            <h3>Up to 40% off on premium essentials</h3>
          </div>
          <Link to="/products" className="secondary-btn light-btn">Shop deals</Link>
        </div>
      </section>

      <section className="container section-block assistant-panel-home">
        <div className="assistant-copy">
          <span className="eyebrow">Need help choosing?</span>
          <h2>Shopping Assistant</h2>
          <p>Ask for product suggestions, compare categories, or find the right fit within your budget.</p>
        </div>

        <div className="assistant-box">
          <textarea
            rows="3"
            value={assistantMessage}
            onChange={(e) => setAssistantMessage(e.target.value)}
            placeholder="Ask for a recommendation..."
          />
          <button className="primary-btn" onClick={handleAssistant} disabled={assistantLoading}>
            {assistantLoading ? 'Thinking...' : 'Ask assistant'}
          </button>
        </div>

        {assistantResponse && (
          <div className="assistant-response">
            <strong>Assistant:</strong>
            <p>{assistantResponse}</p>
            {recommendations.length > 0 && (
              <ul>
                {recommendations.slice(0, 3).map((item) => (
                  <li key={item.productId}>{item.name} — ₹{Number(item.price || 0).toLocaleString('en-IN')}</li>
                ))}
              </ul>
            )}
          </div>
        )}
      </section>
    </>
  );
}
