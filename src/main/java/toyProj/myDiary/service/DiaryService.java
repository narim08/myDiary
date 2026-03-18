package toyProj.myDiary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import toyProj.myDiary.domain.Diary;
import toyProj.myDiary.domain.User;
import toyProj.myDiary.dto.diary.*;
import toyProj.myDiary.repository.DiaryRepository;
import toyProj.myDiary.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    //캘린더 월별로 일기 등록한 날짜 조회
    //해당 월에 일기가 있는 날짜 목록 반환 -> 프론트에서 녹색으로 표시
    public DiaryCalendarResponse getCalendarDates(Long userId, int year, int month) {
        List<LocalDate> dates = diaryRepository
                .findDiaryDatesByUserAndYearMonth(userId, year, month);
        return new DiaryCalendarResponse(dates);
    }

    //해당 날짜 별 일기 전체 목록 조회
    public List<DiaryListResponse> getDiariesByDate(Long userId, LocalDate date) {
        return diaryRepository
                .findByUserIdAndDiaryDateOrderByCreatedAtDescIdDesc(userId, date)
                .stream()
                .map(DiaryListResponse::from) //엔티티 -> DTO 변환
                .collect(Collectors.toList());
    }

    //일기 상세 조회 (본인 것만 조회 가능)
    public DiaryDetailResponse getDiary(Long userId, Long diaryId) {
        Diary diary = findDiaryWithOwnerCheck(userId, diaryId);
        return DiaryDetailResponse.from(diary);
    }

    //일기 작성
    //userId는 JWT 인증 후 SecurityContext에서 꺼내올 예정 (현재는 임시로 파라미터로 직접 받음)
    @Transactional
    public Long create(Long userId, DiaryCreateRequest request) {
        //1. 사용자 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        //2. 일기 생성
        Diary diary = Diary.create(
                request.getTitle(),
                request.getContent(),
                request.getDiaryDate(),
                user
        );

        //3. DB에 엔티티 저장하고 일기 id 받아서 컨트롤러에 넘김
        return diaryRepository.save(diary).getId();
    }

    //일기 수정
    //Dirty Checking: 트랜잭션 안에서 엔티티 수정 시,
    //별도 save() 호출 없이 자동으로 UPDATE 쿼리 실행\
    @Transactional
    public void update(Long userId, Long diaryId, DiaryUpdateRequest request) {
        Diary diary = findDiaryWithOwnerCheck(userId, diaryId);
        diary.update(request.getTitle(), request.getContent());
        //save() 호출 불필요 -> Dirty Checking이 처리
    }

    //일기 삭제
    @Transactional
    public void delete(Long userId, Long diaryId) {
        Diary diary = findDiaryWithOwnerCheck(userId, diaryId);
        diaryRepository.delete(diary);
    }

    //공통 내부 메서드: 일기 조회 + 사용자 검증 (다른 사용자의 일기 접근 방지)
    private Diary findDiaryWithOwnerCheck(Long userId, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        if (!diary.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 글만 접근할 수 있습니다.");
        }

        return diary;
    }



}
