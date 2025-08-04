/**
 * Exibe o modal com uma mensagem.
 * @param {string} message - A mensagem a ser exibida.
 */
export function showModal(message) {
    const modal = document.getElementById('alertModal');
    const messageElement = document.getElementById('alertMessage');
    const closeButton = document.getElementById('closeModalButton');
    if (!modal || !messageElement || !closeButton) return;
    messageElement.textContent = message;
    modal.classList.remove('hidden');
    modal.classList.add('flex');
    closeButton.onclick = closeModal;
    modal.onclick = (event) => {
        if (event.target === modal) {
            closeModal();
        }
    };
}

function closeModal() {
    const modal = document.getElementById('alertModal');
    if (modal) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
}

export function updateButtonState(isLoading) {
    const printButton = document.getElementById('printButton');
    const printButtonText = document.getElementById('printButtonText');
    const spinner = document.getElementById('spinner');
    if (!printButton || !printButtonText || !spinner) return;
    if (isLoading) {
        printButton.disabled = true;
        printButton.classList.add('opacity-70', 'cursor-not-allowed');
        spinner.classList.remove('hidden');
        printButtonText.textContent = 'Imprimindo...';
    } else {
        printButton.disabled = false;
        printButton.classList.remove('opacity-70', 'cursor-not-allowed');
        spinner.classList.add('hidden');
        printButtonText.textContent = 'Imprimir Etiquetas';
    }
}