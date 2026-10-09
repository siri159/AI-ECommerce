const assistantFab = document.querySelector('.assistant-fab');
const assistantPanel = document.querySelector('.assistant-panel');
const closeBtn = document.querySelector('.close-btn');

if (assistantFab && assistantPanel) {
  assistantFab.addEventListener('click', () => {
    assistantPanel.classList.toggle('visible');
  });

  closeBtn?.addEventListener('click', () => {
    assistantPanel.classList.remove('visible');
  });

  document.addEventListener('click', (event) => {
    if (
      !assistantPanel.contains(event.target) &&
      !assistantFab.contains(event.target) &&
      assistantPanel.classList.contains('visible')
    ) {
      assistantPanel.classList.remove('visible');
    }
  });
}
