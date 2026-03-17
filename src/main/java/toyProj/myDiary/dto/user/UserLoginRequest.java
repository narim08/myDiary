package toyProj.myDiary.dto.user;

import lombok.Getter;

//로그인 요청 DTO
@Getter
public class UserLoginRequest {
    private String loginId;
    private String password;
}
