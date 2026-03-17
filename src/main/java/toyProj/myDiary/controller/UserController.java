package toyProj.myDiary.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import toyProj.myDiary.dto.user.UserJoinRequest;
import toyProj.myDiary.dto.user.UserLoginRequest;
import toyProj.myDiary.dto.user.UserLoginResponse;
import toyProj.myDiary.service.UserService;

/*
    사용자 관련 API
    POST /api/users/join    : 회원가입
    POST /api/users/login   : 로그인
 */

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor //final 필드 생성자 자동 생성 (DI)
public class UserController {

    //서비스 로직 사용
    private final UserService userService;

    //회원가입
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestParam UserJoinRequest request) {
        userService.join(request); //dto로 담아서 전달하고 dto로 받음
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    //로그인: JWT 적용 후, 응답 바디에 acessToken 포함 예정
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        UserLoginResponse response = userService.login(request); //dto로 담아서 전달하고 dto로 받음
        return ResponseEntity.ok(response);
    }
}
