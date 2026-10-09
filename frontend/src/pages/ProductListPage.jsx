import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import productApi from '../api/productApi';
import categoryApi from '../api/categoryApi';
import ProductImage from '../components/ProductImage';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';

export default function ProductListPage() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchProducts = async () => {
    try {
      setLoading(true);
      setError('');
      const [productsData, categoryData] = await Promise.all([
        productApi.getProducts({ page: 0, size: 12 }),
        categoryApi.getAll(),
      ]);
      const list = productsData?.content || productsData || [];
      setProducts(list);
      setCategories(categoryData || []);
    } catch (err) {
      setError(err?.response?.data?.message || 'Unable to load products.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const filteredProducts = useMemo(() => {
    return products.filter((product) => {
      const categoryOk = selectedCategory === 'all' || String(product.categoryId) === String(selectedCategory);
      const queryOk = !search || product.name?.toLowerCase().includes(search.toLowerCase());
      return categoryOk && queryOk;
    });
  }, [products, selectedCategory, search]);

  if (loading) return <LoadingSpinner message="Loading catalog..." />;
  if (error) return <ErrorMessage message={error} onRetry={fetchProducts} />;

  return (
    <div className="container section-block">
      <div className="section-header stacked-header">
        <div>
          <span className="eyebrow">Shop all</span>
          <h2>Products</h2>
        </div>
        <div className="filter-bar">
          <input
            type="text"
            className="search-input"
            placeholder="Search products"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <select value={selectedCategory} onChange={(e) => setSelectedCategory(e.target.value)}>
            <option value="all">All categories</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>{category.name}</option>
            ))}
          </select>
        </div>
      </div>

      {filteredProducts.length ? (
        <div className="product-grid">
          {filteredProducts.map((product) => (
            <article className="product-card" key={product.id}>
              <ProductImage src={product.imageUrl} alt={product.name} className="product-thumb" />
              <div className="product-body">
                <span className="muted-label">{product.categoryName || 'Catalog item'}</span>
                <h3><Link to={`/products/${product.id}`}>{product.name}</Link></h3>
                <div className="rating-row">★★★★★ <small>4.8</small></div>
                <div className="price-row">
                  <strong>₹{Number(product.price || 0).toLocaleString('en-IN')}</strong>
                  <span className="badge badge-green">{product.stockQuantity > 0 ? 'Available' : 'Out of stock'}</span>
                </div>
                <div className="card-actions">
                  <Link to={`/products/${product.id}`} className="primary-btn small">View</Link>
                </div>
              </div>
            </article>
          ))}
        </div>
      ) : (
        <EmptyState title="No matching products" message="Try a broader search or switch categories." />
      )}
    </div>
  );
}
