import { sessionStore } from '../core/httpClient.js';
import { getAccessibilityPrefs, updateAccessibilityPrefs } from '../core/accessibility.js';
import { authApi } from '../services/hmsApi.js';

export function showAuthAlert(message, type = 'error') {
	const box = document.getElementById('auth-alert');
	box.textContent = message;
	box.className = `alert alert-${type}`;
}

export function bindAuthScreen(onAuthenticated) {
	const tabLogin = document.getElementById('tab-login');
	const tabRegister = document.getElementById('tab-register');
	const loginForm = document.getElementById('login-form');
	const registerForm = document.getElementById('register-form');

	tabLogin.addEventListener('click', () => switchTab(true, tabLogin, tabRegister, loginForm, registerForm));
	tabRegister.addEventListener('click', () => switchTab(false, tabLogin, tabRegister, loginForm, registerForm));

	document.getElementById('fill-admin-btn').addEventListener('click', () => {
		document.getElementById('login-email').value = 'admin@hms.com';
		document.getElementById('login-password').value = 'admin123';
	});

	document.getElementById('fill-patient-btn').addEventListener('click', () => {
		document.getElementById('login-email').value = 'paciente@hms.com';
		document.getElementById('login-password').value = 'paciente123';
	});

	loginForm.addEventListener('submit', async (e) => {
		e.preventDefault();
		try {
			const data = await authApi.login(
				document.getElementById('login-email').value.trim(),
				document.getElementById('login-password').value
			);
			sessionStore.save(data);
			await onAuthenticated();
		} catch (err) {
			showAuthAlert(err.message, 'error');
		}
	});

	registerForm.addEventListener('submit', async (e) => {
		e.preventDefault();
		try {
			const data = await authApi.register({
				name: document.getElementById('reg-name').value.trim(),
				email: document.getElementById('reg-email').value.trim(),
				password: document.getElementById('reg-password').value,
				role: document.getElementById('reg-role').value
			});
			sessionStore.save(data);
			await onAuthenticated();
		} catch (err) {
			showAuthAlert(err.message, 'error');
		}
	});

	document.getElementById('auth-contrast-toggle').addEventListener('click', () => {
		updateAccessibilityPrefs({ altoContraste: !getAccessibilityPrefs().altoContraste });
	});
}

function switchTab(isLogin, tabLogin, tabRegister, loginForm, registerForm) {
	tabLogin.classList.toggle('active', isLogin);
	tabRegister.classList.toggle('active', !isLogin);
	loginForm.classList.toggle('hidden', !isLogin);
	registerForm.classList.toggle('hidden', isLogin);
	document.getElementById('auth-alert').classList.add('hidden');
}
