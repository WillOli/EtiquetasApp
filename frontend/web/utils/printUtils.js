import * as modalUtils from './modalUtils.js';

export async function printLabels() {
  try {
    const text = document.getElementById('labelText')?.value || 'Etiqueta de Exemplo';
    const quantityInput = document.getElementById('labelQuantity');
    if (!quantityInput) {
      throw new Error('Elemento labelQuantity não encontrado');
    }
    const quantity = parseInt(quantityInput.value) || 1;
    if (isNaN(quantity)) {
      throw new Error('Quantidade inválida');
    }

    const printButton = document.getElementById('printButton');
    const printButtonText = document.getElementById('printButtonText');
    const spinner = document.getElementById('spinner');

    if (!printButton || !printButtonText || !spinner) {
      throw new Error('Elementos de botão de impressão não encontrados');
    }

    if (quantity < 1 || quantity > 100) {
      modalUtils.showModal('A quantidade deve estar entre 1 e 100.');
      return;
    }

    printButton.disabled = true;
    printButton.classList.add('opacity-70', 'cursor-not-allowed');
    spinner.classList.remove('hidden');
    printButtonText.textContent = 'Imprimindo...';

    const response = await fetch('http://localhost:8080/print', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ text, quantity })
    });

    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || 'Erro na impressão');
    }

    const data = await response.text();
    modalUtils.showModal('Impressão enviada com sucesso!');

    // Limpas o texto da etiqueta e redefinir a quantidade após impressão bem-sucedida
    textUtils.clearText();
    quantityUtils.setQuantity(1);
    quantityUtils.updateDuplicateInfo(); // Atualiza a informação de duplicação

  } catch (error) {
    console.error('Erro ao imprimir etiquetas:', error);
    modalUtils.showModal('Erro ao imprimir: ' + error.message);
  } finally {
    try {
      const printButton = document.getElementById('printButton');
      const printButtonText = document.getElementById('printButtonText');
      const spinner = document.getElementById('spinner');

      if (printButton && printButtonText && spinner) {
        printButton.disabled = false;
        printButton.classList.remove('opacity-70', 'cursor-not-allowed');
        spinner.classList.add('hidden');
        printButtonText.textContent = 'Imprimir Etiquetas';
      }
    } catch (error) {
      console.error('Erro ao restaurar estado do botão de impressão:', error);
      modalUtils.showModal('Erro ao restaurar interface: ' + error.message);
    }
  }
}