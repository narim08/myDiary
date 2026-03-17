package toyProj.myDiary.dto.diary;

import lombok.Getter;

//일기 수정 요청 DTO
@Getter
public class DiaryUpdateRequest {
    private String title;
    private String content;
    //날짜는 수정 불가
}
