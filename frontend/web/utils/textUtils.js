import * as modalUtils from './modalUtils.js';

export function appendText(value) {
  try {
    const textarea = document.getElementById('labelText');
    if (!textarea) {
      throw new Error('Elemento labelText não encontrado');
    }
    textarea.value += value;
    updatePreview();
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
    updatePreview();
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
    updatePreview();
  } catch (error) {
    console.error('Erro ao limpar texto:', error);
    modalUtils.showModal('Erro ao limpar texto: ' + error.message);
  }
}

export function updatePreview() {
  try {
    const text = document.getElementById('labelText')?.value || 'Digite o texto acima';
    const previewText1 = document.getElementById('previewText1');
    const previewText2 = document.getElementById('previewText2');
    if (!previewText1 || !previewText2) {
      throw new Error('Elementos de pré-visualização (previewText1 ou previewText2) não encontrados');
    }
    previewText1.textContent = text;
    previewText2.textContent = text;
  } catch (error) {
    console.error('Erro ao atualizar preview:', error);
    modalUtils.showModal('Erro ao atualizar a visualização: ' + error.message);
  }
}