import { showModal, updateButtonState } from './modal.js';

/**
 * Envia uma requisição para imprimir uma etiqueta simples.
 */
export function sendSimplePrintRequest() {
    const text = document.getElementById('labelText')?.value;
    if (!text || text.trim() === '') {
        showModal('O texto da etiqueta simples não pode estar vazio.');
        return;
    }

    const payload = {
        text,
        quantity: parseInt(document.getElementById('labelQuantity').value),
        labelType: document.getElementById('labelType').value
    };

    sendRequest('/print', payload);
}

/**
 * Envia uma requisição para imprimir uma etiqueta de validade.
 */
export function sendValidityPrintRequest() {
    const productName = document.getElementById('productName')?.value;
    const mfgDate = document.getElementById('mfgDate')?.value;
    // ===== INÍCIO DA ALTERAÇÃO FINAL =====
    // Lógica simplificada para ler diretamente do campo de texto
    const validityDays = document.getElementById('validityDays')?.value;
    // ===== FIM DA ALTERAÇÃO FINAL =====

    if (!productName || productName.trim() === '') {
        showModal('O nome do produto não pode estar vazio.');
        return;
    }

    if (!mfgDate) {
        showModal('A data de fabricação é obrigatória.');
        return;
    }

    if (!validityDays || parseInt(validityDays) <= 0) {
        showModal('Os dias de validade devem ser um número maior que zero.');
        return;
    }

    const payload = {
        productName,
        mfgDate,
        validityDays: parseInt(validityDays),
        quantity: parseInt(document.getElementById('labelQuantity').value),
        labelType: document.getElementById('labelType').value
    };

    sendRequest('/print-validade', payload);
}

/**
 * Função genérica para enviar requisições de impressão para o backend.
 * @param {string} endpoint - O endpoint da API ('/print' ou '/print-validade').
 * @param {object} payload - O corpo da requisição.
 */
async function sendRequest(endpoint, payload) {
    updateButtonState(true);

    try {
        const response = await fetch(`http://localhost:8081${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const responseText = await response.text();

        if (!response.ok) {
            throw new Error(responseText || 'Erro desconhecido do servidor.');
        }

        showModal(responseText);
    } catch (error) {
        console.error(`Erro ao chamar ${endpoint}:`, error);
        showModal(`Erro na comunicação: ${error.message}`);
    } finally {
        updateButtonState(false);
    }
}