if (!Auth.isLoggedIn()) location.href = '/index.html';

const params  = new URLSearchParams(location.search);
const date    = params.get('date');   // 작성 모드: 날짜
const diaryId = params.get('id');     // 수정 모드: 일기 id
const isEdit  = !!diaryId;

// 날짜 라벨
if (date) {
    const [y, m, d] = date.split('-');
    document.getElementById('writeDateLabel').textContent = `${y}년 ${m}월 ${d}일`;
}

// ── 수정 모드: 기존 데이터 로드 ──────────────────────
if (isEdit) {
    (async () => {
        try {
            const diary = await api.get(`/api/diaries/${diaryId}`);
            document.getElementById('titleInput').value   = diary.title;
            document.getElementById('contentInput').value = diary.content;

            // 날짜 라벨 (수정 모드에서는 diary에서 읽어옴)
            const [y, m, d] = diary.diaryDate.split('-');
            document.getElementById('writeDateLabel').textContent = `${y}년 ${m}월 ${d}일`;
        } catch (e) {
            alert(e.message);
            history.back();
        }
    })();
}

// ── 취소 ─────────────────────────────────────────────
document.getElementById('cancelBtn').addEventListener('click', () => {
    history.back(); // 이전 화면으로
});

// ── 저장 ─────────────────────────────────────────────
document.getElementById('saveBtn').addEventListener('click', save);

async function save() {
    const title   = document.getElementById('titleInput').value.trim();
    const content = document.getElementById('contentInput').value.trim();

    if (!title)   { alert('제목을 입력해주세요.'); return; }
    if (!content) { alert('내용을 입력해주세요.'); return; }

    try {
        if (isEdit) {
            // 수정 모드
            await api.put(`/api/diaries/${diaryId}`, { title, content });
            location.href = `/detail.html?id=${diaryId}`;
        } else {
            // 작성 모드
            await api.post('/api/diaries', { title, content, diaryDate: date });
            location.href = `/diary.html?date=${date}`;
        }
    } catch (e) {
        alert(e.message);
    }
}