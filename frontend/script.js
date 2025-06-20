document.addEventListener('DOMContentLoaded', () => {
  const labelText = document.getElementById('labelText');
  const labelQuantity = document.getElementById('labelQuantity');
  labelText.addEventListener('input', updatePreview);
  labelQuantity.addEventListener('input', updateDuplicateInfo);
  updatePreview(); // Inicializa visualização
  updateDuplicateInfo(); // Inicializa informação de duplicação
});

function appendText(value) {
  const textarea = document.getElementById('labelText');
  textarea.value += value;
  updatePreview();
}

function appendSpecial() {
  const textarea = document.getElementById('labelText');
  const text = textarea.value;
  const special = " - ESPECIAL ";
  if (!text.includes(special)) {
    textarea.value = text + special;
  }
  updatePreview();
}

function clearText() {
  document.getElementById('labelText').value = '';
  updatePreview();
}

function setQuantity(amount) {
  document.getElementById('labelQuantity').value = amount;
  updateDuplicateInfo();
}

function updatePreview() {
  const text = document.getElementById('labelText').value || 'Digite o texto acima';
  const previewText = document.getElementById('previewText');
  previewText.textContent = text;
}

function updateDuplicateInfo() {
  const quantity = parseInt(document.getElementById('labelQuantity').value) || 1;
  const totalPrinted = quantity * 2;
  const duplicateInfo = document.getElementById('duplicate-info');
  duplicateInfo.textContent = `Quantidade solicitada: ${quantity}, Total impresso: ${totalPrinted}`;
}

/* ========== Função para enviar requisição de impressão ========== */

function printLabels() {
  const text = document.getElementById('labelText').value || 'Etiqueta de Exemplo';
  const quantity = parseInt(document.getElementById('labelQuantity').value) || 1;

  const printButton = document.getElementById('printButton');
  const printButtonText = document.getElementById('printButtonText');
  const spinner = document.getElementById('spinner');

  if (quantity < 1 || quantity > 100) {
    alert('A quantidade deve estar entre 1 e 100.');
    return;
  }

  // Feedback visual: desabilita botão e mostra spinner
  printButton.disabled = true;
  printButton.classList.add('opacity-70', 'cursor-not-allowed');
  spinner.classList.remove('hidden');
  printButtonText.textContent = 'Imprimindo...';

  fetch('http://localhost:8080/print', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ text, quantity })
  })
  .then(response => {
    if (!response.ok) {
      return response.text().then(text => { throw new Error(text || 'Erro na impressão'); });
    }
    return response.text();
  })
  .then(data => {
    showModal('Impressão enviada com sucesso!');
  })
  .catch(error => {
    showModal('Erro ao imprimir: ' + error.message);
  })
  .finally(() => {
    printButton.disabled = false;
    printButton.classList.remove('opacity-70', 'cursor-not-allowed');
    spinner.classList.add('hidden');
    printButtonText.textContent = 'Imprimir Etiquetas';
  });
}

/* ========== Funções de Modal de Alerta ========== */

function showModal(message) {
  const modal = document.getElementById('alertModal');
  const messageElement = document.getElementById('alertMessage');
  messageElement.textContent = message;
  modal.style.display = 'flex';

  // Fecha automaticamente após 3 segundos (3000 ms)
  setTimeout(() => {
    closeModal();
  }, 3000);
}

function closeModal() {
  const modal = document.getElementById('alertModal');
  modal.style.display = 'none';
}