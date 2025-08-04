import { initializeUI } from './ui.js';
import { sendSimplePrintRequest, sendValidityPrintRequest } from './api.js';
import { showModal } from './modal.js';

document.addEventListener('DOMContentLoaded', () => {
    // Inicializa todos os elementos da UI e seus event listeners
    initializeUI();

    // Event listener principal para o botão de impressão
    const printButton = document.getElementById('printButton');
    if (printButton) {
        printButton.addEventListener('click', handlePrintAction);
    }
});

/**
 * Decide qual função de impressão chamar com base no modo ativo.
 */
function handlePrintAction() {
    const validityForm = document.getElementById('validityForm');

    // Validação de campos comuns
    const quantity = parseInt(document.getElementById('labelQuantity')?.value) || 0;
    if (quantity < 1) {
        showModal('A quantidade deve ser de no mínimo 1.');
        return;
    }

    if (validityForm && !validityForm.classList.contains('hidden')) {
        // Modo "Validade" está ativo
        sendValidityPrintRequest();
    } else {
        // Modo "Simples" está ativo
        sendSimplePrintRequest();
    }
}