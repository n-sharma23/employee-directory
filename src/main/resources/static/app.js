'use strict';

const API = '/api/employees';
const DEPARTMENTS = {
  ENGINEERING: 'Engineering',
  PRODUCT: 'Product',
  SALES: 'Sales',
  HR: 'Human Resources',
  FINANCE: 'Finance',
};

const rows = document.getElementById('rows');
const filter = document.getElementById('filter');
const form = document.getElementById('employee-form');
const submitButton = document.getElementById('submit');
const cancelButton = document.getElementById('cancel');
const formHeading = document.getElementById('form-heading');
const message = document.getElementById('message');
const money = new Intl.NumberFormat(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });

function showMessage(text, kind) {
  message.textContent = text;
  message.className = kind;
}

/** Calls the API and turns problem responses into thrown Errors with the server's message. */
async function request(url, options = {}) {
  const response = await fetch(url, options);
  if (!response.ok) {
    let detail = response.statusText;
    try {
      detail = (await response.json()).detail || detail;
    } catch { /* body was not JSON */ }
    throw new Error(detail);
  }
  return response.status === 204 ? null : response.json();
}

function cell(text, className) {
  const td = document.createElement('td');
  td.textContent = text; // textContent, never innerHTML: names come from users
  if (className) td.className = className;
  return td;
}

function actionButton(label, className, onClick) {
  const button = document.createElement('button');
  button.type = 'button';
  button.textContent = label;
  button.className = `small ${className}`;
  button.addEventListener('click', onClick);
  return button;
}

function renderRows(employees) {
  rows.replaceChildren(...employees.map(e => {
    const tr = document.createElement('tr');
    tr.append(
      cell(`${e.firstName} ${e.lastName}`),
      cell(e.email),
      cell(`${e.jobTitle} · ${DEPARTMENTS[e.department]}`),
      cell(money.format(e.salary), 'num'),
      cell(e.hireDate),
    );
    const actions = document.createElement('td');
    actions.className = 'actions';
    actions.append(
      actionButton('Edit', 'secondary', () => startEdit(e)),
      actionButton('+5%', 'secondary', () => giveRaise(e)),
      actionButton('Delete', 'danger', () => remove(e)),
    );
    tr.append(actions);
    return tr;
  }));
  if (employees.length === 0) {
    const tr = document.createElement('tr');
    const td = cell('No employees found.');
    td.colSpan = 6;
    tr.append(td);
    rows.append(tr);
  }
}

async function renderHeadcount() {
  const counts = await request(`${API}/stats/headcount`);
  const list = document.getElementById('headcount');
  list.replaceChildren(...Object.entries(counts).map(([dept, count]) => {
    const li = document.createElement('li');
    const strong = document.createElement('strong');
    strong.textContent = count;
    li.append(strong, DEPARTMENTS[dept]);
    return li;
  }));
}

async function refresh() {
  try {
    const query = filter.value ? `?department=${encodeURIComponent(filter.value)}` : '';
    renderRows(await request(API + query));
    await renderHeadcount();
  } catch (err) {
    showMessage(err.message, 'error');
  }
}

function startEdit(e) {
  for (const field of ['id', 'firstName', 'lastName', 'email', 'department', 'jobTitle', 'salary', 'hireDate']) {
    form.elements[field].value = e[field];
  }
  formHeading.textContent = `Edit ${e.firstName} ${e.lastName}`;
  submitButton.textContent = 'Save';
  cancelButton.hidden = false;
  form.scrollIntoView({ behavior: 'smooth' });
}

function resetForm() {
  form.reset();
  form.elements.id.value = '';
  formHeading.textContent = 'Add employee';
  submitButton.textContent = 'Add';
  cancelButton.hidden = true;
}

async function giveRaise(e) {
  try {
    await request(`${API}/${e.id}/raise`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ percent: 5 }),
    });
    showMessage(`${e.firstName} got a 5% raise.`, 'ok');
    await refresh();
  } catch (err) {
    showMessage(err.message, 'error');
  }
}

async function remove(e) {
  // Plain confirm() keeps this dependency-free; deletes can't be undone.
  if (!confirm(`Delete ${e.firstName} ${e.lastName}?`)) return;
  try {
    await request(`${API}/${e.id}`, { method: 'DELETE' });
    showMessage(`Deleted ${e.firstName} ${e.lastName}.`, 'ok');
    await refresh();
  } catch (err) {
    showMessage(err.message, 'error');
  }
}

form.addEventListener('submit', async event => {
  event.preventDefault();
  if (!form.checkValidity()) {
    form.reportValidity();
    return;
  }
  const data = Object.fromEntries(new FormData(form));
  const id = data.id;
  delete data.id;
  try {
    await request(id ? `${API}/${id}` : API, {
      method: id ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    showMessage(id ? 'Employee updated.' : 'Employee added.', 'ok');
    resetForm();
    await refresh();
  } catch (err) {
    showMessage(err.message, 'error');
  }
});

cancelButton.addEventListener('click', resetForm);
filter.addEventListener('change', refresh);

document.getElementById('import-file').addEventListener('change', async event => {
  const file = event.target.files[0];
  if (!file) return;
  const body = new FormData();
  body.append('file', file);
  try {
    const result = await request(`${API}/import`, { method: 'POST', body });
    showMessage(`Imported ${result.imported} employees.`, 'ok');
    await refresh();
  } catch (err) {
    showMessage(`Import failed: ${err.message}`, 'error');
  } finally {
    event.target.value = '';
  }
});

for (const [value, label] of Object.entries(DEPARTMENTS)) {
  filter.append(new Option(label, value));
  form.elements.department.append(new Option(label, value));
}
refresh();
