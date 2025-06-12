document.addEventListener('DOMContentLoaded', () => {
  const labelText = document.getElementById('labelText');
  const labelWidth = document.getElementById('labelWidth');
  const labelHeight = document.getElementById('labelHeight');

  labelText.addEventListener('input', updatePreview);
  labelWidth.addEventListener('input', updatePreview);
  labelHeight.addEventListener('input', updatePreview);

  updatePreview(); // Inicializa
});

function appendText(value) {
  const textarea = document.getElementById('labelText');
  textarea.value += value;
  updatePreview();
}

function appendSpecial() {
  const textarea = document.getElementById('labelText');
  const text = textarea.value;
  const match = text.match(/^\d+/);
  const special = " - ESPECIAL ";
  if (match && !text.includes(special)) {
    textarea.value = text.slice(0, match[0].length) + special + text.slice(match[0].length);
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
  const width = document.getElementById('labelWidth').value;
  const height = document.getElementById('labelHeight').value;

  const preview = document.getElementById('labelPreview');
  const previewText = document.getElementById('previewText');

  preview.style.width = `${width}cm`;
  preview.style.height = `${height}cm`;
  previewText.textContent = text;
}

function printLabels() {
  const text = document.getElementById('labelText').value || 'Etiqueta de Exemplo';
  const width = document.getElementById('labelWidth').value;
  const height = document.getElementById('labelHeight').value;
  const quantity = parseInt(document.getElementById('labelQuantity').value);

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
