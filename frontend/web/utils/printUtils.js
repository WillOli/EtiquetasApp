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
        // Restaura o texto original do botão usando o atributo data-original-text
        printButtonText.textContent = printButtonText.getAttribute('data-original-text'); 
    }
}

export async function sendPrintRequestWrapper() {
    const text = document.getElementById('labelText')?.value || 'Etiqueta de Exemplo';
    const quantityInput = document.getElementById('labelQuantity');
    const quantity = parseInt(quantityInput.value) || 1;
    const labelTypeSelect = document.getElementById('labelType');
    // Pega o valor do seletor. Se o seletor não for encontrado ou não tiver valor, usa 'STANDARD' como padrão.
    const labelType = labelTypeSelect ? labelTypeSelect.value : 'STANDARD'; 

    // Guarda o texto original do botão antes de chamar sendPrintRequest
    document.getElementById('printButtonText').setAttribute('data-original-text', 'Imprimir Etiquetas');
    await sendPrintRequest(text, quantity, labelType, 'printButton', 'printButtonText', 'spinner');
}