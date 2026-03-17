package toyProj.myDiary.dto.diary;

import lombok.Getter;
import toyProj.myDiary.domain.Diary;

import java.time.LocalDate;
import java.time.LocalDateTime;

//일기 상세 조회 응답 DTO
@Getter
public class DiaryDetailResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate diaryDate;

    //엔티티를 직접 외부에 노출하지 않고 DTO로 변환
    public static DiaryDetailResponse from(Diary diary) {
        DiaryDetailResponse dto = new DiaryDetailResponse();
        dto.id = diary.getId();
        dto.title = diary.getTitle();
        dto.content = diary.getContent();
        dto.createdAt = diary.getCreatedAt();
        dto.updatedAt = diary.getUpdatedAt();
        dto.diaryDate = diary.getDiaryDate();
        return dto;
    }
}
