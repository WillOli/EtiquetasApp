import * as textUtils from '../utils/textUtils.js';
import * as quantityUtils from '../utils/quantityUtils.js';
import * as printUtils from '../utils/printUtils.js';
import * as modalUtils from '../utils/modalUtils.js';

document.addEventListener('DOMContentLoaded', () => {
  const labelText = document.getElementById('labelText');
  const labelQuantity = document.getElementById('labelQuantity');

  labelText.addEventListener('input', textUtils.updatePreview);
  labelQuantity.addEventListener('input', quantityUtils.updateDuplicateInfo);

  textUtils.updatePreview();
  quantityUtils.updateDuplicateInfo();
});

// Expor funções globais para eventos inline no HTML
window.textUtils = textUtils;
window.quantityUtils = quantityUtils;
window.printUtils = printUtils;
window.modalUtils = modalUtils;