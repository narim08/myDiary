package toyProj.myDiary.dto.user;

import lombok.Getter;

/*
    회원가입 요청 DTO
    Controller가 JSON 요청 바디를 이 객체로 받음
    유효성 검증은 @Valid + @NotBlank 등으로 추후 가능
 */
@Getter
public class UserJoinRequest {
    private String loginId;
    private String password;
    private String nickname;
}
