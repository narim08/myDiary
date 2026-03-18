package toyProj.myDiary.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import toyProj.myDiary.domain.Diary;
import toyProj.myDiary.domain.User;
import toyProj.myDiary.dto.diary.DiaryCreateRequest;
import toyProj.myDiary.dto.diary.DiaryDetailResponse;
import toyProj.myDiary.dto.diary.DiaryListResponse;
import toyProj.myDiary.dto.diary.DiaryUpdateRequest;
import toyProj.myDiary.repository.DiaryRepository;
import toyProj.myDiary.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DiaryService diaryService;

    //테스트 전체에서 공통으로 쓸 픽스처
    private User fakeUser;
    private Diary fakeDiary;

    /**
        @BeforeEach: 각 @Test 메서드 실행 전 매번 호출됨
        -> 테스트 간 상태가 공유되지 않도록 픽스처를 새로 만들어야 함
     */
    @BeforeEach
    void setUp() {
        fakeUser = User.create("hong", "encoded1234", "홍길동");
        setField(fakeUser, "id", 1L);

        fakeDiary = Diary.create("테스트 제목", "테스트 내용", LocalDate.of(2026, 3, 18), fakeUser);
        setField(fakeDiary, "id", 10L);
    }

    /* 일기 작성 */
    @Test
    @DisplayName("일기 작성 성공 - 저장된 일기 id 반환")
    void create_success() {
        //given
        DiaryCreateRequest request = createDiaryRequest("제목", "내용", LocalDate.of(2026, 3, 18));

        given(userRepository.findById(1L)).willReturn(Optional.of(fakeUser));
        given(diaryRepository.save(any(Diary.class))).willReturn(fakeDiary);

        //when
        Long savedId = diaryService.create(1L, request);

        //then
        assertThat(savedId).isEqualTo(10L);
        then(diaryRepository).should(times(1)).save(any(Diary.class));
    }

    @Test
    @DisplayName("일기 작성 실패 - 존재하지 않는 사용자")
    void create_fail_userNotFound() {
        //given
        DiaryCreateRequest request = createDiaryRequest("제목", "내용", LocalDate.of(2026, 3, 18));
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> diaryService.create(999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
    }

    /* 날짜별 목록 조회 */
    @Test
    @DisplayName("날짜별 일기 목록 조회 성공")
    void getDiariesByDate_success() {
        //given
        LocalDate date = LocalDate.of(2026, 3, 18);
        Diary diary2 = Diary.create("두번째", "내용2", date, fakeUser);
        setField(diary2, "id", 11L);
        given(diaryRepository.findByUserIdAndDiaryDateOrderByCreatedAtDescIdDesc(1L, date))
                .willReturn(List.of(fakeDiary, diary2));

        //when
        List<DiaryListResponse> result = diaryService.getDiariesByDate(1L, date);

        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 제목");
        assertThat(result.get(1).getTitle()).isEqualTo("두번째");
    }

    @Test
    @DisplayName("날짜별 일기 목록 조회 - 해당 날짜에 일기 없으면 빈 리스트")
    void getDiariesByDate_empty() {
        //given
        LocalDate date = LocalDate.of(2026, 1, 1);
        given(diaryRepository.findByUserIdAndDiaryDateOrderByCreatedAtDescIdDesc(1L, date))
                .willReturn(List.of());

        //when
        List<DiaryListResponse> result = diaryService.getDiariesByDate(1L, date);

        //then
        assertThat(result).isEmpty();
    }

    /* 상세 조회 */
    @Test
    @DisplayName("일기 상세 조회 성공")
    void getDiary_success() {
        //given
        given(diaryRepository.findById(10L)).willReturn(Optional.of(fakeDiary));

        //when
        DiaryDetailResponse result = diaryService.getDiary(1L, 10L);

        //then
        assertThat(result.getTitle()).isEqualTo("테스트 제목");
        assertThat(result.getContent()).isEqualTo("테스트 내용");
    }

    @Test
    @DisplayName("일기 상세 조회 실패 - 다른 사용자의 일기 접근")
    void getDiary_fail_notOwner() {
        //given
        given(diaryRepository.findById(10L)).willReturn(Optional.of(fakeDiary));

        //when & then
        //fakeDiary의 userId는 1L인데, 2L로 접근 시도
        assertThatThrownBy(() -> diaryService.getDiary(2L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인의 글만 접근할 수 있습니다.");
    }

    /* 수정 */
    @Test
    @DisplayName("일기 수정 성공 - Dirty Checking으로 save() 호출 없이 수정")
    void update_success() {
        //given
        DiaryUpdateRequest request = new DiaryUpdateRequest();
        setField(request, "title", "수정된 제목");
        setField(request, "content", "수정된 내용");

        given(diaryRepository.findById(10L)).willReturn(Optional.of(fakeDiary));

        //when
        diaryService.update(1L, 10L, request);

        //then: 엔티티의 값이 실제로 변경됐는지 확인
        assertThat(fakeDiary.getTitle()).isEqualTo("수정된 제목");
        assertThat(fakeDiary.getContent()).isEqualTo("수정된 내용");

        //Dirty Checking 방식이므로 save()는 호출되면 안 됨
        then(diaryRepository).should(never()).save(any());
    }

    /* 삭제 */
    @Test
    @DisplayName("일기 삭제 성공")
    void delete_success() {
        //given
        given(diaryRepository.findById(10L)).willReturn(Optional.of(fakeDiary));

        //when
        diaryService.delete(1L, 10L);

        //then
        then(diaryRepository).should(times(1)).delete(fakeDiary);
    }

    @Test
    @DisplayName("일기 삭제 실패 - 존재하지 않는 일기")
    void delete_fail_diaryNotFound() {
        //given
        given(diaryRepository.findById(999L)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> diaryService.delete(1L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 글입니다.");
    }


    /* 헬퍼 메서드 */
    private DiaryCreateRequest createDiaryRequest(String title, String content, LocalDate date) {
        DiaryCreateRequest req = new DiaryCreateRequest();
        setField(req, "title", title);
        setField(req, "content", content);
        setField(req, "diaryDate", date);
        return req;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
