## 👨‍💻 Autor

Desenvolvido por <a href="https://br.linkedin.com/in/william-silva-oliveira" target="_blank" rel="noopener noreferrer">William Silva Oliveira</a>


-----

# 🖨️ App de Impressão de Etiquetas Espaço Vista

Este aplicativo oferece uma solução simples e eficiente para a impressão de etiquetas, permitindo que usuários preencham textos, definam quantidades e escolham o tipo de etiqueta (padrão ou 62mm) de forma intuitiva através de uma interface web amigável. O backend em Java processa as requisições de impressão, gerando comandos ZPL (Zebra Programming Language) e os enviando para a impressora padrão do sistema.

-----

## 🚀 Funcionalidades

  * **Interface Intuitiva**: Um painel web limpo e fácil de usar.
  * **Preenchimento Rápido**: Botões numéricos e de texto especial para agilizar a entrada de dados.
  * **Seleção de Quantidade**: Defina facilmente a quantidade de etiquetas a serem impressas.
  * **Seleção de Tipo de Etiqueta**: Escolha entre etiquetas **Padrão (40x25mm)** e **62mm (62x62mm)**.
  * **Processamento em Segundo Plano**: O servidor Java gerencia as tarefas de impressão sem travar a interface.
  * **Sistema de Log**: Registro detalhado de todas as operações de impressão para monitoramento.
  * **Feedback Visual**: Indicadores de carregamento e mensagens de alerta para uma melhor experiência do usuário.

-----

## 🛠️ Tecnologias Utilizadas

### Frontend

  * **HTML5**: Estrutura da página web.
  * **CSS3 (Tailwind CSS)**: Estilização moderna e responsiva.
  * **JavaScript (Módulos ES6)**: Lógica de interação da interface e comunicação com o backend.

### Backend

  * **Java**: Linguagem principal do servidor.
  * **ServerSocket & Socket**: Para comunicação de rede (servidor TCP/IP).
  * **javax.print**: API Java para serviços de impressão.
  * **org.json**: Para parsing de requisições JSON.

-----

## ⚙️ Como Rodar o Projeto

### Pré-requisitos

  * **Java Development Kit (JDK) 8 ou superior**
  * Uma **impressora Zebra** (ou compatível com ZPL) configurada como impressora padrão no seu sistema.
  * Navegador web moderno (Chrome, Firefox, Edge, etc.).

### Configuração e Execução

1.  **Clone o Repositório** (ou baixe os arquivos do projeto).

2.  **Backend (Java)**:

      * Navegue até a pasta raiz do projeto Java.
      * Compile os arquivos Java:
        ```bash
        javac -d out Main.java controller/*.java model/*.java service/*.java view/*.java -cp lib/json-20231013.jar
        ```
        (Certifique-se de ter o `json-20231013.jar` na pasta `lib/` e ajuste o caminho conforme necessário.)
      * Execute o servidor:
        ```bash
        java -cp out:lib/json-20231013.jar Main
        ```
        (No Windows, use `;` em vez de `:` no classpath: `java -cp out;lib/json-20231013.jar Main`)
        Você verá a mensagem: `Servidor iniciado na porta 8080`.

3.  **Frontend (Web)**:

      * Abra o arquivo `index.html` em seu navegador web.
      * Alternativamente, você pode usar uma extensão de servidor web local (como "Live Server" para VS Code) ou um servidor HTTP simples para servir os arquivos estáticos da pasta `web`.

-----

## 🖥️ Captura de Tela do Aplicativo
![image alt](https://github.com/WillOli/EtiquetasApp/blob/main/img/tela.png)
