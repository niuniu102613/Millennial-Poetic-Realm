// ===== 千年诗境 · 公共 JS =====
// file:// 直接双击打开时走本地后端；http 部署时走同源
const API_BASE = (location.protocol === 'file:') ? 'http://localhost:8080/api' : '/api';

async function fetchAPI(path) {
    try {
        const res = await fetch(API_BASE + path);
        return await res.json();
    } catch (e) {
        console.warn('接口暂未就绪:', path, e);
        return null;
    }
}

// 掐丝珐琅传统配色（所有页面共用）
const THEME_COLORS = [
    '#19325F',  // 佛头青
    '#77171C',  // 朱樱
    '#C9A96E',  // 描金
    '#3D6036',  // 松绿
    '#A33F32',  // 朱草
    '#C7935F',  // 佛赤
    '#19325F',  // 佛头青（深）
    '#E5D3AA',  // 莲子白
];

// ===== 中英文界面切换 =====
const I18N = {
    zh: {
        'logo': '千年诗境',
        'nav.dynasty': '朝代分析',
        'nav.poet': '诗人图谱',
        'nav.text': '诗词文本分析',
        'nav.imagery': '诗境意象',
        'nav.overview': '诗词数据概览',
        'home.title': '千&nbsp;年&nbsp;诗&nbsp;境',
        'home.card1.title': '朝代分析',
        'home.card1.desc': '各朝代作品数量、诗人数量、平均长度',
        'home.card2.title': '诗人图谱',
        'home.card2.desc': '作品数量 TopN、各朝代代表诗人',
        'home.card3.title': '诗词文本分析',
        'home.card3.desc': '高频字词、长度分布、朝代高频字',
        'home.card4.title': '诗境意象',
        'home.card4.desc': '月、柳、酒等意象出现频率与分布',
        'home.card5.title': '诗词数据概览',
        'home.card5.desc': '类别统计、TopN、朝代×类别、长度区间',
        'switchBg': '换景',
        'bgTitle': '更换背景',
        'langBtn': 'English',
        /* 朝代显示名 */
        'dyn.唐代': '唐代', 'dyn.宋代': '宋代', 'dyn.元代': '元代',
        'dyn.明代': '明代', 'dyn.清代': '清代', 'dyn.魏晋': '魏晋', 'dyn.先秦': '先秦',
        'dyn.东汉': '东汉', 'dyn.战国': '战国',
        /* 类别显示名 */
        'cat.古体诗': '古体诗', 'cat.近体诗': '近体诗', 'cat.词': '词',
        'cat.绝句': '绝句', 'cat.律诗': '律诗',
        /* 诗词数据概览 */
        'ov.title': '诗词数据概览',
        'ov.sub': '诗词类型与结构分析',
        'ov.heroTitle': '诗词数据概览',
        'ov.heroSub': '类别之变，篇幅长短，皆藏于诗卷。',
        'ov.heroSub2': '遍历诗卷，观类别分布、朝代变迁与篇幅气象。',
        'ov.eyebrow': 'Overview of Poetry Data',
        'ov.verL': '百代词章',
        'ov.verR': '一册尽览',
        'ov.kTotal': '诗词总数',
        'ov.kCategory': '诗词类别数',
        'ov.kAvgLen': '平均字数',
        'ov.kTopCat': '最常见类别',
        'ov.kNote': '数据概览 · 实际数值由现有数据接口提供',
        'ov.sSearchTag': '索 · 探微',
        'ov.sSearchTitle': '检索诗词',
        'ov.sSearchDesc': '按类别、朝代或篇幅区间快速检索',
        'ov.s1Tag': '壹 · 类别',
        'ov.s1Title': '诗词类别分布',
        'ov.s1Desc': '各诗词类别数量 · 环形图与排行',
        'ov.s2Tag': '贰 · 朝代',
        'ov.s2Title': '朝代 × 类别分布',
        'ov.s2Desc': '点击柱段，下方联动查看朝代详情',
        'ov.s3Tag': '叁 · 篇幅',
        'ov.s3Title': '诗词长度区间',
        'ov.s3Desc': '各篇幅长度分布 · 含平均线',
        'ov.searchPlaceholder': '搜索类别 / 朝代，如：古体诗、唐代、词...',
        'ov.searchBtn': '搜索',
        'ov.dynastyDist': '各朝代分布',
        'ov.totalPoems': '诗词总数',
        'ov.topCategory': '最常见类别',
        'ov.noResult': '未找到相关数据，试试搜索：古体诗、近体诗、词、唐代、宋代...',
        'ov.hotTags': '热门：',
        'ov.refresh': '换一批',
        'ov.representative': '代表诗句',
        'ov.viewPoems': '查看相关诗句',
        'ov.statTotal': '诗词总数',
        'ov.statCategory': '诗词类别数',
        'ov.statAvgLen': '平均字数',
        'ov.statTopCat': '最常见类别',
        'ov.catPie': '各诗词类别数量（点击扇区下钻）',
        'ov.catTop': '诗词类别 TopN',
        'ov.dynCat': '朝代 × 类别 分布（点击柱段，下方联动查看朝代详情）',
        'ov.detailTitle': '该朝代各类别数量',
        'ov.lenRange': '诗词长度区间分布（含平均线）',
        'ov.totalPrefix': '共计',
        'ov.totalSuffix': '首诗词',
        'ov.noVerse': '（暂无诗句）',
        'ov.quantity': '数量',
        'ov.share': '占比',
        'ov.rangeName': '字数区间',
        'ov.poemCount': '诗词数量',
        'ov.average': '平均',
        'ov.avgFmt': '平均: {c}',
        'ov.lenUnit': '字',
        /* 朝代分析 */
        'dy.title': '朝代分析',
        'dy.sub': '各朝代作品数、诗人数量、平均长度、朝代 TopN',
        'dy.c1': '各朝代诗词数量',
        'dy.c2': '各朝代作者数量',
        'dy.c3': '各朝代平均诗词长度',
        'dy.c4': '各朝代诗词数量 TopN',
        'dy.empty': '数据待接入',
        'dy.emptySub': '等待 MapReduce 结果',
        'dy.eyebrow': 'A Journey Through Chinese Poetry',
        'dy.heroTitle': '诗脉千年',
        'dy.heroSub': '一字一句，皆是山河岁月。',
        'dy.heroSub2': '循着历史的脉络，探寻诗词与诗人的千年风华。',
        'dy.verL': '山河千载',
        'dy.verR': '诗卷长流',
        'dy.kPoems': '收录诗词',
        'dy.kAuthors': '收录作者',
        'dy.kDyn': '统计朝代',
        'dy.kNote': '数据概览 · 实际数值由现有数据接口提供',
        'dy.s1Tag': '壹 · 诗脉',
        'dy.s1Title': '千年诗词，何处繁盛？',
        'dy.s1Desc': '各朝代诗词数量 · 横向墨色条形图',
        'dy.s2Tag': '贰 · 群星',
        'dy.s2Title': '历代作者',
        'dy.s2Desc': '作者数量与朝代分布',
        'dy.s3Tag': '叁 · 篇章',
        'dy.s3Title': '诗词篇幅',
        'dy.s3Desc': '平均长度与差异比较',
        'dy.s4Tag': '肆 · 榜录',
        'dy.s4Title': '诗词数量 · 朝代排行',
        'dy.s4Desc': '以简洁的古籍题签呈现排名，点击朝代可联动上方数据。',
        /* 诗人图谱 */
        'pt.title': '诗人图谱',
        'pt.sub': '作者作品数量 TopN、各朝代代表诗人',
        'pt.c1': '作者作品数量 TopN',
        'pt.c2': '各朝代作者数量',
        'pt.c3': '作者平均作品长度 TopN',
        'pt.c4': '各朝代代表诗人 TopN',
        'pt.eyebrow': 'A Gallery of Poets Through Ages',
        'pt.heroTitle': '群星璀璨',
        'pt.heroSub': '江山代有才人出，各领风骚数百年。',
        'pt.heroSub2': '纵览历代诗家风貌，遇见每个时代的闪耀灵魂。',
        'pt.verL': '群贤毕至',
        'pt.verR': '少长咸集',
        'pt.kPoets': '收录诗人',
        'pt.kWorks': '收录诗词',
        'pt.kDyn': '诗朝',
        'pt.kChampion': '诗家魁首',
        'pt.kNote': '数据概览 · 实际数值由数据接口提供',
        'pt.timelineHint': '点击朝代节点，聚焦该朝诗群',
        'pt.s1Tag': '壹 · 魁首',
        'pt.s1Title': '谁是最高产的诗人？',
        'pt.s1Desc': '作品数量 Top15 · 墨色横向榜',
        'pt.s2Tag': '贰 · 群英',
        'pt.s2Title': '历代诗人，群星璀璨',
        'pt.s2Desc': '各朝代作者数量 · 南丁格尔玫瑰图',
        'pt.s3Tag': '叁 · 华章',
        'pt.s3Title': '多产与长篇',
        'pt.s3Desc': '作品数 × 平均篇幅 · 散点图',
        'pt.s4Tag': '肆 · 星图',
        'pt.s4Title': '各朝诗人星图',
        'pt.s4Desc': '由中心向外：朝代 → 代表诗人；圆点越大，作品越多（可滚轮缩放、拖动）',
        'pt.s5Tag': '伍 · 流芳',
        'pt.s5Title': '各朝代代表诗人',
        'pt.s5Desc': '诗签录 · 点击上方时间轴可聚焦某一朝代',
        'pt.pieces': '首',
        'pt.poetsUnit': '人',
        'pt.charsUnit': '字',
        'pt.works': '作品',
        'pt.avgLen': '平均篇幅',
        'pt.poets': '诗人',
        'pt.poems': '诗词',
        'pt.share': '占比',
        'pt.repPoets': '代表诗人',
        'pt.axisWorks': '作品数（首）',
        'pt.axisAvg': '平均篇幅（字）',
        'pt.noteScatter': '注：《孟子》等典籍为长篇散文，故平均篇幅远高于诗作',
        /* 诗词文本分析 */
        'tx.title': '诗词文本分析',
        'tx.sub': '高频字、高频词、长度分布、各朝代高频字',
        'tx.c1': '高频字统计',
        'tx.c2': '高频词统计',
        'tx.c3': '诗词长度分布',
        'tx.c4': '各朝代高频字统计',
        /* 诗境意象 */
        'im.title': '诗境意象',
        'im.sub': '意象出现次数、意象 TopN、朝代×意象、作者×意象',
        'im.c1': '意象出现次数统计',
        'im.c2': '意象 TopN',
        'im.c3': '朝代 × 意象',
        'im.c4': '作者 × 意象',
        'im.count': '出现次数',
        'im.all': '全部',
        'im.cloud': '高频意象词云',
        'im.quickPoet': '快速选择诗人：',
        'im.times': '次',
        'im.kTotal': '意象种类',
        'im.kPoems': '诗词总量',
        'im.kDyn': '覆盖朝代',
        'im.kAuth': '覆盖诗人',
        /* 登录门禁 */
        'auth.gateTitle': '千 年 诗 境',
        'auth.gateSub': '请登录后进入首页',
        'auth.title': '登 录',
        'auth.titleReg': '注 册',
        'auth.user': '用户名',
        'auth.pass': '密码',
        'auth.pass2': '确认密码',
        'auth.submit': '登 录',
        'auth.submitReg': '注 册',
        'auth.toReg': '没有账号？去注册',
        'auth.toLogin': '已有账号？去登录',
        'auth.errFill': '请输入用户名和密码',
        'auth.errLen': '密码至少 8 位',
        'auth.errType': '密码需含两种字符（字母/数字/符号）',
        'auth.pwdHint': '至少 8 位，需含两种字符（字母/数字/符号）',
        'auth.errMatch': '两次密码不一致',
        'auth.errExists': '用户名已存在',
        'auth.errWrong': '用户名或密码错误',
        'auth.hint': '模拟账号：admin / 123456',
        'auth.welcome': '登录成功',
        'auth.welcomeBack': '欢迎回来',
        'auth.enter': '进入首页',
    },
    en: {
        'logo': 'Millennial Poetry',
        'nav.dynasty': 'Dynasties',
        'nav.poet': 'Poets',
        'nav.text': 'Text Analysis',
        'nav.imagery': 'Imagery',
        'nav.overview': 'Overview',
        'home.title': 'MILLENNIAL<br>POETIC REALM',
        'home.card1.title': 'Dynasty Analysis',
        'home.card1.desc': 'Works, poets & average length per dynasty',
        'home.card2.title': 'Poet Gallery',
        'home.card2.desc': 'Top poets by works, representative poets',
        'home.card3.title': 'Text Analysis',
        'home.card3.desc': 'Frequent characters & words, length distribution',
        'home.card4.title': 'Imagery',
        'home.card4.desc': 'Frequency & distribution of imagery, e.g. moon & willow',
        'home.card5.title': 'Data Overview',
        'home.card5.desc': 'Category stats, TopN, dynasty×category, length range',
        'switchBg': 'Scene',
        'bgTitle': 'Switch background',
        'langBtn': '中文',
        /* 朝代显示名 */
        'dyn.唐代': 'Tang', 'dyn.宋代': 'Song', 'dyn.元代': 'Yuan',
        'dyn.明代': 'Ming', 'dyn.清代': 'Qing', 'dyn.魏晋': 'Wei-Jin', 'dyn.先秦': 'Pre-Qin',
        'dyn.东汉': 'Eastern Han', 'dyn.战国': 'Warring States',
        /* 类别显示名 */
        'cat.古体诗': 'Ancient-style', 'cat.近体诗': 'Recent-style', 'cat.词': 'Ci',
        'cat.绝句': 'Jueju', 'cat.律诗': 'Lushi',
        /* 诗词数据概览 */
        'ov.title': 'Data Overview',
        'ov.heroTitle': 'Poetry Data Overview',
        'ov.heroSub': 'Categories and lengths, all within these verses.',
        'ov.heroSub2': 'Traverse the scrolls to observe categories, dynasties and lengths.',
        'ov.eyebrow': 'Overview of Poetry Data',
        'ov.verL': 'Verses of All Ages',
        'ov.verR': 'All in One Scroll',
        'ov.kTotal': 'Total Poems',
        'ov.kCategory': 'Categories',
        'ov.kAvgLen': 'Avg. Length',
        'ov.kTopCat': 'Top Category',
        'ov.kNote': 'Overview · values provided by data APIs',
        'ov.sSearchTag': 'Search',
        'ov.sSearchTitle': 'Search Poems',
        'ov.sSearchDesc': 'Search by category, dynasty or length range',
        'ov.s1Tag': 'I · Categories',
        'ov.s1Title': 'Category Distribution',
        'ov.s1Desc': 'Counts by category · donut & ranking',
        'ov.s2Tag': 'II · Dynasties',
        'ov.s2Title': 'Dynasty × Category',
        'ov.s2Desc': 'Click a bar segment to view dynasty details',
        'ov.s3Tag': 'III · Length',
        'ov.s3Title': 'Poem Length Ranges',
        'ov.s3Desc': 'Distribution by length · with average line',
        'ov.sub': 'Poetry type & structure analysis',
        'ov.searchPlaceholder': 'Search category / dynasty, e.g. 古体诗, Tang, 词...',
        'ov.searchBtn': 'Search',
        'ov.dynastyDist': 'Distribution by dynasty',
        'ov.totalPoems': 'Total poems',
        'ov.topCategory': 'Top category',
        'ov.noResult': 'No result. Try: 古体诗, 近体诗, 词, Tang, Song...',
        'ov.hotTags': 'Hot:',
        'ov.refresh': 'Refresh',
        'ov.representative': 'Representative',
        'ov.viewPoems': 'View Poems',
        'ov.statTotal': 'Total Poems',
        'ov.statCategory': 'Categories',
        'ov.statAvgLen': 'Avg. Length',
        'ov.statTopCat': 'Top Category',
        'ov.catPie': 'Category counts (click to drill down)',
        'ov.catTop': 'Category TopN',
        'ov.dynCat': 'Dynasty × Category (click a bar for details below)',
        'ov.detailTitle': 'Category counts of this dynasty',
        'ov.lenRange': 'Length range distribution (with average line)',
        'ov.totalPrefix': 'Total',
        'ov.totalSuffix': 'poems',
        'ov.noVerse': '(no verse yet)',
        'ov.quantity': 'Count',
        'ov.share': 'Share',
        'ov.rangeName': 'Length range',
        'ov.poemCount': 'Poem count',
        'ov.average': 'Average',
        'ov.avgFmt': 'Avg: {c}',
        'ov.lenUnit': 'chars',
        /* 朝代分析 */
        'dy.title': 'Dynasty Analysis',
        'dy.sub': 'Works, poets, average length & TopN per dynasty',
        'dy.c1': 'Poems per dynasty',
        'dy.c2': 'Poets per dynasty',
        'dy.c3': 'Average poem length per dynasty',
        'dy.c4': 'TopN poems per dynasty',
        'dy.empty': 'Data pending',
        'dy.emptySub': 'Awaiting MapReduce results',
        'dy.eyebrow': 'A Journey Through Chinese Poetry',
        'dy.heroTitle': 'Poetic Millennium',
        'dy.heroSub': 'Every line is a verse of mountains and rivers.',
        'dy.heroSub2': 'Trace the flow of poetry and poets across dynasties.',
        'dy.verL': 'Mountains & rivers',
        'dy.verR': 'Poems flow on',
        'dy.kPoems': 'Poems Collected',
        'dy.kAuthors': 'Authors Collected',
        'dy.kDyn': 'Dynasties',
        'dy.kNote': 'Overview · live values from the data API',
        'dy.s1Tag': 'I · Vein',
        'dy.s1Title': 'Where did poetry flourish?',
        'dy.s1Desc': 'Poem count per dynasty · horizontal ink bars',
        'dy.s2Tag': 'II · Stars',
        'dy.s2Title': 'Poets Through Ages',
        'dy.s2Desc': 'Author count across dynasties',
        'dy.s3Tag': 'III · Verses',
        'dy.s3Title': 'Poem Length',
        'dy.s3Desc': 'Average length and variance',
        'dy.s4Tag': 'IV · Rank',
        'dy.s4Title': 'Poem Count · Dynasty Ranking',
        'dy.s4Desc': 'A clean scroll-style ranking; click a dynasty to highlight.',
        /* 诗人图谱 */
        'pt.title': 'Poet Gallery',
        'pt.sub': 'Top poets by works, representative poets of dynasties',
        'pt.c1': 'TopN poets by works',
        'pt.c2': 'Poets per dynasty',
        'pt.c3': 'TopN by average work length',
        'pt.c4': 'TopN representative poets per dynasty',
        'pt.eyebrow': 'A Gallery of Poets Through Ages',
        'pt.heroTitle': 'A Galaxy of Poets',
        'pt.heroSub': 'Each age brings its talents, each shining for centuries.',
        'pt.heroSub2': 'Wander through the poetic worlds of every dynasty.',
        'pt.verL': 'Masters Gather',
        'pt.verR': 'Voices of Ages',
        'pt.kPoets': 'Poets',
        'pt.kWorks': 'Poems',
        'pt.kDyn': 'Dynasties',
        'pt.kChampion': 'Top Poet',
        'pt.kNote': 'Overview · live values from data APIs',
        'pt.timelineHint': 'Click a dynasty node to focus',
        'pt.s1Tag': 'I · Champions',
        'pt.s1Title': 'Who wrote the most?',
        'pt.s1Desc': 'Top 15 poets by surviving works',
        'pt.s2Tag': 'II · Galaxy',
        'pt.s2Title': 'Poets across dynasties',
        'pt.s2Desc': 'Poet count per dynasty · nightingale rose',
        'pt.s3Tag': 'III · Verses',
        'pt.s3Title': 'Prolific vs. Epic',
        'pt.s3Desc': 'Works × average length · scatter',
        'pt.s4Tag': 'IV · Star Map',
        'pt.s4Title': 'Poet Star Map',
        'pt.s4Desc': 'From center outward: dynasties → representative poets; larger dots mean more works (scroll to zoom, drag to pan)',
        'pt.s5Tag': 'V · Legacies',
        'pt.s5Title': 'Representative Poets',
        'pt.s5Desc': 'Poet scrolls · click the timeline above to focus a dynasty',
        'pt.pieces': 'works',
        'pt.poetsUnit': '',
        'pt.charsUnit': 'chars',
        'pt.works': 'Works',
        'pt.avgLen': 'Avg. length',
        'pt.poets': 'Poets',
        'pt.poems': 'Poems',
        'pt.share': 'Share',
        'pt.repPoets': 'Rep. poet',
        'pt.axisWorks': 'Works',
        'pt.axisAvg': 'Avg. length (chars)',
        'pt.noteScatter': 'Note: classics such as Mencius are long prose, hence the outlier.',
        /* 诗词文本分析 */
        'tx.title': 'Text Analysis',
        'tx.sub': 'Frequent characters & words, length distribution, per-dynasty frequent chars',
        'tx.c1': 'Frequent character stats',
        'tx.c2': 'Frequent word stats',
        'tx.c3': 'Poem length distribution',
        'tx.c4': 'Frequent characters per dynasty',
        /* 诗境意象 */
        'im.title': 'Imagery',
        'im.sub': 'Imagery counts, TopN, dynasty×imagery, poet×imagery',
        'im.c1': 'Imagery occurrence stats',
        'im.c2': 'Imagery TopN',
        'im.c3': 'Dynasty × Imagery',
        'im.c4': 'Poet × Imagery',
        'im.count': 'Occurrences',
        'im.all': 'All',
        'im.cloud': 'Frequent Imagery Cloud',
        'im.quickPoet': 'Quick pick a poet: ',
        'im.times': 'times',
        'im.kTotal': 'Imagery Types',
        'im.kPoems': 'Total Poems',
        'im.kDyn': 'Dynasties',
        'im.kAuth': 'Poets',
        /* 登录门禁 */
        'auth.gateTitle': 'MILLENNIAL POETRY',
        'auth.gateSub': 'Please sign in to enter',
        'auth.title': 'Sign In',
        'auth.titleReg': 'Register',
        'auth.user': 'Username',
        'auth.pass': 'Password',
        'auth.pass2': 'Confirm password',
        'auth.submit': 'Sign In',
        'auth.submitReg': 'Register',
        'auth.toReg': 'No account? Register',
        'auth.toLogin': 'Have account? Sign in',
        'auth.errFill': 'Enter username and password',
        'auth.errLen': 'Password must be at least 8 characters',
        'auth.errType': 'Password must contain two types (letters / digits / symbols)',
        'auth.pwdHint': 'At least 8 chars with two types (letters/digits/symbols)',
        'auth.errMatch': 'Passwords do not match',
        'auth.errExists': 'Username already exists',
        'auth.errWrong': 'Invalid username or password',
        'auth.hint': 'Demo account: admin / 123456',
        'auth.welcome': 'Signed in',
        'auth.welcomeBack': 'Welcome back',
        'auth.enter': 'Enter',
    }
};

function applyLang(lang) {
    window.__lang = lang;
    const dict = I18N[lang];
    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.dataset.i18n;
        if (dict[key] != null) el.innerHTML = dict[key];
    });
    document.querySelectorAll('[data-i18n-title]').forEach(el => {
        const key = el.dataset.i18nTitle;
        if (dict[key] != null) el.title = dict[key];
    });
    document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
        const key = el.dataset.i18nPlaceholder;
        if (dict[key] != null) el.placeholder = dict[key];
    });
    const btn = document.getElementById('langSwitch');
    if (btn) btn.textContent = dict['langBtn'];
    document.documentElement.lang = (lang === 'zh') ? 'zh-CN' : 'en';
    localStorage.setItem('poetry_lang', lang);
    // 通知各页面脚本重绘/更新动态文本
    window.dispatchEvent(new CustomEvent('langchange', { detail: lang }));
}

// 取当前语言下的文本（供 JS 动态内容使用）
function t(key) {
    const dict = I18N[window.__lang || 'zh'];
    return (dict && dict[key] != null) ? dict[key] : key;
}

// 数据里的朝代/类别名 → 当前语言显示名（找不到就原样返回）
function trDyn(name) {
    if(!name) return name;
    if(t('dyn.'+name) !== 'dyn.'+name) return t('dyn.'+name);
    if(t('dyn.'+name+'代') !== 'dyn.'+name+'代') return t('dyn.'+name+'代');
    return name;
}
function trCat(name) { return t('cat.' + name); }

// 诗人名 → 拼音/通用英译（找不到就原样）
const POET_EN = {
    '曹操':'Cao Cao','曹丕':'Cao Pi','曹植':'Cao Zhi','陶渊明':'Tao Yuanming',
    '李白':'Li Bai','杜甫':'Du Fu','王维':'Wang Wei','孟浩然':'Meng Haoran',
    '王昌龄':'Wang Changling','岑参':'Cen Shen','高适':'Gao Shi','韩愈':'Han Yu',
    '柳宗元':'Liu Zongyuan','白居易':'Bai Juyi','元稹':'Yuan Zhen','刘禹锡':'Liu Yuxi',
    '李贺':'Li He','杜牧':'Du Mu','李商隐':'Li Shangyin','温庭筠':'Wen Tingyun',
    '陆龟蒙':'Lu Guimeng','皮日休':'Pi Rixiu',
    '李煜':'Li Yu','苏轼':'Su Shi','苏辙':'Su Zhe','苏洵':'Su Xun','欧阳修':'Ouyang Xiu',
    '王安石':'Wang Anshi','曾巩':'Zeng Gong','黄庭坚':'Huang Tingjian','秦观':'Qin Guan',
    '柳永':'Liu Yong','周邦彦':'Zhou Bangyan','李清照':'Li Qingzhao','辛弃疾':'Xin Qiji',
    '陆游':'Lu You','杨万里':'Yang Wanli','范成大':'Fan Chengda','朱熹':'Zhu Xi',
    '梅尧臣':'Mei Yaochen','范仲淹':'Fan Zhongyan','晏殊':'Yan Shu','晏几道':'Yan Jidao',
    '姜夔':'Jiang Kui','吴文英':'Wu Wenying','王十朋':'Wang Shipeng','宋祁':'Song Qi',
    '宋庠':'Song Xiang','曹勋':'Cao Xun','洪适':'Hong Shi','姜特立':'Jiang Teli',
    '王之道':'Wang Zhidao','韩元吉':'Han Yuanji','刘子翚':'Liu Zihui','史浩':'Shi Hao',
    '释正觉':'Monk Zhengjue','释印肃':'Monk Yinsu','吴芾':'Wu Fu','无名氏':'Anonymous',
    '孟子及弟子':'Mencius & Disciples','曾子':'Zengzi','子思':'Zisi',
    '关汉卿':'Guan Hanqing','马致远':'Ma Zhiyuan','王实甫':'Wang Shifu',
    '张养浩':'Zhang Yanghao','张可久':'Zhang Kejiu','徐再思':'Xu Zaisi',
    '吴西逸':'Wu Xiyi','查德卿':'Zha Deqing','薛昂夫':'Xue Angfu','沈禧':'Shen Xi',
    '袁桷':'Yuan Jue','高明':'Gao Ming','纳兰性德':'Nalan Xingde','顾云':'Gu Yun',
    '欧阳炯':'Ouyang Jiong','李行甫':'Li Xingfu','李文蔚':'Li Wenwei','吴少微':'Wu Shaowei',
    '高茂卿':'Gao Maoqing','朱凯':'Zhu Kai','舒元舆':'Shu Yuanyu','慧净':'Hui Jing',
    '李薰':'Li Xun','孙松柏':'Sun Songbai','皎然':'Monk Jiaoran','贾岛':'Jia Dao',
};
function trName(name) {
    if (window.__lang !== 'en') return name;
    if (POET_EN[name]) return POET_EN[name];
    // 处理 "诗人·朝代" 这种组合
    if (name && name.includes('·')) {
        const parts = name.split('·');
        return (POET_EN[parts[0]] || parts[0]) + '·' + trDyn(parts[1]);
    }
    return name;
}

// 密码强度校验：至少 8 位，且含两种字符（小写/大写/数字/符号）
function pwdError(p) {
    if (!p || p.length < 8) return t('auth.errLen');
    let types = 0;
    if (/[a-z]/.test(p)) types++;
    if (/[A-Z]/.test(p)) types++;
    if (/[0-9]/.test(p)) types++;
    if (/[^a-zA-Z0-9]/.test(p)) types++;
    if (types < 2) return t('auth.errType');
    return '';
}

function toggleLang() {
    const cur = localStorage.getItem('poetry_lang') || 'zh';
    applyLang(cur === 'zh' ? 'en' : 'zh');
}

document.addEventListener('DOMContentLoaded', () => {
    // 高亮当前页导航
    const page = document.body.dataset.page;
    document.querySelectorAll('.nav a').forEach(a => {
        if (a.dataset.page === page) a.classList.add('active');
    });

    // 注入中英文切换按钮（右下角；首页时紧挨换景按钮）
    if (!document.getElementById('langSwitch')) {
        const btn = document.createElement('button');
        btn.id = 'langSwitch';
        btn.className = 'lang-switch';
        btn.type = 'button';
        btn.onclick = toggleLang;
        document.body.appendChild(btn);
        const hasBg = document.querySelector('.bg-switch');
        btn.style.right = hasBg ? '96px' : '28px';
    }

    // 应用已保存的语言
    applyLang(localStorage.getItem('poetry_lang') || 'zh');
});

// ===== 登录/注册模块（当前为前端模拟，后端接口就位后替换验证逻辑） =====
function initAuth() {
    // 预置模拟账号
    if (!localStorage.getItem('poetry_users')) {
        localStorage.setItem('poetry_users', JSON.stringify([{ u: 'admin', p: '123456' }]));
    }

    // 注入导航右侧用户区
    const nav = document.querySelector('.nav');
    if (nav && !document.getElementById('authBox')) {
        const box = document.createElement('div');
        box.id = 'authBox';
        box.className = 'auth-box';
        nav.appendChild(box);
    }

    // 注入登录/注册模态框
    if (!document.getElementById('authMask')) {
        const mask = document.createElement('div');
        mask.id = 'authMask';
        mask.className = 'auth-mask';
        mask.innerHTML = `
            <div class="auth-modal">
                <button class="auth-close" id="authClose">×</button>
                <div class="auth-title" id="authTitle">❖ 登 录 ❖</div>
                <input class="auth-field" id="authUser" placeholder="用户名" autocomplete="off">
                <div class="pwd-wrap">
                    <input class="auth-field" id="authPass" type="password" placeholder="密码">
                    <button class="pwd-eye" id="authEye" type="button" aria-label="显示密码">👁</button>
                </div>
                <div class="pwd-hint" id="authPwdHint" style="display:none">至少 8 位，需含两种字符（字母/数字/符号）</div>
                <input class="auth-field" id="authPass2" type="password" placeholder="确认密码" style="display:none">
                <div class="auth-err" id="authErr"></div>
                <button class="auth-submit" id="authSubmit">登 录</button>
                <div class="auth-switch" id="authSwitch">没有账号？去注册</div>
                <div class="auth-hint">模拟账号：<b>admin</b> / <b>123456</b></div>
            </div>`;
        document.body.appendChild(mask);

        let mode = 'login';
        const title = document.getElementById('authTitle');
        const pass2 = document.getElementById('authPass2');
        const submit = document.getElementById('authSubmit');
        const sw = document.getElementById('authSwitch');
        const err = document.getElementById('authErr');

        document.getElementById('authClose').onclick = closeAuth;
        mask.onclick = e => { if (e.target === mask) closeAuth(); };

        function switchMode(m) {
            mode = m;
            title.innerHTML = m === 'login' ? '❖ 登 录 ❖' : '❖ 注 册 ❖';
            submit.textContent = m === 'login' ? '登 录' : '注 册';
            sw.innerHTML = m === 'login' ? '没有账号？去注册' : '已有账号？去登录';
            pass2.style.display = m === 'login' ? 'none' : 'block';
            document.getElementById('authPwdHint').style.display = m === 'login' ? 'none' : 'block';
            err.textContent = '';
        }
        // 密码防偷窥（仅切换明文/密文，图标不变）
        document.getElementById('authEye').onclick = () => {
            const p = document.getElementById('authPass');
            p.type = p.type === 'password' ? 'text' : 'password';
        };
        sw.onclick = () => switchMode(mode === 'login' ? 'register' : 'login');

        submit.onclick = () => {
            const u = document.getElementById('authUser').value.trim();
            const p = document.getElementById('authPass').value;
            const p2 = document.getElementById('authPass2').value;
            err.textContent = '';
            if (!u || !p) { err.textContent = '请输入用户名和密码'; return; }
            const users = JSON.parse(localStorage.getItem('poetry_users') || '[]');
            if (mode === 'register') {
                const pe = pwdError(p);
                if (pe) { err.textContent = pe; return; }
                if (p !== p2) { err.textContent = '两次密码不一致'; return; }
                if (users.some(x => x.u === u)) { err.textContent = '用户名已存在'; return; }
                users.push({ u, p });
                localStorage.setItem('poetry_users', JSON.stringify(users));
            } else {
                const user = users.find(x => x.u === u && x.p === p);
                if (!user) { err.textContent = '用户名或密码错误'; return; }
            }
            localStorage.setItem('poetry_user', u);
            closeAuth();
            renderAuth();
        };
    }

    // 打开登录
    window.openAuth = function () {
        document.getElementById('authMask').style.display = 'flex';
        document.getElementById('authUser').focus();
    };

    function closeAuth() {
        document.getElementById('authMask').style.display = 'none';
    }

    // 渲染导航右侧用户区（登录前：按钮；登录后：用户名 + 下拉菜单）
    function renderAuth() {
        const box = document.getElementById('authBox');
        if (!box) return;
        const cur = localStorage.getItem('poetry_user');
        if (cur) {
            box.innerHTML = `
                <div class="auth-user" id="authUserBtn">
                    <span>${cur}</span><span class="caret">▼</span>
                    <div class="auth-menu">
                        <a href="#" data-act="fav">我的收藏</a>
                        <a href="#" data-act="profile">个人中心</a>
                        <a href="#" data-act="logout">登出</a>
                    </div>
                </div>`;
            const btn = document.getElementById('authUserBtn');
            btn.onclick = e => { e.stopPropagation(); btn.classList.toggle('open'); };
            box.querySelectorAll('.auth-menu a').forEach(a => {
                a.onclick = e => {
                    e.preventDefault();
                    e.stopPropagation();
                    btn.classList.remove('open');
                    const act = a.dataset.act;
                    if (act === 'logout') {
                        localStorage.removeItem('poetry_user');
                        sessionStorage.removeItem('poetry_gate_passed');   // 清掉"已过门禁"标记，重新打开要回登录门禁
                        location.reload();   // 登出后回到登录门禁
                    } else {
                        alert(act === 'fav' ? '收藏功能待接入后端' : '个人中心待接入后端');
                    }
                };
            });
        } else {
            box.innerHTML = '<button class="auth-btn" id="authLoginBtn">用户</button>';
            document.getElementById('authLoginBtn').onclick = openAuth;
        }
    }
    // 点击其它区域收起菜单
    document.addEventListener('click', () => {
        const b = document.getElementById('authUserBtn');
        if (b) b.classList.remove('open');
    });
    window.renderAuth = renderAuth;   // 供门禁登录成功后刷新右上角
    renderAuth();
}
document.addEventListener('DOMContentLoaded', initAuth);
