import * as modalUtils from './modalUtils.js';

// As funções precisam da palavra-chave 'export' para serem visíveis por outros arquivos.

export function updateDuplicateInfo() {
  try {
    const quantityInput = document.getElementById('labelQuantity');
    const infoTextElement = document.getElementById('duplicate-info-text');

    if (!quantityInput || !infoTextElement) {
      console.error("Elementos de quantidade não encontrados.");
      return;
    }

    const quantity = parseInt(quantityInput.value) || 0;
    const totalPrinted = quantity * 2;
    
    infoTextElement.textContent = `Quantidade solicitada: ${quantity}, Total impresso: ${totalPrinted}`;

  } catch (error) {
    console.error('Erro ao atualizar informação de duplicação:', error);
    modalUtils.showModal('Erro ao atualizar a quantidade: ' + error.message);
  }
}

export function setQuantity(amount) {
  try {
    const input = document.getElementById('labelQuantity');
    if (!input) {
      throw new Error('Elemento labelQuantity não encontrado');
    }
    input.value = amount;
    // Esta chamada funciona pois a função está no mesmo arquivo (escopo do módulo).
    updateDuplicateInfo();
  } catch (error) {
    console.error('Erro ao definir quantidade:', error);
    modalUtils.showModal('Erro ao definir quantidade: ' + error.message);
  }
}