import * as modalUtils from './modalUtils.js';

async function sendPrintRequest(text, quantity, labelType, buttonId, buttonTextId, spinnerId) {
    const printButton = document.getElementById(buttonId);
    const printButtonText = document.getElementById(buttonTextId);
    const spinner = document.getElementById(spinnerId);

    if (!printButton || !printButtonText || !spinner) {
        throw new Error(`Elementos de botão de impressão (${buttonId}, ${buttonTextId}, ${spinnerId}) não encontrados`);
    }

    if (quantity < 1 || quantity > 100) {
        modalUtils.showModal('A quantidade deve estar entre 1 e 100.');
        return;
    }

    printButton.disabled = true;
    printButton.classList.add('opacity-70', 'cursor-not-allowed');
    spinner.classList.remove('hidden');
    printButtonText.textContent = 'Imprimindo...';

    try {
        const payload = { text, quantity };
        if (labelType) {
            payload.labelType = labelType;
        }

        const response = await fetch('http://localhost:8080/print', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Erro na impressão');
        }

        const data = await response.text();
        modalUtils.showModal('Impressão enviada com sucesso!');
    } catch (error) {
        console.error('Erro ao imprimir etiquetas:', error);
        modalUtils.showModal('Erro ao imprimir: ' + error.message);
    } finally {
        printButton.disabled = false;
        printButton.classList.remove('opacity-70', 'cursor-not-allowed');
        spinner.classList.add('hidden');
        printButtonText.textContent = printButtonText.getAttribute('data-original-text'); // Restaura o texto original
    }
}

export async function printLabels() {
    const text = document.getElementById('labelText')?.value || 'Etiqueta de Exemplo';
    const quantityInput = document.getElementById('labelQuantity');
    const quantity = parseInt(quantityInput.value) || 1;

    // Guarda o texto original do botão antes de chamar sendPrintRequest
    document.getElementById('printButtonStandardText').setAttribute('data-original-text', 'Imprimir Etiquetas (Padrão)');
    await sendPrintRequest(text, quantity, 'STANDARD', 'printButtonStandard', 'printButtonStandardText', 'spinnerStandard');
}

export async function print62mmLabels() {
    const text = document.getElementById('labelText')?.value || 'Etiqueta de Exemplo';
    const quantityInput = document.getElementById('labelQuantity');
    const quantity = parseInt(quantityInput.value) || 1;

    // Guarda o texto original do botão antes de chamar sendPrintRequest
    document.getElementById('printButton62mmText').setAttribute('data-original-text', 'Imprimir Etiquetas (62mm)');
    await sendPrintRequest(text, quantity, 'SIXTY_TWO_MM', 'printButton62mm', 'printButton62mmText', 'spinner62mm');
}

// Funções existentes que não precisam ser alteradas, mas certifique-se de que estão no mesmo arquivo ou importadas corretamente:
/*
document.addEventListener('DOMContentLoaded', () => {
    const labelText = document.getElementById('labelText');
    const labelQuantity = document.getElementById('labelQuantity');
    labelText.addEventListener('input', updatePreview); // Se você tiver um preview
    labelQuantity.addEventListener('input', updateDuplicateInfo);
    // updatePreview(); // Inicializa visualização, se houver
    updateDuplicateInfo(); // Inicializa informação de duplicação
});

function updateDuplicateInfo() {
    const quantity = parseInt(document.getElementById('labelQuantity').value) || 1;
    const totalPrinted = quantity * 2;
    const duplicateInfo = document.getElementById('duplicate-info');
    duplicateInfo.textContent = `Quantidade solicitada: ${quantity}, Total impresso: ${totalPrinted}`;
}
*/