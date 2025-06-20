import * as modalUtils from './modalUtils.js';

export function setQuantity(amount) {
  try {
    const input = document.getElementById('labelQuantity');
    if (!input) {
      throw new Error('Elemento labelQuantity não encontrado');
    }
    input.value = amount;
    updateDuplicateInfo();
  } catch (error) {
    console.error('Erro ao definir quantidade:', error);
    modalUtils.showModal('Erro ao definir quantidade: ' + error.message);
  }
}

export function updateDuplicateInfo() {
  try {
    const input = document.getElementById('labelQuantity');
    if (!input) {
      throw new Error('Elemento labelQuantity não encontrado');
    }
    const quantity = parseInt(input.value) || 1;
    if (isNaN(quantity)) {
      throw new Error('Quantidade inválida');
    }
    const totalPrinted = quantity * 2;
    const duplicateInfo = document.getElementById('duplicate-info');
    if (!duplicateInfo) {
      throw new Error('Elemento duplicate-info não encontrado');
    }
    duplicateInfo.textContent = `Quantidade solicitada: ${quantity}, Total impresso: ${totalPrinted}`;
  } catch (error) {
    console.error('Erro ao atualizar informação de duplicação:', error);
    modalUtils.showModal('Erro ao atualizar a quantidade: ' + error.message);
  }
}