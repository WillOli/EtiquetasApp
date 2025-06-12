document.addEventListener('DOMContentLoaded', () => {
  const labelText = document.getElementById('labelText');
  labelText.addEventListener('input', updatePreview);
  updatePreview(); // Inicializa visualização
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
}

function updatePreview() {
  const text = document.getElementById('labelText').value || 'Digite o texto acima';
  const previewText = document.getElementById('previewText');
  previewText.textContent = text;
}

function printLabels() {
  const text = document.getElementById('labelText').value || 'Etiqueta de Exemplo';
  const quantity = parseInt(document.getElementById('labelQuantity').value) || 1;

  if (quantity < 1 || quantity > 100) {
    alert('A quantidade deve estar entre 1 e 100.');
    return;
  }

  // Tamanho fixo definido no backend
  const width = 6;  // cm
  const height = 4; // cm

  let html = `
    <style>
      @page { margin: 1cm; }
      body { margin: 0; }
      .label-container {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(${width}cm, 1fr));
        gap: 0.4cm;
      }
      .label {
        width: ${width}cm;
        height: ${height}cm;
        border: 1px solid black;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
        font-weight: bold;
        text-align: center;
        box-sizing: border-box;
        padding: 5px;
      }
    </style>
    <div class="label-container">
  `;

  for (let i = 0; i < quantity; i++) {
    html += `<div class="label">${text}</div>`;
  }
  html += '</div>';

  const printFrame = document.getElementById('printFrame');
  const frameDoc = printFrame.contentWindow.document;

  frameDoc.open();
  frameDoc.write(`<html><head><title>Impressão</title></head><body onload="window.print()">${html}</body></html>`);
  frameDoc.close();
}
