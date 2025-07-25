import { showModal } from './modal.js';

export function initializeUI() {
    // Seletores de modo
    const btnModeSimple = document.getElementById('btnModeSimple');
    const btnModeValidity = document.getElementById('btnModeValidity');
    btnModeSimple?.addEventListener('click', () => switchMode('simple'));
    btnModeValidity?.addEventListener('click', () => switchMode('validity'));
    
    // ===== INÍCIO DA LÓGICA RESTAURADA =====
    // Listeners para os botões do teclado numérico
    document.querySelectorAll('.btn-numeric').forEach(btn => {
        btn.addEventListener('click', () => appendToLabelText(btn.dataset.value));
    });
    document.getElementById('btn-special')?.addEventListener('click', appendSpecial);
    document.getElementById('btn-clear')?.addEventListener('click', clearLabelText);
    // ===== FIM DA LÓGICA RESTAURADA =====

    // Listeners para atualização da info de duplicados
    document.getElementById('labelQuantity')?.addEventListener('input', updateDuplicateInfo);
    document.getElementById('labelType')?.addEventListener('change', updateDuplicateInfo);

    // Lógica do menu customizado de validade
    const validityDropdownBtn = document.getElementById('validityDropdownBtn');
    const validityDropdownPanel = document.getElementById('validityDropdownPanel');

    validityDropdownBtn?.addEventListener('click', (event) => {
        event.stopPropagation();
        validityDropdownPanel.classList.toggle('hidden');
    });

    populateValidityDropdown();

    window.addEventListener('click', () => {
        if (!validityDropdownPanel.classList.contains('hidden')) {
            validityDropdownPanel.classList.add('hidden');
        }
    });

    // Inicializa a UI
    switchMode('simple');
    updateDuplicateInfo();
}

function switchMode(mode) {
    const simpleForm = document.getElementById('simpleForm');
    const validityForm = document.getElementById('validityForm');
    const btnModeSimple = document.getElementById('btnModeSimple');
    const btnModeValidity = document.getElementById('btnModeValidity');

    if (mode === 'validity') {
        simpleForm.classList.add('hidden');
        validityForm.classList.remove('hidden');
        btnModeValidity.className = 'btn-primary w-full sm:w-auto';
        btnModeSimple.className = 'btn-secondary w-full sm:w-auto';
        initializeValidityFields();
    } else { // 'simple'
        simpleForm.classList.remove('hidden');
        validityForm.classList.add('hidden');
        btnModeSimple.className = 'btn-primary w-full sm:w-auto';
        btnModeValidity.className = 'btn-secondary w-full sm:w-auto';
    }
}

function initializeValidityFields() {
    const mfgDateInput = document.getElementById('mfgDate');
    const validityDaysInput = document.getElementById('validityDays');
    
    const today = new Date();
    today.setMinutes(today.getMinutes() - today.getTimezoneOffset());
    mfgDateInput.value = today.toISOString().split('T')[0];

    if(validityDaysInput) {
        validityDaysInput.value = 30;
    }
}

function populateValidityDropdown() {
    const panel = document.getElementById('validityDropdownPanel');
    const input = document.getElementById('validityDays');
    if (!panel || !input) return;

    panel.innerHTML = ''; 
    const presetDays = [5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 75, 90, 120, 150, 180, 190];

    presetDays.forEach(days => {
        const optionButton = document.createElement('button');
        optionButton.type = 'button';
        optionButton.textContent = `${days} dias`;
        optionButton.className = 'block w-full text-left px-4 py-2 text-sm text-[var(--text-primary)] hover:bg-[var(--bg-primary)]';
        
        optionButton.addEventListener('click', (event) => {
            event.stopPropagation(); 
            input.value = days; 
            panel.classList.add('hidden'); 
        });

        panel.appendChild(optionButton);
    });
}

// ===== INÍCIO DAS FUNÇÕES DO TECLADO =====
function appendToLabelText(text) {
    const textarea = document.getElementById('labelText');
    if (textarea) {
        textarea.value += text;
    }
}

function appendSpecial() {
    const textarea = document.getElementById('labelText');
    if (textarea && !textarea.value.includes(' - ESPECIAL ')) {
        textarea.value += ' - ESPECIAL ';
    }
}

function clearLabelText() {
    const textarea = document.getElementById('labelText');
    if (textarea) {
        textarea.value = '';
    }
}
// ===== FIM DAS FUNÇÕES DO TECLADO =====

export function updateDuplicateInfo() {
    const quantity = parseInt(document.getElementById('labelQuantity')?.value) || 0;
    const type = document.getElementById('labelType')?.value;
    const infoText = document.getElementById('duplicate-info-text');
    if (!infoText) return;
    let totalPrinted = quantity;
    if (type === 'STANDARD') { totalPrinted *= 2; }
    infoText.textContent = `Quantidade solicitada: ${quantity}, Total impresso: ${totalPrinted}`;
}