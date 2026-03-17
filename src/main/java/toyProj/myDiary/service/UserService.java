package toyProj.myDiary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import toyProj.myDiary.domain.User;
import toyProj.myDiary.dto.user.UserJoinRequest;
import toyProj.myDiary.dto.user.UserLoginRequest;
import toyProj.myDiary.dto.user.UserLoginResponse;
import toyProj.myDiary.repository.UserRepository;

@Service
@RequiredArgsConstructor //final 필드 생성자 자동 생성 (DI)
@Transactional(readOnly = true) //기본은 읽기 전용 트랜잭션 (성능 최적화)
public class UserService {

    private final UserRepository userRepository;

    /*
        [회원가입]
        아이디 중복 체크 후 User 엔티티 저장
        비밀번호 암호화는 추후 BCryptPasswordEncoder 적용
        @Transactional <- 오버라이드하면 read에서 write 됨
     */
    @Transactional
    public void join(UserJoinRequest request) {
        //1. 아이디 중복 검사
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        //2. 비밀번호 4자리 이상인지 검사
        if (request.getPassword().length() < 4) {
            throw new IllegalArgumentException("비밀번호는 4자리 이상이어야 합니다.");
        }

        //3. 비밀번호 암호화 (Spring Security 적용 시 passwordEncoder.encode() 사용)
        User user = User.create(
                request.getLoginId(),
                request.getPassword(), //암호화 전 임시
                request.getNickname()
        );

        //4. DB에 엔티티 저장
        userRepository.save(user);
    }

    /*
        [로그인]
        아이디/비밀번호 검증 후 응답 반환
        JWT 적용 시 토큰 생성 로직 여기에 추가
     */
    public UserLoginResponse login(UserLoginRequest request) {
        //1. 아이디 검증
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 틀렸습니다."));

        //2. 비밀번호 검증
        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 틀렸습니다.");
        }

        //3. 성공 시 응답 반환 (메인 화면에서 닉네임 사용해야 해서)
        return new UserLoginResponse(user.getId(), user.getNickname());
    }

}
