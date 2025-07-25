import { showModal } from './modal.js';
import { updateButtonState } from './modal.js';

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
    if (!productName || productName.trim() === '') {
        showModal('O nome do produto não pode estar vazio.');
        return;
    }

    const payload = {
        productName,
        mfgDate: document.getElementById('mfgDate').value,
        expDate: document.getElementById('expDate').value,
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