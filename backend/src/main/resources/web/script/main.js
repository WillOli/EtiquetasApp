/**
 * @file main.js
 * @description Lógica principal para a aplicação de impressão de etiquetas do Espaço Vista.
 */

const API_BASE_URL = 'http://localhost:8081';

const appState = {
    mode: 'SIMPLE', // 'SIMPLE', 'VALIDITY', 'IMMEDIATE_CONSUMPTION' ou 'PRODUCTION'
};

// =========================================================================
// LISTAS DE EXIBIÇÃO E AUTOCOMPLETE OTIMIZADO (SEPARADAS POR ABA)
// =========================================================================

const VALIDITY_DISPLAY_LIST = [
    "Isca de Filé", "Roast beef", "Parmê de Filé", "Filé surprise",
    "Isca de Frango", "Frango grelhado", "Parmê de Frango", "Frango empanado",
    "Salmão posta", "Hambúrguer salmão 100g", "Hambúrguer salmão 50g",
    "Hambúrguer 160g", "Hambúrguer 140g", "Hambúrguer 100g", "Hambúrguer 50g",
    "Hambúrguer frango 100g", "Hambúrguer frango 50g",
];

const SIMPLE_DISPLAY_LIST = [
    "Brownie", "Bolo de coco", "Cheesescake", "Pão de mel",
    "Chargito", "Surpresa Nutella", "Surpresa Cookie", "Pavê doce de leite",
    "Mousse Chocolate", "Cookie Nutella", "Cookie Red", "Cookie Tradicional"
];

const IMMEDIATE_DISPLAY_LIST = [
    "Alho poró", "Chips de batata doce", "Crocante de batata doce",
    "Croutons", "Farofa de bacon", "Farofa crocante", "Molho de ervas",
    "Molho especial", "Mostarda e mel", "Parmesão", "Suco de laranja",
];

function normalizeString(str) {
    return str.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
}

function createSearchMap(displayList) {
    const searchMap = new Map();
    displayList.forEach(product => {
        const searchKey = normalizeString(product);
        searchMap.set(searchKey, product);
        searchMap.set(product.toLowerCase(), product);
    });
    return searchMap;
}

const VALIDITY_SEARCH_MAP = createSearchMap(VALIDITY_DISPLAY_LIST);
const SIMPLE_SEARCH_MAP = createSearchMap(SIMPLE_DISPLAY_LIST);
const IMMEDIATE_SEARCH_MAP = createSearchMap(IMMEDIATE_DISPLAY_LIST);

function setupAutocomplete(inputId, listId, sourceMap) {
    const inputElement = document.getElementById(inputId);
    const suggestionsList = document.getElementById(listId);

    if (!inputElement || !suggestionsList || sourceMap.size === 0) return;

    const renderSuggestions = () => {
        const query = normalizeString(inputElement.value);
        suggestionsList.innerHTML = '';

        if (query.length === 0 && inputElement !== document.activeElement) {
            suggestionsList.classList.add('hidden');
            return;
        }

        const suggestedProducts = new Set();
        sourceMap.forEach((originalName, searchKey) => {
            if (searchKey.includes(query)) {
                suggestedProducts.add(originalName);
            }
        });

        const filtered = Array.from(suggestedProducts);

        if (filtered.length > 0) {
            filtered.forEach(product => {
                const li = document.createElement('li');
                li.className = 'p-2 cursor-pointer hover:bg-gray-100 text-gray-800 text-sm';
                li.textContent = product;

                li.addEventListener('click', () => {
                    inputElement.value = product;
                    suggestionsList.classList.add('hidden');
                    inputElement.dispatchEvent(new Event('input'));
                });

                suggestionsList.appendChild(li);
            });
            suggestionsList.classList.remove('hidden');
        } else {
            suggestionsList.classList.add('hidden');
        }
    };

    inputElement.addEventListener('input', renderSuggestions);
    inputElement.addEventListener('focus', renderSuggestions);

    document.addEventListener('click', (event) => {
        if (!inputElement.contains(event.target) && !suggestionsList.contains(event.target)) {
            suggestionsList.classList.add('hidden');
        }
    });
}

function formatDateToBR(dateStr) {
    if (!dateStr) return "";
    const parts = dateStr.split('-');
    if (parts.length === 3) {
        return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    return dateStr;
}

// =========================================================================
// INICIALIZAÇÃO DA INTERFACE
// =========================================================================

document.addEventListener('DOMContentLoaded', () => {
    setupAutocomplete('productName', 'productSuggestions', VALIDITY_SEARCH_MAP);
    setupAutocomplete('immediateProductName', 'immediateProductSuggestions', IMMEDIATE_SEARCH_MAP);
    setupAutocomplete('labelText', 'simpleProductSuggestions', SIMPLE_SEARCH_MAP);

    const ui = {
        btnModeSimple: document.getElementById('btnModeSimple'),
        btnModeValidity: document.getElementById('btnModeValidity'),
        btnModeImmediate: document.getElementById('btnModeImmediate'),
        btnModeProduction: document.getElementById('btnModeProduction'),

        simpleForm: document.getElementById('simpleForm'),
        validityForm: document.getElementById('validityForm'),
        immediateForm: document.getElementById('immediateForm'),
        producaoSection: document.getElementById('producao-section'),

        labelText: document.getElementById('labelText'),
        labelSetor: document.getElementById('labelSetor'),
        labelFabDate: document.getElementById('labelFabDate'),
        labelValDate: document.getElementById('labelValDate'),

        productName: document.getElementById('productName'),
        mfgDate: document.getElementById('mfgDate'),
        validityDays: document.getElementById('validityDays'),
        validityDropdownBtn: document.getElementById('validityDropdownBtn'),
        validityDropdownPanel: document.getElementById('validityDropdownPanel'),
        validityUnitLabel: document.getElementById('validityUnitLabel'),

        immediateProductName: document.getElementById('immediateProductName'),

        labelQuantity: document.getElementById('labelQuantity'),
        labelType: document.getElementById('labelType'),
        duplicateInfoText: document.getElementById('duplicate-info-text'),
        printButton: document.getElementById('printButton'),
        printButtonText: document.getElementById('printButtonText'),
        spinner: document.getElementById('spinner'),

        prodProductName: document.getElementById('prod-product-name'),
        prodDataPrep: document.getElementById('prod-data-prep'),
        prodDataVal: document.getElementById('prod-data-val'),
        prodHoraPrep: document.getElementById('prod-hora-prep'),
        prodHoraDesc: document.getElementById('prod-hora-desc'),
    };

    attachEventListeners(ui);
    setupInitialState(ui);
});

function setupInitialState(ui) {
    const hojeStr = new Date().toISOString().split('T')[0];

    if (ui.mfgDate) ui.mfgDate.value = hojeStr;
    if (ui.labelFabDate) ui.labelFabDate.value = hojeStr;
    if (ui.labelValDate) ui.labelValDate.value = hojeStr;
    if (ui.prodDataPrep) ui.prodDataPrep.value = hojeStr;
    if (ui.prodDataVal) ui.prodDataVal.value = hojeStr;

    populateValidityDropdown([1, 2, 3, 5, 7, 10, 15, 30], ui);
    updateDuplicateInfo(ui);
    updateValidityUnitLabel(ui);
    switchMode('SIMPLE', ui);
}

function populateValidityDropdown(daysArray, ui) {
    if (!ui.validityDropdownPanel) return;
    ui.validityDropdownPanel.innerHTML = '';
    daysArray.forEach(days => {
        const div = document.createElement('div');
        div.className = 'p-2 cursor-pointer hover:bg-gray-100 text-sm text-gray-700';
        div.textContent = `${days} ${days === 1 ? 'dia' : 'dias'}`;
        div.addEventListener('click', () => {
            if (ui.validityDays) {
                ui.validityDays.value = days;
                updateValidityUnitLabel(ui);
            }
            ui.validityDropdownPanel.classList.add('hidden');
        });
        ui.validityDropdownPanel.appendChild(div);
    });
}

function attachEventListeners(ui) {
    ui.btnModeSimple?.addEventListener('click', () => switchMode('SIMPLE', ui));
    ui.btnModeValidity?.addEventListener('click', () => switchMode('VALIDITY', ui));
    ui.btnModeImmediate?.addEventListener('click', () => switchMode('IMMEDIATE_CONSUMPTION', ui));
    ui.btnModeProduction?.addEventListener('click', () => switchMode('PRODUCTION', ui));

    ui.validityDropdownBtn?.addEventListener('click', (e) => {
        e.stopPropagation();
        ui.validityDropdownPanel?.classList.toggle('hidden');
    });
    document.addEventListener('click', () => {
        ui.validityDropdownPanel?.classList.add('hidden');
    });

    ui.validityDays?.addEventListener('input', () => updateValidityUnitLabel(ui));
    ui.labelQuantity?.addEventListener('input', () => updateDuplicateInfo(ui));
    ui.labelType?.addEventListener('change', () => updateDuplicateInfo(ui));

    ui.printButton?.addEventListener('click', () => handlePrintAction(ui));
}

function updateValidityUnitLabel(ui) {
    if (!ui.validityDays || !ui.validityUnitLabel) return;
    const days = Number(ui.validityDays.value);
    ui.validityUnitLabel.textContent = (days === 1) ? 'dia' : 'dias';
}

function switchMode(mode, ui) {
    appState.mode = mode;
    const hojeStr = new Date().toISOString().split('T')[0];

    if (ui.labelText) ui.labelText.value = '';
    if (ui.labelSetor) ui.labelSetor.value = 'CONFEITARIA';
    if (ui.labelFabDate) ui.labelFabDate.value = hojeStr;
    if (ui.labelValDate) ui.labelValDate.value = hojeStr;

    if (ui.productName) ui.productName.value = '';
    if (ui.mfgDate) ui.mfgDate.value = hojeStr;
    if (ui.validityDays) {
        ui.validityDays.value = 1;
        updateValidityUnitLabel(ui);
    }
    if (ui.immediateProductName) ui.immediateProductName.value = '';
    if (ui.immediateFabDate) ui.immediateFabDate.value = hojeStr;
    if (ui.immediateValDate) ui.immediateValDate.value = '';

    if (ui.prodProductName) ui.prodProductName.value = '';
    if (ui.prodDataPrep) ui.prodDataPrep.value = hojeStr;
    if (ui.prodDataVal) ui.prodDataVal.value = hojeStr;
    if (ui.prodHoraPrep) ui.prodHoraPrep.value = '';
    if (ui.prodHoraDesc) ui.prodHoraDesc.value = '';

    document.getElementById('productSuggestions')?.classList.add('hidden');
    document.getElementById('immediateProductSuggestions')?.classList.add('hidden');
    document.getElementById('simpleProductSuggestions')?.classList.add('hidden');

    [ui.btnModeSimple, ui.btnModeValidity, ui.btnModeImmediate, ui.btnModeProduction].forEach(btn => {
        if (btn) {
            btn.classList.remove('btn-primary');
            btn.classList.add('btn-secondary');
        }
    });

    ui.simpleForm?.classList.add('hidden');
    ui.validityForm?.classList.add('hidden');
    ui.immediateForm?.classList.add('hidden');
    ui.producaoSection?.classList.add('hidden');

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
    } else if (mode === 'PRODUCTION') {
        ui.btnModeProduction?.classList.remove('btn-secondary');
        ui.btnModeProduction?.classList.add('btn-primary');
        ui.producaoSection?.classList.remove('hidden');
    }

    if (mode === 'PRODUCTION') {
        ui.labelType.value = 'SIXTY_TWO_MM';
        ui.labelType.disabled = true;
        ui.labelType.classList.add('bg-gray-100', 'text-gray-500', 'cursor-not-allowed');
    } else {
        ui.labelType.disabled = false;
        ui.labelType.classList.remove('bg-gray-100', 'text-gray-500', 'cursor-not-allowed');
    }

    // Atualiza o texto que informa a quantidade impressa para refletir a mudança
    updateDuplicateInfo(ui);
}

function updateDuplicateInfo(ui) {
    if (!ui.labelQuantity || !ui.labelType || !ui.duplicateInfoText) return;
    const quantity = parseInt(ui.labelQuantity.value) || 0;
    const isStandard = ui.labelType.value === 'STANDARD';
    const totalPrinted = isStandard ? quantity * 2 : quantity;
    ui.duplicateInfoText.textContent = `Quantidade solicitada: ${quantity}, Total impresso: ${totalPrinted}`;
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
        const setor = ui.labelSetor.value.trim() || "CONFEITARIA";
        const fabInput = document.getElementById('labelFabDate');
        const valInput = document.getElementById('labelValDate');

        if (fabInput?.value && valInput?.value && valInput.value < fabInput.value) {
            showModal('A data de validade não pode ser anterior à data de fabricação.', 'error');
            return;
        }

        const dataFabricacao = fabInput ? formatDateToBR(fabInput.value) : "";
        const dataValidade = valInput ? formatDateToBR(valInput.value) : "";

        if (!text) {
            showModal('O nome do produto não pode estar vazio.', 'error');
            return;
        }
        if (!dataFabricacao) {
            showModal('A data de fabricação é obrigatória.', 'error');
            return;
        }
        if (!dataValidade) {
            showModal('A data de validade é obrigatória.', 'error');
            return;
        }

        const registro = "00001";
        endpoint = '/print';
        payload = {
            text: text,
            setor: setor,
            dataFabricacao: dataFabricacao,
            dataValidade: dataValidade,
            registro: registro,
            mfgDate: dataFabricacao,
            validityDate: dataValidade,
            dataFab: dataFabricacao,
            dataVal: dataValidade,
            lote: registro,
            quantity: quantity,
            labelType: labelType
        };

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
        const fabInput = document.getElementById('immediateFabDate');
        const valInput = document.getElementById('immediateValDate');

        if (fabInput?.value && valInput?.value && valInput.value < fabInput.value) {
            showModal('A data de validade não pode ser anterior à data de fabricação.', 'error');
            return;
        }

        const dataFabricacao = fabInput ? formatDateToBR(fabInput.value) : "";
        const dataValidade = valInput ? formatDateToBR(valInput.value) : "";

        if (!productName) {
            showModal('O nome do produto não pode estar vazio.', 'error');
            return;
        }

        endpoint = '/print-consumo-imediato';
        payload = {
            productName: productName,
            dataFabricacao: dataFabricacao,
            validade: dataValidade,
            quantity: quantity,
            labelType: labelType
        };

    } else if (appState.mode === 'PRODUCTION') {
        const productName = ui.prodProductName?.value.trim();
        const dataPreparacao = ui.prodDataPrep?.value;
        const dataValidade = ui.prodDataVal?.value;
        const horarioPreparo = ui.prodHoraPrep?.value;
        const horarioDescarte = ui.prodHoraDesc?.value;

        if (!productName) {
            showModal('O nome do produto não pode estar vazio.', 'error');
            return;
        }
        if (!dataPreparacao) {
            showModal('A data de preparação é obrigatória.', 'error');
            return;
        }
        if (!dataValidade) {
            showModal('A data de validade é obrigatória.', 'error');
            return;
        }
        if (!horarioPreparo) {
            showModal('O horário de preparo é obrigatório.', 'error');
            return;
        }
        if (!horarioDescarte) {
            showModal('O horário de descarte é obrigatório.', 'error');
            return;
        }

        endpoint = '/print-producao';
        payload = {
            productName: productName,
            dataPreparacao: dataPreparacao,
            horarioPreparo: horarioPreparo,
            horarioDescarte: horarioDescarte,
            dataValidade: dataValidade,
            quantity: quantity,
            labelType: labelType
        };
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

    if (window.modalTimeout) {
        clearTimeout(window.modalTimeout);
    }

    const bgColor = type === 'success' ? 'bg-green-500' : 'bg-red-500';
    const modalHTML = `
        <div id="alertModalContainer" class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
            <div class="bg-white p-6 rounded-lg shadow-xl max-w-sm w-full text-center">
                <div class="${bgColor} text-white p-3 rounded-full w-12 h-12 flex items-center justify-center mx-auto mb-4 font-bold text-xl">
                    ${type === 'success' ? '✓' : '✕'}
                </div>
                <p class="text-gray-800 text-base mb-4 font-medium">${message}</p>
                <button onclick="fecharModalManual()" class="bg-gray-800 text-white px-4 py-2 rounded hover:bg-gray-700 w-full font-bold">
                    OK
                </button>
            </div>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', modalHTML);

    window.fecharModalManual = () => {
        if (window.modalTimeout) {
            clearTimeout(window.modalTimeout);
        }
        document.getElementById('alertModalContainer')?.remove();
    };

    if (type === 'success') {
        window.modalTimeout = setTimeout(() => {
            document.getElementById('alertModalContainer')?.remove();
        }, 3000);
    }
}