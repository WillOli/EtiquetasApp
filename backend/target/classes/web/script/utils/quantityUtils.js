import * as modalUtils from './modalUtils.js';

export function updateDuplicateInfo() {
  try {
    const quantityInput = document.getElementById('labelQuantity');
    const infoTextElement = document.getElementById('duplicate-info-text');
    const labelTypeSelect = document.getElementById('labelType'); // <-- 1. Apanhar o seletor de tipo

    if (!quantityInput || !infoTextElement || !labelTypeSelect) {
      console.error("Elementos de quantidade ou tipo não encontrados.");
      return;
    }

    const quantity = parseInt(quantityInput.value) || 0;
    const selectedType = labelTypeSelect.value; // <-- 2. Apanhar o valor selecionado

    let totalPrinted;

    // --- 3. Lógica Dinâmica ---
    if (selectedType === 'STANDARD') { // 'STANDARD' é o valor para a Etiqueta Dupla
        totalPrinted = quantity * 2;
    } else { // Para a 'Padrão' (80mmx25mm) e qualquer outra
        totalPrinted = quantity;
    }
    // --- Fim da Lógica ---

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
    updateDuplicateInfo();
  } catch (error) {
    console.error('Erro ao definir quantidade:', error);
    modalUtils.showModal('Erro ao definir quantidade: ' + error.message);
  }
}