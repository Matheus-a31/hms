import { escapeHtml, formatCurrency, getViewContainer, setHeader } from '../core/ui.js';
import { reportApi } from '../services/hmsApi.js';

export async function renderDashboardView() {
	setHeader('Dashboard Financeiro & Lucros', '<span class="badge badge-info">Dados em tempo real</span>');
	const summary = await reportApi.dashboardSummary();

	getViewContainer().innerHTML = `
		<div class="kpi-grid-4">
			<div class="kpi-card">
				<div class="kpi-label">Receita Total</div>
				<div class="kpi-value">${formatCurrency(summary.receitaTotal)}</div>
				<span class="badge badge-success">+12.5%</span>
			</div>
			<div class="kpi-card">
				<div class="kpi-label">Despesas Total</div>
				<div class="kpi-value">${formatCurrency(summary.despesasTotal)}</div>
				<span class="badge badge-success">-4.2%</span>
			</div>
			<div class="kpi-card">
				<div class="kpi-label">Lucro Líquido</div>
				<div class="kpi-value text-money">${formatCurrency(summary.lucroLiquido)}</div>
				<span class="badge badge-success">+18.3%</span>
			</div>
			<div class="kpi-card">
				<div class="kpi-label">Margem de Lucro</div>
				<div class="kpi-value text-primary-accent">${summary.margemLucro}%</div>
				<div class="text-muted">Meta esperada: 50%</div>
			</div>
		</div>

		<div class="dashboard-split">
			<div class="card card-padded">
				<div class="chart-header">
					<h2 class="card-title" style="margin:0">Evolução Mensal (Receita x Despesas x Lucro)</h2>
					<div class="chart-legend">
						<span><i class="legend-dot" style="background:#2563eb"></i>Receita</span>
						<span><i class="legend-dot" style="background:#ef4444"></i>Despesas</span>
						<span><i class="legend-dot" style="background:#16a34a"></i>Lucro</span>
					</div>
				</div>
				${buildEvolutionChartSvg(summary.evolucaoMensal)}
			</div>

			<div>
				<div class="card card-padded">
					<h2 class="card-title">Origem da Receita</h2>
					${summary.origemReceita.map(renderSourceBar).join('')}
				</div>
				<div class="card card-padded">
					<h2 class="card-title">Indicadores Chave</h2>
					<div class="indicator-row"><span class="text-muted">Ticket Médio / Paciente:</span><strong>${formatCurrency(summary.ticketMedio)}</strong></div>
					<div class="indicator-row"><span class="text-muted">Faturas Pendentes:</span><strong style="color:var(--warning)">${formatCurrency(summary.faturasPendentes)}</strong></div>
					<div class="indicator-row"><span class="text-muted">Custo Médio / Leito:</span><strong>${formatCurrency(summary.custoMedioLeito)}/dia</strong></div>
					<div style="margin-top:0.85rem">
						<span class="badge badge-success">● Status Financeiro: ${escapeHtml(summary.statusFinanceiro)}</span>
					</div>
				</div>
			</div>
		</div>
	`;
}

function buildEvolutionChartSvg(monthly) {
	const maxVal = Math.max(100, ...monthly.map((m) => m.receita));
	const bars = monthly.map((m, idx) => ({
		x: 35 + idx * 82,
		recH: Math.round((m.receita / maxVal) * 145),
		despH: Math.round((m.despesas / maxVal) * 145),
		profitY: 175 - Math.round((m.lucro / maxVal) * 145),
		label: m.mes
	}));
	const linePoints = bars.map((b) => `${b.x + 22},${b.profitY}`).join(' ');
	const rectMarkup = bars.map((b) => `
		<rect x="${b.x}" y="${175 - b.recH}" width="20" height="${b.recH}" rx="4" fill="#2563eb"></rect>
		<rect x="${b.x + 24}" y="${175 - b.despH}" width="20" height="${b.despH}" rx="4" fill="#ef4444"></rect>
		<text x="${b.x + 22}" y="198" text-anchor="middle" font-size="12" fill="currentColor">${b.label}</text>
	`).join('');

	return `
		<svg viewBox="0 0 540 215" width="100%" role="img" aria-label="Gráfico de evolução mensal de receita, despesas e lucro">
			<line x1="20" y1="175" x2="525" y2="175" stroke="currentColor" stroke-opacity="0.2"></line>
			${rectMarkup}
			<polyline fill="none" stroke="#16a34a" stroke-width="3" points="${linePoints}"></polyline>
		</svg>
	`;
}

function renderSourceBar(source) {
	return `
		<div class="progress-row">
			<div class="progress-head"><span>${escapeHtml(source.categoria)}</span><span>${source.percentual}%</span></div>
			<div class="progress-track"><div class="progress-fill" style="width:${source.percentual}%"></div></div>
		</div>
	`;
}
