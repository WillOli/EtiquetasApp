export function appendText(value) {
  const textarea = document.getElementById('labelText');
  textarea.value += value;
  updatePreview();
}

export function appendSpecial() {
  const textarea = document.getElementById('labelText');
  const text = textarea.value;
  const special = " - ESPECIAL ";
  if (!text.includes(special)) {
    textarea.value = text + special;
  }
  updatePreview();
}

export function clearText() {
  document.getElementById('labelText').value = '';
  updatePreview();
}

export function updatePreview() {
  const text = document.getElementById('labelText').value || 'Digite o texto acima';
  const previewText = document.getElementById('previewText');
  previewText.textContent = text;
}