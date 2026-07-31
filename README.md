## 👨‍💻 Autor

Desenvolvido por <a href="https://br.linkedin.com/in/william-silva-oliveira" target="_blank" rel="noopener noreferrer">William Silva Oliveira</a>

Visão Geral da Arquitetura
O projeto é uma aplicação full-stack voltada para a automação de impressões (provavelmente via ZPL para impressoras térmicas, evidenciado pelas classes de estratégia e constantes ZPL), estruturada da seguinte forma:

Backend: Desenvolvido em Java com o framework Spring Boot (espaco-vista-printer), estruturado em camadas (controller, service, model, config) com arquitetura baseada em estratégias (PrinterStrategyFactory, ILabelStrategy) para lidar com diferentes tipos de layouts de etiquetas (Simple, Validade, ImmediateConsumption).

Frontend: Interface web integrada diretamente no backend (src/main/resources/web), composta por HTML, CSS modularizado e scripts em JavaScript organizados por utilitários (modalUtils.js, printUtils.js, quantityUtils.js, textUtils.js).

Build & Deploy: Gerenciado via Maven (pom.xml, dependency-reduced-pom.xml), com suporte a testes unitários e de integração estruturados.

Sugestão de Atualização do README.md
Aqui está uma proposta completa e profissional para atualizar o README.md do seu repositório, destacando as tecnologias, a arquitetura e as instruções de execução:

Markdown
# 🏷️ EtiquetasApp

Aplicação desenvolvida para otimizar e automatizar o fluxo de emissão e gerenciamento de etiquetas operacionais, oferecendo uma interface web intuitiva acoplada a um backend robusto em Java.

---

## 🚀 Tecnologias Utilizadas

### **Backend**
* **Java** (JDK 17+)
* **Spring Boot**
* **Maven** (Gerenciamento de dependências e build)
* **Padrões de Projeto:** Strategy Pattern (para flexibilidade nos layouts de impressão)

### **Frontend**
* **HTML5 / CSS3**
* **JavaScript (Vanilla)** estruturado com módulos utilitários

---

## 📁 Estrutura do Projeto

```text
EtiquetasApp/
│
├── backend/
│   ├── src/main/java/br/com/espacovista/
│   │   ├── application/      # Classe principal de inicialização (Main.java)
│   │   ├── config/           # Configurações globais da aplicação
│   │   ├── controller/       # Endpoints REST (PrintController.java)
│   │   ├── model/            # Objetos de transferência e requisição
│   │   └── service/          # Regras de negócio e estratégias de impressão
│   │       └── strategies/   # Implementações de layouts de etiquetas
│   ├── src/main/resources/
│   │   ├── web/              # Interface Web (HTML, CSS, JS e Assets)
│   │   └── application.properties
│   └── pom.xml
└── img/                      # Recursos visuais e documentação de layout
⚙️ Arquitetura e Funcionalidades
O sistema utiliza o Padrão Strategy no backend para desacoplar as regras de negócio de diferentes tipos de impressões e layouts:

Simple Layout: Impressão de etiquetas padrão.

Validade Layout: Gestão de lotes e datas de validade.

Immediate Consumption: Foco em itens de consumo imediato.

A interface web comunica-se com a API REST interna para processar os dados e disparar os comandos de impressão de forma ágil e segura.

📥 Como Executar o Projeto
Pré-requisitos
Java JDK 17 ou superior instalado.

Maven configurado no PATH.

Passo a passo
Clone o repositório:

Bash
git clone [https://github.com/WillOli/EtiquetasApp.git](https://github.com/WillOli/EtiquetasApp.git)
Navegue até o diretório do backend:

Bash
cd EtiquetasApp/backend
Compile e empacote o projeto com o Maven:

Bash
mvn clean install
Execute a aplicação:

Bash
mvn spring-boot:run
Acesse a interface web através do navegador no endereço configurado (ex: http://localhost:8080).

🧪 Testes
Para executar a suíte de testes unitários e de integração implementados:

Bash
mvn test
