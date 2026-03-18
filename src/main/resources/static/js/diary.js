if (!Auth.isLoggedIn()) location.href = '/index.html';

// URL에서 날짜 파싱: /diary.html?date=2026-03-17
const params = new URLSearchParams(location.search);
const date   = params.get('date'); // "2026-03-17"

if (!date) location.href = '/main.html';

// 날짜 표시 (2026년 3월 17일 형태)
const [y, m, d] = date.split('-');
document.getElementById('dateTitle').textContent = `${y}년 ${m}월 ${d}일`;

// 글쓰기 버튼: 날짜 정보 넘겨서 write.html로
document.getElementById('writeBtn').addEventListener('click', () => {
    location.href = `/write.html?date=${date}`;
});

// ── 목록 로드 ────────────────────────────────────────
async function loadList() {
    try {
        const list = await api.get(`/api/diaries?date=${date}`);
        renderList(list);
    } catch (e) {
        document.getElementById('diaryList').innerHTML =
            `<p style="color:#e74c3c; text-align:center; margin-top:40px;">${e.message}</p>`;
    }
}

function renderList(list) {
    const container = document.getElementById('diaryList');

    if (!list || list.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="emoji">📭</div>
                <p>아직 작성된 일기가 없어요</p>
                <p style="font-size:13px; margin-top:8px;">오늘의 이야기를 기록해보세요 ✨</p>
            </div>`;
        return;
    }

    container.innerHTML = list.map(item => `
        <div class="diary-item" onclick="location.href='/detail.html?id=${item.id}'">
            <span class="item-title">${escapeHtml(item.title)}</span>
            <span class="item-time">${formatTime(item.createdAt)}</span>
        </div>
    `).join('');
}

// "2026-03-17T14:30:00" → "14:30"
function formatTime(datetime) {
    if (!datetime) return '';
    return datetime.substring(11, 16);
}

// XSS 방지: 사용자 입력 문자열 HTML 이스케이프
function escapeHtml(str) {
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

loadList();