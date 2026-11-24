import { STORAGE_KEYS, sessionStore } from './httpClient.js';
import { preferenceApi } from '../services/hmsApi.js';

const prefs = {
	altoContraste: false,
	tamanhoFonte: 'normal',
	focoAcentuado: true
};

export function getAccessibilityPrefs() {
	return { ...prefs };
}

export function loadLocalAccessibilityPrefs() {
	const raw = localStorage.getItem(STORAGE_KEYS.PREFS);
	if (raw) {
		Object.assign(prefs, JSON.parse(raw));
	}
	applyAccessibilityPrefs();
}

export function applyAccessibilityPrefs() {
	const root = document.documentElement;
	if (prefs.altoContraste) {
		root.dataset.theme = 'high-contrast';
	} else {
		delete root.dataset.theme;
	}
	root.dataset.fontSize = prefs.tamanhoFonte === 'large' ? 'large' : 'normal';
	root.dataset.focusVisible = prefs.focoAcentuado ? 'enhanced' : 'standard';
	localStorage.setItem(STORAGE_KEYS.PREFS, JSON.stringify(prefs));

	const badge = document.getElementById('sidebar-mode-badge');
	if (badge) {
		badge.textContent = prefs.altoContraste
			? 'Modo de Exibição: Alto Contraste'
			: 'Modo de Exibição: Padrão';
	}
	const navA11y = document.querySelector('.nav-item[data-view="accessibility"]');
	if (navA11y) {
		navA11y.textContent = prefs.altoContraste ? 'Acessibilidade (Ativo)' : 'Acessibilidade';
	}
}

export async function syncRemoteAccessibilityPrefs() {
	try {
		const remote = await preferenceApi.get();
		if (remote) {
			prefs.altoContraste = Boolean(remote.altoContraste);
			prefs.tamanhoFonte = remote.tamanhoFonte || 'normal';
			prefs.focoAcentuado = Boolean(remote.focoAcentuado);
			applyAccessibilityPrefs();
		}
	} catch (err) {
		console.warn('Usando preferências locais:', err.message);
	}
}

export async function updateAccessibilityPrefs(partial) {
	Object.assign(prefs, partial);
	applyAccessibilityPrefs();
	if (sessionStore.getAccessToken()) {
		try {
			await preferenceApi.save(prefs);
		} catch (err) {
			console.warn('Preferências salvas localmente:', err.message);
		}
	}
}
