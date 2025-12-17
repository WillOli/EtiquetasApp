/**
 * @file main.js
 * @description Lógica principal para a aplicação de impressão de etiquetas do Espaço Vista.
 */

const API_BASE_URL = 'http://localhost:8081';

const appState = {
    mode: 'SIMPLE', // 'SIMPLE', 'VALIDITY' ou 'IMMEDIATE_CONSUMPTION'
};

// =========================================================================
// INÍCIO: NOVAS VARIÁVEIS E FUNÇÕES PARA AUTOCOMPLETE PROFISSIONAL (BUSCA OTIMIZADA)
// =========================================================================

// 1. LISTAS DE EXIBIÇÃO (Apenas nomes formatados que o usuário deve ver)
const VALIDITY_DISPLAY_LIST = [
    // Carne Bovina
    "Isca de Filé",
    "Roast beef",
    "Parmê de Filé",
    "Filé surprise",
    
    // Frango
    "Isca de Frango",
    "Frango grelhado",
    "Parmê de Frango",
    "Frango empanado",
    
    // Peixe
    "Salmão posta",
    "Hambúrguer salmão 100g",
    "Hambúrguer salmão 50g",
    
    // Hambúrgueres de Carne
    "Hambúrguer 160g",
    "Hambúrguer 140g",
    "Hambúrguer 100g",
    "Hambúrguer 50g",
    
    // Hambúrgueres de Frango
    "Hambúrguer frango 100g",
    "Hambúrguer frango 50g",
];

const IMMEDIATE_DISPLAY_LIST = [
    // Doces
    "Bolo de coco",
    "Brownie",
    "Chargito",
    "Cheesecake",
    "Cookie de Nutella",
    "Cookie red velvet",
    "Cookie tradicional",
    "Mousse de chocolate",
    "Pão de mel",
    "Pavê de doce de leite",
    "Surpresa cookie",
    "Surpresa de Nutella",
    
    // Salgados/Acompanhamentos/Bebidas
    "Alho poró",
    "Chips de batata doce",
    "Crocante de batata doce",
    "Croutons",
    "Farofa de bacon",
    "Farofa crocante",
    "Molho de ervas",
    "Molho especial",
    "Mostarda e mel",
    "Parmesão",
    "Suco de laranja",
];

/**
 * Normaliza uma string removendo acentos, cedilha e convertendo para minúsculas.
 * @param {string} str - String a ser normalizada.
 * @returns {string} String normalizada.
 */
function normalizeString(str) {
    return str.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
}

/**
 * Cria um Mapa de Busca onde a chave é o termo de busca simplificado 
 * e o valor é o nome original formatado.
 * @param {string[]} displayList - Lista de nomes formatados.
 * @returns {Map<string, string>} Mapa {termo_simplificado: nome_original}.
 */
function createSearchMap(displayList) {
    const searchMap = new Map();
    displayList.forEach(product => {
        // A chave de busca será a versão normalizada
        const searchKey = normalizeString(product); 
        // Armazenamos o nome original como valor
        searchMap.set(searchKey, product); 
        
        // Adiciona o nome original (minúsculo) como segunda chave de busca
        searchMap.set(product.toLowerCase(), product); 
    });
    return searchMap;
}

// 2. CRIA OS MAPAS DE BUSCA (Estes são usados na função setupAutocomplete)
const VALIDITY_SEARCH_MAP = createSearchMap(VALIDITY_DISPLAY_LIST);
const IMMEDIATE_SEARCH_MAP = createSearchMap(IMMEDIATE_DISPLAY_LIST);


// =========================================================================
// FUNÇÃO DE AUTOCOMPLETE (REVISADA: USA O MAPA DE BUSCA)
// =========================================================================

function setupAutocomplete(inputId, listId, sourceMap) { 
    const inputElement = document.getElementById(inputId);
    const suggestionsList = document.getElementById(listId);

    if (!inputElement || !suggestionsList || sourceMap.size === 0) return;

    // Função para filtrar e renderizar a lista
    const renderSuggestions = () => {
        const query = normalizeString(inputElement.value); // NORMALIZA A BUSCA DO USUÁRIO
        suggestionsList.innerHTML = '';
        
        // Oculta a lista se a busca estiver vazia e o foco não estiver no elemento
        if (query.length === 0 && inputElement !== document.activeElement) {
            suggestionsList.classList.add('hidden');
            return;
        }

        const suggestedProducts = new Set(); // Usamos um Set para garantir unicidade dos nomes
        
        // Itera sobre todas as chaves no mapa de busca
        sourceMap.forEach((originalName, searchKey) => {
            // Verifica se a chave de busca (normalizada) inclui a query do usuário (normalizada)
            if (searchKey.includes(query)) {
                suggestedProducts.add(originalName); // Adiciona APENAS o nome original/formatado
            }
        });
        
        const filtered = Array.from(suggestedProducts);

        // Renderiza a lista se houver itens
        if (filtered.length > 0) {
            filtered.forEach(product => {
                const li = document.createElement('li');
                li.className = 'p-2 cursor-pointer hover:bg-gray-100 text-gray-800 text-sm';
                li.textContent = product; // Exibe o nome formatado
                
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

    // Eventos 
    inputElement.addEventListener('input', renderSuggestions);

    inputElement.addEventListener('focus', () => {
        // Quando foca, mostra todos os itens (filtrando pela query vazia, que é o padrão)
        renderSuggestions();
    });

    document.addEventListener('click', (event) => {
        // Oculta a lista se o clique for fora do input ou da lista
        if (!inputElement.contains(event.target) && !suggestionsList.contains(event.target)) {
            suggestionsList.classList.add('hidden');
        }
    });
}

// =========================================================================
// FIM: NOVAS VARIÁVEIS E FUNÇÕES PARA AUTOCOMPLETE PROFISSIONAL
// =========================================================================

document.addEventListener('DOMContentLoaded', () => {
    // -----------------------------------------------------------------
    // INICIALIZAÇÃO DO NOVO AUTOCOMPLETE (USANDO OS MAPAS DE BUSCA)
    // -----------------------------------------------------------------
    // Campo de Validade (VALIDITY) usa o mapa VALIDITY_SEARCH_MAP
    setupAutocomplete('productName', 'productSuggestions', VALIDITY_SEARCH_MAP); 
    
    // Campo de Consumo Imediato (IMMEDIATE) usa o mapa IMMEDIATE_SEARCH_MAP
    setupAutocomplete('immediateProductName', 'immediateProductSuggestions', IMMEDIATE_SEARCH_MAP);
    // -----------------------------------------------------------------

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

    
    // Limpa o texto da Etiqueta Simples
    if (ui.labelText) ui.labelText.value = '';

    // Limpa os campos da Etiqueta de Validade
    if (ui.productName) ui.productName.value = '';
    if (ui.mfgDate) ui.mfgDate.value = new Date().toISOString().split('T')[0]; // Reseta para hoje
    if (ui.validityDays) {
        ui.validityDays.value = 1;
        updateValidityUnitLabel(ui); // Garante que o texto mude para "1 dia"
    }

    // Limpa o campo de Consumo Imediato
    if (ui.immediateProductName) ui.immediateProductName.value = '';

    // Esconde as listas de sugestões do autocomplete (se estiverem abertas)
    document.getElementById('productSuggestions')?.classList.add('hidden');
    document.getElementById('immediateProductSuggestions')?.classList.add('hidden');

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