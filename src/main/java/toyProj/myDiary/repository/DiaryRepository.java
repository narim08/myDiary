package toyProj.myDiary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import toyProj.myDiary.domain.Diary;

import java.time.LocalDate;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    //메인 화면 캘린더에서 일기가 존재하는 날짜에만 녹색 표시할 때 사용
    //특정 사용자의 일기가 존재하는 날짜 목록
    @Query("SELECT DISTINCT d.diaryDate FROM Diary d WHERE d.user.id = :userId AND YEAR(d.diaryDate) = :year AND MONTH(d.diaryDate) = :month")
    List<LocalDate> findDiaryDatesByUserAndYearMonth(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month
    );


    //메인 화면에서 캘린더 날짜 클릭 시, 해당 날짜에 전체 글 조회할 때 사용
    //특정 사용자의 특정 날짜 일기 목록 (최신순 정렬)
    List<Diary> findByUserIdAndDiaryDateOrderByCreatedAtDescIdDesc(Long userId, LocalDate diaryDate);

}
