import * as modalUtils from './modalUtils.js';

async function sendPrintRequest(text, quantity, labelType, buttonId, buttonTextId, spinnerId) {
    const printButton = document.getElementById(buttonId);
    const printButtonText = document.getElementById(buttonTextId);
    const spinner = document.getElementById(spinnerId);

    if (quantity < 1 || quantity > 100) {
        modalUtils.showModal('A quantidade deve estar entre 1 e 100.');
        return;
    }

    printButton.disabled = true;
    printButton.classList.add('opacity-70', 'cursor-not-allowed');
    spinner.classList.remove('hidden');
    printButtonText.textContent = 'Imprimindo...';

    try {
        const payload = { text, quantity, labelType };

        const response = await fetch('/print', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Erro na impressão');
        }

        modalUtils.showModal(await response.text());

    } catch (error) {
        console.error('Erro ao imprimir etiquetas:', error);
        modalUtils.showModal('Erro ao imprimir: ' + error.message);
    } finally {
        printButton.disabled = false;
        printButton.classList.remove('opacity-70', 'cursor-not-allowed');
        spinner.classList.add('hidden');
        printButtonText.textContent = 'Imprimir Etiquetas';
    }
}

export async function sendPrintRequestWrapper() {
    const text = document.getElementById('labelText')?.value || '';
    if (!text) {
        modalUtils.showModal('O texto da etiqueta não pode estar vazio.');
        return;
    }
    const quantity = parseInt(document.getElementById('labelQuantity')?.value) || 1;
    const labelType = document.getElementById('labelType')?.value || 'STANDARD';

    await sendPrintRequest(text, quantity, labelType, 'printButton', 'printButtonText', 'spinner');
}