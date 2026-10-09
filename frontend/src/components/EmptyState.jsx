export default function EmptyState({ title, message, actionLabel, onAction }) {
  return (
    <div className="state-box empty-box">
      <h3>{title}</h3>
      <p>{message}</p>
      {onAction && actionLabel && (
        <button className="primary-btn small" onClick={onAction}>{actionLabel}</button>
      )}
    </div>
  );
}
