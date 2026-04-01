/**
 * @file main.js
 * @description Lógica principal para a aplicação de impressão de etiquetas do Espaço Vista.
 */

const API_BASE_URL = 'http://localhost:8081';

const appState = {
    mode: 'SIMPLE', // 'SIMPLE', 'VALIDITY' ou 'IMMEDIATE_CONSUMPTION'
};

// 1. LISTAS DE EXIBIÇÃO PARA VALIDADE E CONSUMO IMEDIATO
const VALIDITY_DISPLAY_LIST = ["Isca de Filé", "Roast beef", "Parmê de Filé", "Filé surprise", "Isca de Frango", "Frango grelhado", "Salmão posta", "Hambúrguer 160g", "Hambúrguer 100g"];
const IMMEDIATE_DISPLAY_LIST = ["Bolo de coco", "Brownie", "Chargito", "Cheesecake", "Cookie de Nutella", "Mousse de chocolate", "Pão de mel", "Chips de batata doce", "Croutons", "Suco de laranja"];

function normalizeString(str) {
    return str.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
}

function createSearchMap(displayList) {
    const searchMap = new Map();
    displayList.forEach(product => {
        searchMap.set(normalizeString(product), product);
        searchMap.set(product.toLowerCase(), product);
    });
    return searchMap;
}

const VALIDITY_SEARCH_MAP = createSearchMap(VALIDITY_DISPLAY_LIST);
const IMMEDIATE_SEARCH_MAP = createSearchMap(IMMEDIATE_DISPLAY_LIST);

// =========================================================================
// FUNÇÃO DE AUTOCOMPLETE
// =========================================================================

function setupAutocomplete(inputId, listId, sourceMap) {
    const inputElement = document.getElementById(inputId);
    const suggestionsList = document.getElementById(listId);
    if (!inputElement || !suggestionsList) return;

    const renderSuggestions = () => {
        const query = normalizeString(inputElement.value);
        suggestionsList.innerHTML = '';
        if (query.length === 0) { suggestionsList.classList.add('hidden'); return; }
        const suggested = new Set();
        sourceMap.forEach((originalName, searchKey) => {
            if (searchKey.includes(query)) suggested.add(originalName);
        });
        const filtered = Array.from(suggested);
        if (filtered.length > 0) {
            filtered.forEach(product => {
                const li = document.createElement('li');
                li.className = 'p-2 cursor-pointer hover:bg-gray-100 text-gray-800 text-sm';
                li.textContent = product;
                li.onclick = () => { inputElement.value = product; suggestionsList.classList.add('hidden'); };
                suggestionsList.appendChild(li);
            });
            suggestionsList.classList.remove('hidden');
        } else { suggestionsList.classList.add('hidden'); }
    };
    inputElement.addEventListener('input', renderSuggestions);
}

// =========================================================================
// INICIALIZAÇÃO
// =========================================================================

document.addEventListener('DOMContentLoaded', () => {
    setupAutocomplete('productName', 'productSuggestions', VALIDITY_SEARCH_MAP);
    setupAutocomplete('immediateProductName', 'immediateProductSuggestions', IMMEDIATE_SEARCH_MAP);

    const ui = {
        btnModeSimple: document.getElementById('btnModeSimple'),
        btnModeValidity: document.getElementById('btnModeValidity'),
        btnModeImmediate: document.getElementById('btnModeImmediate'),
        simpleForm: document.getElementById('simpleForm'),
        validityForm: document.getElementById('validityForm'),
        immediateForm: document.getElementById('immediateForm'),
        labelText: document.getElementById('labelText'),
        labelSector: document.getElementById('labelSector'), // CAMPO DO SETOR
        productName: document.getElementById('productName'),
        mfgDate: document.getElementById('mfgDate'),
        validityDays: document.getElementById('validityDays'),
        labelQuantity: document.getElementById('labelQuantity'),
        labelType: document.getElementById('labelType'),
        printButton: document.getElementById('printButton'),
        printButtonText: document.getElementById('printButtonText'),
        spinner: document.getElementById('spinner'),
        duplicateInfoText: document.getElementById('duplicate-info-text'),
        numericButtons: document.querySelectorAll('.btn-numeric'),
        btnSpecial: document.getElementById('btn-special'),
        btnClear: document.getElementById('btn-clear'),
        validityDropdownBtn: document.getElementById('validityDropdownBtn'),
        validityDropdownPanel: document.getElementById('validityDropdownPanel'),
        validityUnitLabel: document.getElementById('validityUnitLabel'),
    };

    attachEventListeners(ui);
    setupInitialState(ui);
});

function setupInitialState(ui) {
    if (ui.mfgDate) ui.mfgDate.value = new Date().toISOString().split('T')[0];
    switchMode('SIMPLE', ui);
}

function attachEventListeners(ui) {
    ui.btnModeSimple?.addEventListener('click', () => switchMode('SIMPLE', ui));
    ui.btnModeValidity?.addEventListener('click', () => switchMode('VALIDITY', ui));
    ui.btnModeImmediate?.addEventListener('click', () => switchMode('IMMEDIATE_CONSUMPTION', ui));
    ui.printButton?.addEventListener('click', () => handlePrintAction(ui));

    // Listeners Numéricos (Etiqueta Simples)
    ui.numericButtons.forEach(button => {
        button.addEventListener('click', () => { if (ui.labelText) ui.labelText.value += button.dataset.value; });
    });
    ui.btnClear?.addEventListener('click', () => { if (ui.labelText) ui.labelText.value = ''; });
}

function switchMode(mode, ui) {
    appState.mode = mode;
    [ui.simpleForm, ui.validityForm, ui.immediateForm].forEach(f => f?.classList.add('hidden'));
    if (mode === 'SIMPLE') ui.simpleForm?.classList.remove('hidden');
    else if (mode === 'VALIDITY') ui.validityForm?.classList.remove('hidden');
    else if (mode === 'IMMEDIATE_CONSUMPTION') ui.immediateForm?.classList.remove('hidden');
}

// =========================================================================
// FUNÇÃO DE IMPRESSÃO (CORREÇÃO DO SETOR E NOME CURTO)
// =========================================================================

function handlePrintAction(ui) {
    const quantity = parseInt(ui.labelQuantity.value) || 1;
    const labelType = ui.labelType.value;
    let endpoint = '';
    let payload = {};

    if (appState.mode === 'SIMPLE') {
        const text = ui.labelText.value.trim();
        const sector = ui.labelSector.value.trim(); // CAPTURA O SETOR

        if (!text) {
            alert('O texto da etiqueta não pode estar vazio.');
            return;
        }

        endpoint = '/print';
        // O PAYLOAD AGORA INCLUI O SECTOR
        payload = { text, sector, quantity, labelType };
    } else if (appState.mode === 'VALIDITY') {
        endpoint = '/print-validade';
        payload = {
            productName: ui.productName.value,
            mfgDate: ui.mfgDate.value,
            validityDays: parseInt(ui.validityDays.value),
            quantity,
            labelType
        };
    } else if (appState.mode === 'IMMEDIATE_CONSUMPTION') {
        endpoint = '/print-consumo-imediato';
        payload = { productName: ui.immediateProductName.value, quantity, labelType };
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
        if (response.ok) alert("Impressão enviada com sucesso!");
    } catch (error) {
        console.error("Erro:", error);
    } finally {
        setButtonLoading(false, ui);
    }
}

function setButtonLoading(isLoading, ui) {
    if (!ui.printButton) return;
    ui.printButton.disabled = isLoading;
    ui.spinner?.classList.toggle('hidden', !isLoading);
    ui.printButtonText.textContent = isLoading ? 'Imprimindo...' : 'Imprimir Etiquetas';
}