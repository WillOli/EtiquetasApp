export function showModal(message) {
  try {
    const modal = document.getElementById('alertModal');
    const messageElement = document.getElementById('alertMessage');
    if (!modal || !messageElement) {
      throw new Error('Elementos do modal não encontrados');
    }
    messageElement.textContent = message;
    modal.style.display = 'flex';
    const closeButton = modal.querySelector('button');
    const autoCloseTimeout = setTimeout(() => {
      closeModal();
    }, 3000);

    // Limpa o timeout se o usuário fechar manualmente
    closeButton.onclick = () => {
        clearTimeout(autoCloseTimeout);
        closeModal();
    };

  } catch (error) {
    console.error('Erro ao exibir modal:', error);
    alert(message); // Fallback para alerta do navegador
  }
}

export function closeModal() {
  try {
    const modal = document.getElementById('alertModal');
    if (!modal) {
      throw new Error('Elemento alertModal não encontrado');
    }
    modal.style.display = 'none';
  } catch (error) {
    console.error('Erro ao fechar modal:', error);
  }
}