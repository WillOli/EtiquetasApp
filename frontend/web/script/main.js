import * as textUtils from '../utils/textUtils.js';
import * as quantityUtils from '../utils/quantityUtils.js';
import * as printUtils from '../utils/printUtils.js';
import * as modalUtils from '../utils/modalUtils.js';

document.addEventListener('DOMContentLoaded', () => {
  try {
    const labelText = document.getElementById('labelText');
    const labelQuantity = document.getElementById('labelQuantity');

    if (!labelText || !labelQuantity) {
      throw new Error('Elementos labelText ou labelQuantity não encontrados');
    }

    labelText.addEventListener('input', () => {
      try {
        textUtils.updatePreview();
      } catch (error) {
        console.error('Erro ao atualizar preview:', error);
        modalUtils.showModal('Erro ao atualizar a visualização: ' + error.message);
      }
    });

    labelQuantity.addEventListener('input', () => {
      try {
        quantityUtils.updateDuplicateInfo();
      } catch (error) {
        console.error('Erro ao atualizar informação de duplicação:', error);
        modalUtils.showModal('Erro ao atualizar a quantidade: ' + error.message);
      }
    });

    textUtils.updatePreview();
    quantityUtils.updateDuplicateInfo();
  } catch (error) {
    console.error('Erro ao inicializar eventos:', error);
    modalUtils.showModal('Erro ao carregar a página: ' + error.message);
  }
});

// Expor funções globais para eventos inline no HTML
window.textUtils = textUtils;
window.quantityUtils = quantityUtils;
window.printUtils = printUtils;
window.modalUtils = modalUtils;