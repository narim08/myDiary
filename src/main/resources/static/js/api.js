/**
 * API 공통 모듈
 *
 * ★ API 연동 교체 포인트 ★
 * - BASE_URL: 백엔드 서버 주소 (개발 중엔 '' 로 두면 같은 origin으로 요청)
 * - 모든 fetch 호출은 이 파일의 함수를 통해서만 진행
 * - JWT 토큰은 localStorage에 저장 후 헤더에 자동 첨부
 */

const BASE_URL = ''; // 백엔드와 같은 서버면 빈 문자열, 분리 시 'http://localhost:8080'

/** localStorage 토큰/유저 관리 */
const Auth = {
    getToken:    () => localStorage.getItem('accessToken'),
    getUserId:   () => Number(localStorage.getItem('userId')),
    getNickname: () => localStorage.getItem('nickname'),
    save: ({ accessToken, userId, nickname }) => {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('userId',      userId);
        localStorage.setItem('nickname',    nickname);
    },
    clear: () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('userId');
        localStorage.removeItem('nickname');
    },
    isLoggedIn: () => !!localStorage.getItem('accessToken'),
};

/** 공통 fetch 래퍼 */
async function request(method, path, body = null) {
    const headers = { 'Content-Type': 'application/json' };
    const token = Auth.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const res = await fetch(BASE_URL + path, {
        method,
        headers,
        credentials: 'include', //CORS 자격증명 포함
        body: body ? JSON.stringify(body) : null,
    });

    // 인증 만료 시 로그인 화면으로
    if (res.status === 403 || res.status === 401) {
        Auth.clear();
        location.href = '/index.html';
        return;
    }

    const text = await res.text();

    if (!res.ok) { //응답 바디가 없는 경우 (204 No Content 등) 처리
        // GlobalExceptionHandler가 내려준 { message: "..." } 형태
        const data = text ? JSON.parse(text) : null;
        throw new Error(data?.message || '오류가 발생했습니다.');
    }
    try { //성공 응답: JSON 파싱 시도, 실패하면 텍스트 그대로 반환
        return text ? JSON.parse(text) : null;
    } catch {
        return text; // "수정되었습니다." 같은 순수 문자열은 그냥 반환
    }

    return data;
}

const api = {
    post:   (path, body) => request('POST',   path, body),
    get:    (path)       => request('GET',    path),
    put:    (path, body) => request('PUT',    path, body),
    delete: (path)       => request('DELETE', path),
};