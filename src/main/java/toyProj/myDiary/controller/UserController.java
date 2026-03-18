package toyProj.myDiary.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import toyProj.myDiary.dto.user.UserJoinRequest;
import toyProj.myDiary.dto.user.UserLoginRequest;
import toyProj.myDiary.dto.user.UserLoginResponse;
import toyProj.myDiary.service.UserService;

import java.util.Map;

/*
    [사용자 관련 API]
    POST /api/users/join    : 회원가입
    POST /api/users/login   : 로그인
 */

/*
    [전체 인증 흐름 정리]
    1. 회원가입
    POST /api/users/join
    { "loginId": "hong", "password": "1234", "nickname": "홍길동" }
    → 비밀번호 BCrypt 암호화 후 DB 저장

    2. 로그인
    POST /api/users/login
    { "loginId": "hong", "password": "1234" }
    → 응답: { "userId": 1, "nickname": "홍길동", "accessToken": "eyJhbGc..." }

    3. 이후 모든 API 요청
    Header: Authorization: Bearer eyJhbGc...
    → JwtAuthenticationFilter가 토큰 검증
    → SecurityContext에 userId=1 저장
    → Controller에서 @AuthenticationPrincipal Long userId 로 꺼내 씀

    4. 토큰 없이 /api/diaries 접근 시
    → 403 Forbidden (SecurityConfig의 anyRequest().authenticated() 에 막힘)
 */

/*
    [핵심 포인트 정리]

    STATELESS: 세션 방식은 서버가 로그인 상태를 메모리에 저장하지만,
    JWT는 토큰 자체에 정보가 담겨 있어 서버는 아무것도 저장x -> 덕분에 서버 여러 대 늘려도 인증 끊기지x

    BCrypt matches() 원리: encode("1234")는 매번 다른 해시값을 만들지만,
    matches("1234", 해시값)은 내부적으로 salt를 분리해서 비교하기 때문에 항상 올바르게 검증됨.(직접 == 비교 불가)

    @AuthenticationPrincipal 동작 이유:
    필터에서 new UsernamePasswordAuthenticationToken(userId, ...)로 저장할 때마다
    첫번째 인자가 principal인데, 이 값을 @AuthenticationPrincipal가 그대로 꺼내줌

 */

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor //final 필드 생성자 자동 생성 (DI)
public class UserController {

    //서비스 로직 사용
    private final UserService userService;

    //회원가입
    @PostMapping("/join")
    public ResponseEntity<Map<String, String>> join(@RequestBody UserJoinRequest request) {
        userService.join(request); //dto로 담아서 전달하고 dto로 받음
        return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));
        //return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    //로그인: JWT 적용 후, 응답 바디에 acessToken 포함 예정
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        UserLoginResponse response = userService.login(request); //dto로 담아서 전달하고 dto로 받음
        return ResponseEntity.ok(response);
    }
}
