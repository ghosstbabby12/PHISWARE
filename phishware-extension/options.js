'use strict';

const DEFAULT_BACKEND_ROOT = 'http://localhost:8080/api';
const input = document.getElementById('backendRoot');
const status = document.getElementById('status');

async function load() {
  document.querySelectorAll('[data-i18n]').forEach(el => {
    el.textContent = chrome.i18n.getMessage(el.getAttribute('data-i18n')) || el.textContent;
  });
  document.title = chrome.i18n.getMessage('optionsTitle');
  const { backendRoot } = await chrome.storage.sync.get({ backendRoot: DEFAULT_BACKEND_ROOT });
  input.value = backendRoot;
  input.placeholder = DEFAULT_BACKEND_ROOT;
}

document.getElementById('save').addEventListener('click', async () => {
  const value = (input.value || '').trim() || DEFAULT_BACKEND_ROOT;
  let root = value.replace(/\/$/, '');
  if (!/^https?:\/\/.+/i.test(root)) {
    status.textContent = chrome.i18n.getMessage('optionsInvalid');
    return;
  }
  const permissionParams = { origins: [root.replace(/\/api\/?$/, '') + '/*'] };
  try {
    const granted = await chrome.permissions.request(permissionParams);
    if (!granted) {
      status.textContent = chrome.i18n.getMessage('optionsPermissionDenied');
      return;
    }
  } catch {
    // permissions API may be unavailable; store anyway
  }
  await chrome.storage.sync.set({ backendRoot: root });
  status.textContent = chrome.i18n.getMessage('optionsSaved');
});

load();
