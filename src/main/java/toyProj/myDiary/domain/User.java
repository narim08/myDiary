package toyProj.myDiary.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;


@Entity //jpa 켜야 사용 가능
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //기본 생성자 자동 생성, 다른 곳(서비스)에서 new 생성 불가
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동 증가++
    private Long id; //PK

    //로그인 ID: null 불가, 중복 불가, 길이 50
    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    //비밀번호: null 불가 (이후 Spring Security BCrypt 사용)
    @Column(nullable = false)
    private String password;

    //닉네임: null 불가, 길이 50
    @Column(nullable = false, length = 50)
    private String nickname;

    //mappedBy로 Diary 엔티티의 'user' 필드가 주인임을 알림(알림용이라 Column 지정x)
    //cascade: User 삭제 시 연관된 Diary도 함께 삭제
    //orphanRemoval: 컬렉션에서 제거된 Diary도 DB에서 삭제
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Diary> diaries = new ArrayList<>();

    //기본 말고 세팅용 생성자(회원 가입) -> 정적 팩토리 메서드 패턴 사용 권장
    public static User create(String loginId, String password, String nickname) {
        User user = new User();
        user.loginId = loginId;
        user.password = password;
        user.nickname = nickname;
        return user;
    }


}
