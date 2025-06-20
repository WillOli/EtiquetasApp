export function showModal(message) {
  const modal = document.getElementById('alertModal');
  const messageElement = document.getElementById('alertMessage');
  messageElement.textContent = message;
  modal.style.display = 'flex';

  setTimeout(() => {
    closeModal();
  }, 3000);
}

export function closeModal() {
  const modal = document.getElementById('alertModal');
  modal.style.display = 'none';
}