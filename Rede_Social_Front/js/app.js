// ===== Conecta - front (HTML/CSS/JS puro) =====
// As chamadas REST usam caminhos relativos (servidor.py repassa ao back). O WebSocket conecta direto no back:
const WS_URL = `ws://${location.hostname}:8085/ws/chat`;

const $ = s => document.querySelector(s);
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => '&#' + c.charCodeAt(0) + ';');
const fdt = d => new Date(d).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
let me = JSON.parse(localStorage.me || 'null'), ws, conv = null, msgs = [];
const likes = () => JSON.parse(localStorage['l' + me.id] || '[]');
const go = h => location.hash == h ? route() : location.hash = h;

async function api(m, u, b) {
  const h = { 'Content-Type': 'application/json' };
  if (me) h['X-Usuario-Id'] = me.id;
  const r = await fetch(u, { method: m, headers: h, body: b ? JSON.stringify(b) : undefined });
  if (r.status == 204) return null;
  const d = await r.json().catch(() => null);
  if (!r.ok) throw Error(d?.detalhes?.length ? d.detalhes.join('; ') : d?.mensagem || 'Erro ' + r.status);
  return d;
}
function toast(t) { const e = $('#toast'); e.textContent = t; e.className = 'show'; clearTimeout(toast.t); toast.t = setTimeout(() => e.className = '', 3500); }
const tryo = f => async (...a) => { try { return await f(...a); } catch (e) { toast(e.message == 'Failed to fetch' ? 'Back-end offline (porta 8085)' : e.message); } };

const pc = {};
const perfil = id => pc[id] ??= api('GET', `/api/usuarios/${id}/perfil`).catch(() => ({ usuarioId: id, username: 'usuario' + id, nomeExibicao: 'Usuário ' + id }));
const av = (p, c = '') => p.fotoPerfil ? `<img class="av ${c}" src="${esc(p.fotoPerfil)}" alt="">` : `<span class="av ${c}">${esc((p.nomeExibicao || p.username || '?')[0].toUpperCase())}</span>`;

function player(u) {
  const y = u.match(/(?:youtu\.be\/|v=|embed\/|shorts\/)([\w-]{11})/);
  if (y) return `<iframe src="https://www.youtube.com/embed/${y[1]}" allowfullscreen></iframe>`;
  if (/\.(mp4|webm|ogg)(\?|$)/i.test(u)) return `<video src="${esc(u)}" controls></video>`;
  return `<a class="link" href="${esc(u)}" target="_blank" rel="noopener">▶ Abrir vídeo</a>`;
}

// ---------- Login / Cadastro ----------
const fl = (n, t, x = '', eye) => `<label class="fl"><input name="${n}" placeholder=" " ${x} required><span>${t}</span>${eye ? '<button type="button" class="eye" tabindex="-1">👁</button>' : ''}</label>`;
function viewLogin() {
  let reg = false;
  $('#app').innerHTML = `<div class="login"><section class="hero"><i class="blob b1"></i><i class="blob b2"></i><i class="blob b3"></i>
    <div class="hero-in"><span class="logo">Conecta</span><h1>Onde as boas conversas <em>florescem.</em></h1>
    <p>Compartilhe vídeos, siga pessoas e converse em tempo real, num espaço leve e só seu.</p>
    <div class="chips"><span>🎬 Vídeos</span><span>💬 Chat ao vivo</span><span>🌱 Comunidade</span></div></div>
    <div class="float f1"><b>♥ 128</b><small>curtidas hoje</small></div><div class="float f2"><b>💬 Nova mensagem</b><small>“Vamos nos ver hoje?”</small></div></section>
    <form class="panel" id="f"><div class="seg"><i></i><button type="button" data-m="0">Entrar</button><button type="button" data-m="1">Criar conta</button></div>
    <h2 id="ti"></h2><p class="sub" id="st"></p><div id="fd"></div><button class="btn big" id="go"><span></span></button></form></div>`;
  const fields = () => {
    $('.seg').classList.toggle('reg', reg);
    document.querySelectorAll('.seg button').forEach(b => b.classList.toggle('on', (b.dataset.m == 1) == reg));
    $('#ti').textContent = reg ? 'Crie sua conta' : 'Bem-vindo de volta';
    $('#st').textContent = reg ? 'Leva menos de um minuto.' : 'Entre para continuar.';
    $('#go span').textContent = reg ? 'Criar conta' : 'Entrar';
    $('#fd').innerHTML = (reg ? fl('username', 'Nome de usuário', 'minlength=3 maxlength=30') + fl('email', 'E-mail', 'type=email') : fl('identificador', 'Usuário ou e-mail'))
      + fl('senha', reg ? 'Senha (mín. 8 caracteres)' : 'Senha', `type=password minlength=${reg ? 8 : 1}`, true);
  };
  document.querySelectorAll('.seg button').forEach(b => b.onclick = () => { reg = b.dataset.m == 1; fields(); });
  $('#fd').onclick = e => { if (e.target.matches('.eye')) { const i = e.target.parentNode.querySelector('input'); i.type = i.type == 'password' ? 'text' : 'password'; } };
  $('.login').onmousemove = e => { const l = e.currentTarget.style; l.setProperty('--mx', e.clientX / innerWidth - .5); l.setProperty('--my', e.clientY / innerHeight - .5); };
  $('#f').onsubmit = async e => {
    e.preventDefault();
    const f = e.target, btn = $('#go'); btn.classList.add('load');
    const ok = await tryo(async () => {
      const d = Object.fromEntries(new FormData(f));
      if (reg) { await api('POST', '/api/usuarios', d); toast('Conta criada! Entrando…'); d.identificador = d.username; }
      me = await api('POST', '/api/usuarios/login', { identificador: d.identificador, senha: d.senha });
      localStorage.me = JSON.stringify(me); return true;
    })();
    btn.classList.remove('load');
    if (ok) { entrar(); go('#/feed'); } else { f.classList.remove('shake'); void f.offsetWidth; f.classList.add('shake'); }
  };
  fields();
}

// ---------- Feed ----------
const S = p => `<svg viewBox="0 0 24 24">${p}</svg>`;
const ic = { heart: S('<path d="M12 20.5s-8-4.9-8-10.7A4.6 4.6 0 0 1 12 7a4.6 4.6 0 0 1 8 2.8c0 5.8-8 10.7-8 10.7z"/>'), chat: S('<path d="M21 12a8.5 8.5 0 0 1-12.2 7.6L3 21l1.5-5.4A8.5 8.5 0 1 1 21 12z"/>'), trash: S('<path d="M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13"/>') };
const ago = d => { const m = (Date.now() - new Date(d)) / 6e4; return m < 1 ? 'agora' : m < 60 ? `há ${~~m} min` : m < 1440 ? `há ${~~(m / 60)} h` : m < 43200 ? `há ${~~(m / 1440)} d` : fdt(d); };
const rg = (p, c = '') => `<span class="ringw ${c}">${av(p)}</span>`;
const navav = () => perfil(me.id).then(p => { $('#eu').innerHTML = rg(p, 'xs'); });
function modal(el, cls = '') { const m = document.createElement('div'); m.className = 'modal'; m.innerHTML = `<div class="mb ${cls}"></div>`; m.firstChild.append(el); m.onclick = e => { if (e.target == m) m.remove(); }; document.body.append(m); return m; }
async function publicar() {
  const mp = await perfil(me.id), f = document.createElement('form'), vazio = '<div class="pph">🎬<p>A prévia do seu vídeo aparece aqui</p></div>';
  f.className = 'pubw';
  f.innerHTML = `<div class="pv"><div class="ph"><span class="who">${rg(mp, 'sm')}<span><b>${esc(mp.username)}</b><small>agora</small></span></span></div>
    <div class="media" id="pm">${vazio}</div><div class="pc"><p><b id="pt">Seu título</b></p><p id="pd">Sua legenda…</p></div></div>
    <div class="pfm"><h3>Nova publicação</h3>
    <label>Título <small id="c1">0/150</small></label><input name="titulo" maxlength=150 placeholder="Dê um nome ao vídeo" required>
    <label>Link do vídeo</label><input name="url" type="url" placeholder="https://youtube.com/watch?v=… ou arquivo .mp4" required>
    <small class="hint">Aceitamos links do YouTube e arquivos .mp4, .webm ou .ogg.</small>
    <label>Legenda <small id="c2">0/5000</small></label><textarea name="descricao" maxlength=5000 placeholder="Escreva uma legenda…"></textarea>
    <div class="row end"><button type="button" class="ghost" id="cx">Cancelar</button><button class="btn">Compartilhar</button></div></div>`;
  const m = modal(f, 'wide'), t = f.titulo, u = f.url, d = f.descricao;
  t.oninput = () => { $('#c1').textContent = t.value.length + '/150'; $('#pt').textContent = t.value || 'Seu título'; };
  d.oninput = () => { $('#c2').textContent = d.value.length + '/5000'; $('#pd').textContent = d.value || 'Sua legenda…'; };
  let tm; u.oninput = () => { clearTimeout(tm); tm = setTimeout(() => { $('#pm').innerHTML = /^https?:[/][/][^ ]+$/.test(u.value) ? player(u.value) : vazio; }, 400); };
  $('#cx').onclick = () => m.remove();
  f.onsubmit = tryo(async e => { e.preventDefault(); await api('POST', '/videos', { ...Object.fromEntries(new FormData(f)), usuarioId: me.id }); m.remove(); toast('Publicado!'); route(); });
}
async function viewFeed() {
  $('#app').innerHTML = `<div class="feed"><div class="stories" id="st"></div><div id="vids"><div class="card sk"></div><div class="card sk"></div></div></div>`;
  stories(); loadVideos();
}
async function stories() {
  const l = (await tryo(api)('GET', `/api/usuarios/${me.id}/seguidores/seguindo`)) || [], mp = await perfil(me.id), box = $('#st');
  if (!box) return;
  box.innerHTML = [`<a class="story" href="#/perfil/${me.id}">${rg(mp)}<small>Você</small></a>`, ...l.map(u => `<a class="story" href="#/perfil/${u.id}">${rg(u)}<small>${esc(u.username)}</small></a>`)].join('') + (l.length ? '' : '<p class="sub">Siga pessoas para vê-las aqui.</p>');
}
async function loadVideos() {
  const v = (await tryo(api)('GET', '/videos')) || [], box = $('#vids');
  if (!box) return;
  box.innerHTML = v.length ? '' : '<p class="sub empty">Nenhuma publicação ainda. Toque no + para compartilhar a primeira!</p>';
  for (const [i, x] of v.entries()) { const c = await card(x); c.style.setProperty('--i', Math.min(i, 6)); box.append(c); }
}
async function card(x) {
  const p = await perfil(x.usuarioId), liked = likes().includes(x.id), el = document.createElement('article');
  el.className = 'card post';
  el.innerHTML = `<div class="ph"><a href="#/perfil/${x.usuarioId}" class="who">${rg(p, 'sm')}<span><b>${esc(p.username)}</b><small>${esc(p.nomeExibicao)}</small></span></a><small>${ago(x.dataPublicacao)}</small></div>
    <div class="media">${player(x.url)}</div>
    <div class="pa"><button class="ib lk${liked ? ' on' : ''}" title="Curtir">${ic.heart}</button><button class="ib cm" title="Comentários">${ic.chat}</button>${x.usuarioId == me.id ? `<button class="ib rm" title="Excluir">${ic.trash}</button>` : ''}</div>
    <div class="pc"><p><b>${esc(x.titulo)}</b></p><p>${esc(x.descricao || '')}</p></div><div class="coms" hidden></div>`;
  el.querySelector('.lk').onclick = tryo(async e => {
    const b = e.currentTarget, on = !b.classList.contains('on');
    await api(on ? 'POST' : 'DELETE', `/videos/${x.id}/curtidas/${me.id}`);
    b.classList.toggle('on', on); if (on) { b.classList.add('pop'); setTimeout(() => b.classList.remove('pop'), 500); }
    const s = new Set(likes()); on ? s.add(x.id) : s.delete(x.id); localStorage['l' + me.id] = JSON.stringify([...s]);
  });
  el.querySelector('.cm').onclick = () => { const c = el.querySelector('.coms'); c.hidden = !c.hidden; if (!c.hidden) comentarios(c, x.id); };
  const rm = el.querySelector('.rm');
  if (rm) rm.onclick = tryo(async () => { if (confirm('Excluir esta publicação?')) { await api('DELETE', '/videos/' + x.id); el.remove(); } });
  return el;
}
async function comentarios(c, vid) {
  const l = (await tryo(api)('GET', '/comentarios/video/' + vid)) || [], ps = await Promise.all(l.map(k => perfil(k.usuarioId)));
  c.innerHTML = l.map((k, i) => `<div class="com"><b>${esc(ps[i].username)}</b>${esc(k.texto)} <small>${ago(k.dataComentario)}</small>${k.usuarioId == me.id ? ` <a href="#" data-d="${k.id}">excluir</a>` : ''}</div>`).join('')
    + `<form class="cf"><input placeholder="Adicione um comentário…" maxlength=5000 required><button>Publicar</button></form>`;
  c.querySelectorAll('[data-d]').forEach(a => a.onclick = tryo(async e => { e.preventDefault(); await api('DELETE', '/comentarios/' + a.dataset.d); comentarios(c, vid); }));
  c.querySelector('form').onsubmit = tryo(async e => { e.preventDefault(); await api('POST', '/comentarios', { texto: e.target[0].value, videoId: vid, usuarioId: me.id }); comentarios(c, vid); });
}

// ---------- Perfil ----------
const thumb = u => { const y = u.match(/(?:youtu\.be\/|v=|embed\/|shorts\/)([\w-]{11})/); return y ? `background-image:url(https://img.youtube.com/vi/${y[1]}/hqdefault.jpg)` : ''; };
async function viewPerfil(id) {
  const own = id == me.id;
  const [p, sg, sn, vs] = await Promise.all([perfil(id), api('GET', `/api/usuarios/${id}/seguidores/contagem`), api('GET', `/api/usuarios/${id}/seguidores/contagem/seguindo`), api('GET', '/videos')]);
  const mine = vs.filter(v => v.usuarioId == id), seg = own ? false : (await api('GET', `/api/usuarios/${me.id}/seguidores/seguindo`)).some(u => u.id == id);
  $('#app').innerHTML = `<section class="pf">${rg(p, 'xl')}<div class="pfi"><div class="pft"><h2>${esc(p.username)}</h2>
    ${own ? '<button class="ghost" id="ed">Editar perfil</button><button class="ghost" data-sair>Sair</button>' : `<button class="btn" id="sg">${seg ? 'Seguindo' : 'Seguir'}</button><button class="ghost" id="msg">Mensagem</button>`}</div>
    <div class="stats"><span><b>${mine.length}</b> publicações</span><button data-l=""><b>${sg}</b> seguidores</button><button data-l="/seguindo"><b>${sn}</b> seguindo</button></div>
    <b>${esc(p.nomeExibicao)}</b><p>${esc(p.bio || '')}</p></div></section>
    <div class="tabs">PUBLICAÇÕES</div><div class="grid">${mine.map((v, i) => `<button class="tile" data-v="${v.id}" style="${thumb(v.url)};animation-delay:${Math.min(i, 8) * 50}ms"><span>▶</span><em>${esc(v.titulo)}</em></button>`).join('') || '<p class="sub empty">Nenhuma publicação ainda.</p>'}</div>`;
  document.querySelectorAll('.tile').forEach(t => t.onclick = tryo(async () => { const c = await card(mine.find(v => v.id == t.dataset.v)); c.classList.add('in-modal'); modal(c); }));
  document.querySelectorAll('[data-l]').forEach(b => b.onclick = tryo(async () => {
    const l = await api('GET', `/api/usuarios/${id}/seguidores${b.dataset.l}`), d = document.createElement('div'); d.className = 'ulist';
    d.innerHTML = `<h3>${b.dataset.l ? 'Seguindo' : 'Seguidores'}</h3>` + (l.map(u => `<a class="who" href="#/perfil/${u.id}">${rg(u, 'sm')}<span><b>${esc(u.username)}</b><small>${esc(u.nomeExibicao || '')}</small></span></a>`).join('') || '<p class="sub">Ninguém ainda.</p>');
    modal(d);
  }));
  if (own) $('#ed').onclick = () => {
    const f = document.createElement('form'); f.className = 'pubf';
    f.innerHTML = `<h3>Editar perfil</h3><input name="nomeExibicao" value="${esc(p.nomeExibicao)}" maxlength=50 placeholder="Nome de exibição"><textarea name="bio" maxlength=500 placeholder="Bio">${esc(p.bio || '')}</textarea><input name="fotoPerfil" value="${esc(p.fotoPerfil || '')}" maxlength=2048 placeholder="URL da foto de perfil"><button class="btn big">Salvar</button>`;
    const m = modal(f);
    f.onsubmit = tryo(async e => { e.preventDefault(); await api('PUT', `/api/usuarios/${id}/perfil`, Object.fromEntries(new FormData(f))); delete pc[id]; m.remove(); toast('Perfil salvo!'); navav(); viewPerfil(id); });
  };
  else {
    $('#sg').onclick = tryo(async () => { await api(seg ? 'DELETE' : 'POST', `/api/usuarios/${me.id}/seguidores/${id}`); viewPerfil(id); });
    $('#msg').onclick = tryo(async () => { const c = await api('POST', '/api/chat/conversas', { tipo: 'INDIVIDUAL', participantesIds: [id] }); location.hash = '#/chat/' + c.id; });
  }
}

// ---------- Chat ----------
async function titulo(c) {
  if (c.titulo) return { n: c.titulo, p: { nomeExibicao: c.titulo } };
  const o = c.participantes.find(x => x.usuarioId != me.id), p = await perfil(o ? o.usuarioId : me.id);
  return { n: p.nomeExibicao, p };
}
const nm = {}; let convData = null, editando = null, infoOn = innerWidth > 1150;
const hora = d => new Date(d).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
const dia = d => { const x = new Date(d), n = Math.round((new Date(new Date().toDateString()) - new Date(x.toDateString())) / 864e5); return n == 0 ? 'Hoje' : n == 1 ? 'Ontem' : x.toLocaleDateString('pt-BR', { day: '2-digit', month: 'long' }); };
async function novaConversa() {
  const sg = (await api('GET', `/api/usuarios/${me.id}/seguidores/seguindo`)) || [], d = document.createElement('div'); d.className = 'ulist nova';
  d.innerHTML = `<h3>Nova mensagem</h3><div class="sel">${sg.map(u => `<label class="who opt"><input type="checkbox" value="${u.id}">${rg(u, 'sm')}<span><b>${esc(u.username)}</b><small>${esc(u.nomeExibicao || '')}</small></span></label>`).join('') || '<p class="sub">Você ainda não segue ninguém. Use o ID abaixo.</p>'}</div>
    <input id="oid" placeholder="Outros IDs de usuário (ex.: 4, 9)"><input id="gt" placeholder="Nome do grupo" hidden><button class="btn big" id="ir">Conversar</button>`;
  const m = modal(d), ids = () => [...new Set([...d.querySelectorAll('input[type=checkbox]:checked')].map(i => +i.value).concat($('#oid').value.split(',').map(Number).filter(n => n > 0)))];
  d.oninput = () => { $('#gt').hidden = ids().length < 2; };
  $('#ir').onclick = tryo(async () => {
    const l = ids(); if (!l.length) return toast('Escolha alguém para conversar');
    const b = { tipo: l.length > 1 ? 'GRUPO' : 'INDIVIDUAL', participantesIds: l }; if (l.length > 1) b.titulo = $('#gt').value || 'Grupo';
    const c = await api('POST', '/api/chat/conversas', b); m.remove(); location.hash = '#/chat/' + c.id;
  });
}
async function viewChat(cid) {
  $('#app').innerHTML = `<div class="chat"><aside class="card"><div class="ah"><b>Mensagens</b><button class="ib" id="nv" title="Nova mensagem">✎</button></div><input id="fq" class="fq" placeholder="Buscar conversa…"><div id="cl"></div></aside>
    <section class="card" id="th"><div class="empty"><div class="bigic">${ic.chat}</div><h3>Suas mensagens</h3><p class="sub">Escolha uma conversa ou comece uma nova.</p><button class="btn" id="nv2">Enviar mensagem</button></div></section><aside class="card infop" id="inf" hidden></aside></div>`;
  $('#nv').onclick = $('#nv2').onclick = tryo(novaConversa);
  $('#fq').oninput = e => { const q = e.target.value.toLowerCase(); document.querySelectorAll('.conv').forEach(a => a.hidden = !a.textContent.toLowerCase().includes(q)); };
  if (cid) abrir(cid); else listarConversas();
}
async function listarConversas() {
  const pg = await tryo(api)('GET', '/api/chat/conversas?size=50'), l = pg?.content || [], ts = await Promise.all(l.map(titulo)), box = $('#cl');
  if (!box) return;
  box.innerHTML = l.map((c, i) => { const u = c.ultimaMensagem; return `<a class="conv${c.id == conv ? ' on' : ''}" href="#/chat/${c.id}">${rg(ts[i].p, 'md')}<div><b>${esc(ts[i].n)}</b><small>${c.tipo == 'GRUPO' ? '👥 ' : ''}${esc(u ? (u.excluida ? 'mensagem apagada' : u.conteudo) : 'Nova conversa')}</small></div><span class="tm">${u ? ago(u.criadaEm) : ''}${c.naoLidas ? `<i class="badge">${c.naoLidas}</i>` : ''}</span></a>`; }).join('') || '<p class="sub empty">Sem conversas ainda.</p>';
  if ($('#fq')?.value) $('#fq').oninput({ target: $('#fq') });
}
async function abrir(cid) {
  conv = cid; editando = null;
  const c = await tryo(api)('GET', '/api/chat/conversas/' + cid);
  if (!c) return listarConversas();
  convData = c;
  const [t, pg] = await Promise.all([titulo(c), api('GET', `/api/chat/conversas/${cid}/mensagens?size=100`), ...c.participantes.map(async x => { nm[x.usuarioId] = (await perfil(x.usuarioId)).nomeExibicao; })]);
  msgs = pg.content.reverse();
  $('#th').innerHTML = `<div class="th">${rg(t.p, 'sm')}<div class="tt"><b>${esc(t.n)}</b><small>${c.tipo == 'GRUPO' ? c.participantes.length + ' participantes' : 'Conversa individual'}</small></div><button class="ib" id="ib" title="Detalhes">ⓘ</button></div>
    <div id="ms"></div><div id="eb" hidden></div><div id="em" hidden>${['😀', '😂', '😍', '👍', '🔥', '🎉', '😮', '😢', '🙏', '💜'].map(e => `<button type="button">${e}</button>`).join('')}</div>
    <form id="sf"><button type="button" class="ib" id="eg" title="Emojis">😊</button><input placeholder="Mensagem…" maxlength=4000 autocomplete="off" required><button class="btn">Enviar</button></form>`;
  const mostrar = () => { $('.chat').classList.toggle('info', infoOn); $('#inf').hidden = !infoOn; };
  pintar(); info(); mostrar();
  $('#ib').onclick = () => { infoOn = !infoOn; mostrar(); };
  $('#eg').onclick = () => $('#em').hidden = !$('#em').hidden;
  $('#em').onclick = e => { if (e.target.tagName == 'BUTTON') { const i = $('#sf input'); i.value += e.target.textContent; i.focus(); } };
  $('#sf').onsubmit = tryo(async e => {
    e.preventDefault(); const i = e.target[1], v = i.value.trim(); if (!v) return; i.value = ''; $('#em').hidden = true;
    if (editando) { const id = editando; sairEdicao(); merge(await api('PUT', '/api/chat/mensagens/' + id, { conteudo: v })); }
    else merge(await api('POST', `/api/chat/conversas/${cid}/mensagens`, { conteudo: v }));
  });
  api('POST', `/api/chat/conversas/${cid}/leitura`).catch(() => { }).then(listarConversas);
}
function editar(id) { const x = msgs.find(m => m.id == id); editando = id; $('#eb').hidden = false; $('#eb').innerHTML = 'Editando mensagem <button type="button" class="ib" id="xe">✕</button>'; $('#xe').onclick = sairEdicao; const i = $('#sf input'); i.value = x.conteudo; i.focus(); }
function sairEdicao() { editando = null; $('#eb').hidden = true; $('#sf input').value = ''; }
function merge(m) { const i = msgs.findIndex(x => x.id == m.id); i < 0 ? msgs.push(m) : msgs[i] = m; pintar(); }
function pintar() {
  const m = $('#ms'); if (!m) return; let ult = null, dl = null;
  m.innerHTML = msgs.map(x => {
    const d = dia(x.criadaEm), eu = x.remetenteId == me.id, sep = d != dl ? `<div class="dsep"><span>${d}</span></div>` : '', novo = sep || ult != x.remetenteId;
    dl = d; ult = x.remetenteId;
    const nome = !eu && novo && convData?.tipo == 'GRUPO' ? `<small class="nm">${esc(nm[x.remetenteId] || 'Usuário ' + x.remetenteId)}</small>` : '';
    return sep + `<div class="mr${eu ? ' me' : ''}${novo ? ' first' : ''}">${nome}<div class="msg${eu ? ' me' : ''}">${x.excluida ? '<i>mensagem apagada</i>' : esc(x.conteudo)}<small>${hora(x.criadaEm)}${x.editadaEm ? ' · editada' : ''}</small></div>${eu && !x.excluida ? `<span class="acts"><button class="ib" data-e="${x.id}" title="Editar">✎</button><button class="ib" data-x="${x.id}" title="Apagar">${ic.trash}</button></span>` : ''}</div>`;
  }).join('');
  m.querySelectorAll('[data-x]').forEach(b => b.onclick = tryo(async () => { if (!confirm('Apagar esta mensagem?')) return; const x = msgs.find(k => k.id == b.dataset.x); await api('DELETE', '/api/chat/mensagens/' + x.id); merge({ ...x, excluida: true, conteudo: null }); }));
  m.querySelectorAll('[data-e]').forEach(b => b.onclick = () => editar(b.dataset.e));
  m.scrollTop = m.scrollHeight;
}
async function info() {
  const b = $('#inf'), c = convData; if (!b) return;
  if (c.tipo != 'GRUPO') {
    const o = c.participantes.find(x => x.usuarioId != me.id), p = await perfil(o ? o.usuarioId : me.id);
    b.innerHTML = `<div class="ic0">${rg(p, 'xl2')}<h3>${esc(p.nomeExibicao)}</h3><small>@${esc(p.username)}</small><p>${esc(p.bio || '')}</p><a class="btn" href="#/perfil/${p.usuarioId}">Ver perfil</a></div>`; return;
  }
  const ps = await api('GET', `/api/chat/conversas/${c.id}/participantes`), pf = await Promise.all(ps.map(x => perfil(x.usuarioId))), adm = ps.some(x => x.usuarioId == me.id && x.papel == 'ADMIN');
  b.innerHTML = `<div class="ic0"><h3>${esc(c.titulo || 'Grupo')}</h3><small>${ps.length} participantes</small></div><div class="pl">${ps.map((x, i) => `<div class="who">${rg(pf[i], 'sm')}<span><b>${esc(pf[i].username)}</b><small>${x.papel == 'ADMIN' ? 'Admin' : 'Membro'}</small></span>${adm && x.usuarioId != me.id ? `<button class="ib" data-r="${x.usuarioId}" title="Remover">✕</button>` : ''}</div>`).join('')}</div>
    ${adm ? '<div class="row"><input id="ap" placeholder="ID para adicionar"><button class="btn" id="ab">Adicionar</button></div>' : ''}<button class="ghost" id="sg2">Sair do grupo</button>`;
  b.querySelectorAll('[data-r]').forEach(x => x.onclick = tryo(async () => { await api('DELETE', `/api/chat/conversas/${c.id}/participantes/${x.dataset.r}`); abrir(c.id); }));
  if (adm) $('#ab').onclick = tryo(async () => { const v = +$('#ap').value; if (v > 0) { await api('POST', `/api/chat/conversas/${c.id}/participantes/${v}`); abrir(c.id); } });
  $('#sg2').onclick = tryo(async () => { if (confirm('Sair deste grupo?')) { await api('DELETE', `/api/chat/conversas/${c.id}/participantes/${me.id}`); location.hash = '#/chat'; } });
}
function conectar() {
  if (!me) return;
  ws = new WebSocket(`${WS_URL}?usuarioId=${me.id}`);
  ws.onmessage = e => {
    const ev = JSON.parse(e.data);
    if (ev.conversaId == conv) { merge(ev.mensagem); if (ev.tipo == 'MENSAGEM_NOVA' && ev.mensagem.remetenteId != me.id) api('POST', `/api/chat/conversas/${conv}/leitura`).catch(() => { }); }
    else if (ev.tipo == 'MENSAGEM_NOVA' && ev.mensagem.remetenteId != me.id) toast('💬 Nova mensagem');
    listarConversas();
  };
  ws.onclose = () => { if (me) setTimeout(conectar, 3000); };
}

// ---------- Rotas ----------
function entrar() { $('#nav').hidden = false; $('#eu').href = '#/perfil/' + me.id; ws?.close(); conectar(); navav(); }
function route() {
  const [, pg, id] = location.hash.split('/'); conv = null;
  document.querySelectorAll('.modal').forEach(m => m.remove());
  document.body.dataset.pg = pg || 'feed';
  document.body.classList.toggle('auth-page', !me);
  document.querySelectorAll('nav a').forEach(a => a.classList.toggle('on', a.getAttribute('href').split('/')[1] == (pg || 'feed')));
  if (!me) { $('#nav').hidden = true; return viewLogin(); }
  const v = { perfil: () => viewPerfil(+id || me.id), chat: () => viewChat(+id || null) }[pg] || viewFeed;
  Promise.resolve(v()).catch(e => toast(e.message));
}
document.addEventListener('click', e => { if (e.target.closest('[data-sair]')) { const w = ws; me = null; localStorage.removeItem('me'); w?.close(); go('#/login'); } });
$('#novo').onclick = tryo(publicar);
$('#bs').onsubmit = e => { e.preventDefault(); const v = +e.target[0].value; if (v > 0) { e.target[0].value = ''; go('#/perfil/' + v); } };
document.addEventListener('keydown', e => { if (e.key == 'Escape') document.querySelector('.modal')?.remove(); });
// ---------- Efeitos ----------
document.addEventListener('click', e => {            // onda ao clicar nos botões
  const b = e.target.closest('.btn,.ghost'); if (!b) return;
  const r = b.getBoundingClientRect(), o = document.createElement('span');
  o.className = 'rip'; o.style.cssText = `left:${e.clientX - r.left}px;top:${e.clientY - r.top}px`;
  b.append(o); setTimeout(() => o.remove(), 600);
});
document.addEventListener('pointermove', e => {      // brilho que segue o mouse nos cards
  const c = e.target.closest('article.card'); if (!c) return;
  const r = c.getBoundingClientRect(); c.style.setProperty('--x', e.clientX - r.left + 'px'); c.style.setProperty('--y', e.clientY - r.top + 'px');
});
addEventListener('scroll', () => $('#nav').classList.toggle('sc', scrollY > 8), { passive: true });
addEventListener('hashchange', route);
if (me) entrar();
route();
