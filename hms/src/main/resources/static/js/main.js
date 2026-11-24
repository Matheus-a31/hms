import {
	getAccessibilityPrefs,
	loadLocalAccessibilityPrefs,
	syncRemoteAccessibilityPrefs,
	updateAccessibilityPrefs
} from './core/accessibility.js';
import { getTokenRemainingMs, sessionStore } from './core/httpClient.js';
import { closeModal, escapeHtml, getViewContainer, showToast } from './core/ui.js';
import { authApi } from './services/hmsApi.js';
import { renderAccessibilityView } from './views/accessibilityView.js';
import { renderAppointmentsView } from './views/appointmentsView.js';
import { bindAuthScreen, showAuthAlert } from './views/authView.js';
import { renderBillsView } from './views/billsView.js';
import { renderDashboardView } from './views/dashboardView.js';
import { renderDoctorProfitView } from './views/doctorProfitView.js';
import { renderDoctorsView } from './views/doctorsView.js';
import { renderHistoryView } from './views/historyView.js';
import { renderHospitalView } from './views/hospitalView.js';
import { renderInvoicesView } from './views/invoicesView.js';
import { renderPatientsView } from './views/patientsView.js';

/* Open/Closed Principle: novas telas entram apenas neste mapa */
const ROUTES = {
	hospital: () => renderHospitalView(),
	doctors: () => renderDoctorsView(),
	patients: () => renderPatientsView(),
	invoices: () => renderInvoicesView(),
	bills: () => renderBillsView(),
	dashboard: () => renderDashboardView(),
	appointments: () => renderAppointmentsView(),
	history: () => renderHistoryView(),
	'doctor-profit': () => renderDoctorProfitView(),
	accessibility: () => renderAccessibilityView()
};

let currentView = 'hospital';
let expirationTimer = null;

function clearExpirationTimer() {
	if (expirationTimer) {
		clearTimeout(expirationTimer);
		expirationTimer = null;
	}
}

function scheduleSessionExpiration() {
	clearExpirationTimer();
	const remainingMs = getTokenRemainingMs();
	if (remainingMs <= 0) {
		handleSessionExpired();
		return;
	}
	expirationTimer = setTimeout(handleSessionExpired, remainingMs);
}

function handleSessionExpired() {
	clearExpirationTimer();
	sessionStore.clear();
	showLoginScreen();
	showAuthAlert('Sua sessão expirou. Faça login novamente.', 'error');
}

async function navigateTo(view) {
	currentView = ROUTES[view] ? view : 'hospital';
	document.querySelectorAll('.nav-item').forEach((btn) => {
		btn.classList.toggle('active', btn.dataset.view === currentView);
	});
	getViewContainer().innerHTML = '<div class="card card-padded">Carregando dados da API...</div>';
	try {
		await ROUTES[currentView]();
	} catch (err) {
		showToast(err.message, 'error');
		getViewContainer().innerHTML = `<div class="card card-padded"><p class="text-danger">${escapeHtml(err.message)}</p></div>`;
	}
}

function updateSessionBadge() {
	const user = sessionStore.getUser();
	if (!user) {
		return;
	}
	document.getElementById('user-display-name').textContent = user.name;
	document.getElementById('user-display-role').textContent = user.role;
}

function showLoginScreen() {
	document.getElementById('app-shell').classList.add('hidden');
	document.getElementById('auth-screen').classList.remove('hidden');
}

async function enterApp() {
	document.getElementById('auth-screen').classList.add('hidden');
	document.getElementById('app-shell').classList.remove('hidden');
	updateSessionBadge();
	scheduleSessionExpiration();
	await syncRemoteAccessibilityPrefs();
	await navigateTo(currentView);
}

function bindShellEvents() {
	document.querySelectorAll('.nav-item').forEach((btn) => {
		btn.addEventListener('click', () => navigateTo(btn.dataset.view));
	});

	document.getElementById('sidebar-mode-badge').addEventListener('click', async () => {
		await updateAccessibilityPrefs({ altoContraste: !getAccessibilityPrefs().altoContraste });
		if (currentView === 'accessibility') {
			renderAccessibilityView();
		}
	});

	document.getElementById('logout-btn').addEventListener('click', async () => {
		clearExpirationTimer();
		await authApi.logout();
		showLoginScreen();
	});

	document.getElementById('modal-close-btn').addEventListener('click', closeModal);
	document.getElementById('modal-backdrop').addEventListener('click', (e) => {
		if (e.target.id === 'modal-backdrop') {
			closeModal();
		}
	});

	globalThis.addEventListener('hms:session-expired', handleSessionExpired);
}

document.addEventListener('DOMContentLoaded', async () => {
	loadLocalAccessibilityPrefs();
	bindAuthScreen(enterApp);
	bindShellEvents();
	if (sessionStore.getAccessToken() && getTokenRemainingMs() > 0) {
		await enterApp();
	} else {
		sessionStore.clear();
		showLoginScreen();
	}
});
