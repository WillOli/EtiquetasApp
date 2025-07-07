import * as textUtils from './utils/textUtils.js';
import * as quantityUtils from './utils/quantityUtils.js';
import * as printUtils from './utils/printUtils.js';
import * as modalUtils from './utils/modalUtils.js';

// Função para registrar todos os nossos eventos
function initializeEventListeners() {
    console.log("Inicializando eventos..."); // Log para depuração

    // Botões de texto
    document.getElementById('btn-1')?.addEventListener('click', () => textUtils.appendText('1'));
    document.getElementById('btn-2')?.addEventListener('click', () => textUtils.appendText('2'));
    document.getElementById('btn-3')?.addEventListener('click', () => textUtils.appendText('3'));
    document.getElementById('btn-4')?.addEventListener('click', () => textUtils.appendText('4'));
    document.getElementById('btn-5')?.addEventListener('click', () => textUtils.appendText('5'));
    document.getElementById('btn-6')?.addEventListener('click', () => textUtils.appendText('6'));
    document.getElementById('btn-7')?.addEventListener('click', () => textUtils.appendText('7'));
    document.getElementById('btn-8')?.addEventListener('click', () => textUtils.appendText('8'));
    document.getElementById('btn-9')?.addEventListener('click', () => textUtils.appendText('9'));
    document.getElementById('btn-0')?.addEventListener('click', () => textUtils.appendText('0'));
    document.getElementById('btn-special')?.addEventListener('click', textUtils.appendSpecial);
    document.getElementById('btn-clear')?.addEventListener('click', textUtils.clearText);

    // Botões de quantidade
    document.getElementById('btn-q1')?.addEventListener('click', () => quantityUtils.setQuantity(1));
    document.getElementById('btn-q2')?.addEventListener('click', () => quantityUtils.setQuantity(2));
    document.getElementById('btn-q3')?.addEventListener('click', () => quantityUtils.setQuantity(3));
    document.getElementById('btn-q4')?.addEventListener('click', () => quantityUtils.setQuantity(4));
    document.getElementById('btn-q5')?.addEventListener('click', () => quantityUtils.setQuantity(5));
    document.getElementById('btn-q6')?.addEventListener('click', () => quantityUtils.setQuantity(6));
    
    // Botão de impressão principal
    document.getElementById('printButton')?.addEventListener('click', printUtils.sendPrintRequestWrapper);

    // Input de quantidade
    const labelQuantity = document.getElementById('labelQuantity');
    if (labelQuantity) {
        labelQuantity.addEventListener('input', quantityUtils.updateDuplicateInfo);
        quantityUtils.updateDuplicateInfo(); // Inicializa a contagem
    }

    // Modal
    document.getElementById('alertModal')?.querySelector('button')?.addEventListener('click', modalUtils.closeModal);
    
    console.log("Eventos inicializados com sucesso."); // Log para depuração
}

// Roda nossa função de inicialização quando o DOM estiver pronto
document.addEventListener('DOMContentLoaded', () => {
    try {
        initializeEventListeners();
    } catch (error) {
        console.error('Erro fatal ao inicializar eventos:', error);
        modalUtils.showModal('Erro crítico ao carregar a página: ' + error.message);
    }
});