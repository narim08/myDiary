// 비로그인 접근 차단
if (!Auth.isLoggedIn()) location.href = '/index.html';

// 헤더 닉네임 세팅
document.getElementById('headerTitle').textContent = `${Auth.getNickname()} Diary`;

// 로그아웃
document.getElementById('logoutBtn').addEventListener('click', () => {
    Auth.clear();
    location.href = '/index.html';
});

// ── 캘린더 상태 ──────────────────────────────────────
const today = new Date();
let   current = new Date(today.getFullYear(), today.getMonth(), 1); // 현재 보는 달의 1일
let   diaryDates = new Set(); // 해당 월에 일기 있는 날짜 Set

document.getElementById('prevMonth').addEventListener('click', () => {
    current = new Date(current.getFullYear(), current.getMonth() - 1, 1);
    loadCalendar();
});
document.getElementById('nextMonth').addEventListener('click', () => {
    current = new Date(current.getFullYear(), current.getMonth() + 1, 1);
    loadCalendar();
});

// ── 캘린더 로드 ──────────────────────────────────────
async function loadCalendar() {
    const year  = current.getFullYear();
    const month = current.getMonth() + 1; // JS는 0-indexed

    // 월 라벨 업데이트
    document.getElementById('monthLabel').textContent =
        `${year}년 ${month}월`;

    // API: 해당 월 일기 있는 날짜 목록
    try {
        const res = await api.get(
            `/api/diaries/calendar?year=${year}&month=${month}`
        );
        // ["2026-03-17", "2026-03-20"] 형태로 온다고 가정
        diaryDates = new Set(res.datesWithDiary);
    } catch {
        diaryDates = new Set();
    }

    renderCalendar(year, month);
}

// ── 캘린더 렌더링 ─────────────────────────────────────
function renderCalendar(year, month) {
    const grid      = document.getElementById('calGrid');
    const firstDay  = new Date(year, month - 1, 1).getDay(); // 첫 날 요일 (0=일)
    const lastDate  = new Date(year, month, 0).getDate();    // 해당 월 마지막 날짜
    const todayStr  = toDateStr(today);

    grid.innerHTML = '';

    // 빈 칸 (첫 주 앞쪽)
    for (let i = 0; i < firstDay; i++) {
        const empty = document.createElement('div');
        empty.className = 'cal-day empty';
        grid.appendChild(empty);
    }

    // 날짜 셀
    for (let d = 1; d <= lastDate; d++) {
        const dateStr = toDateStr(new Date(year, month - 1, d));
        const cell    = document.createElement('div');
        cell.className = 'cal-day';
        cell.textContent = d;

        if (dateStr === todayStr)         cell.classList.add('today');
        else if (diaryDates.has(dateStr)) cell.classList.add('has-diary');

        // 날짜 클릭 → 일기 목록 화면으로
        cell.addEventListener('click', () => {
            location.href = `/diary.html?date=${dateStr}`;
        });

        grid.appendChild(cell);
    }
}

// "2026-03-17" 형태 문자열 변환
function toDateStr(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

// 초기 로드
loadCalendar();