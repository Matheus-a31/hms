import { sessionStore } from '../core/httpClient.js';
import { escapeHtml, getViewContainer, setHeader, showToast, todayIso } from '../core/ui.js';
import { appointmentApi, patientApi } from '../services/hmsApi.js';

export async function renderAppointmentsView(selectedDate = todayIso()) {
	setHeader('Agendamento de Consultas & Controle de Vagas');
	const [patients, occupancy, appointments] = await Promise.all([
		patientApi.list(),
		appointmentApi.occupancy(selectedDate),
		appointmentApi.list()
	]);

	const totalVagas = occupancy.reduce((sum, o) => sum + o.totalVagas, 0);
	const ocupadas = occupancy.reduce((sum, o) => sum + o.vagasOcupadas, 0);
	const livres = occupancy.reduce((sum, o) => sum + o.vagasDisponiveis, 0);

	getViewContainer().innerHTML = `
		<div class="kpi-grid-3">
			<div class="kpi-card"><div class="kpi-label">Vagas Totais do Dia (${escapeHtml(selectedDate)})</div><div class="kpi-value">${totalVagas} Vagas</div></div>
			<div class="kpi-card"><div class="kpi-label">Vagas Ocupadas (RF30)</div><div class="kpi-value text-primary-accent">${ocupadas} Ocupadas</div></div>
			<div class="kpi-card"><div class="kpi-label">Vagas Disponíveis</div><div class="kpi-value text-money">${livres} Livres</div></div>
		</div>
		${buildBookingFormHtml(patients, occupancy, selectedDate)}
		${buildOccupancyTableHtml(occupancy)}
		${buildAppointmentsTableHtml(appointments)}
	`;

	await bindAppointmentEvents(selectedDate);
}

function buildBookingFormHtml(patients, occupancy, selectedDate) {
	const patientOptions = patients.map((p) => `<option value="${p.id}">${escapeHtml(p.name)} (#${p.id})</option>`).join('');
	const doctorOptions = occupancy.map((o) =>
		`<option value="${o.medicoId}" data-spec="${escapeHtml(o.especialidade)}">${escapeHtml(o.nomeMedico)} — ${o.vagasDisponiveis} vagas livres</option>`
	).join('');
	const defaultSpec = occupancy[0] ? occupancy[0].especialidade : 'Clínica Geral';

	return `
		<div class="card card-padded">
			<h2 class="card-title">Novo Agendamento de Consulta (RF29)</h2>
			<form id="appointment-form">
				<div class="form-grid">
					<div class="form-group"><label for="app-patient">Paciente Solicitante</label><select id="app-patient" required>${patientOptions}</select></div>
					<div class="form-group"><label for="app-specialty">Área de Necessidade / Especialidade</label><input id="app-specialty" type="text" required value="${escapeHtml(defaultSpec)}"></div>
					<div class="form-group"><label for="app-doctor">Médico Responsável (Vagas Disponíveis)</label><select id="app-doctor" required>${doctorOptions}</select></div>
					<div class="form-group"><label for="app-date">Data da Consulta</label><input id="app-date" type="date" required value="${escapeHtml(selectedDate)}"></div>
					<div class="form-group"><label for="app-slot">Horário Disponível</label><select id="app-slot" required></select></div>
					<div class="form-group"><label for="app-fee">Valor da Consulta (R$)</label><input id="app-fee" type="number" min="50" required value="350"></div>
				</div>
				<div class="form-actions"><button type="submit" class="btn btn-primary">Agendar</button></div>
			</form>
		</div>
	`;
}

function buildOccupancyTableHtml(occupancy) {
	const rows = occupancy.map((o) => `
		<tr>
			<td><strong>${escapeHtml(o.nomeMedico)}</strong> <span class="text-muted">(#${o.medicoId})</span></td>
			<td class="text-muted">${escapeHtml(o.especialidade)}</td>
			<td>${o.totalVagas}</td>
			<td class="text-primary-accent">${o.vagasOcupadas}</td>
			<td class="text-money">${o.vagasDisponiveis}</td>
			<td class="text-muted">${o.horariosOcupados.length ? o.horariosOcupados.join(', ') : 'Nenhum horário ocupado'}</td>
		</tr>
	`).join('');

	return `
		<div class="card">
			<div class="card-padded" style="padding-bottom:0.5rem"><h2 class="card-title">Painel de Ocupação por Médico (RF30)</h2></div>
			<div class="table-wrap">
				<table>
					<thead><tr><th>Médico</th><th>Especialidade</th><th>Total Vagas</th><th>Ocupadas</th><th>Livres</th><th>Horários Ativos</th></tr></thead>
					<tbody>${rows}</tbody>
				</table>
			</div>
		</div>
	`;
}

function buildAppointmentsTableHtml(appointments) {
	const canManageStatus = sessionStore.hasRole('ADMIN', 'MEDICO');
	const rows = appointments.map((a) => `
		<tr>
			<td class="text-muted">#AG-${a.id}</td>
			<td><strong>${escapeHtml(a.pacienteNome)}</strong></td>
			<td>${escapeHtml(a.medicoNome)}</td>
			<td class="text-muted">${escapeHtml(a.especialidade)}</td>
			<td>${escapeHtml((a.dataHora || '').replace('T', ' '))}</td>
			<td><span class="badge ${statusBadgeClass(a.status)}">${escapeHtml(a.status)}</span></td>
			<td>
				${canManageStatus && a.status === 'AGENDADO' ? `<button type="button" class="link-btn" data-app-status="${a.id}:CONFIRMADO">Confirmar</button>` : ''}
				${canManageStatus && a.status !== 'CONCLUIDO' && a.status !== 'CANCELADO' ? `<button type="button" class="link-btn" data-app-status="${a.id}:CONCLUIDO">Concluir</button>` : ''}
				${a.status !== 'CANCELADO' && a.status !== 'CONCLUIDO' ? `<button type="button" class="link-btn link-btn-danger" data-app-status="${a.id}:CANCELADO">Cancelar</button>` : ''}
			</td>
		</tr>
	`).join('');

	return `
		<div class="card">
			<div class="card-padded" style="padding-bottom:0.5rem"><h2 class="card-title">Agendamentos Registrados</h2></div>
			<div class="table-wrap">
				<table>
					<thead><tr><th>ID</th><th>Paciente</th><th>Médico</th><th>Especialidade</th><th>Data / Hora</th><th>Status</th><th>Ações</th></tr></thead>
					<tbody>${rows || '<tr><td colspan="7" class="text-muted">Nenhum agendamento encontrado.</td></tr>'}</tbody>
				</table>
			</div>
		</div>
	`;
}

function statusBadgeClass(status) {
	if (status === 'CONFIRMADO' || status === 'CONCLUIDO') {
		return 'badge-success';
	}
	return status === 'CANCELADO' ? 'badge-danger' : 'badge-info';
}

async function bindAppointmentEvents(selectedDate) {
	const doctorSelect = document.getElementById('app-doctor');
	const dateInput = document.getElementById('app-date');
	const slotSelect = document.getElementById('app-slot');

	async function loadSlots() {
		if (!doctorSelect.value) {
			return;
		}
		const slots = await appointmentApi.availableSlots(doctorSelect.value, dateInput.value);
		slotSelect.innerHTML = slots.length
			? slots.map((s) => `<option value="${s}">${s} - Disponível</option>`).join('')
			: '<option value="">Sem horários livres nesta data</option>';
	}

	await loadSlots();

	doctorSelect.addEventListener('change', async () => {
		const opt = doctorSelect.options[doctorSelect.selectedIndex];
		if (opt && opt.dataset.spec) {
			document.getElementById('app-specialty').value = opt.dataset.spec;
		}
		await loadSlots();
	});

	dateInput.addEventListener('change', () => renderAppointmentsView(dateInput.value));

	document.getElementById('appointment-form').addEventListener('submit', async (e) => {
		e.preventDefault();
		if (!slotSelect.value) {
			showToast('Selecione uma data/médico com horário disponível.', 'error');
			return;
		}
		try {
			await appointmentApi.create({
				pacienteId: Number(document.getElementById('app-patient').value),
				medicoId: Number(doctorSelect.value),
				especialidade: document.getElementById('app-specialty').value.trim(),
				dataHora: `${dateInput.value}T${slotSelect.value}:00`,
				valorConsulta: Number(document.getElementById('app-fee').value)
			});
			showToast('Consulta agendada com sucesso!');
			await renderAppointmentsView(selectedDate);
		} catch (err) {
			showToast(err.message, 'error');
		}
	});

	document.querySelectorAll('[data-app-status]').forEach((btn) => {
		btn.addEventListener('click', async () => {
			const [id, status] = btn.dataset.appStatus.split(':');
			await appointmentApi.updateStatus(Number(id), status);
			showToast(`Status do agendamento #AG-${id} atualizado para ${status}.`);
			await renderAppointmentsView(dateInput.value);
		});
	});
}
