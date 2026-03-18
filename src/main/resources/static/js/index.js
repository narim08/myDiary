// 이미 로그인된 상태면 메인으로
if (Auth.isLoggedIn()) location.href = '/main.html';

// ── 로고 애니메이션 ──────────────────────────────────
const logo      = document.getElementById('logo');
const loginCard = document.getElementById('loginCard');

setTimeout(() => {
    logo.classList.add('shrink');        // 로고 작아지며 위로
    setTimeout(() => {
        loginCard.classList.add('show'); // 카드 페이드인
    }, 400);
}, 1000);

// ── 회원가입 패널 토글 ───────────────────────────────
document.getElementById('toggleJoin').addEventListener('click', () => {
    document.getElementById('joinPanel').classList.toggle('open');
});

// ── 로그인 ───────────────────────────────────────────
document.getElementById('loginBtn').addEventListener('click', login);
document.getElementById('password').addEventListener('keydown', e => {
    if (e.key === 'Enter') login();
});

async function login() {
    const loginId  = document.getElementById('loginId').value.trim();
    const password = document.getElementById('password').value;
    const errorEl  = document.getElementById('loginError');

    if (!loginId || !password) {
        showError(errorEl, '아이디와 비밀번호를 입력해주세요.');
        return;
    }

    try {
        const res = await api.post('/api/users/login', { loginId, password });
        Auth.save(res);            // { userId, nickname, accessToken } 저장
        location.href = '/main.html';
    } catch (e) {
        showError(errorEl, e.message);
    }
}

// ── 회원가입 ─────────────────────────────────────────
document.getElementById('joinBtn').addEventListener('click', join);

async function join() {
    const loginId  = document.getElementById('joinId').value.trim();
    const password = document.getElementById('joinPw').value;
    const nickname = document.getElementById('joinNickname').value.trim();
    const errorEl  = document.getElementById('joinError');

    if (!loginId || !password || !nickname) {
        showError(errorEl, '모든 항목을 입력해주세요.');
        return;
    }

    try {
        await api.post('/api/users/join', { loginId, password, nickname });
        alert('회원가입 완료! 로그인해주세요 🎉');
        document.getElementById('joinPanel').classList.remove('open');
        document.getElementById('joinId').value       = '';
        document.getElementById('joinPw').value       = '';
        document.getElementById('joinNickname').value = '';
    } catch (e) {
        showError(errorEl, e.message);
    }
}

function showError(el, msg) {
    el.textContent = msg;
    el.classList.add('show');
    setTimeout(() => el.classList.remove('show'), 3000);
}