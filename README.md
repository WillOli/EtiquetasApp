## 👨‍💻 Autor

Desenvolvido por [William Silva Oliveira](https://br.linkedin.com/in/william-silva-oliveira)

```markdown
# 🖨️ Sistema Local de Impressão de Etiquetas

Aplicação desktop leve e confiável desenvolvida em **Java 21**, com **frontend em HTML/CSS/JavaScript**. O sistema permite a impressão rápida de etiquetas personalizadas, ideal para restaurantes e ambientes operacionais que exigem agilidade e simplicidade.

---

## ✅ Funcionalidades

- Interface intuitiva para inserção do conteúdo da etiqueta
- Seleção rápida de quantidade e atalho para textos comuns
- Envio de dados via requisição HTTP `POST` para o backend local
- Impressão automática utilizando a impressora padrão configurada no Windows
- Backend local sem dependência de frameworks pesados

---

## 🚀 Execução

### Pré-requisitos

- Java 21 instalado
- Ambiente Windows com impressora padrão corretamente configurada
- Navegador moderno (Chrome, Edge, etc.)

### Passos

1. **Compilar o backend Java:**
   ```bash
   javac -d out src/*.java
````

2. **Executar o servidor:**

   ```bash
   java -cp out Main
   ```

3. **Abrir o frontend:**

   * Basta abrir `index.html` no navegador e interagir com a interface.

---

## 🧪 Testes

* Os testes utilizam **JUnit 4**
* Para rodar:

  ```bash
  javac -cp .;junit-4.13.2.jar;hamcrest-core-1.3.jar -d out test/PrinterServiceTest.java
  java -cp out;junit-4.13.2.jar;hamcrest-core-1.3.jar org.junit.runner.JUnitCore PrinterServiceTest
  ```

> É recomendável rodar os testes diretamente pela sua IDE com suporte a JUnit (ex: IntelliJ IDEA).

---

## 🖨️ Impressão

A impressão é feita via `javax.print`, utilizando a **impressora padrão do sistema operacional**. Certifique-se de que a impressora de etiquetas esteja corretamente instalada e definida como padrão no Windows.

---
