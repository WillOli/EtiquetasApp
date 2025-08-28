/**
 * @file main.js
 * @description Lógica principal para a aplicação de impressão de etiquetas do Espaço Vista.
 */

const API_BASE_URL = 'http://localhost:8081';

const appState = {
    mode: 'SIMPLE', // 'SIMPLE', 'VALIDITY' ou 'IMMEDIATE_CONSUMPTION'
};

document.addEventListener('DOMContentLoaded', () => {
    const ui = {
        btnModeSimple: document.getElementById('btnModeSimple'),
        btnModeValidity: document.getElementById('btnModeValidity'),
        btnModeImmediate: document.getElementById('btnModeImmediate'),
        simpleForm: document.getElementById('simpleForm'),
        validityForm: document.getElementById('validityForm'),
        immediateForm: document.getElementById('immediateForm'),
        immediateProductName: document.getElementById('immediateProductName'),
        labelText: document.getElementById('labelText'),
        numericButtons: document.querySelectorAll('.btn-numeric'),
        btnSpecial: document.getElementById('btn-special'),
        btnClear: document.getElementById('btn-clear'),
        productName: document.getElementById('productName'),
        mfgDate: document.getElementById('mfgDate'),
        validityDays: document.getElementById('validityDays'),
        validityDropdownBtn: document.getElementById('validityDropdownBtn'),
        validityDropdownPanel: document.getElementById('validityDropdownPanel'),
        validityUnitLabel: document.getElementById('validityUnitLabel'), // Seletor para o rótulo "dia/dias"
        labelQuantity: document.getElementById('labelQuantity'),
        labelType: document.getElementById('labelType'),
        duplicateInfoText: document.getElementById('duplicate-info-text'),
        printButton: document.getElementById('printButton'),
        printButtonText: document.getElementById('printButtonText'),
        spinner: document.getElementById('spinner'),
    };

    attachEventListeners(ui);
    setupInitialState(ui);
});

function setupInitialState(ui) {
    if (ui.mfgDate) {
        ui.mfgDate.value = new Date().toISOString().split('T')[0];
    }
    populateValidityDropdown([1, 2, 3, 5, 7, 10, 15, 30], ui);
    updateDuplicateInfo(ui);
    updateValidityUnitLabel(ui); // Chama a função para definir o estado inicial
    switchMode('SIMPLE', ui);
}

function attachEventListeners(ui) {
    ui.btnModeSimple?.addEventListener('click', () => switchMode('SIMPLE', ui));
    ui.btnModeValidity?.addEventListener('click', () => switchMode('VALIDITY', ui));
    ui.btnModeImmediate?.addEventListener('click', () => switchMode('IMMEDIATE_CONSUMPTION', ui));

    ui.numericButtons.forEach(button => {
        button.addEventListener('click', () => {
            if (ui.labelText) ui.labelText.value += button.dataset.value;
        });
    });
    ui.btnSpecial?.addEventListener('click', () => {
        if (ui.labelText && !ui.labelText.value.includes(' - ESPECIAL ')) {
            ui.labelText.value += ' - ESPECIAL ';
        }
    });
    ui.btnClear?.addEventListener('click', () => {
        if (ui.labelText) ui.labelText.value = '';
    });

    ui.validityDropdownBtn?.addEventListener('click', (e) => {
        e.stopPropagation();
        ui.validityDropdownPanel?.classList.toggle('hidden');
    });
    document.addEventListener('click', () => {
        ui.validityDropdownPanel?.classList.add('hidden');
    });

    // Listener para atualizar o rótulo "dia/dias" quando o utilizador digita
    ui.validityDays?.addEventListener('input', () => updateValidityUnitLabel(ui));

    ui.labelQuantity?.addEventListener('input', () => updateDuplicateInfo(ui));
    ui.labelType?.addEventListener('change', () => updateDuplicateInfo(ui));

    ui.printButton?.addEventListener('click', () => handlePrintAction(ui));
}

/**
 * Atualiza o texto "dia" ou "dias" com base no valor do input.
 * @param {object} ui - O objeto contendo referências aos elementos da UI.
 */
function updateValidityUnitLabel(ui) {
    if (!ui.validityDays || !ui.validityUnitLabel) return; // Verificação de segurança

    const days = Number(ui.validityDays.value);
    // Se o valor for 1, mostra "dia". Para todos os outros casos (0, 2, 3...), mostra "dias".
    ui.validityUnitLabel.textContent = (days === 1) ? 'dia' : 'dias';
}

function switchMode(mode, ui) {
    appState.mode = mode;
    
    // Reset button states
    ui.btnModeSimple?.classList.remove('btn-primary');
    ui.btnModeSimple?.classList.add('btn-secondary');
    ui.btnModeValidity?.classList.remove('btn-primary');
    ui.btnModeValidity?.classList.add('btn-secondary');
    ui.btnModeImmediate?.classList.remove('btn-primary');
    ui.btnModeImmediate?.classList.add('btn-secondary');
    
    // Hide all forms
    ui.simpleForm?.classList.add('hidden');
    ui.validityForm?.classList.add('hidden');
    ui.immediateForm?.classList.add('hidden');
    
    if (mode === 'SIMPLE') {
        ui.btnModeSimple?.classList.remove('btn-secondary');
        ui.btnModeSimple?.classList.add('btn-primary');
        ui.simpleForm?.classList.remove('hidden');
    } else if (mode === 'VALIDITY') {
        ui.btnModeValidity?.classList.remove('btn-secondary');
        ui.btnModeValidity?.classList.add('btn-primary');
        ui.validityForm?.classList.remove('hidden');
    } else if (mode === 'IMMEDIATE_CONSUMPTION') {
        ui.btnModeImmediate?.classList.remove('btn-secondary');
        ui.btnModeImmediate?.classList.add('btn-primary');
        ui.immediateForm?.classList.remove('hidden');
    }
}

function updateDuplicateInfo(ui) {
    if (!ui.labelQuantity || !ui.labelType || !ui.duplicateInfoText) return;
    const quantity = parseInt(ui.labelQuantity.value) || 0;
    const isStandard = ui.labelType.value === 'STANDARD';
    const totalPrinted = isStandard ? quantity * 2 : quantity;
    ui.duplicateInfoText.textContent = `Quantidade solicitada: ${quantity}, Total impresso: ${totalPrinted}`;
}

function populateValidityDropdown(daysArray, ui) {
    if (!ui.validityDropdownPanel || !ui.validityDays) return;
    ui.validityDropdownPanel.innerHTML = '';
    daysArray.forEach(day => {
        const item = document.createElement('a');
        item.href = '#';
        item.textContent = (day === 1) ? `${day} dia` : `${day} dias`;
        item.className = 'block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer';
        item.addEventListener('click', (e) => {
            e.preventDefault();
            ui.validityDays.value = day;
            updateValidityUnitLabel(ui); // Atualiza o rótulo ao selecionar
            ui.validityDropdownPanel.classList.add('hidden');
        });
        ui.validityDropdownPanel.appendChild(item);
    });
}

function handlePrintAction(ui) {
    const quantity = parseInt(ui.labelQuantity.value) || 0;
    const labelType = ui.labelType.value;

    if (quantity < 1) {
        showModal('A quantidade deve ser de no mínimo 1.', 'error');
        return;
    }

    let endpoint = '';
    let payload = {};

    if (appState.mode === 'SIMPLE') {
        const text = ui.labelText.value.trim();
        if (!text) {
            showModal('O texto da etiqueta não pode estar vazio.', 'error');
            return;
        }
        endpoint = '/print';
        payload = { text, quantity, labelType };
    } else if (appState.mode === 'VALIDITY') {
        const productName = ui.productName.value.trim();
        const mfgDate = ui.mfgDate.value;
        const validityDays = parseInt(ui.validityDays.value);

        if (!productName) {
            showModal('O nome do produto não pode estar vazio.', 'error');
            return;
        }
        if (!mfgDate) {
            showModal('A data de fabricação é obrigatória.', 'error');
            return;
        }
        if (isNaN(validityDays) || validityDays < 0) {
            showModal('O prazo de validade deve ser um número igual ou maior que zero.', 'error');
            return;
        }
        endpoint = '/print-validade';
        payload = { productName, mfgDate, validityDays, quantity, labelType };
    } else if (appState.mode === 'IMMEDIATE_CONSUMPTION') {
        const productName = ui.immediateProductName.value.trim();
        
        if (!productName) {
            showModal('O nome do produto não pode estar vazio.', 'error');
            return;
        }
        endpoint = '/print-consumo-imediato';
        payload = { productName, quantity, labelType };
    }

    sendRequest(endpoint, payload, ui);
}

async function sendRequest(endpoint, payload, ui) {
    setButtonLoading(true, ui);
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const responseText = await response.text();
        if (!response.ok) {
            throw new Error(responseText || 'Erro desconhecido do servidor.');
        }
        showModal(responseText, 'success');
    } catch (error) {
        console.error(`Erro ao chamar ${endpoint}:`, error);
        showModal(`Erro na comunicação: ${error.message}`, 'error');
    } finally {
        setButtonLoading(false, ui);
    }
}

function setButtonLoading(isLoading, ui) {
    if (!ui.printButton) return;
    ui.printButton.disabled = isLoading;
    ui.printButton.classList.toggle('opacity-70', isLoading);
    ui.printButton.classList.toggle('cursor-not-allowed', isLoading);
    ui.spinner?.classList.toggle('hidden', !isLoading);
    ui.printButtonText.textContent = isLoading ? 'Imprimindo...' : 'Imprimir Etiquetas';
}

function showModal(message, type = 'success') {
    document.getElementById('alertModalContainer')?.remove();
    const modalHTML = `...`; // O código do modal permanece o mesmo
    document.body.insertAdjacentHTML('beforeend', modalHTML);
    // ...
}
