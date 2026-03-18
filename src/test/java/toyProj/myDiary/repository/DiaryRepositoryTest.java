package toyProj.myDiary.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import toyProj.myDiary.domain.Diary;
import toyProj.myDiary.domain.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/*
    Repository 슬라이스 테스트

     @DataJpaTest
     -JPA 관련 Bean만 로딩 (전체 Context X) → 빠름
     -H2 인메모리 DB 자동 사용
     -각 테스트마다 @Transactional 적용 → 테스트 후 자동 롤백

     TestEntityManager
     -테스트용 EntityManager
     -persist()로 직접 데이터 저장 (Repository 거치지 않고 픽스처 세팅할 때 유용)
 */
@DataJpaTest
class DiaryRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        //테스트용 유저 저장
        user = User.create("hong", "encoded", "홍길동");
        em.persist(user);
    }

    @Test
    @DisplayName("날짜별 일기 목록 조회 - 최신순 정렬 확인")
    void findByUserIdAndDiaryDate_orderedByCreatedAtDesc() {
        //given
        LocalDate date = LocalDate.of(2026, 3, 18);
        Diary diary1 = Diary.create("첫번째 일기", "내용1", date, user);
        Diary diary2 = Diary.create("두번째 일기", "내용2", date, user); //테스트 실패해서 repository 조건에 id 정렬 추가
        em.persist(diary1);
        em.persist(diary2);
        em.flush(); //영속성 컨텍스트 → DB 반영

        //when
        List<Diary> result = diaryRepository
                .findByUserIdAndDiaryDateOrderByCreatedAtDescIdDesc(user.getId(), date);

        //then
        assertThat(result).hasSize(2);
        //나중에 저장된 diary2가 최신순으로 앞에 와야 함
        assertThat(result.get(0).getTitle()).isEqualTo("두번째 일기");
        assertThat(result.get(1).getTitle()).isEqualTo("첫번째 일기");
    }

    @Test
    @DisplayName("다른 날짜의 일기는 조회되지 않음")
    void findByUserIdAndDiaryDate_differentDate_notIncluded() {
        // given
        Diary diary1 = Diary.create("오늘 일기", "내용", LocalDate.of(2026, 3, 17), user);
        Diary diary2 = Diary.create("어제 일기", "내용", LocalDate.of(2026, 3, 16), user);
        em.persist(diary1);
        em.persist(diary2);
        em.flush();

        //when
        List<Diary> result = diaryRepository
                .findByUserIdAndDiaryDateOrderByCreatedAtDescIdDesc(user.getId(), LocalDate.of(2026, 3, 17));

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("오늘 일기");
    }

    @Test
    @DisplayName("다른 유저의 일기는 조회되지 않음")
    void findByUserIdAndDiaryDate_otherUser_notIncluded() {
        //given
        User otherUser = User.create("other", "encoded", "다른사람");
        em.persist(otherUser);

        LocalDate date = LocalDate.of(2026, 3, 17);
        em.persist(Diary.create("내 일기", "내용", date, user));
        em.persist(Diary.create("남의 일기", "내용", date, otherUser));
        em.flush();

        //when
        List<Diary> result = diaryRepository
                .findByUserIdAndDiaryDateOrderByCreatedAtDescIdDesc(user.getId(), date);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("내 일기");
    }

    @Test
    @DisplayName("캘린더 월별 일기 날짜 조회 - 중복 없이 반환")
    void findDiaryDatesByUserAndYearMonth() {
        //given: 같은 날짜에 일기 2개, 다른 날짜에 1개
        LocalDate date1 = LocalDate.of(2026, 3, 17);
        LocalDate date2 = LocalDate.of(2026, 3, 20);
        em.persist(Diary.create("일기1", "내용", date1, user));
        em.persist(Diary.create("일기2", "내용", date1, user)); // 같은 날 2개
        em.persist(Diary.create("일기3", "내용", date2, user));
        em.flush();

        //when
        List<LocalDate> result = diaryRepository
                .findDiaryDatesByUserAndYearMonth(user.getId(), 2026, 3);

        //then
        //DISTINCT 쿼리이므로 date1은 1번만 포함되어야 함
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(date1, date2);
    }

}
