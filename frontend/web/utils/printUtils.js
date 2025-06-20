import * as modalUtils from './modalUtils.js';

export async function printLabels() {
  const text = document.getElementById('labelText').value || 'Etiqueta de Exemplo';
  const quantity = parseInt(document.getElementById('labelQuantity').value) || 1;

  const printButton = document.getElementById('printButton');
  const printButtonText = document.getElementById('printButtonText');
  const spinner = document.getElementById('spinner');

  if (quantity < 1 || quantity > 100) {
    alert('A quantidade deve estar entre 1 e 100.');
    return;
  }

  printButton.disabled = true;
  printButton.classList.add('opacity-70', 'cursor-not-allowed');
  spinner.classList.remove('hidden');
  printButtonText.textContent = 'Imprimindo...';

  try {
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
  } catch (error) {
    modalUtils.showModal('Erro ao imprimir: ' + error.message);
  } finally {
    printButton.disabled = false;
    printButton.classList.remove('opacity-70', 'cursor-not-allowed');
    spinner.classList.add('hidden');
    printButtonText.textContent = 'Imprimir Etiquetas';
  }
}