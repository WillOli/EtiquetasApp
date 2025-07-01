export function showModal(message) {
  try {
    const modal = document.getElementById('alertModal');
    const messageElement = document.getElementById('alertMessage');
    if (!modal || !messageElement) {
      throw new Error('Elementos do modal não encontrados');
    }
    messageElement.textContent = message;
    modal.style.display = 'flex';

    setTimeout(() => {
      closeModal();
    }, 3000);
  } catch (error) {
    console.error('Erro ao exibir modal:', error);
    alert('Erro ao exibir mensagem: ' + message); // Fallback para alerta do navegador
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