# Consolidação para V1

Referências atualizadas com `git fetch origin` em 02/10/2026. Base: `release/1.0-stabilization`.

`origin/main`, `origin/release/1.0-stabilization` e as branches de autocomplete, consumo imediato, produção, layout/log sequencial, refactor-consumo-imediato e unit-tests-strategies são ancestrais da base inspecionada.

A exceção é `origin/feature/ajuste-etiquetas`, com dois commits exclusivos:

- `c7e579f` — ajustes de logotipo, nomes e setor, de 01/04/2026.
- `f795d78` — layout adaptativo e regra Grupo Vista, de 02/04/2026.

Essa variante monta crachás com nome de pessoa, conectivos removidos e logotipo. Sua SimpleLayoutStrategy não inclui fabricação, validade e registro da etiqueta de alimentos atual; seu frontend substitui fluxos posteriores. Não foi mesclada, pois regrediria o escopo V1. A branch remota permanece preservada.

As alterações locais de validação e envio raw CUPS foram incorporadas. Nenhuma branch remota foi removida e nenhum push, merge em main ou tag de entrega foi realizado. A build e a publicação final ficam com o responsável.
