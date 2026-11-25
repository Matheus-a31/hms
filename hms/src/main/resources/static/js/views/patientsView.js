import { sessionStore } from '../core/httpClient.js';
import { escapeHtml, getViewContainer, setHeader, showToast } from '../core/ui.js';
import { doctorApi, patientApi } from '../services/hmsApi.js';

export async function renderPatientsView(filterText = '') {
	setHeader('Pacientes');
	const [patients, doctors] = await Promise.all([patientApi.list(), doctorApi.list()]);
	const canManage = sessionStore.hasRole('ADMIN');
	const filtered = patients.filter((p) =>
		!filterText || p.name.toLowerCase().includes(filterText.toLowerCase()));

	const rows = filtered.map((p) => `
		<tr>
			<td class="text-muted">#${p.id}</td>
			<td><strong>${escapeHtml(p.name)}</strong></td>
			<td class="text-muted">${escapeHtml(p.disease)}</td>
			<td>${p.doctor ? `${escapeHtml(p.doctor.name)} (#${p.doctor.id})` : '<span class="text-muted">Sem vínculo</span>'}</td>
			${canManage ? `<td>
				<button type="button" class="link-btn" data-edit-patient="${p.id}">Editar</button>
				<span class="text-muted">|</span>
				<button type="button" class="link-btn link-btn-danger" data-delete-patient="${p.id}">Excluir</button>
			</td>` : ''}
		</tr>
	`).join('');

	getViewContainer().innerHTML = `
		<div class="toolbar">
			<input id="patient-search" type="search" class="search-input" value="${escapeHtml(filterText)}" placeholder="Buscar paciente por nome..." aria-label="Buscar paciente por nome">
			${canManage ? '<button type="button" id="btn-new-patient" class="btn btn-primary">+ Novo Paciente</button>' : ''}
		</div>
		<div class="card">
			<div class="table-wrap">
				<table>
					<thead>
						<tr><th>ID</th><th>Paciente</th><th>Enfermidade</th><th>Médico Responsável</th>${canManage ? '<th>Ações</th>' : ''}</tr>
					</thead>
					<tbody>${rows || `<tr><td colspan="${canManage ? 5 : 4}" class="text-muted">Nenhum paciente encontrado.</td></tr>`}</tbody>
				</table>
			</div>
		</div>
	`;

	document.getElementById('patient-search').addEventListener('input', (e) => renderPatientsView(e.target.value));
	const newBtn = document.getElementById('btn-new-patient');
	if (newBtn) {
		newBtn.addEventListener('click', () => renderPatientForm(null, doctors));
	}
	document.querySelectorAll('[data-edit-patient]').forEach((btn) => {
		btn.addEventListener('click', () =>
			renderPatientForm(patients.find((p) => p.id === Number(btn.dataset.editPatient)), doctors));
	});
	document.querySelectorAll('[data-delete-patient]').forEach((btn) => {
		btn.addEventListener('click', async () => {
			try {
				await patientApi.remove(Number(btn.dataset.deletePatient));
				showToast('Paciente removido com sucesso.');
				await renderPatientsView();
			} catch (err) {
				showToast(err.message, 'error');
			}
		});
	});
}

function renderPatientForm(patient, doctors) {
	setHeader('Pacientes', '<button type="button" id="back-patients" class="link-btn">&larr; Voltar à Lista</button>');
	const selectedDoctorId = patient && patient.doctor ? patient.doctor.id : '';
	const doctorOptions = doctors.map((d) =>
		`<option value="${d.id}" ${d.id === selectedDoctorId ? 'selected' : ''}>${escapeHtml(d.name)} (ID: ${d.id})</option>`
	).join('');

	getViewContainer().innerHTML = `
		<div class="card card-padded">
			<form id="patient-form" class="form-grid-single">
				<div class="form-group">
					<label for="pat-name">Nome do Paciente</label>
					<input id="pat-name" type="text" required value="${escapeHtml(patient ? patient.name : '')}" placeholder="Ex: Maria Oliveira">
				</div>
				<div class="form-group">
					<label for="pat-disease">Enfermidade / Diagnóstico</label>
					<input id="pat-disease" type="text" required value="${escapeHtml(patient ? patient.disease : '')}" placeholder="Ex: Arritmia Cardíaca">
				</div>
				<div class="form-group">
					<label for="pat-doctor">Médico Responsável (Vínculo)</label>
					<select id="pat-doctor" required>${doctorOptions}</select>
				</div>
				<div class="form-grid">
					<div class="form-group">
						<label for="pat-gender">Gênero</label>
						<select id="pat-gender">
							<option value="Feminino" ${patient && patient.gender === 'Feminino' ? 'selected' : ''}>Feminino</option>
							<option value="Masculino" ${patient && patient.gender === 'Masculino' ? 'selected' : ''}>Masculino</option>
							<option value="Outro" ${patient && patient.gender === 'Outro' ? 'selected' : ''}>Outro</option>
						</select>
					</div>
					<div class="form-group">
						<label for="pat-phone">Telefone de Contato</label>
						<input id="pat-phone" type="number" required value="${patient ? patient.contactNumber : 85999001122}">
					</div>
				</div>
				<div class="form-group">
					<label for="pat-email">E-mail de Contato</label>
					<input id="pat-email" type="email" required value="${escapeHtml(patient ? patient.emailId : '')}" placeholder="maria@email.com">
				</div>
				<div class="form-group">
					<label for="pat-address">Endereço Paciente</label>
					<input id="pat-address" type="text" required value="${escapeHtml(patient ? patient.address : '')}" placeholder="Rua das Flores, 120 - Aldeota, Fortaleza-CE">
				</div>
				<div class="form-actions">
					<button type="button" id="cancel-patient" class="btn btn-secondary">Cancelar</button>
					<button type="submit" class="btn btn-primary">Salvar</button>
				</div>
			</form>
		</div>
	`;

	document.getElementById('back-patients').addEventListener('click', () => renderPatientsView());
	document.getElementById('cancel-patient').addEventListener('click', () => renderPatientsView());
	document.getElementById('patient-form').addEventListener('submit', async (e) => {
		e.preventDefault();
		const payload = {
			name: document.getElementById('pat-name').value.trim(),
			disease: document.getElementById('pat-disease').value.trim(),
			doctorId: Number(document.getElementById('pat-doctor').value),
			gender: document.getElementById('pat-gender').value,
			contactNumber: Number(document.getElementById('pat-phone').value),
			emailId: document.getElementById('pat-email').value.trim(),
			address: document.getElementById('pat-address').value.trim()
		};
		try {
			if (patient) {
				await patientApi.update(patient.id, payload);
				showToast('Paciente atualizado com sucesso.');
			} else {
				await patientApi.create(payload);
				showToast('Paciente cadastrado com sucesso.');
			}
			await renderPatientsView();
		} catch (err) {
			showToast(err.message, 'error');
		}
	});
}
