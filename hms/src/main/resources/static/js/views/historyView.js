import { escapeHtml, formatCurrency, getViewContainer, setHeader } from '../core/ui.js';
import { appointmentApi, invoiceApi } from '../services/hmsApi.js';

export async function renderHistoryView() {
	setHeader('Histórico de Atendimentos & Faturas');
	const [appointments, invoices] = await Promise.all([appointmentApi.list(), invoiceApi.list()]);

	const appRows = appointments.map((a) => `
		<tr>
			<td class="text-muted">#AT-${a.id}</td>
			<td><strong>${escapeHtml(a.pacienteNome)}</strong></td>
			<td>${escapeHtml(a.medicoNome)}</td>
			<td class="text-muted">${escapeHtml(a.especialidade)}</td>
			<td>${escapeHtml((a.dataHora || '').replace('T', ' '))}</td>
			<td class="text-money">${formatCurrency(a.valorConsulta)}</td>
			<td><span class="badge badge-info">${escapeHtml(a.status)}</span></td>
		</tr>
	`).join('');

	const invRows = invoices.map((i) => `
		<tr>
			<td class="text-muted">#NF-${i.id}</td>
			<td><strong>${escapeHtml(i.patientName)}</strong></td>
			<td class="text-muted">${escapeHtml(i.billDescription)}</td>
			<td class="text-money">${formatCurrency(i.totalAmount)}</td>
			<td><span class="badge ${i.paymentStatus === 'PAGO' ? 'badge-success' : 'badge-warning'}">${escapeHtml(i.paymentStatus)}</span></td>
		</tr>
	`).join('');

	getViewContainer().innerHTML = `
		<div class="card">
			<div class="card-padded" style="padding-bottom:0.5rem"><h2 class="card-title">Atendimentos Registrados</h2></div>
			<div class="table-wrap">
				<table>
					<thead><tr><th>ID</th><th>Paciente</th><th>Médico</th><th>Especialidade</th><th>Data/Hora</th><th>Valor</th><th>Status</th></tr></thead>
					<tbody>${appRows || '<tr><td colspan="7" class="text-muted">Sem registros.</td></tr>'}</tbody>
				</table>
			</div>
		</div>
		<div class="card">
			<div class="card-padded" style="padding-bottom:0.5rem"><h2 class="card-title">Notas / Faturas Emitidas</h2></div>
			<div class="table-wrap">
				<table>
					<thead><tr><th>NF</th><th>Paciente</th><th>Serviço</th><th>Valor</th><th>Status</th></tr></thead>
					<tbody>${invRows || '<tr><td colspan="5" class="text-muted">Sem faturas.</td></tr>'}</tbody>
				</table>
			</div>
		</div>
	`;
}
