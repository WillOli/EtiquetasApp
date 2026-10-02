# Backlog V1 — fechamento do desenvolvimento em 02/10/2026

O responsável pediu a execução do backlog e depois delimitou: **concluir a parte de desenvolvimento; ele fará a build**. Também informou que os fluxos físicos estão funcionando. Este documento registra o código verificado, sem declarar como executados testes presenciais, release ou instalação que ficaram com ele.

## Entrega técnica

- Branch de trabalho: `release/1.0-stabilization`.
- Alterações locais anteriores de validação e CUPS preservadas e continuadas.
- Commit separado de limpeza do Git: `93c1ba9`; arquivos locais de IDE e macOS preservados.
- Testes: `mvn clean test` com JDK 21, alvo Java 17. Nesta rodada não foi gerada build de distribuição.
- Sem push, merge em main, tag RC ou tag final.

## Fases do backlog

| Fase | Estado no escopo atual | Evidência / observação |
| --- | --- | --- |
| 1 — Consolidação | Concluída a análise técnica | Fetch realizado; main e branches aplicáveis contidas na estabilização. Variante antiga de crachás mantida fora; decisão em BRANCHES.md. |
| 2 — Validade | Desenvolvimento concluído | Fabricação, abertura e validade nos DTOs e layouts; ordem validada; datas brasileiras nas etiquetas. Confirmação física fornecida pelo responsável. |
| 3 — Sequência | Desenvolvimento concluído | Quantidade física reservada, sem fallback; testes de 1/2/3/5, consecutivas, concorrência e nova JVM. |
| 4 — Armazenamento | Desenvolvimento concluído | Caminho externo configurável, arquivo ausente bloqueia, lock entre processos, gravação sincronizada e troca atômica. Inicialização explícita e backup sem sobrescrever; recuperação documentada. |
| 5 — Validações | Concluída | Quatro endpoints, obrigatoriedade, quantidade 1–50, trim, datas e horários estritos, cronologia e Produção somente simples. |
| 6 — ZPL | Concluída | ZplSanitizer aplicado nas fronteiras de campos dos sete layouts; comandos/controles/quebras neutralizados, acentos preservados, limites de produto/setor e testes de injeção. |
| 7 — Impressora | Desenvolvimento concluído | printer.name, seleção por nome exato, ausência/pausa tratadas, sem dependência da padrão. Fila Mac consultada; CUPS raw preservado. Situações físicas ficam com o responsável. |
| 8 — PrinterService | Concluída | ZPL em DEBUG, logs INFO de fila, causa preservada, interrupção restaurada, erros compreensíveis e testes com envio simulado. |
| 9 — Controller | Concluída | Tratamento comum dos quatro endpoints, JSON estrito, 400/500/503, corpo do pedido fora dos logs e erro técnico fora das respostas. |
| 10 — Strategies antigas | Concluída | ImmediateConsumptionStrategy e teste removidos após verificar referências; construtores de compatibilidade sem consumidores produtivos removidos. |
| 11 — Simple layouts | Desenvolvimento concluído | Quantidade física, registros e datas testados; renderização repetida não muda registros. Coordenadas preservadas. Diferença entre layouts documentada abaixo. |
| 12 — labelType | Concluída | Obrigatório em todos os pedidos, sem fallback; null, ausente e valor desconhecido rejeitados. |
| 13 — Git | Concluída | target/IDE/macOS ignorados; arquivos locais removidos somente do índice; compilação não cria mudanças rastreadas nesses diretórios. |
| 14 — Dev/produção | Desenvolvimento concluído | Modo explícito, frontend no classpath, configuração externa e porta configurável. Aplicação iniciada a partir de /private/tmp para verificar independência da pasta de fontes. Build final a cargo do responsável. |
| 15 — CORS/local | Concluída | anyHost removido; servidor em 127.0.0.1, API relativa, verificação de Origin e application/json. Testes de bloqueio e da mesma origem. |
| 16 — Testes | Concluída no escopo técnico | Suíte de validação, ZPL, layouts, seleção de impressora, falhas CUPS, armazenamento, concorrência e servidor. Sem impressão física nos testes. |
| 17 — Zebra física | Responsável | Funcionamento informado por ele; não foram simuladas evidências de todos os itens do checklist. |
| 18 — Erros reais | Desenvolvimento concluído; operação com responsável | Erros de entrada, armazenamento, fila simulada e timeout cobertos. Falta de papel/USB/reinício do computador exigem equipamento. |
| 19 — Instalação | Preparação técnica concluída; build/instalação com responsável | Configuração de exemplo e manual. Não foi instalado atalho nem alterada inicialização do computador. |
| 20 — Usuários | Responsável | Depende de observação e aceite de operadores; não declarada concluída pelo agente. |
| 21 — RC | Responsável após build | Sem criar artefato/tag ou congelamento fictício antes do aceite. |
| 22 — V1 | Responsável após build e aceite | Manual e procedimentos prontos; merge/tag/artefato final não executados. |

## Decisões de comportamento

- STANDARD: quantidade solicitada representa pares, com duas etiquetas físicas por unidade. SIXTY_TWO_MM representa o total físico; no formulário Simples, SimpleLayoutStrategy distribui esse total em até duas colunas por página, sem dobrá-lo. O nome legado do enum foi preservado para evitar romper frontend/API.
- Produção e Consumo Imediato simples agora repetem a quantidade correta; não geram sempre uma única etiqueta.
- Validação aceita datas ISO e brasileiras; horários com minutos ou segundos. Descarte pode atravessar meia-noite; não foi inventada uma restrição de horário.
- Segurança de texto: produto até 80 caracteres, setor até 40, pedidos até 16 KiB; metacaracteres ZPL e controles viram espaços. Acentos permanecem em UTF-8 com ^CI28.
- A mensagem de sucesso indica envio à fila. Estado da impressora depende das informações do driver, sem alegar confirmação física.
- Sequência já existente é preservada. Ausência exige inicialização administrativa; recuperação nunca escolhe automaticamente um número anterior.

## Limites conhecidos

- Limites de caracteres evitam pedidos excessivos; a legibilidade de nomes longos depende da dimensão física e da homologação do layout. Não foram redesenhadas coordenadas já aprovadas.
- A folha de estilos utilitária ainda usa o CDN Tailwind. Uso totalmente offline exige empacotar esses estilos em trabalho separado.
- A garantia de sequência pressupõe armazenamento local confiável e uso do mesmo arquivo. Pastas de rede/sincronização, exclusão manual de locks durante execução e perda de energia precisam de procedimentos operacionais; não se promete tolerância absoluta a perda de disco.
- Os testes do comando CUPS usam scripts simulados em macOS/Linux e são específicos dessas plataformas; a validação do driver Windows continua com o responsável pela máquina.

## V1.1

Migração de diretórios/packages, divisão do JavaScript em módulos, Factory pura/Application Service, renomeação de enums, cobertura/CI, histórico persistente e banco de dados permanecem fora da V1 conforme o backlog original. Concluir a V1 não significa antecipar funcionalidades da V1.1.

## Verificação final desta entrega

`mvn clean test`: **238 testes, 0 falhas, 0 erros, 0 ignorados** em 02/10/2026, JDK 21. O agente Byte Buddy do Mockito é carregado na inicialização da JVM de testes para evitar dependência de attach dinâmico. `git diff --check` passou.

Verificação de interface em `127.0.0.1:18881`, com servidor iniciado em `/private/tmp`: página carregou; Produção desabilitou layout duplo; quantidade 51 foi rejeitada com mensagem legível sem imprimir. Nenhum pedido físico foi enviado pelo agente nesta etapa.
