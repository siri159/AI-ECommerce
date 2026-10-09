export default function LoadingSpinner({ message = 'Loading...' }) {
  return (
    <div className="state-box loading-box">
      <div className="spinner" aria-label="Loading" />
      <p>{message}</p>
    </div>
  );
}
