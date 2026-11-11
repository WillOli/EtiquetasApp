const FIELD_IDS = [
    'labelText', 'labelSetor', 'labelFabDate', 'labelValDate',
    'productName', 'validityFabDate', 'validityOpenDate', 'validityExpirationDate',
    'immediateProductName', 'immediateFabDate', 'immediateValDate',
    'prod-product-name', 'prod-data-prep', 'prod-data-val', 'prod-hora-prep', 'prod-hora-desc'
];

const STORAGE_KEY = 'etiquetas-preview-style-v2';
const LEGACY_STORAGE_KEY = 'etiquetas-preview-style-v1';
const DEFAULT_STYLE = { fontScale: 100, alignment: 'center', firstLabelAlignment: 'center', secondLabelAlignment: 'center', offsetXmm: 0, offsetYmm: 0, firstLabelOffsetXmm: 0, firstLabelOffsetYmm: 0, secondLabelOffsetXmm: 0, secondLabelOffsetYmm: 0, spacing: 'normal', fontFamily: '0', border: 'none' };

function styleKey(mode, labelType) {
    return `${mode}:${labelType}`;
}

function safeReadStyle(mode, labelType) {
    try {
        const savedStyles = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}');
        const legacy = JSON.parse(localStorage.getItem(LEGACY_STORAGE_KEY) || '{}');
        const saved = savedStyles[styleKey(mode, labelType)] || legacy;
        const savedAlignment = ['left', 'center', 'right'].includes(saved.alignment) ? saved.alignment : DEFAULT_STYLE.alignment;
        return {
            fontScale: Number.isFinite(Number(saved.fontScale)) ? Math.min(125, Math.max(80, Number(saved.fontScale))) : DEFAULT_STYLE.fontScale,
            alignment: savedAlignment,
            firstLabelAlignment: ['left', 'center', 'right'].includes(saved.firstLabelAlignment) ? saved.firstLabelAlignment : savedAlignment,
            secondLabelAlignment: ['left', 'center', 'right'].includes(saved.secondLabelAlignment) ? saved.secondLabelAlignment : savedAlignment,
            offsetXmm: Number.isFinite(Number(saved.offsetXmm)) ? Math.min(10, Math.max(-10, Number(saved.offsetXmm))) : DEFAULT_STYLE.offsetXmm,
            offsetYmm: Number.isFinite(Number(saved.offsetYmm)) ? Math.min(10, Math.max(-10, Number(saved.offsetYmm))) : DEFAULT_STYLE.offsetYmm,
            firstLabelOffsetXmm: Number.isFinite(Number(saved.firstLabelOffsetXmm)) ? Math.min(6, Math.max(-6, Number(saved.firstLabelOffsetXmm))) : (Number(saved.offsetXmm) || 0),
            firstLabelOffsetYmm: Number.isFinite(Number(saved.firstLabelOffsetYmm)) ? Math.min(3, Math.max(-3, Number(saved.firstLabelOffsetYmm))) : (Number(saved.offsetYmm) || 0),
            secondLabelOffsetXmm: Number.isFinite(Number(saved.secondLabelOffsetXmm)) ? Math.min(6, Math.max(-6, Number(saved.secondLabelOffsetXmm))) : (Number(saved.offsetXmm) || 0),
            secondLabelOffsetYmm: Number.isFinite(Number(saved.secondLabelOffsetYmm)) ? Math.min(3, Math.max(-3, Number(saved.secondLabelOffsetYmm))) : (Number(saved.offsetYmm) || 0),
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
    const isDouble = ['STANDARD', 'DOUBLE_25X25'].includes(labelType);
    if (mode === 'VALIDITY') {
        const fields = isDouble
            ? [
                ['Produto', currentValue('productName', 'Seu produto'), true],
                [labelType === 'DOUBLE_25X25' ? 'Fab' : 'Fab.', formatDate(document.getElementById('validityFabDate')?.value)],
                [labelType === 'DOUBLE_25X25' ? 'Abert' : 'Abert.', formatDate(document.getElementById('validityOpenDate')?.value)],
                [labelType === 'DOUBLE_25X25' ? 'Val' : 'Val.', formatDate(document.getElementById('validityExpirationDate')?.value)],
            ]
            : [
                ['Produto', currentValue('productName', 'Seu produto'), true],
                ['Fabricacao', formatDate(document.getElementById('validityFabDate')?.value)],
                ['Data Abertura', formatDate(document.getElementById('validityOpenDate')?.value)],
                ['Validade (aberto)', formatDate(document.getElementById('validityExpirationDate')?.value)],
            ];
        return {
            name: 'Etiqueta de Validade',
            heading: isDouble ? '' : 'ETIQUETA DE VALIDADE',
            fields,
        };
    }

    if (mode === 'IMMEDIATE_CONSUMPTION') {
        const fields = isDouble
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
                [labelType === 'DOUBLE_25X25' ? 'Prep.' : 'Data Prep', formatDate(document.getElementById('prod-data-prep')?.value)],
                [labelType === 'DOUBLE_25X25' ? 'Val.' : 'Validade', formatDate(document.getElementById('prod-data-val')?.value)],
                [labelType === 'DOUBLE_25X25' ? 'H. Prep.' : 'Hora Prep', formatTime(document.getElementById('prod-hora-prep')?.value)],
                [labelType === 'DOUBLE_25X25' ? 'Desc.' : 'Descarte', formatTime(document.getElementById('prod-hora-desc')?.value)],
            ],
        };
    }

    return {
        name: 'Etiqueta Simples',
        heading: '',
        fields: isDouble
            ? [
                ['Produto', currentValue('labelText', 'Seu produto').toLocaleUpperCase('pt-BR'), true],
                ['Setor', currentValue('labelSetor', 'CONFEITARIA').toLocaleUpperCase('pt-BR')],
                ['Fab.', formatDate(document.getElementById('labelFabDate')?.value)],
                ['Val.', formatDate(document.getElementById('labelValDate')?.value)],
                ['Reg.', '00001'],
            ]
            : labelType === 'SIXTY_TWO_MM'
            ? [
                ['Produto', currentValue('labelText', 'Seu produto').toLocaleUpperCase('pt-BR'), true],
                ['SETOR', currentValue('labelSetor', 'CONFEITARIA').toLocaleUpperCase('pt-BR')],
                ['FAB.', formatDate(document.getElementById('labelFabDate')?.value)],
                ['VAL.', formatDate(document.getElementById('labelValDate')?.value)],
                ['REG.', '00001'],
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
    return ['STANDARD', 'DOUBLE_25X25'].includes(labelType) ? 2 : 1;
}

function getLabelDimensions(mode, labelType) {
    if (labelType === 'DOUBLE_25X25') return { width: 25, height: 25 };
    if (labelType === 'STANDARD') return { width: 40, height: 25 };
    if (labelType === 'SINGLE_30X50') return { width: 50, height: 30 };
    if (labelType === 'SIXTY_TWO_MM' && mode !== 'PRODUCTION') return { width: 80, height: 25 };
    if (labelType === 'SINGLE_25X50') return { width: 50, height: 25 };
    if (mode === 'PRODUCTION') return { width: 80, height: 30 };
    if (mode === 'SIMPLE') return { width: 103, height: 30 };
    return { width: 80, height: 25 };
}

function getFormatLabel(mode, labelType) {
    if (labelType === 'SIXTY_TWO_MM' && mode !== 'PRODUCTION') return 'Etiqueta simples contínua · 80 × 25 mm';
    if (labelType === 'SINGLE_25X50') return 'Etiqueta simples horizontal · 50 × 25 mm';
    if (labelType === 'DOUBLE_25X25') return 'Etiqueta dupla · 2 × 25 × 25 mm';
    if (labelType === 'SINGLE_30X50') return 'Etiqueta retangular horizontal · 50 × 30 mm';
    if (mode === 'PRODUCTION') return 'Etiqueta contínua · 80 × 30 mm';
    if (labelType === 'STANDARD') return 'Etiqueta dupla · 2 × 40 × 25 mm';
    if (mode === 'SIMPLE') return 'Etiqueta simples contínua · 80 × 25 mm';
    if (mode === 'VALIDITY' || mode === 'IMMEDIATE_CONSUMPTION') return 'Etiqueta contínua · 80 × 25 mm';
    return 'Etiqueta contínua';
}

export function getLabelPrintStyle(mode = 'SIMPLE') {
    const labelType = document.getElementById('labelType')?.value || 'SIXTY_TWO_MM';
    return safeReadStyle(mode, labelType);
}

export function initLabelPreview(getMode) {
    const preview = document.getElementById('previewPage');
    const previewLabels = document.getElementById('previewLabels');
    const fontScale = document.getElementById('previewFontScale');
    const alignment = document.getElementById('previewAlignment');
    const alignmentLabel = document.getElementById('previewAlignmentLabel');
    const firstLabelAlignment = document.getElementById('previewFirstLabelAlignment');
    const secondLabelAlignment = document.getElementById('previewSecondLabelAlignment');
    const firstLabelAlignmentLabel = document.getElementById('previewFirstLabelAlignmentLabel');
    const secondLabelAlignmentLabel = document.getElementById('previewSecondLabelAlignmentLabel');
    const offsetX = document.getElementById('previewOffsetX');
    const offsetY = document.getElementById('previewOffsetY');
    const doublePositionControls = document.getElementById('doublePositionControls');
    const applyDoubleAlignment = document.getElementById('applyDoubleAlignment');
    const individualOffsets = ['firstLabelOffsetXmm', 'firstLabelOffsetYmm', 'secondLabelOffsetXmm', 'secondLabelOffsetYmm'];
    const individualIds = ['previewFirstOffsetX', 'previewFirstOffsetY', 'previewSecondOffsetX', 'previewSecondOffsetY'];
    const individualControls = individualIds.map(id => document.getElementById(id));
    const individualValues = individualIds.map(id => document.getElementById(`${id}Value`));
    const offsetXLabel = document.getElementById('previewOffsetXLabel');
    const offsetYLabel = document.getElementById('previewOffsetYLabel');
    const spacing = document.getElementById('previewSpacing');
    const fontFamily = document.getElementById('previewFontFamily');
    const border = document.getElementById('previewBorder');
    const reset = document.getElementById('resetPreviewStyle');
    const modeName = document.getElementById('previewModeName');
    const labelTypeName = document.getElementById('previewLabelType');
    const fontScaleValue = document.getElementById('previewFontScaleValue');
    const offsetXValue = document.getElementById('previewOffsetXValue');
    const offsetYValue = document.getElementById('previewOffsetYValue');
    const labelTypeSelect = document.getElementById('labelType');
    if (!preview || !previewLabels || !fontScale || !alignment || !offsetX || !offsetY || !spacing || !fontFamily || !border) return () => {};

    let style = safeReadStyle(getMode(), labelTypeSelect?.value || 'SIXTY_TWO_MM');
    let activeStyleKey = '';

    const syncControls = () => {
        fontScale.value = String(style.fontScale);
        alignment.value = style.alignment;
        if (firstLabelAlignment) firstLabelAlignment.value = style.firstLabelAlignment;
        if (secondLabelAlignment) secondLabelAlignment.value = style.secondLabelAlignment;
        offsetX.value = String(style.offsetXmm);
        offsetY.value = String(style.offsetYmm);
        individualControls.forEach((control, index) => { if (control) control.value = String(style[individualOffsets[index]]); });
        spacing.value = style.spacing;
        fontFamily.value = style.fontFamily;
        border.value = style.border;
    };

    const saveStyle = () => {
        style = {
            fontScale: Number(fontScale.value),
            alignment: alignment.value,
            firstLabelAlignment: firstLabelAlignment?.value || style.firstLabelAlignment,
            secondLabelAlignment: secondLabelAlignment?.value || style.secondLabelAlignment,
            offsetXmm: Number(offsetX.value),
            offsetYmm: Number(offsetY.value),
            ...Object.fromEntries(individualOffsets.map((key, index) => [key, Number(individualControls[index]?.value ?? 0)])),
            spacing: spacing.value,
            fontFamily: fontFamily.value,
            border: border.value,
        };
        try {
            const savedStyles = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}');
            savedStyles[activeStyleKey] = style;
            localStorage.setItem(STORAGE_KEY, JSON.stringify(savedStyles));
        } catch { /* Prévia continua disponível sem armazenamento. */ }
        preview.style.setProperty('--preview-font-scale', String(style.fontScale / 100));
        preview.dataset.alignment = style.alignment;
        preview.dataset.spacing = style.spacing;
        preview.dataset.fontFamily = style.fontFamily;
        preview.dataset.border = style.border;
        preview.dataset.offsetX = String(style.offsetXmm);
        preview.dataset.offsetY = String(style.offsetYmm);
        fontScaleValue.textContent = `${style.fontScale}%`;
        offsetXValue.textContent = `${style.offsetXmm} mm`;
        offsetYValue.textContent = `${style.offsetYmm} mm`;
        individualValues.forEach((output, index) => { if (output) output.textContent = `${style[individualOffsets[index]]} mm`; });
        render();
    };

    const applyAlignmentToBothDoubleLabels = () => {
        const isDoubleLabel = ['STANDARD', 'DOUBLE_25X25'].includes(labelTypeSelect?.value);
        if (!isDoubleLabel) {
            saveStyle();
            return;
        }
        if (firstLabelAlignment) firstLabelAlignment.value = alignment.value;
        if (secondLabelAlignment) secondLabelAlignment.value = alignment.value;
        // Cada escolha comum começa com as duas etiquetas na mesma referência física.
        // Depois disso, os ajustes de posição voltam a ser independentes.
        if (labelTypeSelect.value === 'DOUBLE_25X25') {
            individualControls.forEach(control => { if (control) control.value = '0'; });
        } else {
            offsetX.value = '0';
            offsetY.value = '0';
        }
        saveStyle();
    };

    const render = () => {
        const mode = getMode();
        const labelType = labelTypeSelect?.value || 'SIXTY_TWO_MM';
        const nextStyleKey = styleKey(mode, labelType);
        if (nextStyleKey !== activeStyleKey) {
            activeStyleKey = nextStyleKey;
            style = safeReadStyle(mode, labelType);
            syncControls();
            preview.style.setProperty('--preview-font-scale', String(style.fontScale / 100));
            preview.dataset.alignment = style.alignment;
            preview.dataset.spacing = style.spacing;
            preview.dataset.fontFamily = style.fontFamily;
            preview.dataset.border = style.border;
            preview.dataset.offsetX = String(style.offsetXmm);
            preview.dataset.offsetY = String(style.offsetYmm);
            fontScaleValue.textContent = `${style.fontScale}%`;
            offsetXValue.textContent = `${style.offsetXmm} mm`;
            offsetYValue.textContent = `${style.offsetYmm} mm`;
            individualValues.forEach((output, index) => { if (output) output.textContent = `${style[individualOffsets[index]]} mm`; });
        }
        const isDouble25 = labelType === 'DOUBLE_25X25';
        const isDoubleLabel = ['STANDARD', 'DOUBLE_25X25'].includes(labelType);
        const isValidityDoubleLabel = mode === 'VALIDITY' && isDouble25;
        if (doublePositionControls) doublePositionControls.hidden = !isDouble25;
        if (firstLabelAlignmentLabel) firstLabelAlignmentLabel.hidden = !isValidityDoubleLabel;
        if (firstLabelAlignment) firstLabelAlignment.hidden = !isValidityDoubleLabel;
        if (secondLabelAlignmentLabel) secondLabelAlignmentLabel.hidden = !isValidityDoubleLabel;
        if (secondLabelAlignment) secondLabelAlignment.hidden = !isValidityDoubleLabel;
        const sharedDoubleAlignment = isDoubleLabel;
        if (alignmentLabel) {
            alignmentLabel.hidden = isDouble25 && !sharedDoubleAlignment;
            alignmentLabel.textContent = sharedDoubleAlignment
                ? 'Aplicar alinhamento às duas posições'
                : 'Alinhamento';
        }
        alignment.hidden = isDouble25 && !sharedDoubleAlignment;
        if (applyDoubleAlignment) applyDoubleAlignment.hidden = !isDoubleLabel;
        if (offsetXLabel) offsetXLabel.hidden = isDouble25;
        offsetX.hidden = isDouble25;
        if (offsetYLabel) offsetYLabel.hidden = isDouble25;
        offsetY.hidden = isDouble25;
        const layout = getPreviewLayout(mode, labelType);
        modeName.textContent = layout.name;
        labelTypeName.textContent = getFormatLabel(mode, labelType);
        previewLabels.replaceChildren();
        const count = getPositionCount(mode, labelType);
        const dimensions = getLabelDimensions(mode, labelType);
        previewLabels.dataset.count = String(count);
        previewLabels.dataset.format = count === 2 ? 'double' : 'continuous';
        previewLabels.dataset.type = labelType;
        preview.dataset.format = previewLabels.dataset.format;
        preview.dataset.mode = mode;
        preview.dataset.labelType = labelType;
        preview.dataset.orientation = 'landscape';
        preview.style.aspectRatio = labelType === 'SIXTY_TWO_MM' && mode !== 'PRODUCTION' ? '16 / 5'
            : labelType === 'SINGLE_25X50' ? '2 / 1'
            : labelType === 'DOUBLE_25X25' ? '2.1 / 1'
            : labelType === 'SINGLE_30X50' ? '5 / 3'
            : mode === 'PRODUCTION' ? '8 / 3'
            : labelType === 'STANDARD'
            ? '3.3 / 1'
                    : mode === 'VALIDITY' || mode === 'IMMEDIATE_CONSUMPTION' ? '3.2 / 1'
                    : mode === 'SIMPLE' ? '103 / 30' : '3.3 / 1';

        for (let index = 0; index < count; index += 1) {
            const card = document.createElement('article');
            card.className = 'label-preview';
            card.dataset.position = String(index + 1);
            if (mode === 'SIMPLE' && labelType === 'SIXTY_TWO_MM') card.dataset.layout = 'simple-80x25';
            if (mode === 'VALIDITY' && labelType === 'SIXTY_TWO_MM') card.dataset.layout = 'validity-80x25';
            if (mode === 'IMMEDIATE_CONSUMPTION' && labelType === 'SIXTY_TWO_MM') card.dataset.layout = 'immediate-80x25';
            if (mode === 'PRODUCTION' && labelType === 'SIXTY_TWO_MM') card.dataset.layout = 'production-80x30';
            if (labelType === 'DOUBLE_25X25') card.dataset.layout = `double-25x25-${mode.toLowerCase()}`;
            const cardAlignment = isValidityDoubleLabel
                ? (index === 0 ? style.firstLabelAlignment : style.secondLabelAlignment)
                : style.alignment;
            card.dataset.alignment = cardAlignment;
            card.dataset.spacing = style.spacing;
            card.style.setProperty('--preview-font-scale', String(style.fontScale / 100));
            card.style.overflow = 'hidden';

            const position = document.createElement('span');
            position.className = 'label-preview-position';
            position.textContent = count > 1 ? `Posição ${index + 1}` : 'Etiqueta';
            card.appendChild(position);

            const content = document.createElement('div');
            content.className = 'label-preview-content';
            const edgeSafeSimple = mode === 'SIMPLE' && labelType === 'SIXTY_TWO_MM';
            const validityContinuous = mode === 'VALIDITY' && labelType === 'SIXTY_TWO_MM';
            const validityDouble = mode === 'VALIDITY' && labelType === 'DOUBLE_25X25';
            const immediateContinuous = mode === 'IMMEDIATE_CONSUMPTION' && labelType === 'SIXTY_TWO_MM';
            const productionContinuous = mode === 'PRODUCTION' && labelType === 'SIXTY_TWO_MM';
            const edgeSafeDouble = labelType === 'DOUBLE_25X25';
            const printableWidthMm = dimensions.width - (edgeSafeSimple || edgeSafeDouble ? 6 : 0);
            const printableHeightMm = dimensions.height - (edgeSafeSimple ? 2 : 0);
            const cardOffsetX = edgeSafeDouble
                ? Math.max(-3, Math.min(3, style[individualOffsets[index < 1 ? 0 : 2]]))
                : style.offsetXmm;
            const cardOffsetY = edgeSafeDouble ? style[individualOffsets[index < 1 ? 1 : 3]] : style.offsetYmm;
            content.style.transform = `translate(${cardOffsetX / printableWidthMm * 100}%, ${cardOffsetY / printableHeightMm * 100}%)`;

            if (layout.heading) {
                const heading = document.createElement('div');
                heading.className = 'label-preview-heading';
                heading.textContent = layout.heading;
                content.appendChild(heading);
            }

            const fields = document.createElement('div');
            fields.className = 'label-preview-fields';
            layout.fields.forEach(([key, value], rowIndex) => {
                const row = document.createElement('div');
                row.className = 'label-preview-row';
                if (key === 'Produto' && (validityDouble
                    || ((layout.heading || mode === 'SIMPLE')
                        && !(validityContinuous || immediateContinuous || productionContinuous)))) {
                    const valueNode = document.createElement('span');
                    valueNode.className = 'label-preview-value';
                    valueNode.textContent = value;
                    row.appendChild(valueNode);
                } else if (validityDouble) {
                    const valueNode = document.createElement('span');
                    valueNode.className = 'label-preview-value';
                    valueNode.textContent = `${key} ${value}`;
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
                if (card.dataset.layout === 'simple-80x25' || validityContinuous || immediateContinuous || productionContinuous || card.dataset.layout?.startsWith('double-25x25-')) {
                    const spacingFactor = style.spacing === 'compact' ? 0.8 : style.spacing === 'relaxed' ? 1.05 : 1;
                    const text = rowIndex === 0 && !validityContinuous && !immediateContinuous && !productionContinuous
                        ? String(value || '')
                        : `${key}${validityDouble ? ' ' : ': '}${value || ''}`;
                    const maximumFont = card.dataset.layout === 'simple-80x25'
                        ? rowIndex === 0 ? 30 : 22
                        : validityContinuous ? 22
                        : validityDouble ? (rowIndex === 0 ? 24 : 18) * 100 / style.fontScale
                        : immediateContinuous ? 25
                        : productionContinuous ? 22
                        : mode === 'SIMPLE' ? rowIndex === 0 ? 24 : 14
                        : mode === 'PRODUCTION' ? 24 : 15;
                    const productionColumn = productionContinuous && rowIndex !== 0;
                    const availableWidth = validityDouble ? 152 : productionContinuous ? (productionColumn ? 290 : 600)
                        : card.dataset.layout === 'simple-80x25' || validityContinuous || immediateContinuous ? 592 : 152;
                    const estimatedFont = text.length === 0 ? maximumFont
                        : Math.floor(availableWidth * (validityDouble ? 0.93 : 0.88)
                            / (text.length * (validityDouble ? 0.58 : 0.62)));
                    let fittedFont = validityDouble
                        ? Math.max(8, Math.min(maximumFont, Math.round(estimatedFont * 100 / style.fontScale)))
                        : Math.max(10, Math.min(maximumFont, estimatedFont));
                    let validityTitleLines = 1;
                    if (validityDouble && rowIndex === 0) {
                        for (let candidate = Math.max(10, Math.floor(24 * 100 / style.fontScale)); candidate >= 8; candidate--) {
                            const printedFont = Math.max(8, Math.round(candidate * style.fontScale / 100));
                            const requiredLines = Math.max(1, Math.ceil(text.length * printedFont * 0.58 / (availableWidth * 0.93)));
                            if (requiredLines <= 4 && requiredLines * printedFont <= 44) {
                                fittedFont = candidate;
                                validityTitleLines = requiredLines;
                                break;
                            }
                        }
                    }
                    row.style.position = 'absolute';
                    const isContinuous80 = card.dataset.layout === 'simple-80x25';
                    const isValidityContinuous = validityContinuous;
                    const isImmediateContinuous = immediateContinuous;
                    const isProductionContinuous = productionContinuous;
                    const doubleY = mode === 'SIMPLE' ? [18, 54, 90, 126, 162][rowIndex]
                        : mode === 'VALIDITY' ? [20, 66, 112, 158][rowIndex]
                        : mode === 'IMMEDIATE_CONSUMPTION' ? [66, 112, 158][rowIndex]
                        : [45, 72, 99, 126, 153][rowIndex];
                    const yPercent = isContinuous80 ? rowIndex * 36 * spacingFactor / 184 * 100
                        : isValidityContinuous ? (52 + rowIndex * 32) * spacingFactor / 200 * 100
                        : isImmediateContinuous ? [55, 95, 135][rowIndex] * spacingFactor / 200 * 100
                        : isProductionContinuous ? (48 + rowIndex * 38) * spacingFactor / 240 * 100
                        : doubleY * spacingFactor / 200 * 100;
                    row.style.top = `${yPercent}%`;
                    row.style.height = `${isContinuous80 ? 36 / 184 * 100 : validityContinuous || isImmediateContinuous ? 28 / 200 * 100 : isProductionContinuous ? 30 / 240 * 100 : mode === 'PRODUCTION' && edgeSafeDouble ? 27 / 200 * 100 : 18}%`;
                    row.style.flexWrap = 'nowrap';
                    row.style.whiteSpace = 'nowrap';
                    row.style.overflow = 'hidden';
                    row.style.lineHeight = '1';
                    row.dataset.fontDots = String(fittedFont * style.fontScale / 100);
                    if (edgeSafeDouble) {
                        row.style.left = '0';
                        row.style.width = '100%';
                        row.style.justifyContent = cardAlignment === 'right' ? 'flex-end'
                            : cardAlignment === 'center' ? 'center' : 'flex-start';
                        row.style.textAlign = cardAlignment;
                        if (validityDouble && rowIndex === 0) row.style.whiteSpace = 'normal';
                    }
                    if (validityDouble) {
                        const baseY = [20, 66, 112, 158][rowIndex] * spacingFactor + cardOffsetY * 8;
                        const lineHeight = Number(row.dataset.fontDots);
                        const rowHeight = lineHeight * (rowIndex === 0 ? validityTitleLines : 1);
                        const safeY = Math.max(0, Math.min(200 - rowHeight, baseY));
                        row.style.top = `${safeY / 200 * 100}%`;
                        row.style.height = `${rowHeight / 200 * 100}%`;
                    }
                }
                fields.appendChild(row);
            });
            if (productionContinuous) {
                const spacingFactor = style.spacing === 'compact' ? 0.8 : style.spacing === 'relaxed' ? 1.05 : 1;
                content.style.position = 'absolute';
                content.style.left = '0';
                content.style.right = '0';
                content.style.top = '0';
                content.style.width = '100%';
                content.style.height = '100%';
                fields.style.position = 'relative';
                fields.style.height = '100%';
                const heading = content.querySelector('.label-preview-heading');
                if (heading) {
                    heading.style.position = 'absolute';
                    heading.style.left = `${20 / 640 * 100}%`;
                    heading.style.top = `${10 * spacingFactor / 240 * 100}%`;
                    heading.style.width = `${600 / 640 * 100}%`;
                    heading.style.height = `${30 / 240 * 100}%`;
                    heading.style.overflow = 'hidden';
                    heading.dataset.fontDots = String(24 * style.fontScale / 100);
                }
                card.querySelectorAll('.label-preview-row').forEach((row, rowIndex) => {
                    row.style.left = `${20 / 640 * 100}%`;
                    row.style.width = `${600 / 640 * 100}%`;
                    row.style.textAlign = 'center';
                });
            } else if (card.dataset.layout === 'simple-80x25') {
                content.style.position = 'absolute';
                content.style.left = '3.75%';
                content.style.right = '3.75%';
                content.style.top = '4%';
                content.style.width = 'auto';
                content.style.height = '92%';
                fields.style.position = 'relative';
                fields.style.height = '100%';
            } else if (validityContinuous) {
                const offsetX = style.offsetXmm * 8;
                const spacingFactor = style.spacing === 'compact' ? 0.8 : style.spacing === 'relaxed' ? 1.05 : 1;
                content.style.position = 'absolute';
                content.style.left = `${(24 + offsetX) / 640 * 100}%`;
                content.style.right = 'auto';
                content.style.width = `${Math.max(1, 592 - offsetX) / 640 * 100}%`;
                content.style.top = `${style.offsetYmm / 25 * 100}%`;
                content.style.height = '100%';
                content.style.transform = 'none';
                fields.style.position = 'relative';
                fields.style.height = '100%';
                const heading = content.querySelector('.label-preview-heading');
                if (heading) {
                    heading.style.position = 'absolute';
                    heading.style.top = `${18 * spacingFactor / 200 * 100}%`;
                    heading.style.width = '100%';
                    heading.style.height = '12%';
                    heading.style.overflow = 'hidden';
                    heading.dataset.fontDots = String(22 * style.fontScale / 100);
                }
            } else if (immediateContinuous) {
                const offsetX = style.offsetXmm * 8;
                const spacingFactor = style.spacing === 'compact' ? 0.8 : style.spacing === 'relaxed' ? 1.05 : 1;
                content.style.position = 'absolute';
                content.style.left = `${(24 + offsetX) / 640 * 100}%`;
                content.style.right = 'auto';
                content.style.width = `${Math.max(1, 592 - offsetX) / 640 * 100}%`;
                content.style.top = `${style.offsetYmm / 25 * 100}%`;
                content.style.height = '100%';
                content.style.transform = 'none';
                fields.style.position = 'relative';
                fields.style.height = '100%';
                const heading = content.querySelector('.label-preview-heading');
                if (heading) {
                    heading.style.position = 'absolute';
                    heading.style.top = `${15 * spacingFactor / 200 * 100}%`;
                    heading.style.width = '100%';
                    heading.style.height = '16%';
                    heading.style.overflow = 'hidden';
                    heading.dataset.fontDots = String(30 * style.fontScale / 100);
                }
            } else if (card.dataset.layout?.startsWith('double-25x25-')) {
                content.style.position = 'absolute';
                content.style.left = validityDouble ? `${(24 + cardOffsetX * 8) / 200 * 100}%` : '12%';
                content.style.right = validityDouble ? 'auto' : '12%';
                content.style.top = '0';
                content.style.width = validityDouble ? '76%' : 'auto';
                content.style.height = '100%';
                if (validityDouble) content.style.transform = 'none';
                fields.style.position = 'relative';
                fields.style.height = '100%';
                const heading = content.querySelector('.label-preview-heading');
                if (heading && (mode === 'IMMEDIATE_CONSUMPTION' || mode === 'PRODUCTION')) {
                    const topDots = mode === 'PRODUCTION' ? 18 : 20;
                    const maximumFont = mode === 'PRODUCTION' ? 24 : 15;
                    const headingText = heading.textContent || '';
                    const fittedFont = Math.max(10, Math.min(maximumFont,
                        Math.floor(152 * 0.88 / (Math.max(1, headingText.length) * 0.62))));
                    heading.style.position = 'absolute';
                    heading.style.top = `${topDots / 200 * 100}%`;
                    heading.style.width = '100%';
                    heading.style.height = '15%';
                    heading.style.overflow = 'hidden';
                    heading.style.textAlign = cardAlignment;
                    heading.dataset.fontDots = String(fittedFont * style.fontScale / 100);
                }
            }
            content.appendChild(fields);
            card.appendChild(content);
            previewLabels.appendChild(card);
            if (card.dataset.layout === 'simple-80x25' || validityContinuous || immediateContinuous || productionContinuous || card.dataset.layout?.startsWith('double-25x25-')) {
                card.querySelectorAll('.label-preview-row').forEach(row => {
                    const printableHeightDots = card.dataset.layout === 'simple-80x25' ? 184 : productionContinuous ? 240 : 200;
                    row.style.fontSize = `${content.clientHeight * Number(row.dataset.fontDots) / printableHeightDots}px`;
                });
                const heading = content.querySelector('.label-preview-heading[data-font-dots]');
                if (heading) heading.style.fontSize = `${content.clientHeight * Number(heading.dataset.fontDots) / (productionContinuous ? 240 : 200)}px`;
            }
        }
    };

    [fontScale, firstLabelAlignment, secondLabelAlignment, offsetX, offsetY, ...individualControls, spacing, fontFamily, border].filter(Boolean).forEach(control => {
        control.addEventListener('input', saveStyle);
        control.addEventListener('change', saveStyle);
    });
    alignment.addEventListener('input', applyAlignmentToBothDoubleLabels);
    alignment.addEventListener('change', applyAlignmentToBothDoubleLabels);
    applyDoubleAlignment?.addEventListener('click', applyAlignmentToBothDoubleLabels);
    reset?.addEventListener('click', () => {
        fontScale.value = String(DEFAULT_STYLE.fontScale);
        alignment.value = DEFAULT_STYLE.alignment;
        if (firstLabelAlignment) firstLabelAlignment.value = DEFAULT_STYLE.firstLabelAlignment;
        if (secondLabelAlignment) secondLabelAlignment.value = DEFAULT_STYLE.secondLabelAlignment;
        offsetX.value = String(DEFAULT_STYLE.offsetXmm);
        offsetY.value = String(DEFAULT_STYLE.offsetYmm);
        individualControls.forEach((control, index) => { if (control) control.value = String(DEFAULT_STYLE[individualOffsets[index]]); });
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
    labelTypeSelect?.addEventListener('change', render);

    activeStyleKey = styleKey(getMode(), labelTypeSelect?.value || 'SIXTY_TWO_MM');
    syncControls();
    saveStyle();
    return render;
}
