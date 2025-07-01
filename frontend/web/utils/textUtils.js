import * as modalUtils from './modalUtils.js';

export function appendText(value) {
  try {
    const textarea = document.getElementById('labelText');
    if (!textarea) {
      throw new Error('Elemento labelText não encontrado');
    }
    textarea.value += value;
    // Se você tiver uma função updatePreview e um elemento para exibir o preview
    // updatePreview(); 
  } catch (error) {
    console.error('Erro ao adicionar texto:', error);
    modalUtils.showModal('Erro ao adicionar texto: ' + error.message);
  }
}

export function appendSpecial() {
  try {
    const textarea = document.getElementById('labelText');
    if (!textarea) {
      throw new Error('Elemento labelText não encontrado');
    }
    const text = textarea.value;
    const special = " - ESPECIAL ";
    if (!text.includes(special)) {
      textarea.value = text + special;
    }
    // Se você tiver uma função updatePreview e um elemento para exibir o preview
    // updatePreview(); 
  } catch (error) {
    console.error('Erro ao adicionar texto especial:', error);
    modalUtils.showModal('Erro ao adicionar texto especial: ' + error.message);
  }
}

export function clearText() {
  try {
    const textarea = document.getElementById('labelText');
    if (!textarea) {
      throw new Error('Elemento labelText não encontrado');
    }
    textarea.value = '';
    // Se você tiver uma função updatePreview e um elemento para exibir o preview
    // updatePreview(); 
  } catch (error) {
    console.error('Erro ao limpar texto:', error);
    modalUtils.showModal('Erro ao limpar texto: ' + error.message);
  }
}

// Se você tiver uma função updatePreview, ela deve estar aqui ou ser importada de outro lugar
// function updatePreview() {
//   const text = document.getElementById('labelText').value || 'Digite o texto acima';
//   const previewText = document.getElementById('previewText'); // Certifique-se de ter um elemento com id 'previewText' no seu HTML
//   if (previewText) {
//     previewText.textContent = text;
//   }
// }