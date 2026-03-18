package toyProj.myDiary.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

//로그인 응답 dto
@Getter
@AllArgsConstructor
public class UserLoginResponse {
    private Long userId;
    private String nickname;
    private String accessToken; //JWT 토큰
}
