import { escapeHtml, getViewContainer, setHeader, showToast } from '../core/ui.js';
import { doctorApi } from '../services/hmsApi.js';

export async function renderDoctorsView(filterText = '') {
	setHeader('Médicos');
	const doctors = await doctorApi.list();
	const filtered = doctors.filter((d) =>
		!filterText || d.name.toLowerCase().includes(filterText.toLowerCase()));

	const rows = filtered.map((d) => `
		<tr>
			<td class="text-muted">#${d.id}</td>
			<td><strong>${escapeHtml(d.name)}</strong></td>
			<td class="text-muted">${escapeHtml(d.designation)}</td>
			<td class="text-muted">${escapeHtml(d.emailId)}</td>
			<td>
				<button type="button" class="link-btn" data-edit-doctor="${d.id}">Editar</button>
				<span class="text-muted">|</span>
				<button type="button" class="link-btn link-btn-danger" data-delete-doctor="${d.id}">Excluir</button>
			</td>
		</tr>
	`).join('');

	getViewContainer().innerHTML = `
		<div class="toolbar">
			<input id="doctor-search" type="search" class="search-input" value="${escapeHtml(filterText)}" placeholder="Buscar médico por nome..." aria-label="Buscar médico por nome">
			<button type="button" id="btn-new-doctor" class="btn btn-primary">+ Novo Médico</button>
		</div>
		<div class="card">
			<div class="table-wrap">
				<table>
					<thead>
						<tr><th>ID</th><th>Nome</th><th>Especialidade / Cargo</th><th>E-mail</th><th>Ações</th></tr>
					</thead>
					<tbody>${rows || '<tr><td colspan="5" class="text-muted">Nenhum médico encontrado.</td></tr>'}</tbody>
				</table>
			</div>
		</div>
	`;

	document.getElementById('doctor-search').addEventListener('input', (e) => renderDoctorsView(e.target.value));
	document.getElementById('btn-new-doctor').addEventListener('click', () => renderDoctorForm(null));
	document.querySelectorAll('[data-edit-doctor]').forEach((btn) => {
		btn.addEventListener('click', () => renderDoctorForm(doctors.find((d) => d.id === Number(btn.dataset.editDoctor))));
	});
	document.querySelectorAll('[data-delete-doctor]').forEach((btn) => {
		btn.addEventListener('click', async () => {
			try {
				await doctorApi.remove(Number(btn.dataset.deleteDoctor));
				showToast('Médico removido com sucesso.');
				await renderDoctorsView();
			} catch (err) {
				showToast(err.message, 'error');
			}
		});
	});
}

function renderDoctorForm(doctor) {
	setHeader('Médicos', '<button type="button" id="back-doctors" class="link-btn">&larr; Voltar à Lista</button>');
	getViewContainer().innerHTML = `
		<div class="card card-padded">
			<form id="doctor-form">
				<div class="form-grid">
					<div class="form-group">
						<label for="doc-name">Nome Completo (name)</label>
						<input id="doc-name" type="text" required value="${escapeHtml(doctor ? doctor.name : '')}" placeholder="Dr. Fernando Ribeiro">
					</div>
					<div class="form-group">
						<label for="doc-designation">Cargo / Função (designation)</label>
						<input id="doc-designation" type="text" required value="${escapeHtml(doctor ? doctor.designation : '')}" placeholder="Médico Efetivo / Plantonista">
					</div>
					<div class="form-group">
						<label for="doc-degree">Especialização / Titulação (degree)</label>
						<input id="doc-degree" type="text" required value="${escapeHtml(doctor ? doctor.degree : '')}" placeholder="Neurologia Clínica">
					</div>
					<div class="form-group">
						<label for="doc-exp">Anos de Experiência (experience)</label>
						<input id="doc-exp" type="number" min="0" required value="${doctor ? doctor.experience : 1}">
					</div>
					<div class="form-group">
						<label for="doc-email">E-mail (emailId)</label>
						<input id="doc-email" type="email" required value="${escapeHtml(doctor ? doctor.emailId : '')}" placeholder="fernando.ribeiro@hms.com">
					</div>
					<div class="form-group">
						<label for="doc-phone">Telefone de Contato (contactNumber)</label>
						<input id="doc-phone" type="number" required value="${doctor ? doctor.contactNumber : ''}" placeholder="85999887766">
					</div>
					<div class="form-group span-2">
						<label for="doc-address">Endereço Residencial/Profissional (address)</label>
						<input id="doc-address" type="text" required value="${escapeHtml(doctor ? doctor.address : '')}" placeholder="Rua das Flores, 120 - Aldeota, Fortaleza-CE">
					</div>
				</div>
				<div class="form-actions">
					<button type="button" id="cancel-doctor" class="btn btn-secondary">Cancelar</button>
					<button type="submit" class="btn btn-primary">Salvar</button>
				</div>
			</form>
		</div>
	`;

	document.getElementById('back-doctors').addEventListener('click', () => renderDoctorsView());
	document.getElementById('cancel-doctor').addEventListener('click', () => renderDoctorsView());
	document.getElementById('doctor-form').addEventListener('submit', async (e) => {
		e.preventDefault();
		const payload = {
			name: document.getElementById('doc-name').value.trim(),
			designation: document.getElementById('doc-designation').value.trim(),
			degree: document.getElementById('doc-degree').value.trim(),
			experience: Number(document.getElementById('doc-exp').value),
			emailId: document.getElementById('doc-email').value.trim(),
			contactNumber: Number(document.getElementById('doc-phone').value),
			address: document.getElementById('doc-address').value.trim()
		};
		try {
			if (doctor) {
				await doctorApi.update(doctor.id, payload);
				showToast('Médico atualizado com sucesso.');
			} else {
				await doctorApi.create(payload);
				showToast('Médico cadastrado com sucesso.');
			}
			await renderDoctorsView();
		} catch (err) {
			showToast(err.message, 'error');
		}
	});
}
