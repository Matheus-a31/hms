import { closeModal, escapeHtml, formatCurrency, getViewContainer, openModal, setHeader, showToast } from '../core/ui.js';
import { billApi } from '../services/hmsApi.js';

export async function renderBillsView(filterText = '') {
	setHeader('Gestão de Itens de Conta (Bill)');
	const bills = await billApi.list();
	const filtered = bills.filter((b) =>
		!filterText || b.particular.toLowerCase().includes(filterText.toLowerCase()));

	const rows = filtered.map((b) => `
		<tr>
			<td class="text-muted">#B-${b.id}</td>
			<td><strong>${escapeHtml(b.particular)}</strong></td>
			<td>${b.quantity}</td>
			<td class="text-muted">${formatCurrency(b.unitValue)}</td>
			<td class="text-money">${formatCurrency(b.totalValue)}</td>
			<td>
				<button type="button" class="link-btn" data-edit-bill="${b.id}">Editar</button>
				<span class="text-muted">|</span>
				<button type="button" class="link-btn link-btn-danger" data-delete-bill="${b.id}">Excluir</button>
			</td>
		</tr>
	`).join('');

	getViewContainer().innerHTML = `
		<div class="toolbar">
			<input id="bill-search" type="search" class="search-input" value="${escapeHtml(filterText)}" placeholder="Buscar por descrição do serviço..." aria-label="Buscar item de conta">
			<button type="button" id="btn-new-bill" class="btn btn-primary">+ Novo Item</button>
		</div>
		<div class="card">
			<div class="table-wrap">
				<table>
					<thead>
						<tr><th>ID</th><th>Descrição (Particular)</th><th>Qtd</th><th>Valor Unit.</th><th>Valor Total</th><th>Ações</th></tr>
					</thead>
					<tbody>${rows || '<tr><td colspan="6" class="text-muted">Nenhum item de conta encontrado.</td></tr>'}</tbody>
				</table>
			</div>
		</div>
	`;

	document.getElementById('bill-search').addEventListener('input', (e) => renderBillsView(e.target.value));
	document.getElementById('btn-new-bill').addEventListener('click', () => openBillModal(null));
	document.querySelectorAll('[data-edit-bill]').forEach((btn) => {
		btn.addEventListener('click', () => openBillModal(bills.find((b) => b.id === Number(btn.dataset.editBill))));
	});
	document.querySelectorAll('[data-delete-bill]').forEach((btn) => {
		btn.addEventListener('click', async () => {
			await billApi.remove(Number(btn.dataset.deleteBill));
			showToast('Item de conta removido.');
			await renderBillsView();
		});
	});
}

function openBillModal(bill) {
	openModal(bill ? `Editar Item #B-${bill.id}` : 'Novo Item de Conta (Bill)', `
		<form id="bill-form">
			<div class="form-group">
				<label for="b-particular">Descrição (Particular)</label>
				<input id="b-particular" type="text" required value="${escapeHtml(bill ? bill.particular : '')}" placeholder="Ex: Diária de UTI / Leito 04">
			</div>
			<div class="form-grid">
				<div class="form-group"><label for="b-qty">Quantidade</label><input id="b-qty" type="number" min="1" required value="${bill ? bill.quantity : 1}"></div>
				<div class="form-group"><label for="b-unit">Valor Unitário (R$)</label><input id="b-unit" type="number" min="0" required value="${bill ? bill.unitValue : 100}"></div>
			</div>
			<div class="form-actions"><button type="submit" class="btn btn-primary">Salvar Item</button></div>
		</form>
	`);
	document.getElementById('bill-form').addEventListener('submit', async (e) => {
		e.preventDefault();
		const payload = {
			particular: document.getElementById('b-particular').value.trim(),
			quantity: Number(document.getElementById('b-qty').value),
			unitValue: Number(document.getElementById('b-unit').value)
		};
		if (bill) {
			await billApi.update(bill.id, payload);
			showToast('Item de conta atualizado.');
		} else {
			await billApi.create(payload);
			showToast('Item de conta criado.');
		}
		closeModal();
		await renderBillsView();
	});
}
