export default function ErrorMessage({ message, onRetry }) {
  return (
    <div className="state-box error-box">
      <h3>Something went wrong</h3>
      <p>{message || 'The request could not be completed.'}</p>
      {onRetry && (
        <button className="primary-btn small" onClick={onRetry}>Try again</button>
      )}
    </div>
  );
}
