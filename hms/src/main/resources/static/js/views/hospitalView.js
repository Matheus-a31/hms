import { closeModal, escapeHtml, getViewContainer, openModal, setHeader, showToast } from '../core/ui.js';
import { hospitalApi } from '../services/hmsApi.js';

export async function renderHospitalView() {
	setHeader('Informações da Instituição Hospitalar');
	const hospitals = await hospitalApi.list();
	const hosp = hospitals[0];
	if (!hosp) {
		getViewContainer().innerHTML = '<div class="card card-padded">Nenhum hospital cadastrado.</div>';
		return;
	}

	getViewContainer().innerHTML = `
		<div class="card">
			<div class="hospital-banner">
				<h2>Hospital Management System (Unidade Central)</h2>
				<p>Cadastro Institucional &amp; Configurações da Unidade</p>
			</div>
			<div class="info-grid">
				<div class="info-item"><div class="info-label">Nome Institucional</div><div class="info-value">${escapeHtml(hosp.name)}</div></div>
				<div class="info-item"><div class="info-label">Endereço da Unidade</div><div class="info-value">${escapeHtml(hosp.address)}</div></div>
				<div class="info-item"><div class="info-label">Website Oficial</div><div class="info-value text-primary-accent">${escapeHtml(hosp.website)}</div></div>
				<div class="info-item"><div class="info-label">Telefone de Contato / Central</div><div class="info-value">${escapeHtml(hosp.contactNumber)}</div></div>
				<div class="info-item"><div class="info-label">Identificador no Sistema (ID)</div><div class="info-value">#HOSP-0${hosp.id}</div></div>
				<div class="info-item"><div class="info-label">Situação do Cadastro</div><div class="info-value"><span class="badge badge-success">● Ativo no HMS</span></div></div>
			</div>
			<div class="form-actions" style="padding:0 1.75rem 1.5rem">
				<button type="button" id="edit-hospital-btn" class="btn btn-primary">Editar Cadastro</button>
			</div>
		</div>
	`;

	document.getElementById('edit-hospital-btn').addEventListener('click', () => openHospitalEditModal(hosp));
}

function openHospitalEditModal(hosp) {
	openModal('Editar Cadastro do Hospital', `
		<form id="hospital-edit-form">
			<div class="form-group"><label for="h-name">Nome Institucional</label><input id="h-name" type="text" required value="${escapeHtml(hosp.name)}"></div>
			<div class="form-group"><label for="h-addr">Endereço</label><input id="h-addr" type="text" required value="${escapeHtml(hosp.address)}"></div>
			<div class="form-group"><label for="h-web">Website</label><input id="h-web" type="text" value="${escapeHtml(hosp.website)}"></div>
			<div class="form-group"><label for="h-phone">Telefone</label><input id="h-phone" type="number" required value="${hosp.contactNumber}"></div>
			<div class="form-actions"><button type="submit" class="btn btn-primary">Salvar Alterações</button></div>
		</form>
	`);
	document.getElementById('hospital-edit-form').addEventListener('submit', async (e) => {
		e.preventDefault();
		await hospitalApi.update(hosp.id, {
			id: hosp.id,
			name: document.getElementById('h-name').value.trim(),
			address: document.getElementById('h-addr').value.trim(),
			website: document.getElementById('h-web').value.trim(),
			contactNumber: Number(document.getElementById('h-phone').value)
		});
		closeModal();
		showToast('Dados do hospital atualizados.');
		await renderHospitalView();
	});
}
