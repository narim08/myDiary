package toyProj.myDiary.domain;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED) //기본 생성자 자동 생성
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //글 제목: null 불가
    @Column(nullable = false)
    private String title;

    //글 내용: null 불가, TEXT 타입으로 긴 내용 저장 가능
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreatedDate //최초 저장 시 자동으로 현재 시간 세팅
    @Column(updatable = false) //이후 변경 불가
    private LocalDateTime createdAt;

    @LastModifiedDate //수정 시 현재 시간 갱신
    @Column //변경 가능
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private LocalDate diaryDate; //날짜 (캘린더 구분용)

    //연관 관계의 주인: Diary가 user_id를 FK로 관리
    //LAZY: 일기 조회 시 User 정보를 즉시 불러오지 않음
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //생성자
    public static Diary create(String title, String content, LocalDate diaryDate, User user) {
        Diary diary = new Diary();
        diary.title = title;
        diary.content = content;
        diary.diaryDate = diaryDate;
        diary.user = user;
        return diary;
    }

    //수정 메서드 (제목, 내용만 변경 가능)
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        //수정 시간은 @LastModifiedDate가 자동으로 갱신함
    }
}
