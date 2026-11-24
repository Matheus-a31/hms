import { escapeHtml, formatCurrency, getViewContainer, openModal, setHeader } from '../core/ui.js';
import { reportApi } from '../services/hmsApi.js';

export async function renderDoctorProfitView() {
	setHeader('Relatório Financeiro & Lucro por Médico (RF31)');
	const reports = await reportApi.allDoctorsProfit();

	const rows = reports.map((r) => `
		<tr>
			<td>
				<strong>${escapeHtml(r.nomeMedico)} (#${r.medicoId})</strong>
				<div class="text-muted">${escapeHtml(r.especialidade)}</div>
			</td>
			<td>${r.totalConsultas} consultas</td>
			<td class="text-money">${formatCurrency(r.receitaBruta)}</td>
			<td class="text-danger">${formatCurrency(r.custoOperacional)}</td>
			<td class="text-money">${formatCurrency(r.lucroLiquido)}</td>
			<td><button type="button" class="link-btn" data-doctor-report="${r.medicoId}">Ver Histórico</button></td>
		</tr>
	`).join('');

	getViewContainer().innerHTML = `
		<div class="card">
			<div class="table-wrap">
				<table>
					<thead>
						<tr><th>Médico (ID)</th><th>Consultas Realizadas</th><th>Receita Bruta</th><th>Custo Operacional</th><th>Lucro Líquido</th><th>Detalhes</th></tr>
					</thead>
					<tbody>${rows || '<tr><td colspan="6" class="text-muted">Nenhum relatório disponível.</td></tr>'}</tbody>
				</table>
			</div>
		</div>
	`;

	document.querySelectorAll('[data-doctor-report]').forEach((btn) => {
		btn.addEventListener('click', () => openDoctorReportModal(Number(btn.dataset.doctorReport)));
	});
}

async function openDoctorReportModal(doctorId) {
	const detail = await reportApi.doctorProfit(doctorId);
	const detailRows = detail.consultas.map((c) => `
		<tr>
			<td>#${c.consultaId}</td>
			<td>${escapeHtml((c.dataHora || '').replace('T', ' '))}</td>
			<td>${escapeHtml(c.pacienteNome)}</td>
			<td class="text-money">${formatCurrency(c.lucroAtendimento)}</td>
		</tr>
	`).join('');

	openModal(`Histórico & Lucro — ${detail.nomeMedico}`, `
		<p style="margin-bottom:1rem">
			<strong>Total Consultas:</strong> ${detail.totalConsultas} •
			<strong>Receita Bruta:</strong> ${formatCurrency(detail.receitaBruta)} •
			<strong>Lucro Líquido:</strong> <span class="text-money">${formatCurrency(detail.lucroLiquido)}</span>
		</p>
		<div class="table-wrap">
			<table>
				<thead><tr><th>Consulta</th><th>Data/Hora</th><th>Paciente</th><th>Lucro</th></tr></thead>
				<tbody>${detailRows || '<tr><td colspan="4" class="text-muted">Sem consultas registradas.</td></tr>'}</tbody>
			</table>
		</div>
	`);
}
