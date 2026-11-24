import { httpRequest, sessionStore } from '../core/httpClient.js';

export const authApi = {
	login: (email, password) => httpRequest('/hms/auth/login', {
		method: 'POST',
		body: JSON.stringify({ email, password })
	}, false),
	register: (payload) => httpRequest('/hms/auth/register', {
		method: 'POST',
		body: JSON.stringify(payload)
	}, false),
	async logout() {
		const refreshToken = sessionStore.getRefreshToken();
		if (refreshToken) {
			try {
				await fetch('/hms/auth/logout', {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({ refreshToken })
				});
			} catch (err) {
				console.warn('Falha ao revogar token:', err);
			}
		}
		sessionStore.clear();
	}
};

export const preferenceApi = {
	get: () => httpRequest('/hms/preferences'),
	save: (payload) => httpRequest('/hms/preferences', { method: 'PUT', body: JSON.stringify(payload) })
};

export const hospitalApi = {
	list: () => httpRequest('/hms/hospital'),
	update: (id, payload) => httpRequest(`/hms/hospital/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
};

export const doctorApi = {
	list: () => httpRequest('/hms/doctors'),
	create: (payload) => httpRequest('/hms/doctors', { method: 'POST', body: JSON.stringify(payload) }),
	update: (id, payload) => httpRequest(`/hms/doctors/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
	remove: (id) => httpRequest(`/hms/doctors/${id}`, { method: 'DELETE' })
};

export const patientApi = {
	list: () => httpRequest('/hms/patients'),
	create: (payload) => httpRequest('/hms/patients', { method: 'POST', body: JSON.stringify(payload) }),
	update: (id, payload) => httpRequest(`/hms/patients/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
	remove: (id) => httpRequest(`/hms/patients/${id}`, { method: 'DELETE' })
};

export const billApi = {
	list: () => httpRequest('/hms/bills'),
	create: (payload) => httpRequest('/hms/bills', { method: 'POST', body: JSON.stringify(payload) }),
	update: (id, payload) => httpRequest(`/hms/bills/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
	remove: (id) => httpRequest(`/hms/bills/${id}`, { method: 'DELETE' })
};

export const invoiceApi = {
	list: () => httpRequest('/hms/invoices'),
	create: (payload) => httpRequest('/hms/invoices', { method: 'POST', body: JSON.stringify(payload) }),
	update: (id, payload) => httpRequest(`/hms/invoices/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
	remove: (id) => httpRequest(`/hms/invoices/${id}`, { method: 'DELETE' })
};

export const appointmentApi = {
	list: () => httpRequest('/hms/appointments'),
	create: (payload) => httpRequest('/hms/appointments', { method: 'POST', body: JSON.stringify(payload) }),
	updateStatus: (id, status) => httpRequest(`/hms/appointments/${id}/status?status=${encodeURIComponent(status)}`, { method: 'PATCH' }),
	availableSlots: (doctorId, date) => httpRequest(`/hms/appointments/available?doctorId=${doctorId}&date=${encodeURIComponent(date)}`),
	occupancy: (date) => httpRequest(`/hms/appointments/occupancy?date=${encodeURIComponent(date)}`)
};

export const reportApi = {
	allDoctorsProfit: () => httpRequest('/hms/doctors/financial-report'),
	doctorProfit: (doctorId) => httpRequest(`/hms/doctors/${doctorId}/financial-report`),
	dashboardSummary: () => httpRequest('/hms/dashboard/summary')
};
