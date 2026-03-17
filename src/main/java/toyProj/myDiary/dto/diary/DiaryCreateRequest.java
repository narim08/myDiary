package toyProj.myDiary.dto.diary;

import lombok.Getter;
import java.time.LocalDate;

//일기 작성 요청 DTO
@Getter
public class DiaryCreateRequest {
    private String title;
    private String content;
    private LocalDate diaryDate; //캘린더에서 선택한 날짜
}
