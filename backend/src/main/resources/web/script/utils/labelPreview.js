const FIELD_IDS = [
    'labelText', 'labelSetor', 'labelFabDate', 'labelValDate',
    'productName', 'validityFabDate', 'validityOpenDate', 'validityExpirationDate',
    'immediateProductName', 'immediateFabDate', 'immediateValDate',
    'prod-product-name', 'prod-data-prep', 'prod-data-val', 'prod-hora-prep', 'prod-hora-desc'
];

const STORAGE_KEY = 'etiquetas-preview-style-v1';
const DEFAULT_STYLE = { fontScale: 100, alignment: 'center', spacing: 'normal', fontFamily: '0', border: 'none' };

function safeReadStyle() {
    try {
        const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}');
        return {
            fontScale: Number.isFinite(Number(saved.fontScale)) ? Math.min(125, Math.max(80, Number(saved.fontScale))) : DEFAULT_STYLE.fontScale,
            alignment: ['left', 'center', 'right'].includes(saved.alignment) ? saved.alignment : DEFAULT_STYLE.alignment,
            spacing: ['compact', 'normal', 'relaxed'].includes(saved.spacing) ? saved.spacing : DEFAULT_STYLE.spacing,
            fontFamily: ['0', 'A', 'D'].includes(saved.fontFamily) ? saved.fontFamily : DEFAULT_STYLE.fontFamily,
            border: ['solid', 'none'].includes(saved.border) ? saved.border : DEFAULT_STYLE.border,
        };
    } catch {
        return { ...DEFAULT_STYLE };
    }
}

function formatDate(value) {
    if (!value) return 'DD/MM/AAAA';
    const parts = value.split('-');
    return parts.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : value;
}

function formatTime(value) {
    if (!value) return '--h --min';
    const [hours, minutes] = value.split(':');
    return `${hours}h ${minutes}min`;
}

function currentValue(id, fallback) {
    const value = document.getElementById(id)?.value?.trim();
    return value || fallback;
}

function getPreviewLayout(mode, labelType) {
    if (mode === 'VALIDITY') {
        const fields = labelType === 'STANDARD'
            ? [
                ['Produto', currentValue('productName', 'Seu produto'), true],
                ['Fab.', formatDate(document.getElementById('validityFabDate')?.value)],
                ['Abert.', formatDate(document.getElementById('validityOpenDate')?.value)],
                ['Val.', formatDate(document.getElementById('validityExpirationDate')?.value)],
            ]
            : [
                ['Produto', currentValue('productName', 'Seu produto'), true],
                ['Fabricacao', formatDate(document.getElementById('validityFabDate')?.value)],
                ['Data Abertura', formatDate(document.getElementById('validityOpenDate')?.value)],
                ['Validade (aberto)', formatDate(document.getElementById('validityExpirationDate')?.value)],
            ];
        return {
            name: 'Etiqueta de Validade',
            heading: labelType === 'STANDARD' ? '' : 'ETIQUETA DE VALIDADE',
            fields,
        };
    }

    if (mode === 'IMMEDIATE_CONSUMPTION') {
        const fields = labelType === 'STANDARD'
            ? [
                ['Produto', currentValue('immediateProductName', 'Seu produto'), true],
                ['Fabricação', formatDate(document.getElementById('immediateFabDate')?.value)],
                ['Validade', formatDate(document.getElementById('immediateValDate')?.value)],
            ]
            : [
                ['Produto', currentValue('immediateProductName', 'Seu produto'), true],
                ['Data de Fabricação', formatDate(document.getElementById('immediateFabDate')?.value)],
                ['Validade', formatDate(document.getElementById('immediateValDate')?.value)],
            ];
        return {
            name: 'Consumo Imediato',
            heading: 'CONSUMO IMEDIATO',
            fields,
        };
    }

    if (mode === 'PRODUCTION') {
        return {
            name: 'Etiqueta de Produção',
            heading: 'ETIQUETA DE PRODUCAO',
            fields: [
                ['Produto', currentValue('prod-product-name', 'Seu produto'), true],
                ['Data Prep', formatDate(document.getElementById('prod-data-prep')?.value)],
                ['Validade', formatDate(document.getElementById('prod-data-val')?.value)],
                ['Hora Prep', formatTime(document.getElementById('prod-hora-prep')?.value)],
                ['Descarte', formatTime(document.getElementById('prod-hora-desc')?.value)],
            ],
        };
    }

    return {
        name: 'Etiqueta Simples',
        heading: '',
        fields: labelType === 'STANDARD'
            ? [
                ['Produto', currentValue('labelText', 'Seu produto').toLocaleUpperCase('pt-BR'), true],
                ['Setor', currentValue('labelSetor', 'CONFEITARIA').toLocaleUpperCase('pt-BR')],
                ['Fab.', formatDate(document.getElementById('labelFabDate')?.value)],
                ['Val.', formatDate(document.getElementById('labelValDate')?.value)],
                ['Reg.', '00001'],
            ]
            : [
                ['Produto', currentValue('labelText', 'Seu produto').toLocaleUpperCase('pt-BR'), true],
                ['Setor', currentValue('labelSetor', 'CONFEITARIA').toLocaleUpperCase('pt-BR')],
                ['Fabricação', formatDate(document.getElementById('labelFabDate')?.value)],
                ['Validade', formatDate(document.getElementById('labelValDate')?.value)],
                ['Registro', '00001'],
            ],
    };
}

function getPositionCount(mode, labelType) {
    return mode !== 'PRODUCTION' && labelType === 'STANDARD' ? 2 : 1;
}

function getFormatLabel(mode, labelType) {
    if (mode === 'PRODUCTION') return 'Etiqueta contínua · 80 × 30 mm';
    if (labelType === 'STANDARD') return 'Etiqueta dupla · 2 × 40 × 25 mm';
    if (mode === 'SIMPLE') return 'Etiqueta contínua · 103 × 30 mm';
    if (mode === 'VALIDITY' || mode === 'IMMEDIATE_CONSUMPTION') return 'Etiqueta contínua · 80 × 25 mm';
    return 'Etiqueta contínua';
}

export function getLabelPrintStyle() {
    const { fontScale, alignment, spacing, fontFamily, border } = safeReadStyle();
    return { fontScale, alignment, spacing, fontFamily, border };
}

export function initLabelPreview(getMode) {
    const preview = document.getElementById('previewPage');
    const previewLabels = document.getElementById('previewLabels');
    const fontScale = document.getElementById('previewFontScale');
    const alignment = document.getElementById('previewAlignment');
    const spacing = document.getElementById('previewSpacing');
    const fontFamily = document.getElementById('previewFontFamily');
    const border = document.getElementById('previewBorder');
    const reset = document.getElementById('resetPreviewStyle');
    const modeName = document.getElementById('previewModeName');
    const labelTypeName = document.getElementById('previewLabelType');
    const fontScaleValue = document.getElementById('previewFontScaleValue');
    if (!preview || !previewLabels || !fontScale || !alignment || !spacing || !fontFamily || !border) return () => {};

    let style = safeReadStyle();
    fontScale.value = String(style.fontScale);
    alignment.value = style.alignment;
    spacing.value = style.spacing;
    fontFamily.value = style.fontFamily;
    border.value = style.border;

    const saveStyle = () => {
        style = {
            fontScale: Number(fontScale.value),
            alignment: alignment.value,
            spacing: spacing.value,
            fontFamily: fontFamily.value,
            border: border.value,
        };
        try { localStorage.setItem(STORAGE_KEY, JSON.stringify(style)); } catch { /* Prévia continua disponível sem armazenamento. */ }
        preview.style.setProperty('--preview-font-scale', String(style.fontScale / 100));
        preview.dataset.alignment = style.alignment;
        preview.dataset.spacing = style.spacing;
        preview.dataset.fontFamily = style.fontFamily;
        preview.dataset.border = style.border;
        fontScaleValue.textContent = `${style.fontScale}%`;
        render();
    };

    const render = () => {
        const mode = getMode();
        const labelType = document.getElementById('labelType')?.value || 'SIXTY_TWO_MM';
        const layout = getPreviewLayout(mode, labelType);
        modeName.textContent = layout.name;
        labelTypeName.textContent = getFormatLabel(mode, labelType);
        previewLabels.replaceChildren();
        const count = getPositionCount(mode, labelType);
        previewLabels.dataset.count = String(count);
        previewLabels.dataset.format = count === 2 ? 'double' : 'continuous';
        preview.dataset.format = previewLabels.dataset.format;
        preview.dataset.mode = mode;
        preview.style.aspectRatio = mode === 'PRODUCTION' ? '8 / 3'
            : labelType === 'STANDARD'
            ? '3.3 / 1'
                    : mode === 'VALIDITY' || mode === 'IMMEDIATE_CONSUMPTION' ? '3.2 / 1'
                    : mode === 'SIMPLE' ? '103 / 30' : '3.3 / 1';

        for (let index = 0; index < count; index += 1) {
            const card = document.createElement('article');
            card.className = 'label-preview';
            card.dataset.position = String(index + 1);
            card.dataset.alignment = style.alignment;
            card.dataset.spacing = style.spacing;
            card.style.setProperty('--preview-font-scale', String(style.fontScale / 100));

            const position = document.createElement('span');
            position.className = 'label-preview-position';
            position.textContent = count > 1 ? `Posição ${index + 1}` : 'Etiqueta';
            card.appendChild(position);

            if (layout.heading) {
                const heading = document.createElement('div');
                heading.className = 'label-preview-heading';
                heading.textContent = layout.heading;
                card.appendChild(heading);
            }

            const fields = document.createElement('div');
            fields.className = 'label-preview-fields';
            layout.fields.forEach(([key, value]) => {
                const row = document.createElement('div');
                row.className = 'label-preview-row';
                if ((layout.heading || mode === 'SIMPLE') && key === 'Produto') {
                    const valueNode = document.createElement('span');
                    valueNode.className = 'label-preview-value';
                    valueNode.textContent = value;
                    row.appendChild(valueNode);
                } else {
                    const keyNode = document.createElement('span');
                    keyNode.className = 'label-preview-key';
                    keyNode.textContent = `${key}:`;
                    const valueNode = document.createElement('span');
                    valueNode.className = 'label-preview-value';
                    valueNode.textContent = value;
                    row.append(keyNode, valueNode);
                }
                fields.appendChild(row);
            });
            card.appendChild(fields);
            previewLabels.appendChild(card);
        }
    };

    [fontScale, alignment, spacing, fontFamily, border].forEach(control => {
        control.addEventListener('input', saveStyle);
        control.addEventListener('change', saveStyle);
    });
    reset?.addEventListener('click', () => {
        fontScale.value = String(DEFAULT_STYLE.fontScale);
        alignment.value = DEFAULT_STYLE.alignment;
        spacing.value = DEFAULT_STYLE.spacing;
        fontFamily.value = DEFAULT_STYLE.fontFamily;
        border.value = DEFAULT_STYLE.border;
        saveStyle();
    });
    FIELD_IDS.forEach(id => {
        const field = document.getElementById(id);
        field?.addEventListener('input', render);
        field?.addEventListener('change', render);
    });
    document.getElementById('labelType')?.addEventListener('change', render);

    saveStyle();
    return render;
}
