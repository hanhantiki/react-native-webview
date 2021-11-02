/* eslint-disable no-restricted-globals */
/* eslint-disable no-undef */
const url = 'miniapp-resource://framework/tf-tiniapp.worker.js';
// const url = 'https://tiniapp.tikicdn.com/tiniapps/framework_files/1.81.22/worker_files/tf-tiniapp.worker.js';
const startLoadScript = Date.now();
importScripts(url);
const totalCosts = Date.now() - startLoadScript;
const importScriptsCosts = __frameworkStartTime - startLoadScript;
const loadCosts = __frameworkEndTime - __frameworkStartTime;

const { connection } = navigator;
console.log(`connection: ${connection.effectiveType}`);
console.log(`importScripts costs: ${importScriptsCosts}ms`)
console.log(`  Load script costs: ${loadCosts}ms`);
console.log(`        Total costs: ${totalCosts}ms`);
console.log(`       check (== 0): ${totalCosts - importScriptsCosts - loadCosts}`);

self.postMessage({
  url,
  connection: connection.effectiveType,
  importScriptsCosts,
  loadCosts,
});
