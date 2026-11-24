import { getAccessibilityPrefs, updateAccessibilityPrefs } from '../core/accessibility.js';
import { getViewContainer, setHeader, showToast } from '../core/ui.js';

export function renderAccessibilityView() {
	setHeader('Configurações de Acessibilidade & Alto Contraste (RF32)');
	const prefs = getAccessibilityPrefs();
	const isHighContrast = prefs.altoContraste;
	const isLarge = prefs.tamanhoFonte === 'large';
	const isEnhancedFocus = prefs.focoAcentuado;

	getViewContainer().innerHTML = `
		<div class="card card-padded">
			<div class="a11y-row">
				<div>
					<h3>Modo de Alto Contraste (WCAG 2.1 AAA)</h3>
					<p class="text-muted">Aumenta a relação de contraste das cores para fundo preto e detalhes amarelos vívidos (baixa visão).</p>
				</div>
				<div class="a11y-controls">
					<button type="button" id="toggle-contrast-btn" class="btn ${isHighContrast ? 'btn-primary' : 'btn-outline'}" aria-pressed="${isHighContrast}">
						${isHighContrast ? '✓ Ativado (Amarelo)' : 'Desativado'}
					</button>
				</div>
			</div>

			<div class="a11y-row">
				<div>
					<h3>Tamanho da Fonte da Interface</h3>
					<p class="text-muted">Ajusta globalmente a escala tipográfica de formulários, botões e tabelas no sistema.</p>
				</div>
				<div class="a11y-controls">
					<button type="button" id="font-normal-btn" class="btn ${isLarge ? 'btn-outline' : 'btn-primary'}" aria-pressed="${!isLarge}">Normal</button>
					<button type="button" id="font-large-btn" class="btn ${isLarge ? 'btn-primary' : 'btn-outline'}" aria-pressed="${isLarge}">Grande (A+)</button>
				</div>
			</div>

			<div class="a11y-row">
				<div>
					<h3>Indicadores de Foco Acentuados (Teclado)</h3>
					<p class="text-muted">Exibe bordas duplas em elementos selecionados via tecla TAB para navegação sem mouse.</p>
				</div>
				<div class="a11y-controls">
					<button type="button" id="toggle-focus-btn" class="btn ${isEnhancedFocus ? 'btn-primary' : 'btn-outline'}" aria-pressed="${isEnhancedFocus}">
						${isEnhancedFocus ? '✓ Habilitado' : 'Padrão'}
					</button>
				</div>
			</div>
		</div>
	`;

	document.getElementById('toggle-contrast-btn').addEventListener('click', async () => {
		await updateAccessibilityPrefs({ altoContraste: !isHighContrast });
		renderAccessibilityView();
		showToast('Preferência de alto contraste salva.');
	});

	document.getElementById('font-normal-btn').addEventListener('click', async () => {
		await updateAccessibilityPrefs({ tamanhoFonte: 'normal' });
		renderAccessibilityView();
		showToast('Tamanho de fonte ajustado para Normal.');
	});

	document.getElementById('font-large-btn').addEventListener('click', async () => {
		await updateAccessibilityPrefs({ tamanhoFonte: 'large' });
		renderAccessibilityView();
		showToast('Tamanho de fonte ajustado para Grande (A+).');
	});

	document.getElementById('toggle-focus-btn').addEventListener('click', async () => {
		await updateAccessibilityPrefs({ focoAcentuado: !isEnhancedFocus });
		renderAccessibilityView();
		showToast('Indicador de foco por teclado atualizado.');
	});
}
