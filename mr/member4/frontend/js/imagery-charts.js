const BASE_URL = 'http://localhost:8081';

const PALETTE = {
    blue: '#0071e3',
    blueSoft: '#7ab6ff',
    teal: '#2a9daf',
    tealSoft: '#7dd0dc',
    purple: '#7f5af0',
    purpleSoft: '#b7a4f4',
    green: '#2ecc71',
    orange: '#f59e0b',
    pink: '#ef476f',
    gray: '#9ca3af'
};

const TEXT_2 = '#424245';
const TEXT_3 = '#6e6e73';
const TEXT_4 = '#86868b';
const LINE = '#ececec';
const GRID = '#f1f1f3';

function $(id) {
    return document.getElementById(id);
}

function animateNumber(el, target, duration = 1400) {
    const start = 0;
    const startTime = performance.now();

    function update(now) {
        const progress = Math.min((now - startTime) / duration, 1);
        const ease = 1 - Math.pow(1 - progress, 3);
        const current = Math.floor(start + (target - start) * ease);
        el.textContent = current + (target > 99 ? '+' : '');

        if (progress < 1) {
            requestAnimationFrame(update);
        }
    }

    requestAnimationFrame(update);
}

function getTooltip(trigger = 'axis') {
    return {
        trigger,
        backgroundColor: 'rgba(255, 255, 255, 0.98)',
        borderColor: 'rgba(0, 0, 0, 0.08)',
        borderWidth: 1,
        borderRadius: 14,
        padding: [10, 14],
        textStyle: {
            color: '#1d1d1f',
            fontSize: 13
        },
        extraCssText: 'box-shadow: 0 20px 40px rgba(0,0,0,0.12);'
    };
}

function renderWordCloud() {
    const chart = echarts.init($('wordCloudChart'));

    fetch(`${BASE_URL}/api/imagery/total`)
        .then(res => res.json())
        .then(res => {
            const data = Object.entries(res.data || {}).map(([name, value]) => ({
                name,
                value
            }));

            animateNumber($('totalImageryCount'), data.length);

            chart.setOption({
                tooltip: {
                    ...getTooltip('item'),
                    formatter: '{b}：{c} 首'
                },
                series: [{
                    type: 'wordCloud',
                    shape: 'circle',
                    gridSize: 8,
                    sizeRange: [16, 56],
                    rotationRange: [-30, 30],
                    rotationStep: 15,
                    drawOutOfBound: false,
                    data,
                    textStyle: {
                        fontWeight: 500,
                        color: function () {
                            const colors = [
                                PALETTE.blue,
                                PALETTE.blueSoft,
                                PALETTE.teal,
                                PALETTE.tealSoft,
                                PALETTE.purple,
                                PALETTE.purpleSoft
                            ];
                            return colors[Math.floor(Math.random() * colors.length)];
                        }
                    },
                    emphasis: {
                        textStyle: {
                            shadowBlur: 14,
                            shadowColor: 'rgba(0, 113, 227, 0.35)'
                        }
                    }
                }]
            });
        });
}

function renderTopN() {
    const chart = echarts.init($('topNChart'));

    fetch(`${BASE_URL}/api/imagery/topn?limit=10`)
        .then(res => res.json())
        .then(res => {
            const list = res.data || [];
            const names = list.map(item => item.imagery);
            const values = list.map(item => item.count);

            chart.setOption({
                tooltip: {
                    ...getTooltip(),
                    formatter: '{b}：{c} 首'
                },
                grid: { left: 44, right: 18, top: 18, bottom: 30 },
                xAxis: {
                    type: 'category',
                    data: names,
                    axisLine: { lineStyle: { color: LINE } },
                    axisTick: { show: false },
                    axisLabel: {
                        color: TEXT_3,
                        fontSize: 12
                    }
                },
                yAxis: {
                    type: 'value',
                    splitLine: { lineStyle: { color: GRID } },
                    axisLine: { show: false },
                    axisTick: { show: false },
                    axisLabel: { color: TEXT_4, fontSize: 12 }
                },
                series: [{
                    type: 'bar',
                    data: values,
                    barWidth: '40%',
                    itemStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: '#0071e3' },
                            { offset: 1, color: '#7ab6ff' }
                        ]),
                        borderRadius: [10, 10, 0, 0]
                    }
                }]
            });
        });
}

let dynastyChart = null;
function renderDynasty(dynasty = '') {
    if (!dynastyChart) {
        dynastyChart = echarts.init($('dynastyChart'));
    }

    fetch(`${BASE_URL}/api/imagery/by-dynasty?dynasty=${dynasty}`)
        .then(res => res.json())
        .then(res => {
            const data = Object.entries(res.data || {});
            const names = data.map(item => item[0]);
            const values = data.map(item => item[1]);

            dynastyChart.setOption({
                tooltip: {
                    ...getTooltip(),
                    formatter: '{b}：{c} 首'
                },
                grid: { left: 46, right: 20, top: 20, bottom: 50 },
                dataZoom: [
                    {
                        type: 'inside',
                        start: 0,
                        end: 50
                    }
                ],
                xAxis: {
                    type: 'category',
                    data: names,
                    axisLine: { lineStyle: { color: LINE } },
                    axisTick: { show: false },
                    axisLabel: {
                        color: TEXT_3,
                        fontSize: 12,
                        rotate: 25,
                        align: 'right'
                    }
                },
                yAxis: {
                    type: 'value',
                    splitLine: { lineStyle: { color: GRID } },
                    axisLine: { show: false },
                    axisTick: { show: false },
                    axisLabel: { color: TEXT_4, fontSize: 12 }
                },
                series: [{
                    type: 'bar',
                    data: values,
                    barWidth: '34%',
                    itemStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: '#2a9daf' },
                            { offset: 1, color: '#7dd0dc' }
                        ]),
                        borderRadius: [10, 10, 0, 0]
                    }
                }]
            }, true);
        });
}

let authorChart = null;
function renderAuthor(author = '李白') {
    $('currentAuthorName').textContent = author;
    if (!authorChart) {
        authorChart = echarts.init($('authorChart'));
    }

    fetch(`${BASE_URL}/api/imagery/by-author?author=${author}`)
        .then(res => res.json())
        .then(res => {
            const data = Object.entries(res.data || {})
                .map(item => ({
                    name: item[0].split('_')[1] || item[0],
                    value: item[1]
                }))
                .sort((a, b) => b.value - a.value);

            const top = data.slice(0, 7);
            const other = data.slice(7).reduce((sum, item) => sum + item.value, 0);

            if (other > 0) {
                top.push({ name: '其他', value: other });
            }

            const palette = [
                PALETTE.blue,
                PALETTE.teal,
                PALETTE.purple,
                PALETTE.green,
                PALETTE.orange,
                PALETTE.pink,
                PALETTE.blueSoft,
                "#c8b8a8"
            ];

            authorChart.setOption({
                tooltip: {
                    ...getTooltip('item'),
                    formatter: '{b}：{c} 首（{d}%）'
                },
                legend: {
                    orient: 'horizontal',
                    bottom: 0,
                    left: 'center',
                    textStyle: {
                        color: TEXT_3,
                        fontSize: 12
                    },
                    itemWidth: 10,
                    itemHeight: 10,
                    itemGap: 14
                },
                color: palette,
                graphic: [
                    {
                        type: 'text',
                        left: 'center',
                        top: '44%',
                        style: {
                            text: author,
                            fontSize: 18,
                            fontWeight: 600,
                            fill: '#1d1d1f',
                            textAlign: 'center'
                        }
                    }
                ],
                series: [{
                    type: 'pie',
                    radius: ['46%', '68%'],
                    center: ['50%', '46%'],
                    avoidLabelOverlap: true,
                    data: top,
                    label: { show: false },
                    labelLine: { show: false },
                    itemStyle:{
                        color:function(params){
                            if(params.name === '其他'){
                                return "#c8b8a8";
                            }
                            return palette[params.dataIndex % palette.length];
                        },
                        opacity:function(params){
                            return params.name === '其他' ? 0.72 : 1;
                        }
                    },
                    emphasis: {
                        scale: true,
                        scaleSize: 8,
                        label: {
                            show: true,
                            fontSize: 13,
                            fontWeight: 500
                        }
                    }
                }]
            }, true);
        });
}

function loadMetrics() {
    animateNumber($('dynastyCount'), 4);
    animateNumber($('authorCount'), 300);
    animateNumber($('totalPoemCount'), 50000);
}

function bindEvents() {
    //朝代筛选
    document.querySelectorAll('.seg-item').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.seg-item').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            renderDynasty(btn.dataset.dynasty);
        });
    });

    //下拉切换诗人
    $('authorSelect').addEventListener('change', e => {
        $('authorInput').value = "";
        renderAuthor(e.target.value);
    });

    //自定义诗人查询按钮
    $('authorSearchBtn').addEventListener('click', ()=>{
        const name = $('authorInput').value.trim();
        if(name){
            renderAuthor(name);
        }
    })
    //回车查询
    $('authorInput').addEventListener('keydown', (e)=>{
        if(e.key === 'Enter'){
            const name = $('authorInput').value.trim();
            if(name){
                renderAuthor(name);
            }
        }
    })

    //刷新按钮
    $('refreshBtn').addEventListener('click', () => {
        renderWordCloud();
        renderTopN();
        renderDynasty();
        renderAuthor($('authorSelect').value);
    });

    window.addEventListener('resize', () => {
        dynastyChart?.resize();
        authorChart?.resize();
        echarts.getInstanceByDom($('wordCloudChart'))?.resize();
        echarts.getInstanceByDom($('topNChart'))?.resize();
    });
}

window.addEventListener('load', () => {
    loadMetrics();
    renderWordCloud();
    renderTopN();
    renderDynasty();
    renderAuthor();
    bindEvents();
});
