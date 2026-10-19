'use strict';
const $ = id => document.getElementById(id);
const hospital = $('hospital'), department = $('department'), session = $('session');
let activeQuery = null, progressRequest = null, optionsRequest = null, loading = false, nextRefresh = 0, timer = null;
let optionsReady = false;
const taipeiHour = Number(new Intl.DateTimeFormat('en-GB', {timeZone:'Asia/Taipei',hour:'2-digit',hourCycle:'h23'}).format(new Date()));
session.value = taipeiHour < 12 ? '1' : taipeiHour < 17 ? '2' : '3';
const params = new URLSearchParams(location.search);
if (['1','2','3'].includes(params.get('session'))) session.value = params.get('session');
function message(text) { $('message').textContent = text; $('message').hidden = !text; }
function status(text, type = '') { $('status').textContent = text; $('status').className = `status ${type}`; }
function controls() { $('search-button').disabled = !optionsReady || loading; $('refresh-button').disabled = !activeQuery || loading || !optionsReady; }
function selectedLabel(select) { return select.selectedOptions[0]?.textContent || ''; }
function option(value, label) { const item = document.createElement('option'); item.value = value; item.textContent = label; return item; }
async function json(url, signal) {
  const response = await fetch(url, {signal, headers:{Accept:'application/json'}});
  let data; try { data = await response.json(); } catch { throw new Error('服務暫時無法使用，請稍後重試。'); }
  if (!response.ok) throw new Error(data.error || '查詢失敗，請稍後再試。');
  return data;
}
function clearResults() {
  activeQuery = null; nextRefresh = 0;
  if (progressRequest) progressRequest.abort();
  loading = false;
  $('results').classList.remove('loading'); $('results').replaceChildren();
  document.querySelector('.results').setAttribute('aria-busy','false');
  $('source-time').textContent='院方資料時間 —'; $('fetched-time').textContent='';
  $('query-summary').textContent='條件已變更，請按「查詢叫號」查看結果。';
  $('official-link').href=`https://register.cgmh.org.tw/Progress/${encodeURIComponent(hospital.value)}`;
  status('尚未查詢'); message(''); empty('選擇查詢條件','按下「查詢叫號」，查看所選時段的門診進度。'); controls();
}
function empty(title, detail) {
  const box = document.createElement('div'); box.className='empty';
  const icon=document.createElement('div'); icon.className='empty-icon'; icon.textContent='＋';
  const h=document.createElement('h3'); h.textContent=title;
  const p=document.createElement('p'); p.textContent=detail;
  box.append(icon,h,p); $('results').replaceChildren(box);
}
async function loadDepartments(preferred) {
  if (optionsRequest) optionsRequest.abort();
  const request = new AbortController(); optionsRequest = request;
  optionsReady=false; department.disabled=true; department.replaceChildren(option('','載入科別中…')); controls();
  try {
    const data=await json(`/api/hospitals/cgmh/${encodeURIComponent(hospital.value)}/departments`,request.signal);
    if (optionsRequest!==request) return false;
    department.replaceChildren(...data.map(d=>option(d.code,d.name)));
    if (!data.length) throw new Error('此院區目前沒有可查詢的科別。');
    department.value=data.some(d=>d.code===preferred)?preferred:(data.some(d=>d.code==='02')?'02':data[0].code);
    optionsReady=true; department.disabled=false; controls(); return true;
  } catch(error) {
    if (error.name==='AbortError') return false;
    if (optionsRequest!==request) return false;
    department.replaceChildren(option('','科別載入失敗'));
    message(`${error.message} 可重新選取院區重試。`); status('載入失敗','error'); controls(); return false;
  }
}
function render(data) {
  const grid=$('results'); grid.replaceChildren();
  if (!data.clinics.length) empty('這個條件目前沒有診間資料','可切換科別或時段查詢；沒有資料不代表整間醫院休診。');
  for (const clinic of data.clinics) {
    const card=$('clinic-template').content.cloneNode(true);
    card.querySelector('.department-name').textContent=clinic.department || '未提供科別';
    const doctor=card.querySelector('.doctor-name'); doctor.textContent=clinic.doctor || '未提供醫師';
    const small=document.createElement('small'); small.textContent='醫師'; doctor.append(small);
    const current=card.querySelector('.current-number'); current.textContent=clinic.currentNumber || '尚未提供';
    if (!clinic.currentNumber) current.classList.add('no-number');
    else if (clinic.currentNumber.includes('過號')) current.classList.add('overdue');
    card.querySelector('.next-number').textContent=clinic.nextNumber || '—';
    card.querySelector('.following-number').textContent=clinic.followingNumber || '—';
    card.querySelector('.location').textContent=clinic.location || '院方未提供看診位置';
    grid.append(card);
  }
  $('source-time').textContent=`院方資料時間 ${data.sourceUpdatedAt || '未提供'}`;
  $('fetched-time').textContent=`本次擷取：${new Intl.DateTimeFormat('zh-TW',{timeZone:'Asia/Taipei',dateStyle:'short',timeStyle:'medium'}).format(new Date(data.fetchedAt))} · ${data.clinics.length} 個診間`;
  $('official-link').href=data.sourceUrl;
  status(data.stale?'資料過期':data.clinics.length?'查詢成功':'無診間資料',data.stale?'warning':'live');
  message(data.stale?'暫時無法取得最新資料，以下保留上次成功結果，請留意院方資料時間。':'');
}
async function search(query) {
  if (progressRequest) progressRequest.abort();
  const request=new AbortController(); progressRequest=request;
  activeQuery=query; loading=true; nextRefresh=0; controls(); message(''); status('查詢中');
  $('query-summary').textContent=`${query.hospitalName} ／ ${query.departmentName} ／ ${query.sessionName}`;
  $('results').classList.add('loading'); document.querySelector('.results').setAttribute('aria-busy','true');
  try {
    const data=await json(`/api/hospitals/cgmh/${encodeURIComponent(query.hospital)}/progress?department=${encodeURIComponent(query.department)}&session=${encodeURIComponent(query.session)}`,request.signal);
    if (progressRequest!==request) return;
    render(data);
    const url=new URL(location.href); url.search=new URLSearchParams({hospital:query.hospital,department:query.department,session:query.session}); history.replaceState(null,'',url);
  } catch(error) {
    if (error.name==='AbortError' || progressRequest!==request) return;
    // Never show a previous successful response as a current result after an HTTP error.
    empty('暫時無法取得叫號', '請稍後重新整理，或前往長庚官方查詢。');
    $('source-time').textContent='院方資料時間 —'; $('fetched-time').textContent='';
    message(error.message); status('查詢失敗','error');
  } finally {
    if (progressRequest===request) {
      loading=false; nextRefresh=Date.now()+60000; controls();
      $('results').classList.remove('loading'); document.querySelector('.results').setAttribute('aria-busy','false');
    }
  }
}
$('search-form').addEventListener('submit',event=>{event.preventDefault(); if(optionsReady&&!loading)search({hospital:hospital.value,department:department.value,session:session.value,hospitalName:selectedLabel(hospital),departmentName:selectedLabel(department),sessionName:selectedLabel(session)});});
hospital.addEventListener('change',async()=>{clearResults();await loadDepartments(department.value);});
department.addEventListener('change',clearResults); session.addEventListener('change',clearResults);
$('refresh-button').addEventListener('click',()=>{if(activeQuery&&!loading)search(activeQuery);});
$('auto-refresh').addEventListener('change',()=>{nextRefresh=Date.now()+60000;tick();});
function tick() {
  if (!$('auto-refresh').checked) { $('countdown').textContent='自動更新已暫停'; return; }
  if (!activeQuery) { $('countdown').textContent='查詢後每分鐘自動更新'; return; }
  if (loading) { $('countdown').textContent='正在更新…'; return; }
  if (document.hidden) { $('countdown').textContent='頁面在背景，暫停更新'; return; }
  const seconds=Math.max(0,Math.ceil((nextRefresh-Date.now())/1000));
  $('countdown').textContent=`${seconds} 秒後自動更新`;
  if (!seconds&&optionsReady) search(activeQuery);
}
document.addEventListener('visibilitychange',tick);
async function init() {
  try {
    const branches=await json('/api/hospitals/cgmh/branches');
    const order=['1','2','E','3','5','V','6','8','T','M','B','N'];
    hospital.replaceChildren(...order.filter(code=>branches[code]).map(code=>option(code,branches[code])));
    hospital.value=branches[params.get('hospital')]?params.get('hospital'):'1'; hospital.disabled=false;
    if(await loadDepartments(params.get('department'))) $('search-form').requestSubmit();
    timer=setInterval(tick,1000); tick();
  } catch(error) { message('無法連線至查詢服務，請重新載入頁面再試。'); status('連線失敗','error'); }
}
init();
