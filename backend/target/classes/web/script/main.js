/**
 * @file main.js
 * @description Integração final entre o Frontend dinâmico e o Servidor Java.
 */

const API_BASE_URL = 'http://localhost:8081';

const appState = {
    mode: 'SIMPLE',
};

// =========================================================================
// FUNÇÃO DE IMPRESSÃO (O CORAÇÃO DO ENVIO)
// =========================================================================
async function handlePrintAction() {
    const labelQuantity = document.getElementById('labelQuantity');
    const labelType = document.getElementById('labelType');
    const labelText = document.getElementById('labelText');
    const labelSector = document.getElementById('labelSector');

    const quantity = parseInt(labelQuantity.value) || 1;
    const type = labelType.value;

    if (quantity < 1) {
        alert('A quantidade deve ser de no mínimo 1.');
        return;
    }

    let endpoint = '';
    let payload = {};

    if (appState.mode === 'SIMPLE') {
        const text = labelText.value.trim();
        const sector = labelSector.value.trim();

        if (!text) {
            alert('O nome do colaborador não pode estar vazio.');
            return;
        }

        endpoint = '/print';
        // GARANTIA: Enviamos o texto (nome curto) e o setor que está na tela
        payload = {
            text: text,
            sector: sector,
            quantity: quantity,
            labelType: type
        };
    }
    // Outros modos (Validade/Consumo) seguem aqui...

    sendRequest(endpoint, payload);
}

// =========================================================================
// COMUNICAÇÃO COM O SERVIDOR
// =========================================================================
async function sendRequest(endpoint, payload) {
    const btn = document.getElementById('printButton');
    const btnText = document.getElementById('printButtonText');

    // Feedback visual de carregamento
    btn.disabled = true;
    btnText.textContent = 'Enviando...';

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const result = await response.text();

        if (response.ok) {
            alert('Etiqueta enviada para a impressora!');
        } else {
            throw new Error(result || 'Erro no servidor.');
        }
    } catch (error) {
        console.error('Erro na impressão:', error);
        alert('Erro ao imprimir: ' + error.message);
    } finally {
        btn.disabled = false;
        btnText.textContent = 'IMPRIMIR AGORA';
    }
}

// =========================================================================
// INICIALIZAÇÃO DOS EVENTOS
// =========================================================================
document.addEventListener('DOMContentLoaded', () => {
    const printBtn = document.getElementById('printButton');
    if (printBtn) {
        printBtn.addEventListener('click', handlePrintAction);
    }

    // Gerenciamento de Modos (Simples, Validade, Consumo)
    const btnSimple = document.getElementById('btnModeSimple');
    const btnValidity = document.getElementById('btnModeValidity');
    const btnImmediate = document.getElementById('btnModeImmediate');

    btnSimple?.addEventListener('click', () => switchMode('SIMPLE'));
    btnValidity?.addEventListener('click', () => switchMode('VALIDITY'));
    btnImmediate?.addEventListener('click', () => switchMode('IMMEDIATE_CONSUMPTION'));
});

function switchMode(mode) {
    appState.mode = mode;
    const simpleForm = document.getElementById('simpleForm');
    const validityForm = document.getElementById('validityForm');
    const immediateForm = document.getElementById('immediateForm');

    // Esconde todos e mostra o selecionado
    [simpleForm, validityForm, immediateForm].forEach(f => f?.classList.add('hidden'));

    if (mode === 'SIMPLE') simpleForm?.classList.remove('hidden');
    // Adicionar lógica para outros forms conforme necessário
}