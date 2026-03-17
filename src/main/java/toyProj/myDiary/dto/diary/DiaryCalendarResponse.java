package toyProj.myDiary.dto.diary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

//캘린더용 응답 DTO
//해당 월에 일기가 존재하는 날짜 목록 반환
//-> 프론트에서 이 날짜들에 녹색 표시
@Getter
@AllArgsConstructor
public class DiaryCalendarResponse {
    private List<LocalDate> datesWithDiary;
}
