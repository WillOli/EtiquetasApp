export function setQuantity(amount) {
  document.getElementById('labelQuantity').value = amount;
  updateDuplicateInfo();
}

export function updateDuplicateInfo() {
  const quantity = parseInt(document.getElementById('labelQuantity').value) || 1;
  const totalPrinted = quantity * 2;
  const duplicateInfo = document.getElementById('duplicate-info');
  duplicateInfo.textContent = `Quantidade solicitada: ${quantity}, Total impresso: ${totalPrinted}`;
}