import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import cartApi from '../api/cartApi';
import productApi from '../api/productApi';
import reviewApi from '../api/reviewApi';
import { useAuth } from '../context/AuthContext';
import ProductImage from '../components/ProductImage';
import ErrorMessage from '../components/ErrorMessage';
import LoadingSpinner from '../components/LoadingSpinner';

export default function ProductDetailPage() {
  const { id } = useParams();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [product, setProduct] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reviewForm, setReviewForm] = useState({ rating: 5, comment: '' });
  const [submitting, setSubmitting] = useState(false);

  const fetchProduct = async () => {
    try {
      setLoading(true);
      setError('');
      const [productData, reviewData] = await Promise.all([
        productApi.getProductById(id),
        reviewApi.getForProduct(id),
      ]);
      setProduct(productData);
      setReviews(reviewData || []);
    } catch (err) {
      setError(err?.response?.data?.message || 'Unable to load this product.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProduct();
  }, [id]);

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    try {
      await cartApi.addItem({ productId: Number(id), quantity: 1 });
      navigate('/cart');
    } catch (err) {
      setError(err?.response?.data?.message || 'Unable to add the item to cart.');
    }
  };

  const handleReviewSubmit = async (event) => {
    event.preventDefault();
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    try {
      setSubmitting(true);
      await reviewApi.create(id, reviewForm);
      setReviewForm({ rating: 5, comment: '' });
      await fetchProduct();
    } catch (err) {
      setError(err?.response?.data?.message || 'Unable to submit review.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <LoadingSpinner message="Loading product details..." />;
  if (error) return <ErrorMessage message={error} onRetry={fetchProduct} />;
  if (!product) return null;

  return (
    <div className="container section-block product-detail-layout">
      <Link to="/products" className="back-link">← Back to products</Link>

      <div className="product-detail-card">
        <ProductImage src={product.imageUrl} alt={product.name} className="product-detail-image" />
        <div className="product-detail-info">
          <span className="muted-label">{product.categoryName || 'Premium product'}</span>
          <h2>{product.name}</h2>
          <div className="rating-row">★★★★★ <small>{reviews.length} reviews</small></div>
          <p className="product-description">{product.description || 'No description provided by the backend yet.'}</p>
          <div className="detail-price-row">
            <strong>₹{Number(product.price || 0).toLocaleString('en-IN')}</strong>
            <span className="badge badge-green">{product.stockQuantity > 0 ? 'In stock' : 'Out of stock'}</span>
          </div>
          <div className="detail-actions">
            <button className="primary-btn" onClick={handleAddToCart}>Add to cart</button>
            <Link to="/products" className="secondary-btn">Continue shopping</Link>
          </div>
        </div>
      </div>

      <section className="review-section">
        <div className="section-header">
          <div>
            <span className="eyebrow">Reviews</span>
            <h3>Customer feedback</h3>
          </div>
        </div>

        <div className="review-list">
          {reviews.length ? (
            reviews.map((review) => (
              <div className="review-item" key={review.id}>
                <div className="review-stars">{'★'.repeat(review.rating || 5)}</div>
                <p>{review.comment}</p>
              </div>
            ))
          ) : (
            <p className="muted-text">No reviews yet. Be the first to leave feedback.</p>
          )}
        </div>

        <form className="review-form" onSubmit={handleReviewSubmit}>
          <h4>Write a review</h4>
          <label>
            Rating
            <select value={reviewForm.rating} onChange={(e) => setReviewForm({ ...reviewForm, rating: Number(e.target.value) })}>
              <option value={5}>5 stars</option>
              <option value={4}>4 stars</option>
              <option value={3}>3 stars</option>
              <option value={2}>2 stars</option>
              <option value={1}>1 star</option>
            </select>
          </label>
          <label>
            Comment
            <textarea
              rows="4"
              value={reviewForm.comment}
              onChange={(e) => setReviewForm({ ...reviewForm, comment: e.target.value })}
              placeholder="Share your experience"
              required
            />
          </label>
          <button className="primary-btn" type="submit" disabled={submitting}>
            {submitting ? 'Submitting...' : 'Submit review'}
          </button>
        </form>
      </section>
    </div>
  );
}
