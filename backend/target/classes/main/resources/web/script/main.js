/**
 * @file main.js
 * @description Lógica principal para a aplicação de impressão de etiquetas do Espaço Vista.
 */

const API_BASE_URL = 'http://localhost:8081';

const appState = {
    mode: 'SIMPLE', // 'SIMPLE' ou 'VALIDITY'
};

document.addEventListener('DOMContentLoaded', () => {
    console.log("DEBUG: O DOM foi carregado. A iniciar a aplicação.");

    const ui = {
        btnModeSimple: document.getElementById('btnModeSimple'),
        btnModeValidity: document.getElementById('btnModeValidity'),
        simpleForm: document.getElementById('simpleForm'),
        validityForm: document.getElementById('validityForm'),
        labelText: document.getElementById('labelText'),
        numericButtons: document.querySelectorAll('.btn-numeric'),
        btnSpecial: document.getElementById('btn-special'),
        btnClear: document.getElementById('btn-clear'),
        productName: document.getElementById('productName'),
        mfgDate: document.getElementById('mfgDate'),
        validityDays: document.getElementById('validityDays'),
        validityDropdownBtn: document.getElementById('validityDropdownBtn'),
        validityDropdownPanel: document.getElementById('validityDropdownPanel'),
        labelQuantity: document.getElementById('labelQuantity'),
        labelType: document.getElementById('labelType'),
        duplicateInfoText: document.getElementById('duplicate-info-text'),
        printButton: document.getElementById('printButton'),
        printButtonText: document.getElementById('printButtonText'),
        spinner: document.getElementById('spinner'),
    };

    // O bloco de verificação de erros foi removido para permitir que o navegador reporte o erro exato.

    try {
        attachEventListeners(ui);
        setupInitialState(ui);
        console.log("DEBUG: Aplicação inicializada com sucesso.");
    } catch (error) {
        console.error("ERRO FATAL DURANTE A INICIALIZAÇÃO:", error);
        showModal(`Erro fatal ao iniciar a aplicação: ${error.message}. Verifique o console.`, 'error');
    }
});

function setupInitialState(ui) {
    ui.mfgDate.value = new Date().toISOString().split('T')[0];
    populateValidityDropdown([1, 2, 3, 5, 7, 10, 15, 30], ui);
    updateDuplicateInfo(ui);
    switchMode('SIMPLE', ui);
}

function attachEventListeners(ui) {
    ui.btnModeSimple.addEventListener('click', () => switchMode('SIMPLE', ui));
    ui.btnModeValidity.addEventListener('click', () => switchMode('VALIDITY', ui));

    ui.numericButtons.forEach(button => {
        button.addEventListener('click', () => {
            ui.labelText.value += button.dataset.value;
        });
    });
    ui.btnSpecial.addEventListener('click', () => {
        if (!ui.labelText.value.includes(' - ESPECIAL ')) {
            ui.labelText.value += ' - ESPECIAL ';
        }
    });
    ui.btnClear.addEventListener('click', () => {
        ui.labelText.value = '';
    });

    ui.validityDropdownBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        ui.validityDropdownPanel.classList.toggle('hidden');
    });
    document.addEventListener('click', () => {
        ui.validityDropdownPanel.classList.add('hidden');
    });

    ui.labelQuantity.addEventListener('input', () => updateDuplicateInfo(ui));
    ui.labelType.addEventListener('change', () => updateDuplicateInfo(ui));

    ui.printButton.addEventListener('click', () => handlePrintAction(ui));
}

function switchMode(mode, ui) {
    appState.mode = mode;
    const isSimpleMode = mode === 'SIMPLE';

    ui.simpleForm.classList.toggle('hidden', !isSimpleMode);
    ui.validityForm.classList.toggle('hidden', isSimpleMode);

    ui.btnModeSimple.classList.toggle('btn-primary', isSimpleMode);
    ui.btnModeSimple.classList.toggle('btn-secondary', !isSimpleMode);

    ui.btnModeValidity.classList.toggle('btn-primary', !isSimpleMode);
    ui.btnModeValidity.classList.toggle('btn-secondary', isSimpleMode);
}

function updateDuplicateInfo(ui) {
    const quantity = parseInt(ui.labelQuantity.value) || 0;
    const isStandard = ui.labelType.value === 'STANDARD';
    const totalPrinted = isStandard ? quantity * 2 : quantity;
    ui.duplicateInfoText.textContent = `Quantidade solicitada: ${quantity}, Total impresso: ${totalPrinted}`;
}

function populateValidityDropdown(daysArray, ui) {
    ui.validityDropdownPanel.innerHTML = '';
    daysArray.forEach(day => {
        const item = document.createElement('a');
        item.href = '#';
        item.textContent = `${day} dias`;
        item.className = 'block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer';
        item.addEventListener('click', (e) => {
            e.preventDefault();
            ui.validityDays.value = day;
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
    } else { // 'VALIDITY'
        const productName = ui.productName.value.trim();
        const mfgDate = ui.mfgDate.value;
        const validityDays = parseInt(ui.validityDays.value) || 0;

        if (!productName) {
            showModal('O nome do produto não pode estar vazio.', 'error');
            return;
        }
        if (!mfgDate) {
            showModal('A data de fabricação é obrigatória.', 'error');
            return;
        }
        if (validityDays <= 0) {
            showModal('O prazo de validade deve ser maior que zero.', 'error');
            return;
        }
        endpoint = '/print-validade';
        payload = { productName, mfgDate, validityDays, quantity, labelType };
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
    ui.printButton.disabled = isLoading;
    ui.printButton.classList.toggle('opacity-70', isLoading);
    ui.printButton.classList.toggle('cursor-not-allowed', isLoading);
    ui.spinner.classList.toggle('hidden', !isLoading);
    ui.printButtonText.textContent = isLoading ? 'Imprimindo...' : 'Imprimir Etiquetas';
}

function showModal(message, type = 'success') {
    document.getElementById('alertModalContainer')?.remove();

    const bgColor = type === 'success' ? 'bg-green-100' : 'bg-red-100';
    const borderColor = type === 'success' ? 'border-green-400' : 'border-red-400';
    const textColor = type === 'success' ? 'text-green-700' : 'text-red-700';

    const modalHTML = `
        <div id="alertModalContainer" style="position: fixed; top: 1.25rem; right: 1.25rem; z-index: 50;">
            <div style="max-width: 24rem; margin-left: auto; margin-right: auto; border-radius: 0.5rem; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -2px rgba(0,0,0,0.05);" class="${bgColor} border ${borderColor}">
                <div style="display: flex; align-items: center; padding: 1rem;">
                    <p style="font-weight: 500;" class="${textColor}">${message}</p>
                    <button id="closeModalButton" style="margin-left: 1rem; padding: 0.25rem; border-radius: 0.375rem; background-color: transparent; border: none; font-size: 1.25rem; line-height: 1; cursor: pointer;">&times;</button>
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHTML);

    const modalContainer = document.getElementById('alertModalContainer');
    const closeButton = document.getElementById('closeModalButton');

    const closeModal = () => modalContainer?.remove();

    closeButton.addEventListener('click', closeModal);
    setTimeout(closeModal, 4000);
}
