export function escapeHtml(value) {
	if (value === null || value === undefined) {
		return '';
	}
	return String(value)
		.replaceAll('&', '&amp;')
		.replaceAll('<', '&lt;')
		.replaceAll('>', '&gt;')
		.replaceAll('"', '&quot;')
		.replaceAll("'", '&#39;');
}

export function formatCurrency(value) {
	return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value || 0));
}

export function todayIso() {
	return new Date().toISOString().slice(0, 10);
}

export function setHeader(title, rightHtml = '') {
	document.getElementById('page-title').textContent = title;
	document.getElementById('topbar-actions').innerHTML = rightHtml;
}

export function getViewContainer() {
	return document.getElementById('view-container');
}

export function showToast(message, type = 'success') {
	const toast = document.getElementById('global-toast');
	toast.textContent = message;
	toast.className = `toast toast-${type}`;
	setTimeout(() => toast.classList.add('hidden'), 4200);
}

export function openModal(title, htmlContent) {
	document.getElementById('modal-title').textContent = title;
	document.getElementById('modal-body').innerHTML = htmlContent;
	document.getElementById('modal-backdrop').classList.remove('hidden');
}

export function closeModal() {
	document.getElementById('modal-backdrop').classList.add('hidden');
}
