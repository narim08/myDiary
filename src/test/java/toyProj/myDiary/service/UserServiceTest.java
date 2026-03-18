package toyProj.myDiary.service;

/*
    단위 테스트 (Unit Test)
      └── Service 테스트
            - 외부 의존성(DB, 네트워크)을 Mockito로 가짜 객체로 대체
            - 비즈니스 로직만 빠르게 검증

    슬라이스 테스트 (Slice Test)
      └── Controller 테스트 (@WebMvcTest)
            - 실제 HTTP 요청/응답 형태로 검증
            - Service는 Mock으로 대체

    통합 테스트 (Integration Test)
      └── Repository 테스트 (@DataJpaTest)
            - 실제 DB(인메모리 H2)로 JPA 쿼리 검증
 */

/*
    [UserService 단위 테스트]
    @ExtendWith(MockitoExtension.class)
    → JUnit5 + Mockito 연동. Spring Context를 띄우지 않아서 빠름.
    @Mock      : 가짜 객체 생성 (실제 동작 없음, 기본 반환값 null/false/0)
    @InjectMocks: @Mock으로 만든 객체들을 주입받는 테스트 대상

 */

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import toyProj.myDiary.domain.User;
import toyProj.myDiary.dto.user.UserJoinRequest;
import toyProj.myDiary.dto.user.UserLoginRequest;
import toyProj.myDiary.dto.user.UserLoginResponse;
import toyProj.myDiary.repository.UserRepository;
import toyProj.myDiary.security.JwtTokenProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*; //given, then

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks //UsersService 생성자에 위 @Mock들을 자동 주입
    private UserService userService;

    /* 회원가입 테스트 */
    @Test
    @DisplayName("회원가입 성공")
    void join_success() {
        //given
        UserJoinRequest request = createJoinRequest("hong", "1234", "홍길동");
        given(userRepository.existsByLoginId("hong")).willReturn(false); // 중복 없음
        given(passwordEncoder.encode("1234")).willReturn("encoded1234");
        given(userRepository.save(any(User.class))).willReturn(any());

        //when (실행)
        assertThatCode(() -> userService.join(request))
                .doesNotThrowAnyException(); // 예외 없이 통과하면 성공

        //then (검증)
        //save()가 정확히 1번 호출됐는지 확인
        then(userRepository).should(times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 아이디 중복")
    void join_fail_duplicateLoginId() {
        //given
        UserJoinRequest request = createJoinRequest("hong", "1234", "홍길동");
        //existsByLoginId가 true를 반환하도록 설정 (이미 존재하는 아이디)
        given(userRepository.existsByLoginId("hong")).willReturn(true);

        //when & then
        //join() 호출 시 IllegalArgumentException이 던져지는지 확인
        assertThatThrownBy(() -> userService.join(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 아이디입니다.");
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 4자리 미만")
    void join_fail_shortPassword() {
        //given
        UserJoinRequest request = createJoinRequest("hong", "123", "홍길동"); // 3자리
        given(userRepository.existsByLoginId("hong")).willReturn(false);

        //when & then
        assertThatThrownBy(() -> userService.join(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 4자리 이상이어야 합니다.");
    }


    /* 로그인 테스트 */
    @Test
    @DisplayName("로그인 성공 - 토큰 반환")
    void login_success() {
        //given
        UserLoginRequest request = createLoginRequest("hong", "1234");
        User fakeUser = User.create("hong", "encoded1234", "홍길동");
        setUserId(fakeUser, 1L); //리플렉션으로 id 세팅

        given(userRepository.findByLoginId("hong")).willReturn(Optional.of(fakeUser));
        given(passwordEncoder.matches("1234", "encoded1234")).willReturn(true);
        given(jwtTokenProvider.createToken(1L)).willReturn("fake.jwt.token");

        //when
        UserLoginResponse response = userService.login(request);

        //then
        assertThat(response.getNickname()).isEqualTo("홍길동");
        assertThat(response.getAccessToken()).isEqualTo("fake.jwt.token");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 아이디")
    void login_fail_userNotFound() {
        // given
        UserLoginRequest request = createLoginRequest("nobody", "1234");
        given(userRepository.findByLoginId("nobody")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디 또는 비밀번호가 틀렸습니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_wrongPassword() {
        // given
        UserLoginRequest request = createLoginRequest("hong", "wrong");
        User fakeUser = User.create("hong", "encoded1234", "홍길동");

        given(userRepository.findByLoginId("hong")).willReturn(Optional.of(fakeUser));
        given(passwordEncoder.matches("wrong", "encoded1234")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디 또는 비밀번호가 틀렸습니다.");
    }


    /* 헬퍼 메서드 */
    private UserJoinRequest createJoinRequest(String loginId, String password, String nickname) {
        UserJoinRequest request = new UserJoinRequest();
        //DTO 필드가 private라서 리플렉션 사용
        setField(request, "loginId", loginId);
        setField(request, "password", password);
        setField(request, "nickname", nickname);
        return request;
    }

    private UserLoginRequest createLoginRequest(String loginId, String password) {
        UserLoginRequest request = new UserLoginRequest();
        setField(request, "loginId", loginId);
        setField(request, "password", password);
        return request;
    }

    //리플렉션으로 private 필드 값 강제 세팅
    //테스트에서 DTO/엔티티의 private 필드를 직접 설정할 때 사용 (실제 코드에선 절대 사용x)
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field =  target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void setUserId(User user, Long id) {
        setField(user, "id", id);
    }
}
