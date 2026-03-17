package toyProj.myDiary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import toyProj.myDiary.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /*JPA는 형식만 맞으면 이름 지어서 해도 알아서 처리해줌
        1) 함수명부터 작성: find, exist 등
        2) 그러면 관련 함수 타입 자동 완성 (Id (PK))
        3) 필요한 변수명으로 바꾸고 override 삭제, 인자도 변경
        !) 기존 형식에 없는 건 따로 SQL 쿼리 작성해야 함
    */

    //로그인 시 아이디로 사용자 조회
    Optional<User> findByLoginId(String loginId);

    //회원가입 시 아이디 중복 체크
    boolean existsByLoginId(String loginId);
}
