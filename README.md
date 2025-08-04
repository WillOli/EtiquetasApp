## 👨‍💻 Autor

Desenvolvido por <a href="https://br.linkedin.com/in/william-silva-oliveira" target="_blank" rel="noopener noreferrer">William Silva Oliveira</a>

# Servidor de Impressão de Etiquetas - Espaço Vista

![Logo Espaço Vista](https://www.espacovista.com.br/wp-content/uploads/2022/06/LOGO_ESPACO.png) <!-- Substitua pela URL do seu logo -->

![Status](https://img.shields.io/badge/status-ativo-brightgreen)
![Java](https://img.shields.io/badge/Java-11+-blue?logo=java&logoColor=white)
![Javalin](https://img.shields.io/badge/Javalin-5.6.3-blue)
![JavaScript](https://img.shields.io/badge/JavaScript-ES6-yellow?logo=javascript)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-3.4-blue?logo=tailwind-css)

---

## 📄 Sobre o Projeto

O **Servidor de Impressão de Etiquetas** é uma aplicação web full-stack desenvolvida para simplificar a geração e impressão de etiquetas em impressoras térmicas que utilizam a linguagem ZPL (Zebra Programming Language). A solução consiste num backend robusto em Java com Javalin e um frontend moderno e intuitivo em Vanilla JavaScript e Tailwind CSS.

O projeto foi criado para atender às necessidades do Espaço Vista, permitindo a impressão rápida de etiquetas de produtos e de validade diretamente do navegador, com suporte para diferentes layouts e tamanhos.

---

## ✨ Funcionalidades Principais

* **Interface Web Intuitiva:** Frontend limpo e responsivo para uma fácil utilização em qualquer dispositivo.
* **Dois Modos de Impressão:**
    * **Etiqueta Simples:** Para textos genéricos.
    * **Etiqueta de Validade:** Com campos estruturados para nome do produto, data de fabrico e prazo de validade.
* **Suporte a Múltiplos Formatos:** Geração de ZPL para etiquetas de coluna única (80mm x 25mm) e de coluna dupla (40mm x 25mm).
* **Geração Dinâmica de ZPL:** O código de impressão é gerado no servidor com base nos dados inseridos pelo utilizador.
* **Logging Profissional:** Utiliza SLF4J e Logback para registar todas as operações de impressão, facilitando o diagnóstico e a monitorização.
* **Arquitetura Extensível:** O uso do Padrão de Projeto *Strategy* permite adicionar facilmente novos tipos e layouts de etiquetas no futuro.

---

## 🛠️ Tecnologias Utilizadas

| Componente | Tecnologias                                           |
| :--------- | :---------------------------------------------------- |
| **Backend** | ☕️ Java 11+, 🚀 Javalin, 📦 Maven, 📝 SLF4J + Logback   |
| **Frontend** | 🌐 HTML5, 🎨 CSS3 + Tailwind CSS, 💡 Vanilla JavaScript (ES6) |
| **Linguagem de Impressão** | 🦓 ZPL (Zebra Programming Language)                   |

---

## 🚀 Como Executar o Projeto Localmente

Siga os passos abaixo para configurar e executar a aplicação no seu ambiente de desenvolvimento.

### Pré-requisitos

* **Java JDK 11** ou superior.
* **Apache Maven** instalado e configurado.
* Uma **impressora térmica** compatível com ZPL definida como impressora padrão no seu sistema operativo.

### Passos para a Instalação

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/seu-usuario/app-impressora-etiquetas.git](https://github.com/seu-usuario/app-impressora-etiquetas.git)
    cd app-impressora-etiquetas/backend
    ```

2.  **Compile o projeto com o Maven:**
    Este comando irá descarregar as dependências e compilar o código.
    ```bash
    mvn clean package
    ```

3.  **Execute a aplicação:**
    Inicie o servidor executando a classe `Main`.
    ```bash
    java -jar target/seu-artefato-com-dependencias.jar
    ```
    *Ou, diretamente da sua IDE, execute o método `main` na classe `application.Main`.*

4.  **Aceda ao Frontend:**
    Abra o seu navegador e aceda ao seguinte URL:
    [http://localhost:8081/web/index.html](http://localhost:8081/web/index.html) (verifique a porta configurada no seu ficheiro `application.properties`).

---

## 🏛️ Arquitetura do Software

O backend foi desenhado com uma clara separação de responsabilidades para garantir a manutenibilidade e a escalabilidade.

* **`Controller` (PrintController):** Responsável por receber as requisições HTTP, validar os dados de entrada e delegar a lógica de negócio.
* **`Service` (PrinterService):** Orquestra a operação de impressão. Ele recebe o pedido, utiliza a `PrinterStrategyFactory` para selecionar a estratégia correta e envia o ZPL gerado para a impressora.
* **Padrão de Projeto *Strategy*:** O núcleo da lógica de geração de ZPL.
    * **`ILabelStrategy` (Interface):** Define o contrato que todas as estratégias de impressão devem seguir.
    * **Classes de Estratégia Concretas:** Cada classe (`SimpleLayoutStrategy`, `ValidadeStandardStrategy`, etc.) é responsável por gerar o ZPL para um layout de etiqueta específico.
    * **`AbstractTwoColumnStrategy`:** Uma classe base abstrata que elimina a duplicação de código para layouts de duas colunas.

Esta arquitetura torna trivial a adição de um novo tipo de etiqueta: basta criar uma nova classe de estratégia e adicioná-la à fábrica, sem alterar o resto do sistema.

---

## 🔮 Melhorias Futuras

* [ ] Adicionar animações e transições na UI para uma experiência mais fluida.
* [ ] Implementar validação de formulários no lado do cliente para um feedback mais imediato.
* [ ] Criar um sistema de templates que permita ao utilizador guardar e reutilizar etiquetas predefinidas.
* [ ] Adicionar mais testes de unidade para cobrir casos de erro e cenários extremos.
