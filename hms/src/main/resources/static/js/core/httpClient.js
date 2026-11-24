export const STORAGE_KEYS = {
	ACCESS_TOKEN: 'hms_access_token',
	REFRESH_TOKEN: 'hms_refresh_token',
	USER: 'hms_user',
	PREFS: 'hms_a11y_prefs'
};

export const sessionStore = {
	getAccessToken: () => localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN),
	getRefreshToken: () => localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN),
	getUser() {
		const raw = localStorage.getItem(STORAGE_KEYS.USER);
		return raw ? JSON.parse(raw) : null;
	},
	save(authData) {
		if (authData.accessToken) {
			localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, authData.accessToken);
		}
		if (authData.refreshToken) {
			localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, authData.refreshToken);
		}
		localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify({
			userId: authData.userId,
			name: authData.name,
			email: authData.email,
			role: authData.role,
			expiresInSeconds: authData.expiresInSeconds || 900
		}));
	},
	clear() {
		localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
		localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
		localStorage.removeItem(STORAGE_KEYS.USER);
	},
	hasRole(...roles) {
		const user = this.getUser();
		return Boolean(user && roles.includes(user.role));
	}
};

export function getTokenRemainingMs() {
	const token = sessionStore.getAccessToken();
	if (!token) {
		return 0;
	}
	try {
		const payloadPart = token.split('.')[1].replaceAll('-', '+').replaceAll('_', '/');
		const payload = JSON.parse(atob(payloadPart));
		return (payload.exp * 1000) - Date.now();
	} catch {
		return 0;
	}
}

export async function httpRequest(url, options = {}, retryOnUnauthorized = true) {
	const headers = { ...(options.headers || {}) };
	const token = sessionStore.getAccessToken();
	if (token) {
		headers.Authorization = `Bearer ${token}`;
	}
	if (options.body && !headers['Content-Type']) {
		headers['Content-Type'] = 'application/json';
	}

	const response = await fetch(url, { ...options, headers });

	if (response.status === 401 && retryOnUnauthorized) {
		sessionStore.clear();
		globalThis.dispatchEvent(new CustomEvent('hms:session-expired'));
		throw new Error('Sessão expirada. Faça login novamente.');
	}

	if (response.status === 204) {
		return null;
	}

	const text = await response.text();
	const data = text ? JSON.parse(text) : null;
	if (!response.ok) {
		throw new Error((data && data.message) || `Erro na requisição (${response.status})`);
	}
	return data;
}
