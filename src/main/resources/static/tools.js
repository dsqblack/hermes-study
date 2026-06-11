(function() {
'use strict';

/* ============================================================
   UTILITIES
   ============================================================ */
function $(id){return document.getElementById(id)}

function toast(msg, type){
  type=type||'info';var c=$('toastContainer');var t=document.createElement('div');
  t.className='toast '+type;
  var icon={info:'&#x2139;',success:'&#x2705;',error:'&#x274C;'}[type]||'&#x2139;';
  t.innerHTML='<span class="t-icon">'+icon+'</span>'+msg;
  c.appendChild(t);
  setTimeout(function(){t.style.opacity='0';t.style.transition='opacity 300ms';setTimeout(function(){t.remove()},300)},2500);
}

function copyText(id){
  var el=$(id);if(!el)return;
  if(el.tagName==='TEXTAREA'||el.tagName==='INPUT'){el.select();navigator.clipboard.writeText(el.value).then(function(){toast('已复制','success')})}
  else{navigator.clipboard.writeText(el.textContent).then(function(){toast('已复制','success')})}
}
function copyTextVal(id){return copyText(id)}

/* ============================================================
   DARK MODE
   ============================================================ */
  var theme=localStorage.getItem('tools-theme')||'light';
  function applyTheme(t){
    if(t==='dark'){
      document.documentElement.setAttribute('data-theme','dark');
      var sun=document.getElementById('iconSun');if(sun)sun.style.display='none';
      var moon=document.getElementById('iconMoon');if(moon)moon.style.display='block';
    }else{
      document.documentElement.removeAttribute('data-theme');
      var sun=document.getElementById('iconSun');if(sun)sun.style.display='block';
      var moon=document.getElementById('iconMoon');if(moon)moon.style.display='none';
    }
  }
  applyTheme(theme);
  $('themeToggle').addEventListener('click',function(){
    var isDark=document.documentElement.getAttribute('data-theme')==='dark';
    theme=isDark?'light':'dark';
    localStorage.setItem('tools-theme',theme);
    applyTheme(theme);
  });

/* ============================================================
   MOBILE MENU
   ============================================================ */
$('menuToggle').addEventListener('click',function(){
  $('sidebar').classList.toggle('show');
  $('sidebarOverlay').classList.toggle('show');
});
$('sidebarOverlay').addEventListener('click',function(){
  $('sidebar').classList.remove('show');
  $('sidebarOverlay').classList.remove('show');
});

/* ============================================================
   PAGE TITLE
   ============================================================ */
var toolNames={
  'home':'开发者工具箱',
  'tool-1':'JSON/XML/YAML 格式化校验互转',
  'tool-2':'正则表达式测试器',
  'tool-3':'SQL 格式化',
  'tool-4':'Cron 表达式生成器',
  'tool-5':'时间戳 ↔ 日期互转',
  'tool-6':'Base64/URL/HTML 编解码',
  'tool-7':'文本对比 (Diff)',
  'tool-8':'进制转换',
  'tool-9':'哈希计算',
  'tool-10':'字符串工具',
  'tool-11':'IP / DNS 查询',
  'tool-12':'HTTP 请求调试',
  'tool-13':'UUID / 密码生成器',
  'tool-14':'番茄钟 (Pomodoro)',
  'tool-15':'Markdown 实时预览',
  'tool-16':'打字速度测试',
  'tool-17':'彩虹屁 / 毒鸡汤生成器'
};

/* ============================================================
   ROUTER
   ============================================================ */
function navigate(viewId){
  // hide all
  var views=document.querySelectorAll('.tool-view');
  for(var i=0;i<views.length;i++)views[i].classList.remove('active');
  // show target
  var target=$(viewId);
  if(target){target.classList.add('active')}
  // update title
  var name=toolNames[viewId]||'开发者工具箱';
  $('pageTitle').textContent=name;
  document.title=name+' - 开发者工具箱';
}
function goHome(){navigate('view-home');switchCategory('all')}
window.goHome=goHome;

/* ============================================================
   TOOL DATA: 17 tools
   ============================================================ */
var toolsData=[
  {id:'tool-1',cat:'dev',num:1,name:'JSON/XML/YAML 格式化校验互转',icon:'&#x1F4CB;',desc:'粘贴文本自动检测格式，格式化、校验、任意格式互转'},
  {id:'tool-2',cat:'dev',num:2,name:'正则表达式测试器',icon:'&#x1F50D;',desc:'实时高亮匹配项，分组捕获，常用正则收藏'},
  {id:'tool-3',cat:'dev',num:3,name:'SQL 格式化',icon:'&#x1F4BE;',desc:'SQL 美化缩进，支持多种方言，一键压缩'},
  {id:'tool-4',cat:'dev',num:4,name:'Cron 表达式生成器',icon:'&#x23F0;',desc:'可视化选择，反向解析，常用预设'},
  {id:'tool-5',cat:'dev',num:5,name:'时间戳 ↔ 日期互转',icon:'&#x1F4C5;',desc:'Unix秒/毫秒/纳秒与多种格式日期互转'},
  {id:'tool-6',cat:'encode',num:6,name:'Base64/URL/HTML 编解码',icon:'&#x1F512;',desc:'Base64、URL、HTML、Unicode 编码解码'},
  {id:'tool-7',cat:'encode',num:7,name:'文本对比 (Diff)',icon:'&#x2194;',desc:'双栏输入，逐行/逐字符差异高亮'},
  {id:'tool-8',cat:'encode',num:8,name:'进制转换',icon:'&#x1F522;',desc:'2/8/10/16进制实时联动转换'},
  {id:'tool-9',cat:'encode',num:9,name:'哈希计算',icon:'&#x1F4A7;',desc:'MD5/SHA1/SHA256/SHA512/SHA3-256'},
  {id:'tool-10',cat:'encode',num:10,name:'字符串工具',icon:'&#x1F4DD;',desc:'大小写/驼峰/反转/去重/排序，实时统计'},
  {id:'tool-11',cat:'network',num:11,name:'IP / DNS 查询',icon:'&#x1F310;',desc:'本机公网 IP，DNS 记录查询'},
  {id:'tool-12',cat:'network',num:12,name:'HTTP 请求调试',icon:'&#x1F4E1;',desc:'简版 Postman，支持各种 Method 和 Header'},
  {id:'tool-13',cat:'network',num:13,name:'UUID / 密码生成器',icon:'&#x1F511;',desc:'UUID 批量生成，可配置强密码生成'},
  {id:'tool-14',cat:'utility',num:14,name:'番茄钟 (Pomodoro)',icon:'&#x23F1;',desc:'专注计时，可自定义时长，浏览器通知'},
  {id:'tool-15',cat:'utility',num:15,name:'Markdown 实时预览',icon:'&#x1F4D0;',desc:'左侧编辑、右侧渲染、支持导出 HTML'},
  {id:'tool-16',cat:'fun',num:16,name:'打字速度测试',icon:'&#x2328;',desc:'中英文打字，实时 WPM 和准确率'},
  {id:'tool-17',cat:'fun',num:17,name:'彩虹屁 / 毒鸡汤生成器',icon:'&#x2728;',desc:'一键生成彩虹屁或毒鸡汤，内置 100+ 语料'}
];

function switchCategory(cat){
  // sidebar active
  var items=document.querySelectorAll('.sidebar-item');
  for(var i=0;i<items.length;i++)items[i].classList.remove('active');
  var activeItem=document.querySelector('.sidebar-item[data-cat="'+cat+'"]');
  if(activeItem)activeItem.classList.add('active');
  // close mobile sidebar
  $('sidebar').classList.remove('show');
  $('sidebarOverlay').classList.remove('show');
  // filter cards
  renderCards(cat);
  navigate('view-home');
}
window.switchCategory=switchCategory;

function renderCards(cat){
  var grid=$('cardGrid');grid.innerHTML='';
  var filtered=cat==='all'?toolsData:toolsData.filter(function(t){return t.cat===cat});
  for(var i=0;i<filtered.length;i++){
    var t=filtered[i];
    var card=document.createElement('div');
    card.className='tool-card cat-'+t.cat;
    card.onclick=function(id){return function(){openTool(id)}}(t.id);
    card.innerHTML=
      '<div class="tool-card-head">工具 '+(t.num<10?'0':'')+t.num+'</div>'+
      '<div class="tool-card-body">'+
        '<div class="tool-card-icon">'+t.icon+'</div>'+
        '<div class="tool-card-info">'+
          '<h3>'+t.name+'</h3>'+
          '<p>'+t.desc+'</p>'+
        '</div>'+
      '</div>';
    grid.appendChild(card);
  }
}

function openTool(id){
  navigate(id);
  // setup tool if first time
  if(id==='tool-15'&&typeof marked!=='undefined')md15_preview();
}
window.openTool=openTool;

// Init cards
renderCards('all');

/* ============================================================
   TOOL 1: JSON/XML/YAML
   ============================================================ */
window.fmt1_format=function(){
  var input=$('fmt1_input').value.trim();
  if(!input){toast('请先输入内容','error');return}
  var type=$('fmt1_inputType').value;
  if(type==='auto')type=detectType(input);
  $('fmt1_detected').textContent='('+type.toUpperCase()+')';
  try{
    var formatted;
    if(type==='json'){formatted=JSON.stringify(JSON.parse(input),null,2)}
    else if(type==='yaml'){formatted=jsyaml.dump(jsyaml.load(input),{indent:2})}
    else if(type==='xml'){formatted=formatXml(input)}
    else{toast('无法识别格式','error');return}
    $('fmt1_output').value=formatted;
    toast('格式化成功','success');
  }catch(e){toast('格式错误: '+e.message,'error')}
};
window.fmt1_validate=function(){
  var input=$('fmt1_input').value.trim();
  if(!input){toast('请先输入内容','error');return}
  var type=$('fmt1_inputType').value;
  if(type==='auto')type=detectType(input);
  try{
    if(type==='json'){JSON.parse(input);toast('✅ JSON 语法正确','success')}
    else if(type==='yaml'){jsyaml.load(input);toast('✅ YAML 语法正确','success')}
    else if(type==='xml'){var p=new DOMParser();var d=p.parseFromString(input,'text/xml');if(d.querySelector('parsererror'))throw new Error(d.querySelector('parsererror').textContent);toast('✅ XML 语法正确','success')}
    else toast('无法识别格式','error');
  }catch(e){toast('❌ 格式错误: '+e.message,'error')}
};
window.fmt1_convert=function(){
  var input=$('fmt1_input').value.trim();
  if(!input){toast('请先输入内容','error');return}
  var fromType=$('fmt1_inputType').value;
  if(fromType==='auto')fromType=detectType(input);
  var toType=$('fmt1_targetType').value;
  if(fromType===toType){toast('源格式和目标格式相同','error');return}
  try{
    var obj;
    if(fromType==='json')obj=JSON.parse(input);
    else if(fromType==='yaml')obj=jsyaml.load(input);
    else if(fromType==='xml'){var p=new DOMParser();var d=p.parseFromString(input,'text/xml');if(d.querySelector('parsererror'))throw new Error('XML 解析失败');obj=xmlToObj(d.documentElement)}
    else{toast('无法解析源格式','error');return}
    var output;
    if(toType==='json')output=JSON.stringify(obj,null,2);
    else if(toType==='yaml')output=jsyaml.dump(obj,{indent:2});
    else if(toType==='xml')output=objToXml(obj);
    else{toast('不支持的目标格式','error');return}
    $('fmt1_output').value=output;
    toast('转换成功','success');
  }catch(e){toast('转换失败: '+e.message,'error')}
};
function detectType(s){
  s=s.trim();
  if(s.startsWith('{')||s.startsWith('['))return'json';
  if(s.startsWith('<'))return'xml';
  return'yaml';
}
function formatXml(xml){
  var p=new DOMParser();var d=p.parseFromString(xml,'text/xml');
  if(d.querySelector('parsererror'))throw new Error(d.querySelector('parsererror').textContent);
  return xmlToString(d.documentElement,0);
}
function xmlToString(node,indent){
  var pad='  '.repeat(indent);
  var s=pad+'<'+node.tagName;
  var attrs=node.attributes;
  for(var i=0;i<attrs.length;i++)s+=' '+attrs[i].name+'="'+escapeXml(attrs[i].value)+'"';
  if(node.childNodes.length===0){s+='/>';return s}
  s+='>';
  var hasText=false;
  for(var i=0;i<node.childNodes.length;i++){
    if(node.childNodes[i].nodeType===3){hasText=true;break}
  }
  if(hasText){
    for(var i=0;i<node.childNodes.length;i++){
      if(node.childNodes[i].nodeType===3)s+=escapeXml(node.childNodes[i].nodeValue);
      else s+='\n'+xmlToString(node.childNodes[i],indent+1);
    }
    s+='\n'+pad+'</'+node.tagName+'>';
  }else{
    s+='\n';
    for(var i=0;i<node.childNodes.length;i++)s+=xmlToString(node.childNodes[i],indent+1)+'\n';
    s+=pad+'</'+node.tagName+'>';
  }
  return s;
}
function escapeXml(s){return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;')}
function xmlToObj(node){
  var obj={};obj['#name']=node.tagName;
  var attrs=node.attributes;
  if(attrs.length){obj['@attributes']={};for(var i=0;i<attrs.length;i++)obj['@attributes'][attrs[i].name]=attrs[i].value}
  var children=[];var text='';
  for(var i=0;i<node.childNodes.length;i++){
    var n=node.childNodes[i];
    if(n.nodeType===3){text+=n.nodeValue}
    else{children.push(xmlToObj(n))}
  }
  if(children.length)obj['#children']=children;
  if(text.trim())obj['#text']=text.trim();
  return obj;
}
function objToXml(obj,rootName){
  if(typeof obj==='string'||typeof obj==='number')return escapeXml(String(obj));
  if(Array.isArray(obj)){return obj.map(function(v){return objToXml(v)}).join('\n')}
  var name=obj['#name']||'root';
  var s='<'+name;
  if(obj['@attributes']){for(var k in obj['@attributes']){s+=' '+k+'="'+escapeXml(String(obj['@attributes'][k]))+'"'}}
  s+='>';
  if(obj['#children']){for(var i=0;i<obj['#children'].length;i++)s+='\n'+objToXml(obj['#children'][i]);s+='\n'}
  if(obj['#text'])s+=escapeXml(obj['#text']);
  s+='</'+name+'>';
  return s;
}

/* ============================================================
   TOOL 2: 正则测试器
   ============================================================ */
window.re2_test=function(){
  var pattern=$('re2_pattern').value;
  var flags=$('re2_flags').value;
  var text=$('re2_text').value;
  if(!pattern){toast('请输入正则表达式','error');return}
  try{
    var re=new RegExp(pattern,flags);
    var matches=[];var match;
    var hasGlobal=flags.indexOf('g')!==-1;
    if(hasGlobal){while((match=re.exec(text))!==null){matches.push({index:match.index,value:match[0],groups:match.slice(1)});if(match.index===re.lastIndex)re.lastIndex++}}
    else{match=re.exec(text);if(match)matches.push({index:match.index,value:match[0],groups:match.slice(1)})}
    $('re2_matchInfo').textContent=matches.length+' 个匹配';
    // highlight
    var html='';var last=0;
    if(matches.length){
      // rebuild with highlights
      var highlighted=text.replace(re,function(m){return'@@HL@@'+m+'@@HLEND@@'});
      html=highlighted.replace(/@@HL@@/g,'<mark style="background:#fde68a;padding:1px 0;border-radius:2px">').replace(/@@HLEND@@/g,'</mark>');
    }
    $('re2_results').innerHTML=matches.length?html:'无匹配';
    // show details
    if(matches.length){
      var details='\n\n--- 匹配详情 ---\n';
      for(var i=0;i<matches.length;i++){
        details+='\n#'+(i+1)+' ('+matches[i].index+'): "'+matches[i].value+'"';
        if(matches[i].groups.length){details+='\n  分组: '+matches[i].groups.map(function(g,j){return '$'+(j+1)+'="'+(g||'')+'"'}).join(', ')}
      }
      $('re2_results').innerHTML+=details.replace(/\n/g,'<br>');
    }
  }catch(e){toast('正则错误: '+e.message,'error')}
};
window.re2_clear=function(){
  $('re2_pattern').value='';$('re2_flags').value='';$('re2_text').value='';
  $('re2_results').innerHTML='输入正则并测试…';$('re2_matchInfo').textContent='';
};
window.re2_insertPreset=function(name,pat){
  $('re2_pattern').value=pat;$('re2_flags').value='g';
  toast('已填入: '+name,'success');
  re2_test();
};

/* ============================================================
   TOOL 3: SQL 格式化
   ============================================================ */
window.sql3_format=function(){
  var input=$('sql3_input').value.trim();
  if(!input){toast('请输入 SQL','error');return}
  try{
    if(typeof sqlFormatter!=='undefined'){
      var dialect=$('sql3_dialect').value;
      $('sql3_output').value=sqlFormatter.format(input,{language:dialect,indent:'  '});
    }else{
      $('sql3_output').value=simpleSqlFormat(input);
    }
    toast('格式化成功','success');
  }catch(e){toast('格式化失败: '+e.message,'error')}
};
window.sql3_minify=function(){
  var input=$('sql3_input').value.trim();
  if(!input){toast('请输入 SQL','error');return}
  $('sql3_output').value=input.replace(/\s+/g,' ').trim();
  toast('压缩成功','success');
};
function simpleSqlFormat(sql){
  var keywords=['SELECT','FROM','WHERE','AND','OR','ORDER BY','GROUP BY','HAVING','LIMIT','OFFSET','JOIN','LEFT JOIN','RIGHT JOIN','INNER JOIN','OUTER JOIN','ON','INSERT INTO','VALUES','UPDATE','SET','DELETE','CREATE TABLE','ALTER TABLE','DROP TABLE','INDEX','UNIQUE','PRIMARY KEY','FOREIGN KEY','NOT NULL','DEFAULT','CASE','WHEN','THEN','ELSE','END','AS','IN','BETWEEN','LIKE','IS NULL','IS NOT NULL','UNION','UNION ALL'];
  var upper=sql.toUpperCase();
  var result=sql;
  for(var i=0;i<keywords.length;i++){
    var re=new RegExp('\\b'+keywords[i].replace(/ /g,'\\s+')+'\\b','gi');
    result=result.replace(re,'\n'+keywords[i].toUpperCase());
  }
  return result.replace(/\n{2,}/g,'\n').trim();
}

/* ============================================================
   TOOL 4: Cron 表达式
   ============================================================ */
window.cron4_parse=function(){
  var expr=$('cron4_expr').value.trim();
  var parts=expr.split(/\s+/);
  if(parts.length<5){$('cron4_desc').textContent='格式错误：需要5个字段';return}
  var names=['分钟','小时','日','月','周'];
  var descs=[];
  for(var i=0;i<5;i++){
    if(parts[i]==='*')descs.push('每'+names[i]);
    else if(parts[i].indexOf('/')!==-1){var sp=parts[i].split('/');descs.push('每'+sp[1]+names[i])}
    else descs.push(parts[i]+names[i]);
  }
  $('cron4_desc').textContent=descs.join('，');
};
window.cron4_preset=function(expr){
  $('cron4_expr').value=expr;
  var parts=expr.split(/\s+/);
  if(parts.length>=5){
    $('cron_min').value=parts[0];$('cron_hour').value=parts[1];
    $('cron_day').value=parts[2];$('cron_month').value=parts[3];$('cron_week').value=parts[4];
  }
  cron4_parse();
};
// sync inputs
['cron_min','cron_hour','cron_day','cron_month','cron_week'].forEach(function(id){
  var el=$(id);
  el.addEventListener('input',function(){
    var ids=['cron_min','cron_hour','cron_day','cron_month','cron_week'];
    $('cron4_expr').value=ids.map(function(i){return $(i).value||'*'}).join(' ');
    cron4_parse();
  });
});

/* ============================================================
   TOOL 5: 时间戳
   ============================================================ */
window.ts5_toDate=function(){
  var val=$('ts5_input').value.trim();
  if(!val){toast('请输入时间戳','error');return}
  var ts=Number(val);
  if(isNaN(ts)){toast('请输入有效数字','error');return}
  var tz=$('ts5_timezone').value;
  if(val.length<=10)ts*=1000;
  var d=new Date(ts);
  if(isNaN(d.getTime())){toast('无效的时间戳','error');return}
  $('ts5_dateResult').textContent=d.toLocaleString('zh-CN',{timeZone:tz==='UTC'?'UTC':tz==='Asia/Shanghai'?'Asia/Shanghai':tz==='America/New_York'?'America/New_York':tz==='Europe/London'?'Europe/London':tz==='Asia/Tokyo'?'Asia/Tokyo':'Asia/Shanghai'});
};
window.ts5_toTs=function(){
  var val=$('ts5_dateInput').value.trim();
  if(!val){toast('请输入日期','error');return}
  var d=new Date(val);
  if(isNaN(d.getTime())){toast('无效的日期格式，请使用 yyyy-MM-dd HH:mm:ss','error');return}
  var ms=d.getTime();
  $('ts5_tsSec').textContent=Math.floor(ms/1000);
  $('ts5_tsMs').textContent=ms;
  $('ts5_tsNs').textContent=ms*1000000;
};
window.ts5_now=function(){
  var now=Date.now();
  $('ts5_input').value=Math.floor(now/1000);
  ts5_toDate();
  $('ts5_dateInput').value=new Date().toISOString().slice(0,19).replace('T',' ');
  ts5_toTs();
};

/* ============================================================
   TOOL 6: 编解码
   ============================================================ */
var enc6_currentType='base64';
window.enc6_switchType=function(type){
  enc6_currentType=type;
  var tabs=$('enc6_tabs').querySelectorAll('.tab');
  for(var i=0;i<tabs.length;i++)tabs[i].classList.remove('active');
  tabs[Array.prototype.indexOf.call(['base64','url','html','unicode'],type)].classList.add('active');
};
window.enc6_convert=function(){
  var input=$('enc6_input').value;
  if(!input){toast('请输入内容','error');return}
  var isEncode=!document.querySelector('#enc6_direction.on');
  try{
    var result='';
    if(enc6_currentType==='base64'){
      if(isEncode)result=btoa(unescape(encodeURIComponent(input)));
      else result=decodeURIComponent(escape(atob(input)));
    }else if(enc6_currentType==='url'){
      if(isEncode)result=encodeURIComponent(input);
      else result=decodeURIComponent(input);
    }else if(enc6_currentType==='html'){
      if(isEncode){var d=document.createElement('div');d.appendChild(document.createTextNode(input));result=d.innerHTML}
      else{var d=document.createElement('div');d.innerHTML=input;result=d.textContent}
    }else if(enc6_currentType==='unicode'){
      if(isEncode){result='';for(var i=0;i<input.length;i++)result+='\\u'+input.charCodeAt(i).toString(16).padStart(4,'0')}
      else result=JSON.parse('"'+input.replace(/"/g,'\\"')+'"');
    }
    $('enc6_output').value=result;
    toast((isEncode?'编码':'解码')+'成功','success');
  }catch(e){toast('操作失败: '+e.message,'error')}
};
// toggle direction
$('enc6_direction').addEventListener('click',function(){this.classList.toggle('on')});

/* ============================================================
   TOOL 7: Diff
   ============================================================ */
var diff7_splitView=true;
window.diff7_compare=function(){
  var a=$('diff7_a').value;var b=$('diff7_b').value;
  if(!a&&!b){toast('请先输入文本','error');return}
  // simple line diff
  var linesA=a.split('\n');var linesB=b.split('\n');
  var maxLen=Math.max(linesA.length,linesB.length);
  var html='';
  for(var i=0;i<maxLen;i++){
    var la=linesA[i]||'';var lb=linesB[i]||'';
    if(la===lb)html+=('<span class="diff-eq">'+escapeHtml(la)+'</span>\n');
    else{
      if(la)html+=('<span class="diff-remove">- '+escapeHtml(la)+'</span>\n');
      if(lb)html+=('<span class="diff-add">+ '+escapeHtml(lb)+'</span>\n');
    }
  }
  $('diff7_result').innerHTML=html;
};
window.diff7_toggleView=function(){
  diff7_splitView=!diff7_splitView;
  toast('视图切换','info');
  diff7_compare();
};
window.diff7_clear=function(){
  $('diff7_a').value='';$('diff7_b').value='';
  $('diff7_result').innerHTML='输入两边文本后点击"对比"';
};
function escapeHtml(s){var d=document.createElement('div');d.textContent=s;return d.innerHTML}

/* Simple MD5 implementation for Tool 9 */
function md5(str){
  function md5cycle(x,k){var a=x[0],b=x[1],c=x[2],d=x[3];a=ff(a,b,c,d,k[0],7,-680876936);d=ff(d,a,b,c,k[1],12,-389564586);c=ff(c,d,a,b,k[2],17,606105819);b=ff(b,c,d,a,k[3],22,-1044525330);a=ff(a,b,c,d,k[4],7,-176418897);d=ff(d,a,b,c,k[5],12,1200080426);c=ff(c,d,a,b,k[6],17,-1473231341);b=ff(b,c,d,a,k[7],22,-45705983);a=ff(a,b,c,d,k[8],7,1770035416);d=ff(d,a,b,c,k[9],12,-1958414417);c=ff(c,d,a,b,k[10],17,-42063);b=ff(b,c,d,a,k[11],22,-1990404162);a=ff(a,b,c,d,k[12],7,1804603682);d=ff(d,a,b,c,k[13],12,-40341101);c=ff(c,d,a,b,k[14],17,-1502002290);b=ff(b,c,d,a,k[15],22,1236535329);a=gg(a,b,c,d,k[1],5,-165796510);d=gg(d,a,b,c,k[6],9,-1069501632);c=gg(c,d,a,b,k[11],14,643717713);b=gg(b,c,d,a,k[0],20,-373897302);a=gg(a,b,c,d,k[5],5,-701558691);d=gg(d,a,b,c,k[10],9,38016083);c=gg(c,d,a,b,k[15],14,-660478335);b=gg(b,c,d,a,k[4],20,-405537848);a=gg(a,b,c,d,k[9],5,568446438);d=gg(d,a,b,c,k[14],9,-1019803690);c=gg(c,d,a,b,k[3],14,-187363961);b=gg(b,c,d,a,k[8],20,1163531501);a=gg(a,b,c,d,k[13],5,-1444681467);d=gg(d,a,b,c,k[2],9,-51403784);c=gg(c,d,a,b,k[7],14,1735328473);b=gg(b,c,d,a,k[12],20,-1926607734);a=hh(a,b,c,d,k[5],4,-378558);d=hh(d,a,b,c,k[8],11,-2022574463);c=hh(c,d,a,b,k[11],16,1839030562);b=hh(b,c,d,a,k[14],23,-35309556);a=hh(a,b,c,d,k[1],4,-1530992060);d=hh(d,a,b,c,k[4],11,1272893353);c=hh(c,d,a,b,k[7],16,-155497632);b=hh(b,c,d,a,k[10],23,-1094730640);a=hh(a,b,c,d,k[13],4,681279174);d=hh(d,a,b,c,k[0],11,-358537222);c=hh(c,d,a,b,k[3],16,-722521979);b=hh(b,c,d,a,k[6],23,76029189);a=hh(a,b,c,d,k[9],4,-640364487);d=hh(d,a,b,c,k[12],11,-421815835);c=hh(c,d,a,b,k[15],16,530742520);b=hh(b,c,d,a,k[2],23,-995338651);a=ii(a,b,c,d,k[0],6,-198630844);d=ii(d,a,b,c,k[7],10,1126891415);c=ii(c,d,a,b,k[14],15,-1416354905);b=ii(b,c,d,a,k[5],21,-57434055);a=ii(a,b,c,d,k[12],6,1700485571);d=ii(d,a,b,c,k[3],10,-1894986606);c=ii(c,d,a,b,k[10],15,-1051523);b=ii(b,c,d,a,k[1],21,-2054922799);a=ii(a,b,c,d,k[8],6,1873313359);d=ii(d,a,b,c,k[15],10,-30611744);c=ii(c,d,a,b,k[6],15,-1560198380);b=ii(b,c,d,a,k[13],21,1309151649);a=ii(a,b,c,d,k[4],6,-145523070);d=ii(d,a,b,c,k[11],10,-1120210379);c=ii(c,d,a,b,k[2],15,718787259);b=ii(b,c,d,a,k[9],21,-343485551);x[0]=add32(a,x[0]);x[1]=add32(b,x[1]);x[2]=add32(c,x[2]);x[3]=add32(d,x[3])}
  function cmn(q,a,b,x,s,t){return add32(bit32(rot(a, s)+(q?1:0)+x+t), b)}
  function ff(a,b,c,d,x,s,t){return cmn((b&c)|((~b)&d),a,b,x,s,t)}
  function gg(a,b,c,d,x,s,t){return cmn((b&d)|(c&(~d)),a,b,x,s,t)}
  function hh(a,b,c,d,x,s,t){return cmn(b^c^d,a,b,x,s,t)}
  function ii(a,b,c,d,x,s,t){return cmn(c^(b|(~d)),a,b,x,s,t)}
  function rot(x,n){return(x<<n)|(x>>>(32-n))}
  function bit32(x){return x&0xFFFFFFFF}
  function add32(a,b){return bit32(a+b)}
  function str2binl(s){var bin=[];for(var i=0;i<s.length*8;i+=8)bin[i>>5]|=(s.charCodeAt(i/8)&255)<<(i%32);return bin}
  function binl2hex(bin){var hex='';for(var i=0;i<bin.length*4;i++)hex+='0123456789abcdef'.charAt((bin[i>>2]>>((i%4)*8+4))&0xF)+'0123456789abcdef'.charAt((bin[i>>2]>>((i%4)*8))&0xF);return hex}
  var s=str2binl(str),len=str.length*8;
  s[len>>5]|=0x80<<(len%32);s[(((len+64)>>>9)<<4)+14]=len;
  var x=[1732584193,-271733879,-1732584194,271733878];
  for(var i=0;i<s.length;i+=16){var y=x.slice();md5cycle(x,s.slice(i,i+16));for(var j=0;j<4;j++)x[j]=add32(x[j],y[j])}
  return binl2hex(x);
}

/* ============================================================
   TOOL 8: 进制转换
   ============================================================ */
window.base8_from=function(source){
  var val;
  try{
    if(source==='bin')val=parseInt($('base8_bin').value.replace(/^0b/i,''),2);
    else if(source==='oct')val=parseInt($('base8_oct').value.replace(/^0o/i,''),8);
    else if(source==='dec')val=parseInt($('base8_dec').value,10);
    else if(source==='hex')val=parseInt($('base8_hex').value.replace(/^0x/i,''),16);
  }catch(e){return}
  if(isNaN(val))return;
  if(!Number.isSafeInteger(val)){toast('数字超出安全范围','error');return}
  $('base8_bin').value='0b'+val.toString(2);
  $('base8_oct').value='0o'+val.toString(8);
  $('base8_dec').value=val.toString(10);
  $('base8_hex').value='0x'+val.toString(16).toUpperCase();
};

/* ============================================================
   TOOL 9: 哈希计算
   ============================================================ */
window.hash9_calc=function(){
  var input=$('hash9_input').value;
  if(!input){toast('请输入文本','error');return}
  var enc=new TextEncoder();
  var data=enc.encode(input);
  var algos=['MD5','SHA-1','SHA-256','SHA-512','SHA3-256'];
  var apiNames={'MD5':null,'SHA-1':'SHA-1','SHA-256':'SHA-256','SHA-512':'SHA-512','SHA3-256':'SHA3-256'};
  // Simple MD5 using a basic implementation
  var md5Result=md5(input);
  var results={
    'MD5':md5Result,
    'SHA-1':null,'SHA-256':null,'SHA-512':null,'SHA3-256':null
  };
  var pending=4;
  function updateResults(){
    var html='';
    for(var i=0;i<algos.length;i++){
      var name=algos[i];var val=results[name]||'计算中…';
      html+='<div class="hash-row"><span class="hr-name">'+name+'</span><span class="hr-value">'+val+'</span><button class="btn-icon hr-copy" onclick="navigator.clipboard.writeText(\''+val+'\').then(function(){toast(\'已复制\',\'success\')})">&#x1F4CB;</button></div>';
    }
    $('hash9_results').innerHTML=html;
  }
  updateResults();
  // Web Crypto for SHA
  ['SHA-1','SHA-256','SHA-512','SHA3-256'].forEach(function(algo){
    crypto.subtle.digest(algo,data).then(function(buf){
      var hash=Array.from(new Uint8Array(buf)).map(function(b){return b.toString(16).padStart(2,'0')}).join('');
      results[algo]=hash;
      pending--;
      if(pending===0)updateResults();
    }).catch(function(){
      results[algo]='不支持';
      pending--;if(pending===0)updateResults();
    });
  });
};

/* ============================================================
   TOOL 10: 字符串工具
   ============================================================ */
window.str10_convert=function(mode){
  var input=$('str10_input').value;
  if(!input){toast('请输入字符串','error');return}
  var result='';
  switch(mode){
    case 'upper':result=input.toUpperCase();break;
    case 'lower':result=input.toLowerCase();break;
    case 'capitalize':result=input.replace(/\b\w/g,function(c){return c.toUpperCase()});break;
    case 'camel':result=input.replace(/[-_\s]+(.)/g,function(_,c){return c.toUpperCase()}).replace(/^[A-Z]/,function(c){return c.toLowerCase()});break;
    case 'snake':result=input.replace(/([A-Z])/g,'_$1').toLowerCase().replace(/^_/,'').replace(/[-\s]+/g,'_');break;
    case 'kebab':result=input.replace(/([A-Z])/g,'-$1').toLowerCase().replace(/^-/,'').replace(/[_\s]+/g,'-');break;
    case 'constant':result=input.replace(/([A-Z])/g,'_$1').toUpperCase().replace(/^_/,'').replace(/[-\s]+/g,'_');break;
    case 'reverse':result=input.split('').reverse().join('');break;
    case 'dedup':result=input.split('\n').filter(function(v,i,a){return a.indexOf(v)===i}).join('\n');break;
    case 'sort':result=input.split('\n').sort().join('\n');break;
  }
  $('str10_output').value=result;
};
window.str10_stats=function(){
  var input=$('str10_input').value;
  $('s_char').textContent=input.length;
  $('s_word').textContent=input.trim()?input.trim().split(/\s+/).length:0;
  $('s_line').textContent=input.split('\n').length;
  $('s_byte').textContent=new Blob([input]).size;
};

/* ============================================================
   TOOL 11: IP/DNS
   ============================================================ */
window.ip11_myIP=function(){
  $('ip11_myip').textContent='查询中…';
  fetch('https://ip-api.com/json/?fields=query,city,country,isp').then(function(r){return r.json()}).then(function(d){
    $('ip11_myip').textContent=d.query;
    $('ip11_myloc').textContent=[d.city,d.country,d.isp].filter(Boolean).join(' · ');
  }).catch(function(){
    $('ip11_myip').textContent='获取失败';
    toast('获取公网IP失败','error');
  });
};
window.ip11_dns=function(){
  var domain=$('ip11_domain').value.trim();
  if(!domain){toast('请输入域名','error');return}
  var type=$('ip11_type').value;
  $('ip11_dnsResult').textContent='查询中…';
  fetch('https://dns.google/resolve?name='+encodeURIComponent(domain)+'&type='+type).then(function(r){return r.json()}).then(function(d){
    if(d.Answer&&d.Answer.length){
      var html='';
      for(var i=0;i<d.Answer.length;i++){
        html+=d.Answer[i].name+' ('+d.Answer[i].type+') → '+d.Answer[i].data+'\n';
      }
      $('ip11_dnsResult').innerHTML=html.replace(/\n/g,'<br>');
    }else{
      $('ip11_dnsResult').textContent='无记录或查询失败';
    }
  }).catch(function(){
    $('ip11_dnsResult').textContent='DNS查询失败';
    toast('DNS查询失败','error');
  });
};
// auto get IP on load
ip11_myIP();

/* ============================================================
   TOOL 12: HTTP 调试
   ============================================================ */
window.http12_send=function(){
  var url=$('http12_url').value.trim();
  if(!url){toast('请输入 URL','error');return}
  var method=$('http12_method').value;
  var headers={};
  var headerInputs=$('http12_headers').querySelectorAll('.h-row');
  for(var i=0;i<headerInputs.length;i++){
    var key=headerInputs[i].querySelector('.h-key').value.trim();
    var val=headerInputs[i].querySelector('.h-val').value.trim();
    if(key)headers[key]=val;
  }
  var body='';
  if(method!=='GET'&&method!=='DELETE'){
    body=$('http12_body').value;
    var ct=$('http12_contentType').value;
    if(!headers['Content-Type']&&!headers['content-type'])headers['Content-Type']=ct;
  }
  var btn=$('http12_send');
  btn.textContent='发送中…';btn.disabled=true;
  $('http12_status').textContent='请求中…';
  $('http12_timing').textContent='';
  $('http12_responseBody').textContent='';
  fetch('/api/tools/http-proxy',{
    method:'POST',
    headers:{'Content-Type':'application/json'},
    body:JSON.stringify({url:url,method:method,headers:headers,body:body})
  }).then(function(r){return r.json()}).then(function(d){
    btn.textContent='发送';btn.disabled=false;
    if(d.success){
      var statusColor='var(--green)';
      if(d.status>=400)statusColor='var(--red)';
      else if(d.status>=300)statusColor='var(--amber)';
      $('http12_status').innerHTML='<span style="display:inline-block;padding:2px 8px;border-radius:4px;background:'+statusColor+'20;color:'+statusColor+';font-weight:600">'+d.status+' '+d.statusText+'</span>';
      $('http12_timing').textContent=d.timing+'ms';
      // format response
      var body=d.body||'';
      try{body=JSON.stringify(JSON.parse(body),null,2)}catch(e){}
      $('http12_responseBody').textContent=body;
    }else{
      $('http12_status').textContent='请求失败';
      $('http12_responseBody').textContent=d.error||'未知错误';
      toast('请求失败: '+(d.error||'未知错误'),'error');
    }
  }).catch(function(e){
    btn.textContent='发送';btn.disabled=false;
    $('http12_status').textContent='网络错误';
    $('http12_responseBody').textContent=e.message;
    toast('网络错误: '+e.message,'error');
  });
};
window.http12_addHeader=function(){
  var container=$('http12_headers');
  if(container.querySelector('.h-row')){
    container.innerHTML='';
  }
  var row=document.createElement('div');
  row.className='h-row';
  row.style='display:flex;gap:6px;margin-bottom:4px';
  row.innerHTML='<input class="input h-key" placeholder="Header" style="flex:1;padding:5px 8px;font-size:12px"><input class="input h-val" placeholder="Value" style="flex:1;padding:5px 8px;font-size:12px"><button class="btn-icon" onclick="this.parentElement.remove()" style="font-size:14px">&#x2716;</button>';
  container.appendChild(row);
  // add a few more rows
  for(var i=0;i<2;i++){
    var r=row.cloneNode(true);
    r.querySelector('.h-key').value='';r.querySelector('.h-val').value='';
    container.appendChild(r);
  }
};
window.http12_clear=function(){
  $('http12_url').value='';$('http12_body').value='';
  $('http12_headers').innerHTML='<div style="color:var(--text-muted);font-size:12px">点击"添加"增加请求头</div>';
  $('http12_responseBody').textContent='发送请求后显示响应';
  $('http12_status').textContent='状态码';
  $('http12_timing').textContent='';
};

/* ============================================================
   TOOL 13: UUID/密码
   ============================================================ */
window.uuid13_gen=function(){
  var uuid=crypto.randomUUID();
  $('uuid13_list').innerHTML='<div style="display:flex;justify-content:space-between;align-items:center;padding:4px 0;border-bottom:1px solid var(--border)"><span>'+uuid+'</span><button class="btn-icon" onclick="navigator.clipboard.writeText(\''+uuid+'\').then(function(){toast(\'已复制\',\'success\')})" style="font-size:12px">&#x1F4CB;</button></div>';
};
window.uuid13_batch=function(){
  var n=parseInt($('uuid13_count').value)||5;
  if(n>50)n=50;
  var html='';
  for(var i=0;i<n;i++){
    var uuid=crypto.randomUUID();
    html+='<div style="display:flex;justify-content:space-between;align-items:center;padding:3px 0;border-bottom:1px solid var(--border)"><span>'+(i+1)+'. '+uuid+'</span><button class="btn-icon" onclick="navigator.clipboard.writeText(\''+uuid+'\').then(function(){toast(\'已复制\',\'success\')})" style="font-size:12px">&#x1F4CB;</button></div>';
  }
  $('uuid13_list').innerHTML=html;
};
window.pwd13_gen=function(){
  var len=parseInt($('pwd13_length').value)||16;
  var sets=[];
  if($('pwd13_upper').classList.contains('on'))sets.push('ABCDEFGHIJKLMNOPQRSTUVWXYZ');
  if($('pwd13_lower').classList.contains('on'))sets.push('abcdefghijklmnopqrstuvwxyz');
  if($('pwd13_digit').classList.contains('on'))sets.push('0123456789');
  if($('pwd13_symbol').classList.contains('on'))sets.push('!@#$%^&*()_+-=[]{}|;:,.<>?');
  if(!sets.length){toast('请至少选择一种字符类型','error');return}
  var allChars=sets.join('');
  var pwd='';
  // ensure at least one from each selected set
  for(var i=0;i<sets.length;i++)pwd+=sets[i][crypto.getRandomValues(new Uint8Array(1))[0]%sets[i].length];
  for(var i=pwd.length;i<len;i++)pwd+=allChars[crypto.getRandomValues(new Uint8Array(1))[0]%allChars.length];
  // shuffle
  pwd=pwd.split('').sort(function(){return crypto.getRandomValues(new Uint8Array(1))[0]-128}).join('');
  $('pwd13_result').textContent=pwd;
  // strength
  var strength=0;
  if(/[a-z]/.test(pwd))strength++;
  if(/[A-Z]/.test(pwd))strength++;
  if(/[0-9]/.test(pwd))strength++;
  if(/[^a-zA-Z0-9]/.test(pwd))strength++;
  if(len>=12)strength++;
  if(len>=20)strength++;
  var pct=Math.min(100,strength*17);
  if(len>=20)pct=100;
  var bar=$('pwd13_bar');
  bar.style.width=pct+'%';
  bar.style.background=pct<40?'var(--red)':pct<70?'var(--amber)':'var(--green)';
};
window.pwd13_copy=function(){
  var pwd=$('pwd13_result').textContent;
  if(pwd&&pwd!=='-'){navigator.clipboard.writeText(pwd).then(function(){toast('密码已复制','success')})}
};
// password length slider
$('pwd13_length').addEventListener('input',function(){
  $('pwd13_lenVal').textContent=this.value;
});

/* ============================================================
   TOOL 14: 番茄钟
   ============================================================ */
var pomo14_state='stopped'; // stopped, running, paused
var pomo14_mode='focus'; // focus, break
var pomo14_remaining=25*60;
var pomo14_timer=null;
var pomo14_count=parseInt(localStorage.getItem('pomo-count')||'0');
$('pomo14_count').textContent=pomo14_count;

window.pomo14_toggle=function(){
  if(pomo14_state==='stopped'||pomo14_state==='paused'){
    pomo14_state='running';
    $('pomo14_startBtn').textContent='暂停';
    pomo14_start();
  }else if(pomo14_state==='running'){
    pomo14_state='paused';
    $('pomo14_startBtn').textContent='继续';
    if(pomo14_timer){clearInterval(pomo14_timer);pomo14_timer=null}
  }
};
function pomo14_start(){
  if(pomo14_timer)clearInterval(pomo14_timer);
  pomo14_timer=setInterval(function(){
    if(pomo14_remaining<=0){
      clearInterval(pomo14_timer);pomo14_timer=null;
      if(pomo14_mode==='focus'){
        pomo14_count++;
        localStorage.setItem('pomo-count',pomo14_count);
        $('pomo14_count').textContent=pomo14_count;
        // switch to break
        pomo14_mode='break';
        pomo14_remaining=parseInt($('pomo14_break').value)*60;
        $('pomo14_label').textContent='休息时间 ☕';
        if(Notification.permission==='granted')new Notification('番茄钟',{body:'专注时间结束！休息一下吧 ☕'});
        toast('专注结束，休息一下吧 ☕','success');
      }else{
        pomo14_mode='focus';
        pomo14_remaining=parseInt($('pomo14_focus').value)*60;
        $('pomo14_label').textContent='专注时间 🌟';
        if(Notification.permission==='granted')new Notification('番茄钟',{body:'休息结束，开始新的专注 🌟'});
        toast('休息结束，开始专注 🌟','success');
      }
      pomo14_state='stopped';
      $('pomo14_startBtn').textContent='开始';
      pomo14_updateDisplay();
      return;
    }
    pomo14_remaining--;
    pomo14_updateDisplay();
  },1000);
}
function pomo14_updateDisplay(){
  var m=Math.floor(pomo14_remaining/60);
  var s=pomo14_remaining%60;
  $('pomo14_time').textContent=String(m).padStart(2,'0')+':'+String(s).padStart(2,'0');
  var total=pomo14_mode==='focus'?parseInt($('pomo14_focus').value)*60:parseInt($('pomo14_break').value)*60;
  var pct=((total-pomo14_remaining)/total)*553;
  $('pomo14_progress').setAttribute('stroke-dashoffset',553-pct);
}
window.pomo14_reset=function(){
  if(pomo14_timer){clearInterval(pomo14_timer);pomo14_timer=null}
  pomo14_state='stopped';
  pomo14_mode='focus';
  $('pomo14_label').textContent='专注时间 🌟';
  pomo14_remaining=parseInt($('pomo14_focus').value)*60;
  $('pomo14_startBtn').textContent='开始';
  pomo14_updateDisplay();
};
// request notification permission
if('Notification'in window&&Notification.permission!=='granted'&&Notification.permission!=='denied')Notification.requestPermission();

/* ============================================================
   TOOL 15: Markdown 预览
   ============================================================ */
window.md15_preview=function(){
  var input=$('md15_input').value;
  if(typeof marked!=='undefined'){
    $('md15_output').innerHTML=marked.parse(input);
    $('md15_html').value=$('md15_output').innerHTML;
  }else{
    $('md15_output').innerHTML='<pre>'+escapeHtml(input)+'</pre>';
  }
};
window.md15_export=function(){
  var html=$('md15_html').value||$('md15_output').innerHTML;
  if(!html){toast('请先输入 Markdown','error');return}
  var full='<!DOCTYPE html><html><head><meta charset="UTF-8"><title>Markdown Export</title><style>body{max-width:800px;margin:0 auto;padding:20px;font-family:-apple-system,sans-serif;line-height:1.6}img{max-width:100%}code{background:#f1f5f9;padding:2px 6px;border-radius:3px}pre code{display:block;padding:12px;overflow:auto}</style></head><body>'+html+'</body></html>';
  var blob=new Blob([full],{type:'text/html'});
  var a=document.createElement('a');
  a.href=URL.createObjectURL(blob);
  a.download='markdown-export.html';
  a.click();
  URL.revokeObjectURL(a.href);
  toast('导出成功','success');
};
// initial preview
md15_preview();

/* ============================================================
   TOOL 16: 打字测试
   ============================================================ */
var type16_lang='zh';
var type16_text='';
var type16_pos=0;
var type16_errors=0;
var type16_total=0;
var type16_startTime=null;
var type16_timer=null;
var type16_finished=false;
var type16_isComposing=false;

var type16_samples={
  zh:['生活就像一盒巧克力，你永远不知道下一颗是什么味道。','千里之行始于足下，不积跬步无以至千里。','编程是一种思维方式，而不仅仅是一项技能。','时间是最好的老师，可惜它教会了我们一切，却无法让我们永远年轻。','世界上本没有路，走的人多了，也便成了路。','成功不是终点，失败也不是终结，唯有勇气才是永恒。','人生在勤，不索何获。','读书破万卷，下笔如有神。','海内存知己，天涯若比邻。','业精于勤荒于嬉，行成于思毁于随。'],
  en:['The quick brown fox jumps over the lazy dog.','Programming is the art of telling another human being what one wants the computer to do.','Simplicity is the ultimate sophistication.','In the middle of difficulty lies opportunity.','Code is like humor. When you have to explain it, its bad.']
};

window.type16_setLang=function(lang){
  type16_lang=lang;
  var tabs=document.querySelectorAll('#tool-16 .tab');
  for(var i=0;i<tabs.length;i++)tabs[i].classList.remove('active');
  tabs[lang==='zh'?0:1].classList.add('active');
  type16_reset();
};
window.type16_reset=function(){
  var samples=type16_samples[type16_lang];
  type16_text=samples[Math.floor(Math.random()*samples.length)];
  type16_pos=0;type16_errors=0;type16_total=0;type16_finished=false;
  type16_startTime=null;
  if(type16_timer){clearInterval(type16_timer);type16_timer=null}
  var html='';
  for(var i=0;i<type16_text.length;i++){
    html+='<span class="char'+(i===0?' active':'')+'">'+escapeHtml(type16_text[i])+'</span>';
  }
  $('type16_display').innerHTML=html;
  $('type16_input').value='';
  $('type16_input').disabled=false;
  $('type16_input').focus();
  $('type16_wpm').textContent='0';
  $('type16_acc').textContent='0%';
  $('type16_time').textContent='0';
};
$('type16_input').addEventListener('input',function(){
  if(type16_finished||type16_isComposing)return;
  if(!type16_startTime){type16_startTime=Date.now();type16_timer=setInterval(type16_updateStats,200)}
  type16_processInput(this);
});
$('type16_input').addEventListener('compositionstart',function(){
  type16_isComposing=true;
});
$('type16_input').addEventListener('compositionend',function(){
  type16_isComposing=false;
  if(!type16_finished){
    if(!type16_startTime){type16_startTime=Date.now();type16_timer=setInterval(type16_updateStats,200)}
    type16_processInput(this);
  }
});
function type16_processInput(el){
  var val=el.value;
  var chars=$('type16_display').querySelectorAll('.char');
  for(var i=0;i<val.length&&i<type16_text.length;i++){
    if(val[i]===type16_text[i]){
      if(i<chars.length){chars[i].classList.add('correct');chars[i].classList.remove('active')}
    }else{
      if(i<chars.length){chars[i].classList.add('incorrect');chars[i].classList.remove('active')}
    }
  }
  type16_pos=val.length;
  type16_errors=0;
  for(var i=0;i<val.length&&i<type16_text.length;i++){if(val[i]!==type16_text[i])type16_errors++}
  type16_total=val.length;
  // next char highlight
  if(val.length<type16_text.length&&val.length<chars.length){
    chars[val.length].classList.add('active');
  }
  // finished
  if(val.length>=type16_text.length){
    type16_finished=true;
    if(type16_timer){clearInterval(type16_timer);type16_timer=null}
    type16_updateStats();
    $('type16_input').disabled=true;
    toast('完成！','success');
  }
}
function type16_updateStats(){
  var elapsed=(Date.now()-type16_startTime)/1000;
  $('type16_time').textContent=Math.floor(elapsed);
  if(elapsed>0){
    var wpm=type16_lang==='en'?Math.round((type16_total/5)/(elapsed/60)):Math.round((type16_total)/(elapsed/60));
    $('type16_wpm').textContent=wpm;
    var acc=type16_total>0?Math.round((1-type16_errors/type16_total)*100):100;
    $('type16_acc').textContent=acc+'%';
  }
}
type16_reset();

/* ============================================================
   TOOL 17: 彩虹屁/毒鸡汤
   ============================================================ */
var q17_mode='love';
var q17_love=[
  '你是我见过最美好的存在，像春日里的第一缕阳光。',
  '你笑起来的样子，比星星还要耀眼。',
  '你的存在本身，就是这个世界最温柔的馈赠。',
  '遇见你，是我这一生最美丽的意外。',
  '你就像一道光，照亮了我平淡的生活。',
  '你的眼睛里，藏着整个银河的温柔。',
  '你是我的超级英雄，也是最可爱的那个人。',
  '和你在一起的每一天，都是情人节。',
  '你像夏天的西瓜，冬天的火锅，让我无法抗拒。',
  '你是我平淡生活里最亮的色彩。',
  '要是能重来，我还是会选你。',
  '你的出现，让所有标准都变成了例外。',
  '你的眼睛里有星辰大海，而我愿意溺死在其中。',
  '我走过许多地方的路，行过许多地方的桥，却只爱过一个正当最好年龄的人。',
  '你是我的小确幸，小而确定的幸福。',
  '你的微笑是我这辈子最大的动力。',
  '我喜欢的样子，你都有。',
  '你的温柔像春天的风，吹过我心头的每一寸土地。'
];
var q17_poison=[
  '努力不一定成功，但不努力真的很轻松。',
  '你以为你是主角，其实你只是路人甲。',
  '生活不止眼前的苟且，还有未来的苟且。',
  '你并没什么不好，只是没什么好。',
  '上帝是公平的，给了你丑的外表，还给了你低的智商。',
  '你以为有钱人很快乐吗？他们的快乐你根本想象不到。',
  '万事开头难，然后中间难，最后结尾难。',
  '别灰心，人生就是这样起起落落落落落落落落落。',
  '世上无难事，只要肯放弃。',
  '不是所有人都有才华，但所有人都有头发——除非你秃了。',
  '你努力的样子很感人，可惜结果很气人。',
  '有时候你不努力一下，都不知道什么叫绝望。',
  '失败是成功之母，但成功经常难产。',
  '比你优秀的人还在努力，那你努力还有什么用？',
  '只要肯努力，没有什么事是你搞不砸的。',
  '人生就像心电图，一帆风顺就说明你挂了。',
  '别说什么来日方长，实际上人生苦短，短到你可能连个对象都找不到。'
];

window.q17_switch=function(mode){
  q17_mode=mode;
  $('q17_tab_love').classList.toggle('active',mode==='love');
  $('q17_tab_poison').classList.toggle('active',mode==='poison');
  q17_next();
};
window.q17_next=function(){
  var arr=q17_mode==='love'?q17_love:q17_poison;
  var idx=Math.floor(Math.random()*arr.length);
  $('q17_text').textContent='"'+arr[idx]+'"';
  $('q17_author').textContent=q17_mode==='love'?'—— 彩虹屁生成器':'—— 毒鸡汤生成器';
};
window.q17_copy=function(){
  var text=$('q17_text').textContent;
  if(text){navigator.clipboard.writeText(text).then(function(){toast('已复制','success')})}
};
q17_next();

/* ============================================================
   INIT
   ============================================================ */
// ensure all tool views properly initialize
$('cron4_expr').addEventListener('input',function(){cron4_parse()});
$('cron4_expr').value='* * * * *';

// call str10_stats on load
str10_stats();

// auto-setup method color
$('http12_method').addEventListener('change',function(){
  var opt=this.options[this.selectedIndex];
  this.style.color=opt.style.color;
});

})();
