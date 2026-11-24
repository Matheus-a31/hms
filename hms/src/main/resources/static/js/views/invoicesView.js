import { closeModal, escapeHtml, formatCurrency, getViewContainer, openModal, setHeader, showToast, todayIso } from '../core/ui.js';
import { billApi, doctorApi, invoiceApi, patientApi } from '../services/hmsApi.js';

export async function renderInvoicesView(filterText = '') {
	setHeader('Gestão Financeira & Faturas');
	const [invoices, patients, doctors, bills] = await Promise.all([
		invoiceApi.list(),
		patientApi.list(),
		doctorApi.list(),
		billApi.list()
	]);

	const filtered = invoices.filter((inv) =>
		!filterText || String(inv.id).includes(filterText.trim()) ||
		(inv.patientName || '').toLowerCase().includes(filterText.toLowerCase()));

	const rows = filtered.map((inv) => `
		<tr>
			<td class="text-muted">#INV-${inv.id}</td>
			<td><strong>${escapeHtml(inv.patientName)}</strong> ${inv.patientId ? `(#${inv.patientId})` : ''}</td>
			<td class="text-muted">${escapeHtml(inv.doctorName)} ${inv.doctorId ? `(#${inv.doctorId})` : ''}</td>
			<td class="text-money">${formatCurrency(inv.totalAmount)}</td>
			<td><span class="badge ${inv.paymentStatus === 'PAGO' ? 'badge-success' : 'badge-warning'}">${escapeHtml(inv.paymentStatus)}</span></td>
			<td>
				<button type="button" class="link-btn" data-view-invoice="${inv.id}">Ver Detalhes</button>
				<span class="text-muted">|</span>
				<button type="button" class="link-btn link-btn-danger" data-delete-invoice="${inv.id}">Excluir</button>
			</td>
		</tr>
	`).join('');

	getViewContainer().innerHTML = `
		<div class="toolbar">
			<input id="invoice-search" type="search" class="search-input" value="${escapeHtml(filterText)}" placeholder="Buscar fatura por ID..." aria-label="Buscar fatura por ID">
			<button type="button" id="btn-new-invoice" class="btn btn-primary">+ Nova Fatura</button>
		</div>
		<div class="card">
			<div class="table-wrap">
				<table>
					<thead>
						<tr><th>ID Fatura</th><th>Paciente (ID)</th><th>Médico (ID)</th><th>Valor Total</th><th>Status</th><th>Ações</th></tr>
					</thead>
					<tbody>${rows || '<tr><td colspan="6" class="text-muted">Nenhuma fatura registrada.</td></tr>'}</tbody>
				</table>
			</div>
		</div>
	`;

	document.getElementById('invoice-search').addEventListener('input', (e) => renderInvoicesView(e.target.value));
	document.getElementById('btn-new-invoice').addEventListener('click', () => renderInvoiceForm(patients, doctors, bills));
	document.querySelectorAll('[data-view-invoice]').forEach((btn) => {
		btn.addEventListener('click', () => showInvoiceDetailModal(invoices.find((i) => i.id === Number(btn.dataset.viewInvoice))));
	});
	document.querySelectorAll('[data-delete-invoice]').forEach((btn) => {
		btn.addEventListener('click', async () => {
			await invoiceApi.remove(Number(btn.dataset.deleteInvoice));
			showToast('Fatura removida.');
			await renderInvoicesView();
		});
	});
}

function showInvoiceDetailModal(inv) {
	openModal(`Detalhes da Fatura #INV-${inv.id}`, `
		<div class="info-grid" style="padding:0">
			<div class="info-item"><div class="info-label">Tipo</div><div class="info-value">${escapeHtml(inv.invoiceType)}</div></div>
			<div class="info-item"><div class="info-label">Status</div><div class="info-value">${escapeHtml(inv.paymentStatus)}</div></div>
			<div class="info-item"><div class="info-label">Paciente</div><div class="info-value">${escapeHtml(inv.patientName)}</div></div>
			<div class="info-item"><div class="info-label">Médico</div><div class="info-value">${escapeHtml(inv.doctorName)}</div></div>
			<div class="info-item"><div class="info-label">Item de Conta (Bill)</div><div class="info-value">${escapeHtml(inv.billDescription)}</div></div>
			<div class="info-item"><div class="info-label">Valor Total</div><div class="info-value text-money">${formatCurrency(inv.totalAmount)}</div></div>
		</div>
		<div class="form-actions">
			${inv.paymentStatus !== 'PAGO' ? '<button type="button" id="mark-paid-btn" class="btn btn-primary">Marcar como PAGO</button>' : ''}
		</div>
	`);
	const paidBtn = document.getElementById('mark-paid-btn');
	if (paidBtn) {
		paidBtn.addEventListener('click', async () => {
			await invoiceApi.update(inv.id, { ...inv, paymentStatus: 'PAGO' });
			closeModal();
			showToast(`Fatura #INV-${inv.id} atualizada para PAGO.`);
			await renderInvoicesView();
		});
	}
}

function renderInvoiceForm(patients, doctors, bills) {
	setHeader('Emitir Nova Fatura', '<button type="button" id="back-invoices" class="link-btn">&larr; Voltar às Faturas</button>');
	const patientOptions = patients.map((p) => `<option value="${p.id}">${escapeHtml(p.name)} (ID: #${p.id})</option>`).join('');
	const doctorOptions = doctors.map((d) => `<option value="${d.id}">${escapeHtml(d.name)} (ID: #${d.id})</option>`).join('');
	const billOptions = bills.map((b) => `<option value="${b.id}" data-total="${b.totalValue}">#B-${b.id} - ${escapeHtml(b.particular)} (${formatCurrency(b.totalValue)})</option>`).join('');
	const initialTotal = bills.length ? bills[0].totalValue : 0;

	getViewContainer().innerHTML = `
		<div class="card card-padded">
			<form id="invoice-form">
				<p class="info-label" style="margin-bottom:0.6rem">TIPO E DESTINATÁRIO DA FATURA</p>
				<div class="radio-grid">
					<label class="radio-card selected" id="card-type-paciente">
						<input type="radio" name="invoiceType" value="PACIENTE" checked> Fatura de Paciente (Atendimento)
					</label>
					<label class="radio-card" id="card-type-institucional">
						<input type="radio" name="invoiceType" value="INSTITUCIONAL"> Fatura Institucional (Hospital / Fornecedor)
					</label>
				</div>
				<div class="form-grid">
					<div class="form-group"><label for="inv-patient">Paciente Vinculado (patient_id)</label><select id="inv-patient">${patientOptions}</select></div>
					<div class="form-group"><label for="inv-doctor">Médico Responsável (doctor_id)</label><select id="inv-doctor">${doctorOptions}</select></div>
					<div class="form-group"><label for="inv-bill">Item de Conta / Cobrança (bill_id)</label><select id="inv-bill">${billOptions}</select></div>
					<div class="form-group"><label for="inv-amount">Valor Total da Fatura (R$)</label><input id="inv-amount" type="number" min="0" required value="${initialTotal}"></div>
					<div class="form-group"><label for="inv-due">Data de Vencimento</label><input id="inv-due" type="date" required value="${todayIso()}"></div>
					<div class="form-group">
						<label for="inv-status">Status de Pagamento</label>
						<select id="inv-status">
							<option value="PENDENTE">Pendente / Aguardando</option>
							<option value="PAGO">Pago</option>
							<option value="CANCELADO">Cancelado</option>
						</select>
					</div>
				</div>
				<div class="form-actions">
					<button type="button" id="cancel-invoice" class="btn btn-secondary">Cancelar</button>
					<button type="submit" class="btn btn-primary">Emitir Fatura</button>
				</div>
			</form>
		</div>
	`;

	document.getElementById('back-invoices').addEventListener('click', () => renderInvoicesView());
	document.getElementById('cancel-invoice').addEventListener('click', () => renderInvoicesView());
	document.querySelectorAll('input[name="invoiceType"]').forEach((radio) => {
		radio.addEventListener('change', (e) => {
			document.getElementById('card-type-paciente').classList.toggle('selected', e.target.value === 'PACIENTE');
			document.getElementById('card-type-institucional').classList.toggle('selected', e.target.value === 'INSTITUCIONAL');
		});
	});
	document.getElementById('inv-bill').addEventListener('change', (e) => {
		const selected = e.target.options[e.target.selectedIndex];
		if (selected && selected.dataset.total) {
			document.getElementById('inv-amount').value = selected.dataset.total;
		}
	});
	document.getElementById('invoice-form').addEventListener('submit', async (e) => {
		e.preventDefault();
		const invoiceType = document.querySelector('input[name="invoiceType"]:checked').value;
		try {
			await invoiceApi.create({
				invoiceType,
				patientId: invoiceType === 'PACIENTE' ? Number(document.getElementById('inv-patient').value) : null,
				doctorId: Number(document.getElementById('inv-doctor').value),
				billId: Number(document.getElementById('inv-bill').value),
				totalAmount: Number(document.getElementById('inv-amount').value),
				dueDate: document.getElementById('inv-due').value,
				paymentStatus: document.getElementById('inv-status').value
			});
			showToast('Fatura emitida com sucesso!');
			await renderInvoicesView();
		} catch (err) {
			showToast(err.message, 'error');
		}
	});
}
