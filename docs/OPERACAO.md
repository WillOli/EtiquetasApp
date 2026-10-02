# Configuração, operação e recuperação

## Máquina de operação

1. Instale Java 17 ou superior; o desenvolvimento foi testado com JDK 21.
2. Faça sua build e coloque o JAR em uma pasta de aplicação. Dados e backups devem ficar em diretório separado, preservado nas atualizações.
3. Configure `printer.name` com a saída de `java -jar app.jar --list-printers` na própria máquina. A fila padrão não é utilizada.
4. Configure `sequence.file` para o arquivo já usado na operação. O padrão continua sendo a pasta `EspacoVista/EtiquetasApp/data` do usuário; não há migração automática.
5. Dê ao usuário normal da operação leitura/escrita na pasta da sequência.
6. Inicie com `-Detiquetas.config.file=...` antes de `-jar` e abra `http://localhost:PORTA/web/index.html`.

Exemplo Windows, ajustando os caminhos:

```bat
java -Detiquetas.config.file="C:/EtiquetasApp/config/local.properties" -jar "C:/EtiquetasApp/app.jar"
```

Um atalho pode apontar para esse comando. Inicialização pelo Agendador de Tarefas no logon deve usar a conta normal da operação e ser validada pelo responsável; não foi criada uma tarefa nem alterada a inicialização desta máquina. Redirecione stdout/stderr para um arquivo de diagnóstico se iniciar sem janela visível. Não iniciar duas cópias na mesma porta.

## Primeiro uso da sequência

Somente em instalação nova, sem etiquetas anteriores, inicialize em 1. Se já houver registros, preserve o arquivo existente ou use um próximo número conferido acima de todos os registros já emitidos.

```sh
java -Detiquetas.config.file=/caminho/local.properties -jar app.jar --initialize-sequence 1
```

O comando falha se o arquivo existir. Não remove nem substitui dados. O servidor nunca cria a sequência implicitamente durante uma impressão.

## Backup

Faça backups frequentes em destino separado. O diretório de destino deve existir e o nome de arquivo deve ser novo.

```sh
java -Detiquetas.config.file=/caminho/local.properties -jar app.jar --backup-sequence /backups/sequence-2026-10-02.txt
```

O backup usa o mesmo bloqueio das reservas e contém o próximo registro naquele momento. A cópia não inclui registros reservados depois; restaurar cegamente um backup antigo pode causar duplicação. Não há restauração regressiva automática.

## Perda ou corrupção

1. Interrompa a impressão e preserve o arquivo problemático, logs e backups.
2. Determine o maior registro já usado ou reservado, inclusive após o último backup. Considere etiquetas na fila e pedidos cujo resultado foi incerto.
3. Escolha um próximo número maior que todos eles. Se não puder comprovar isso, mantenha a operação bloqueada até conferir com o responsável.
4. Com a aplicação parada, mova o arquivo inválido para uma cópia de investigação; não descarte o original.
5. Use `--initialize-sequence NUMERO_CONFERIDO` no caminho configurado. O comando não sobrescreve arquivo existente.
6. Faça backup e valide a primeira etiqueta antes de liberar a operação.

Nunca reutilize automaticamente números de um envio que falhou: pode haver impressão parcial ou trabalho aceito pelo spooler. Lacunas são aceitáveis; duplicações não.

A implementação usa lock entre processos e troca atômica no mesmo diretório, com sincronização dos dados temporários antes da troca. Se o volume não suportar troca atômica, bloqueia. Use armazenamento local confiável; pastas de rede/sincronização e recuperação após perda de energia precisam de validação operacional específica.

## Problemas de impressão

- **Impressora não encontrada:** rode `--list-printers`; ajuste o nome exato. Confira driver e USB. Não há dependência da impressora padrão.
- **Fila pausada/indisponível:** confira papel, pausa e conexão. O estado informado pelo driver pode não refletir imediatamente a condição física.
- **Pedido aceito, nada saiu:** confira a fila antes de reenviar. Sucesso significa envio ao spooler.
- **Falha de sequência:** siga o procedimento acima, sem zerar o arquivo.
- **Servidor não inicia:** confira Java, porta ocupada, arquivo de configuração e logs. Arquivo externo inexistente falha na inicialização.
- **Página sem estilo:** confira acesso ao CDN Tailwind. O restante dos arquivos web é servido pelo JAR.

## Antes da release feita pelo responsável

- Executar `mvn clean test` e sua build.
- Conferir os quatro fluxos e layouts: quantidade 1, 2, 3 e 5, acentos, nomes longos, datas e registros.
- Conferir impressora desligada, sem papel, pausada, USB reconectado e mudança de impressora padrão.
- Reiniciar aplicação/computador e confirmar o próximo registro.
- Testar com o usuário normal e validar atalho/inicialização.
- Guardar artefato, SHA do commit, data, máquina, fila configurada e caminho da sequência.
- Criar RC/tag final e fazer merge para main após essa verificação.

O responsável informou que os fluxos físicos estão funcionando e assumiu a build. Esta lista registra critérios finais, não simula homologação executada pelo agente.
