import { showModal } from './modal.js';

export function initializeUI() {
    // Seletores de modo
    const btnModeSimple = document.getElementById('btnModeSimple');
    const btnModeValidity = document.getElementById('btnModeValidity');
    btnModeSimple?.addEventListener('click', () => switchMode('simple'));
    btnModeValidity?.addEventListener('click', () => switchMode('validity'));
    
    // Botões do formulário simples
    document.querySelectorAll('.btn-numeric').forEach(btn => {
        btn.addEventListener('click', () => appendToLabelText(btn.dataset.value));
    });
    document.getElementById('btn-special')?.addEventListener('click', appendSpecial);
    document.getElementById('btn-clear')?.addEventListener('click', clearLabelText);

    // Listeners para atualização da info de duplicados
    document.getElementById('labelQuantity')?.addEventListener('input', updateDuplicateInfo);
    document.getElementById('labelType')?.addEventListener('change', updateDuplicateInfo);

    // Inicializa a UI no modo simples
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
        initializeValidityDates();
    } else { // 'simple'
        simpleForm.classList.remove('hidden');
        validityForm.classList.add('hidden');
        btnModeSimple.className = 'btn-primary w-full sm:w-auto';
        btnModeValidity.className = 'btn-secondary w-full sm:w-auto';
    }
}

function initializeValidityDates() {
    const mfgDateInput = document.getElementById('mfgDate');
    const expDateInput = document.getElementById('expDate');
    const today = new Date();
    today.setMinutes(today.getMinutes() - today.getTimezoneOffset());
    mfgDateInput.value = today.toISOString().split('T')[0];
    const thirtyDaysFromNow = new Date(today);
    thirtyDaysFromNow.setDate(today.getDate() + 30);
    expDateInput.value = thirtyDaysFromNow.toISOString().split('T')[0];
}

function appendToLabelText(text) {
    document.getElementById('labelText').value += text;
}

function appendSpecial() {
    const textarea = document.getElementById('labelText');
    if (!textarea.value.includes(' - ESPECIAL ')) {
        textarea.value += ' - ESPECIAL ';
    }
}

function clearLabelText() {
    document.getElementById('labelText').value = '';
}

export function updateDuplicateInfo() {
    const quantity = parseInt(document.getElementById('labelQuantity')?.value) || 0;
    const type = document.getElementById('labelType')?.value;
    const infoText = document.getElementById('duplicate-info-text');
    if (!infoText) return;
    let totalPrinted = quantity;
    if (type === 'STANDARD') { // Etiqueta Dupla
        totalPrinted *= 2;
    }
    infoText.textContent = `Quantidade solicitada: ${quantity}, Total impresso: ${totalPrinted}`;
}