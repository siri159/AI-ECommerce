import { useState } from 'react';

const DEFAULT_IMAGE = 'https://images.unsplash.com/photo-1524758631624-e2822e304c36?auto=format&fit=crop&w=1200&q=80';

export default function ProductImage({ src, alt, className = '', fallback = DEFAULT_IMAGE }) {
  const [imageSrc, setImageSrc] = useState(src || fallback);
  const [isLoading, setIsLoading] = useState(true);

  const handleError = () => {
    if (imageSrc !== fallback) {
      setImageSrc(fallback);
    }
    setIsLoading(false);
  };

  return (
    <div className={`product-image-shell ${className}`.trim()}>
      {isLoading && <div className="image-skeleton" aria-hidden="true" />}
      <img
        src={imageSrc}
        alt={alt}
        loading="lazy"
        onLoad={() => setIsLoading(false)}
        onError={handleError}
        className="product-image"
      />
    </div>
  );
}
