# EtiquetasApp

Aplicação Java/Javalin para imprimir etiquetas Simples, Validade, Consumo Imediato e Produção em uma Zebra com ZPL. Frontend HTML/CSS/JavaScript servido pelo próprio backend.

Desenvolvido por William Silva Oliveira.

## Desenvolvimento

JDK 21 recomendado; código compilado para Java 17. Maven 3.9+.

```sh
cd backend
mvn clean test
```

Os fontes permanecem em `src/main/br.com.espacovista` e os testes em `test`. A migração de diretórios é trabalho futuro, não requisito da V1.

A build final será feita pelo responsável pelo projeto:

```sh
mvn clean package
java -jar target/espaco-vista-printer-1.0.0.jar
```

Abra `http://localhost:8081/web/index.html`. O modo padrão serve os recursos internos do JAR, sem exigir a pasta `src`. O servidor atende somente em `127.0.0.1`. Alterar a porta também funciona no frontend, que usa URLs relativas.

## Configuração

Copie `config/application.example.properties` para `config/local.properties` e ajuste a máquina de destino. Carregue com:

```sh
java -Detiquetas.config.file=/caminho/absoluto/config/local.properties -jar /caminho/app.jar
```

Prioridade: propriedade JVM > variável de ambiente > arquivo externo > configuração incluída no JAR.

| Campo | Propriedade JVM | Ambiente |
| --- | --- | --- |
| Arquivo externo | `etiquetas.config.file` | `ETIQUETAS_CONFIG_FILE` |
| Porta | `etiquetas.server.port` | `ETIQUETAS_SERVER_PORT` |
| Impressora | `etiquetas.printer.name` | `ETIQUETAS_PRINTER_NAME` |
| Sequência | `etiquetas.sequence.file` | `ETIQUETAS_SEQUENCE_FILE` |
| Modo | `etiquetas.mode` | `ETIQUETAS_MODE` |
| Diretório web de desenvolvimento | `etiquetas.web.directory` | `ETIQUETAS_WEB_DIRECTORY` |

`mode=production` usa recursos do classpath; `mode=development` usa `web.directory`. A porta deve estar entre 1 e 65535; configuração inválida não é substituída silenciosamente.

A fila incluída corresponde à Zebra confirmada neste Mac: `Zebra_Technologies_ZTC_ZD230_203dpi_ZPL`. Para outra máquina, descubra e configure o nome exato:

```sh
java -jar app.jar --list-printers
```

Não há fallback para impressora padrão. No macOS, o envio continua raw pelo CUPS; nos demais sistemas usa Java Print Service. Sucesso significa aceitação pela fila, não confirmação física. Logs usam INFO; `ETIQUETAS_LOG_LEVEL=DEBUG` habilita ZPL para diagnóstico.

## Contrato da API

Quatro endpoints POST: `/print`, `/print-validade`, `/print-consumo-imediato`, `/print-producao`. Exigem `application/json`, corpo até 16 KiB e dados válidos. Requisições de outras origens são bloqueadas; frontend e backend usam a mesma origem.

- Quantidade inteira de 1 a 50. STANDARD representa pares, portanto 2 a 100 etiquetas físicas. SIXTY_TWO_MM representa a quantidade física solicitada.
- `labelType` obrigatório: STANDARD ou SIXTY_TWO_MM. Produção aceita somente SIXTY_TWO_MM.
- Produto até 80 caracteres e setor até 40. Acentos são preservados; controles, quebras, `^`, `~` e barra invertida viram espaços para impedir comandos ZPL. Esses limites não garantem legibilidade física para qualquer texto; confira nomes longos nos layouts homologados.
- Datas DD/MM/AAAA ou AAAA-MM-DD, calendário estrito e ordem cronológica válida. Datas ISO são exibidas em formato brasileiro.
- Horários HH:mm ou HH:mm:ss. Descarte pode atravessar a meia-noite; não se presume que seu horário seja maior que o de preparo.
- Respostas: 200 aceito pela fila; 400 dados inválidos; 403 origem; 413 corpo grande; 415 tipo de conteúdo; 503 impressora ou sequência indisponível; 500 falha inesperada. O frontend mostra texto, sem interpretar HTML retornado.

## Sequência e operação

**O arquivo ausente bloqueia impressão de Etiqueta Simples. Não reinicialize uma instalação já utilizada em 1.** O caminho padrão existente foi preservado: `~/EspacoVista/EtiquetasApp/data/sequence.txt`. Configure o caminho externo definitivo antes de usar.

Leia [operação e recuperação](docs/OPERACAO.md), [situação do backlog](docs/STATUS-V1.md) e [decisão sobre branches](docs/BRANCHES.md).

Os testes não enviam impressão física e não alteram a sequência operacional. A rotina visual foi conferida em outra porta, sem impressão. A build, instalação e validação física final ficam com o responsável, conforme combinado. O frontend ainda utiliza o CDN do Tailwind; a rede precisa estar disponível para carregar esses estilos.
