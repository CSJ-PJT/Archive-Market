const STORAGE_KEY = "archive.locale";
const LEGACY_KEY = "archive-market-language";
const locales = ["ko", "en", "ja", "zh-CN"];

const dictionaries = {
  ko: {
    "brand.subtitle": "합성 커머스 백엔드",
    "nav.overview": "개요",
    "nav.orders": "주문",
    "nav.outbox": "Outbox",
    "nav.contracts": "연동 계약",
    "sidebar.noteTitle": "Synthetic only",
    "sidebar.noteBody": "실제 고객, 결제, 주소, 배송사, 금융 데이터는 사용하지 않습니다.",
    "header.eyebrow": "Archive Platform Ecosystem",
    "header.title": "Market 커머스 관제 보드",
    "header.description": "Nexus, Ledger, ArchiveOS 연동을 위한 수요, 주문, 매출, 리스크, outbox, 외부 이벤트 상태를 관제합니다.",
    "action.refresh": "새로고침",
    "action.publish": "Outbox 발행",
    "action.sampleInbox": "샘플 이벤트 수신",
    "metric.health": "상태",
    "metric.revenue": "매출",
    "metric.profit": "이익",
    "metric.outbox": "Outbox 대기",
    "metric.syntheticKrw": "Synthetic KRW",
    "panel.operationsEyebrow": "운영",
    "panel.operationsTitle": "서비스 요약",
    "panel.riskEyebrow": "리스크",
    "panel.riskTitle": "수요 및 클레임 신호",
    "panel.ordersEyebrow": "주문 흐름",
    "panel.ordersTitle": "최근 Market 주문",
    "panel.outboxEyebrow": "Outbox 패턴",
    "panel.outboxTitle": "외부 전달 상태",
    "panel.inboxEyebrow": "외부 이벤트",
    "panel.inboxTitle": "Inbox 가드",
    "panel.contractEyebrow": "계약",
    "panel.contractTitle": "연동 Payload 예시",
    "risk.returnRate": "반품률",
    "risk.claimRate": "클레임률",
    "risk.highRiskOrders": "고위험 주문",
    "table.orderId": "주문 ID",
    "table.customer": "고객",
    "table.status": "상태",
    "table.amount": "결제액",
    "table.risk": "리스크",
    "state.loading": "로딩 중",
    "state.empty": "아직 주문이 없습니다",
    "toast.refreshed": "대시보드 데이터를 갱신했습니다.",
    "toast.published": "Outbox 발행 요청이 처리되었습니다.",
    "toast.inbox": "샘플 외부 이벤트를 수신했습니다.",
    "label.orders": "주문",
    "label.confirmed": "확정",
    "label.cancelled": "취소",
    "label.returned": "반품",
    "label.claimed": "클레임",
    "label.cost": "비용",
    "label.cash": "현금 잔액",
    "label.burn": "Burn rate",
    "label.inboxTotal": "Inbox 합계",
    "label.inboxProcessed": "처리됨",
    "label.inboxRejected": "거부됨"
  },
  en: {
    "brand.subtitle": "Synthetic Commerce Backend",
    "nav.overview": "Overview",
    "nav.orders": "Orders",
    "nav.outbox": "Outbox",
    "nav.contracts": "Integration Contracts",
    "sidebar.noteTitle": "Synthetic only",
    "header.eyebrow": "Archive Platform Ecosystem",
    "header.title": "Market Commerce Control Board",
    "sidebar.noteBody": "No real customer, payment, address, carrier, or financial data is used.",
    "header.description": "Monitor demand, orders, revenue, risk, outbox, and inbound event state for Nexus, Ledger, and ArchiveOS integration.",
    "action.refresh": "Refresh",
    "action.publish": "Publish Outbox",
    "action.sampleInbox": "Send Sample Event",
    "metric.health": "Health",
    "metric.revenue": "Revenue",
    "metric.profit": "Profit",
    "metric.outbox": "Outbox Pending",
    "metric.syntheticKrw": "Synthetic KRW",
    "panel.operationsEyebrow": "Operations",
    "panel.operationsTitle": "Service Summary",
    "panel.riskEyebrow": "Risk",
    "panel.riskTitle": "Demand and Claim Signal",
    "panel.ordersEyebrow": "Order Stream",
    "panel.ordersTitle": "Latest Market Orders",
    "panel.outboxEyebrow": "Outbox Pattern",
    "panel.outboxTitle": "Outbound Delivery State",
    "panel.inboxEyebrow": "External Events",
    "panel.inboxTitle": "Inbox Guard",
    "panel.contractEyebrow": "Contracts",
    "panel.contractTitle": "Integration Payload Examples",
    "risk.returnRate": "Return rate",
    "risk.claimRate": "Claim rate",
    "risk.highRiskOrders": "High-risk orders",
    "table.orderId": "Order ID",
    "table.customer": "Customer",
    "table.status": "Status",
    "table.amount": "Payment",
    "table.risk": "Risk",
    "state.loading": "Loading",
    "state.empty": "No orders yet",
    "toast.refreshed": "Dashboard data was refreshed.",
    "toast.published": "Outbox publish request was processed.",
    "toast.inbox": "Sample external event was received.",
    "label.orders": "Orders",
    "label.confirmed": "Confirmed",
    "label.cancelled": "Cancelled",
    "label.returned": "Returned",
    "label.claimed": "Claimed",
    "label.cost": "Cost",
    "label.cash": "Cash balance",
    "label.burn": "Burn rate",
    "label.inboxTotal": "Inbox total",
    "label.inboxProcessed": "Processed",
    "label.inboxRejected": "Rejected"
  },
  ja: {
    "brand.subtitle": "合成コマースバックエンド",
    "nav.overview": "概要",
    "nav.orders": "注文",
    "nav.outbox": "Outbox",
    "nav.contracts": "連携契約",
    "sidebar.noteTitle": "Synthetic only",
    "sidebar.noteBody": "実在の顧客、決済、住所、配送会社、金融データは使用しません。",
    "header.eyebrow": "Archive Platform Ecosystem",
    "header.title": "Market コマース管理ボード",
    "header.description": "Nexus、Ledger、ArchiveOS 連携のために需要、注文、収益、リスク、outbox、外部イベントを監視します。",
    "action.refresh": "更新",
    "action.publish": "Outbox 発行",
    "action.sampleInbox": "サンプルイベント受信",
    "metric.health": "Health",
    "metric.revenue": "収益",
    "metric.profit": "利益",
    "metric.outbox": "Outbox 待機",
    "metric.syntheticKrw": "Synthetic KRW",
    "panel.operationsEyebrow": "運用",
    "panel.operationsTitle": "サービス概要",
    "panel.riskEyebrow": "リスク",
    "panel.riskTitle": "需要とクレームシグナル",
    "panel.ordersEyebrow": "注文ストリーム",
    "panel.ordersTitle": "最新 Market 注文",
    "panel.outboxEyebrow": "Outbox パターン",
    "panel.outboxTitle": "外部配信状態",
    "panel.inboxEyebrow": "外部イベント",
    "panel.inboxTitle": "Inbox ガード",
    "panel.contractEyebrow": "契約",
    "panel.contractTitle": "連携 Payload 例",
    "risk.returnRate": "返品率",
    "risk.claimRate": "クレーム率",
    "risk.highRiskOrders": "高リスク注文",
    "table.orderId": "注文 ID",
    "table.customer": "顧客",
    "table.status": "状態",
    "table.amount": "決済額",
    "table.risk": "リスク",
    "state.loading": "読み込み中",
    "state.empty": "注文はまだありません",
    "toast.refreshed": "ダッシュボードデータを更新しました。",
    "toast.published": "Outbox 発行リクエストを処理しました。",
    "toast.inbox": "サンプル外部イベントを受信しました。",
    "label.orders": "注文",
    "label.confirmed": "確定",
    "label.cancelled": "キャンセル",
    "label.returned": "返品",
    "label.claimed": "クレーム",
    "label.cost": "コスト",
    "label.cash": "現金残高",
    "label.burn": "バーンレート",
    "label.inboxTotal": "Inbox 合計",
    "label.inboxProcessed": "処理済み",
    "label.inboxRejected": "拒否"
  },
  "zh-CN": {
    "brand.subtitle": "合成商务后端",
    "nav.overview": "概览",
    "nav.orders": "订单",
    "nav.outbox": "Outbox",
    "nav.contracts": "集成契约",
    "sidebar.noteTitle": "Synthetic only",
    "sidebar.noteBody": "不使用真实客户、支付、地址、承运商或金融数据。",
    "header.eyebrow": "Archive Platform Ecosystem",
    "header.title": "Market 商务控制面板",
    "header.description": "监控面向 Nexus、Ledger、ArchiveOS 集成的需求、订单、收入、风险、outbox 与外部事件状态。",
    "action.refresh": "刷新",
    "action.publish": "发布 Outbox",
    "action.sampleInbox": "发送示例事件",
    "metric.health": "Health",
    "metric.revenue": "收入",
    "metric.profit": "利润",
    "metric.outbox": "Outbox 待处理",
    "metric.syntheticKrw": "Synthetic KRW",
    "panel.operationsEyebrow": "运营",
    "panel.operationsTitle": "服务摘要",
    "panel.riskEyebrow": "风险",
    "panel.riskTitle": "需求与索赔信号",
    "panel.ordersEyebrow": "订单流",
    "panel.ordersTitle": "最新 Market 订单",
    "panel.outboxEyebrow": "Outbox 模式",
    "panel.outboxTitle": "外发状态",
    "panel.inboxEyebrow": "外部事件",
    "panel.inboxTitle": "Inbox 防护",
    "panel.contractEyebrow": "契约",
    "panel.contractTitle": "集成 Payload 示例",
    "risk.returnRate": "退货率",
    "risk.claimRate": "索赔率",
    "risk.highRiskOrders": "高风险订单",
    "table.orderId": "订单 ID",
    "table.customer": "客户",
    "table.status": "状态",
    "table.amount": "支付额",
    "table.risk": "风险",
    "state.loading": "加载中",
    "state.empty": "暂无订单",
    "toast.refreshed": "仪表盘数据已刷新。",
    "toast.published": "Outbox 发布请求已处理。",
    "toast.inbox": "已接收示例外部事件。",
    "label.orders": "订单",
    "label.confirmed": "已确认",
    "label.cancelled": "已取消",
    "label.returned": "退货",
    "label.claimed": "索赔",
    "label.cost": "成本",
    "label.cash": "现金余额",
    "label.burn": "燃烧率",
    "label.inboxTotal": "Inbox 总数",
    "label.inboxProcessed": "已处理",
    "label.inboxRejected": "已拒绝"
  }
};

const languageLabels = {
  ko: "한국어",
  en: "English",
  ja: "日本語",
  "zh-CN": "简体中文"
};

const contractEvents = [
  ["NEXUS", "MARKET_ORDER_PLACED", { orderId: "ORD-20260710-001", customerType: "B2B_CUSTOMER", productType: "BATTERY_MODULE", quantity: 10, orderAmount: 1200000, priority: "HIGH", requiresShipment: true }],
  ["NEXUS", "PRODUCTION_REQUESTED", { orderId: "ORD-20260710-001", productType: "BATTERY_MODULE", quantity: 10, orderAmount: 1200000, priority: "HIGH", requiresShipment: true }],
  ["NEXUS", "SHIPMENT_REQUESTED", { orderId: "ORD-20260710-001", customerType: "B2B_CUSTOMER", requiresShipment: true, priority: "HIGH" }],
  ["NEXUS", "ORDER_CANCELLED", { orderId: "ORD-20260710-001", reason: "Synthetic customer cancellation", refundRequired: true }],
  ["NEXUS", "RETURN_REQUESTED", { orderId: "ORD-20260710-001", returnAmount: 300000, reason: "Synthetic return request" }],
  ["NEXUS", "QUALITY_CLAIM_CREATED", { orderId: "ORD-20260710-001", claimAmount: 120000, reason: "Synthetic quality claim" }],
  ["LEDGER", "SALES_REVENUE_CONFIRMED", { orderId: "ORD-20260710-001", revenueType: "PRODUCT_SALES_REVENUE_RECOGNIZED", amount: 1200000, currency: "KRW" }],
  ["LEDGER", "PAYMENT_CAPTURED", { orderId: "ORD-20260710-001", paymentId: "PAY-20260710-001", amount: 1200000, currency: "KRW" }],
  ["LEDGER", "REFUND_REQUESTED", { orderId: "ORD-20260710-001", amount: 300000, currency: "KRW", reason: "Synthetic refund request" }],
  ["LEDGER", "CLAIM_COMPENSATION_CONFIRMED", { orderId: "ORD-20260710-001", amount: 120000, currency: "KRW", reason: "Synthetic claim compensation" }],
  ["LEDGER", "MARKET_SERVICE_FEE_PAID", { orderId: "ORD-20260710-001", amount: 12000, currency: "KRW", reason: "Synthetic market service fee" }],
  ["LEDGER", "PAYMENT_PROCESSING_FEE_PAID", { orderId: "ORD-20260710-001", amount: 24000, currency: "KRW", reason: "Synthetic payment processing fee" }]
];

const state = {
  locale: readLocale(),
  operations: null,
  economy: null,
  runtime: null,
  runtimeEvents: [],
  cashflow: null,
  workforce: null,
  capacity: null,
  profitability: null,
  outbox: {},
  inbox: [],
  orders: []
};

const $ = (id) => document.getElementById(id);

document.addEventListener("DOMContentLoaded", () => {
  initLanguage();
  bindActions();
  renderContracts();
  applyLocale(state.locale);
  refreshAll();
});

function readLocale() {
  const stored = localStorage.getItem(STORAGE_KEY) || localStorage.getItem(LEGACY_KEY);
  return locales.includes(stored) ? stored : "ko";
}

function t(key) {
  return dictionaries[state.locale]?.[key] || dictionaries.ko[key] || dictionaries.en[key] || key;
}

function initLanguage() {
  const select = $("languageSelect");
  select.innerHTML = locales.map((locale) => `<option value="${locale}">${languageLabels[locale]}</option>`).join("");
  select.value = state.locale;
  select.addEventListener("change", (event) => {
    state.locale = event.target.value;
    applyLocale(state.locale);
    renderAll();
  });
}

function applyLocale(locale) {
  localStorage.setItem(STORAGE_KEY, locale);
  localStorage.setItem(LEGACY_KEY, locale);
  document.documentElement.lang = locale;
  document.documentElement.dataset.language = locale;
  document.querySelectorAll("[data-i18n]").forEach((element) => {
    element.textContent = t(element.dataset.i18n);
  });
  $("languageSelect").value = locale;
}

function bindActions() {
  $("refreshButton").addEventListener("click", async () => {
    await refreshAll();
    toast(t("toast.refreshed"));
  });
  $("publishButton").addEventListener("click", async () => {
    await api("/api/outbox/publish", { method: "POST" });
    await refreshAll();
    toast(t("toast.published"));
  });
}

async function refreshAll() {
  const [health, operations, economy, runtime, runtimeEvents, cashflow, workforce, capacity, profitability, outbox, inbox, orders] = await Promise.allSettled([
    fetch("/actuator/health").then((response) => response.json()),
    api("/api/operations/summary"),
    api("/api/market-economy/summary"),
    api("/api/runtime/status"),
    api("/api/runtime-events/recent?limit=8"),
    api("/api/market-cashflow/summary"),
    api("/api/workforce/summary"),
    api("/api/capacity/summary"),
    api("/api/market-profitability/summary"),
    api("/api/outbox/summary"),
    api("/api/events/inbox"),
    api("/api/orders?size=8&sort=createdAt,desc")
  ]);
  state.health = unwrap(health);
  state.operations = unwrap(operations);
  state.economy = unwrap(economy);
  state.runtime = unwrap(runtime);
  state.runtimeEvents = unwrap(runtimeEvents) || [];
  state.cashflow = unwrap(cashflow);
  state.workforce = unwrap(workforce);
  state.capacity = unwrap(capacity);
  state.profitability = unwrap(profitability);
  state.outbox = unwrap(outbox) || {};
  state.inbox = unwrap(inbox) || [];
  state.orders = unwrap(orders)?.content || [];
  renderAll();
}

async function api(path, options) {
  const response = await fetch(path, options);
  if (!response.ok) throw new Error(`${response.status} ${path}`);
  const body = await response.json();
  return body?.data ?? body;
}

function unwrap(result) {
  return result.status === "fulfilled" ? result.value : null;
}

function renderAll() {
  renderMetrics();
  renderRuntime();
  renderBalance();
  renderCapitalAndWorkforce();
  renderOperations();
  renderRisk();
  renderOrders();
  renderOutbox();
  renderInbox();
}

function renderMetrics() {
  const economy = state.economy?.economy || {};
  const runtime = state.runtime || state.operations?.runtime || {};
  $("healthStatus").textContent = state.health?.status || state.operations?.status || "-";
  $("updatedAt").textContent = new Intl.DateTimeFormat(toIntlLocale(), { dateStyle: "short", timeStyle: "medium" }).format(new Date());
  $("gmvAmount").textContent = money(economy.gmv);
  $("recognizedRevenue").textContent = money(economy.recognizedRevenue);
  $("calculationScope").textContent = economy.calculationScope || "-";
  $("profitAmount").textContent = money(economy.operatingProfit ?? economy.profit);
  $("operatingMargin").textContent = `Margin ${number(economy.operatingMargin)}%`;
  $("cashBalance").textContent = money(economy.cashBalance);
  $("bankruptcyRisk").textContent = `Bankruptcy risk: ${economy.bankruptcyRisk || "-"}`;
  $("runtimeBacklog").textContent = number(runtime.backlogCount ?? economy.backlogCount);
  $("runtimePipeline").textContent = runtime.pipelineStatus || "NO_DATA";
  $("outboxPending").textContent = number(state.outbox.PENDING || state.outbox.pending || 0);
  $("outboxDryRun").textContent = `DRY_RUN ${number(state.outbox.DRY_RUN || state.outbox.dry_run || 0)}`;
  $("serviceStatus").textContent = state.operations?.status || state.economy?.status || "-";
}

function renderRuntime() {
  const runtime = state.runtime || state.operations?.runtime || {};
  $("runtimeStatus").textContent = runtime.pipelineStatus || "NO_DATA";
  const rows = [
    ["Scheduler", runtime.schedulerStatus || "-"],
    ["Last work", dateTime(runtime.lastWorkAt)],
    ["Last event", dateTime(runtime.lastEventAt)],
    ["Events / tick", number(runtime.eventsProducedLastTick)],
    ["Backlog", number(runtime.backlogCount)],
    ["Oldest backlog", `${number(runtime.oldestBacklogAgeSeconds)} sec`],
    ["Cursor", shortId(runtime.latestCursor)],
    ["Degraded", runtime.degradedReason || "NONE"]
  ];
  $("runtimeList").innerHTML = summaryRows(rows);
  $("runtimeEventList").innerHTML = state.runtimeEvents.length
    ? state.runtimeEvents.map((event) => `
      <article class="event-item severity-${String(event.severity || "INFO").toLowerCase()}">
        <div><strong>${escapeHtml(event.eventType || "-")}</strong><span>${escapeHtml(event.status || "-")}</span></div>
        <p>${escapeHtml(event.displayLabel || event.entityId || "-")}</p>
        <small>${escapeHtml(event.entityId || "-")} · ${dateTime(event.occurredAt)}</small>
      </article>
    `).join("")
    : `<p class="empty-state">${t("state.empty")}</p>`;
}

function renderBalance() {
  const economy = state.economy?.economy || {};
  const cards = [
    ["Total expense", money(economy.totalExpense)],
    ["Reserve balance", money(economy.reserveBalance)],
    ["Outstanding payables", money(economy.outstandingPayables)],
    ["Pending settlement", money(economy.pendingSettlementAmount)],
    ["Negative profit streak", number(economy.negativeProfitStreak)],
    ["Capacity utilization", `${number(economy.capacityUtilization)}%`]
  ];
  $("balanceGrid").innerHTML = cards.map(([label, value]) => `
    <div class="balance-card"><span>${label}</span><strong>${value}</strong></div>
  `).join("");
  $("costDriverList").innerHTML = summaryRows([
    ["Production purchase", money(economy.productionPurchaseCost)],
    ["Logistics fulfillment", money(economy.logisticsFulfillmentCost)],
    ["Settlement agency", money(economy.settlementAgencyFee)],
    ["Control tower", money(economy.controlTowerFee)],
    ["Workforce payroll", money(economy.workforceCost)]
  ]);
  $("calculationList").innerHTML = summaryRows([
    ["Scope", economy.calculationScope || "NO_DATA"],
    ["Period start", dateTime(economy.periodStart)],
    ["Period end", dateTime(economy.periodEnd)],
    ["Calculated", dateTime(economy.calculatedAt)],
    ["Data available", economy.dataAvailable === true ? "YES" : "NO"]
  ]);
}

function renderCapitalAndWorkforce() {
  const cashflow = state.cashflow || state.operations?.cashflow || {};
  const workforce = state.workforce || state.operations?.workforce || {};
  const capacity = state.capacity || {};
  const profitability = state.profitability || state.operations?.profitability || {};
  $("cashflowList").innerHTML = summaryRows([
    ["Available cash", money(cashflow.availableCash)],
    ["Expected receivable", money(cashflow.expectedReceivable)],
    ["Pending settlement", money(cashflow.pendingSettlementAmount)],
    ["Outstanding payables", money(cashflow.outstandingPayables)],
    ["Net profit", money(cashflow.netProfit)],
    ["Reserve balance", money(cashflow.reserveBalance)]
  ]);
  $("workforceList").innerHTML = summaryRows([
    ["Headcount", number(workforce.totalHeadcount)],
    ["Effective capacity", number(capacity.effectiveCapacity ?? workforce.effectiveCapacity)],
    ["Used capacity", number(capacity.usedCapacity ?? workforce.usedCapacity)],
    ["Backlog", number(capacity.backlog ?? workforce.backlog)],
    ["Utilization", `${number(capacity.capacityUtilization ?? workforce.capacityUtilization)}%`],
    ["Review required", number(profitability.reviewRequired ?? profitability.reviewRequiredOrders)]
  ]);
}

function renderOperations() {
  const orders = state.operations?.orders || state.economy?.orders || {};
  const economy = state.operations?.economy || state.economy?.economy || {};
  const rows = [
    [t("label.orders"), number(orders.total)],
    [t("label.confirmed"), number(orders.confirmed)],
    [t("label.cancelled"), number(orders.cancelled)],
    [t("label.returned"), number(orders.returned)],
    [t("label.claimed"), number(orders.claimed)],
    [t("label.cost"), money(economy.totalCost)],
    [t("label.cash"), money(economy.cashBalance)],
    [t("label.burn"), money(economy.burnRate)]
  ];
  $("operationsList").innerHTML = summaryRows(rows);
}

function renderRisk() {
  const risk = state.economy?.risk || state.operations?.risk || {};
  setMeter("returnRate", risk.returnRate || 0, "returnRateText", `${number(risk.returnRate || 0)}%`);
  setMeter("claimRate", risk.claimRate || 0, "claimRateText", `${number(risk.claimRate || 0)}%`);
  setMeter("highRiskOrders", Math.min(risk.highRiskOrders || 0, 100), "highRiskOrdersText", number(risk.highRiskOrders || 0));
}

function renderOrders() {
  const body = $("ordersBody");
  if (!state.orders.length) {
    body.innerHTML = `<tr><td colspan="5">${t("state.empty")}</td></tr>`;
    return;
  }
  body.innerHTML = state.orders.map((order) => `
    <tr>
      <td>${escapeHtml(order.orderId || "-")}</td>
      <td>${escapeHtml(order.customerType || order.customerId || "-")}</td>
      <td>${escapeHtml(order.orderStatus || "-")}</td>
      <td>${money(order.paymentAmount)}</td>
      <td>${number(order.riskScore || 0)}</td>
    </tr>
  `).join("");
}

function renderOutbox() {
  const keys = ["PENDING", "PUBLISHED", "RETRY", "FAILED", "SKIPPED", "DRY_RUN"];
  $("outboxGrid").innerHTML = keys.map((key) => `
    <div class="status-card">
      <span>${key}</span>
      <strong>${number(state.outbox[key] || state.outbox[key.toLowerCase()] || 0)}</strong>
    </div>
  `).join("");
}

function renderInbox() {
  const counts = state.inbox.reduce((acc, item) => {
    const status = item.status || "UNKNOWN";
    acc[status] = (acc[status] || 0) + 1;
    return acc;
  }, {});
  const rows = [
    [t("label.inboxTotal"), state.inbox.length],
    [t("label.inboxProcessed"), counts.PROCESSED || 0],
    [t("label.inboxRejected"), counts.REJECTED || 0]
  ];
  $("inboxList").innerHTML = summaryRows(rows.map(([key, value]) => [key, number(value)]));
}

function renderContracts() {
  $("contractGrid").innerHTML = contractEvents.map(([target, eventType, payload]) => {
    const event = {
      eventId: `EVT-${eventType}-001`,
      idempotencyKey: `MARKET:${eventType}:ORD-20260710-001`,
      source: "Archive-Market",
      target,
      eventType,
      schemaVersion: 1,
      occurredAt: "2026-07-10T00:00:00Z",
      simulationRunId: "SIM-20260710-001",
      settlementCycleId: "SETTLEMENT-2026-07-10",
      correlationId: "CORR-20260710-001",
      causationId: "ORD-20260710-001",
      hopCount: 1,
      maxHop: 5,
      payload
    };
    return `
      <article class="contract-card">
        <header><strong>${eventType}</strong><code>${target}</code></header>
        <pre>${escapeHtml(JSON.stringify(event, null, 2))}</pre>
      </article>
    `;
  }).join("");
}

function setMeter(meterId, value, textId, text) {
  $(meterId).value = Number(value) || 0;
  $(textId).textContent = text;
}

function money(value) {
  const numeric = Number(value || 0);
  return new Intl.NumberFormat(toIntlLocale(), { style: "currency", currency: "KRW", maximumFractionDigits: 0 }).format(numeric);
}

function number(value) {
  return new Intl.NumberFormat(toIntlLocale(), { maximumFractionDigits: 2 }).format(Number(value || 0));
}

function summaryRows(rows) {
  return rows.map(([key, value]) => `<div><dt>${escapeHtml(key)}</dt><dd>${escapeHtml(value)}</dd></div>`).join("");
}

function dateTime(value) {
  if (!value) return "-";
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "-"
    : new Intl.DateTimeFormat(toIntlLocale(), { dateStyle: "short", timeStyle: "medium" }).format(date);
}

function shortId(value) {
  if (!value) return "-";
  return value.length > 26 ? `${value.slice(0, 14)}...${value.slice(-8)}` : value;
}

function toIntlLocale() {
  if (state.locale === "zh-CN") return "zh-CN";
  if (state.locale === "ja") return "ja-JP";
  if (state.locale === "en") return "en-US";
  return "ko-KR";
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function toast(message) {
  const toastElement = $("toast");
  toastElement.textContent = message;
  toastElement.classList.add("show");
  window.clearTimeout(toastElement.dataset.timer);
  toastElement.dataset.timer = window.setTimeout(() => toastElement.classList.remove("show"), 2600);
}
