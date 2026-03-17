package toyProj.myDiary.dto.diary;

import lombok.Getter;
import toyProj.myDiary.domain.Diary;

import java.time.LocalDateTime;

//일기 목록 응답 DTO
//목록에서는 제목과 작성 시간만 노출 (내용은 제외)
//엔티티 -> DTO 변환은 정적 팩토리 메서드로
@Getter
public class DiaryListResponse {
    private Long id;
    private String title;
    private LocalDateTime createdAt;

    //엔티티를 직접 외부에 노출하지 않고 DTO로 변환
    public static DiaryListResponse from(Diary diary) {
        DiaryListResponse dto = new DiaryListResponse();
        dto.id = diary.getId();
        dto.title = diary.getTitle();
        dto.createdAt = diary.getCreatedAt();
        return dto;
    }
}
