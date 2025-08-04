import * as modalUtils from './modalUtils.js';

export function appendText(value) {
  try {
    const textarea = document.getElementById('labelText');
    if (!textarea) {
      throw new Error('Elemento labelText não encontrado');
    }
    textarea.value += value;
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
  } catch (error) {
    console.error('Erro ao limpar texto:', error);
    modalUtils.showModal('Erro ao limpar texto: ' + error.message);
  }
}