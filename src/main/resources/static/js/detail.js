if (!Auth.isLoggedIn()) location.href = '/index.html';

const params  = new URLSearchParams(location.search);
const diaryId = params.get('id');
if (!diaryId) location.href = '/main.html';

let currentDiary = null; // 뒤로가기 시 날짜 복원용

// ── 뒤로가기 ─────────────────────────────────────────
document.getElementById('backBtn').addEventListener('click', () => {
    if (currentDiary?.diaryDate) {
        location.href = `/diary.html?date=${currentDiary.diaryDate}`;
    } else {
        history.back();
    }
});

// ── 상세 로드 ────────────────────────────────────────
async function loadDetail() {
    try {
        const diary = await api.get(`/api/diaries/${diaryId}`);
        currentDiary = diary;
        render(diary);
    } catch (e) {
        alert(e.message);
        location.href = '/main.html';
    }
}

function render(diary) {
    document.getElementById('detailTitle').textContent   = diary.title;
    document.getElementById('detailContent').textContent = diary.content;
    document.getElementById('detailCreated').textContent =
        `작성 ${formatDatetime(diary.createdAt)}`;

    // 수정 이력이 있을 때만 표시
    if (diary.updatedAt && diary.updatedAt !== diary.createdAt) {
        document.getElementById('detailUpdated').textContent =
            `수정 ${formatDatetime(diary.updatedAt)}`;
    }
}

// ── 수정 ─────────────────────────────────────────────
document.getElementById('editBtn').addEventListener('click', () => {
    location.href = `/write.html?id=${diaryId}`;
});

// ── 삭제 ─────────────────────────────────────────────
document.getElementById('deleteBtn').addEventListener('click', async () => {
    if (!confirm('일기를 삭제할까요?')) return;

    try {
        await api.delete(`/api/diaries/${diaryId}`);
        // 삭제 후 해당 날짜 목록으로 복귀
        location.href = currentDiary?.diaryDate
            ? `/diary.html?date=${currentDiary.diaryDate}`
            : '/main.html';
    } catch (e) {
        alert(e.message);
    }
});

// "2026-03-17T14:30:00" → "2026.03.17 14:30"
function formatDatetime(datetime) {
    if (!datetime) return '';
    const [datePart, timePart] = datetime.split('T');
    return `${datePart.replace(/-/g, '.')} ${timePart.substring(0, 5)}`;
}

loadDetail();