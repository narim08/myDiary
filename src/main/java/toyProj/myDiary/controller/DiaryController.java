package toyProj.myDiary.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import toyProj.myDiary.dto.diary.*;
import toyProj.myDiary.service.DiaryService;

import java.time.LocalDate;
import java.util.List;

/*
    [일기 관련 API (RESTful API 설계)]

    캘린더 월별로 일기 등록한 날짜 조회
    GET     /api/diaries/calendar?userId=1&year=2026&month=3

    해당 날짜 별 일기 전체 목록 조회
    GET     /api/diaries?userId=1&date=2026-03-17

    일기 상세 조회
    GET     /api/diaries/{id}?userId=1

    일기 작성
    POST    /api/diaries?userId=1

    일기 수정
    PUT     /api/diaries/{id}?userId=1

    일기 삭제
    DELETE  /api/diaries/{id}?userId=1

    !) userId를 쿼리 파라미터로 받는 건 임시 구조
    -> JWT 적용 후, SecurityContext에서 자동으로 꺼내 쓸 예정
    -> @AuthenticationPrincipal: JwtAuthenticationFilter에서 SecurityContext에 저장한
        UsernamePasswordAuthenticationToken의 principal (= userId Long값) 을 꺼냄
 */

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor //final 필드 생성자 자동 생성 (DI)
public class DiaryController {

    //서비스 로직 사용
    private final DiaryService diaryService;

    //캘린더 월별로 일기 등록한 날짜 조회
    @GetMapping("/calendar")
    public ResponseEntity<DiaryCalendarResponse> getCalendar(
            //@RequestParam Long userId,
            @AuthenticationPrincipal Long userId, //userId 자동 주입됨
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(diaryService.getCalendarDates(userId, year, month));
    }

    //해당 날짜 별 일기 전체 목록 조회
    @GetMapping
    public ResponseEntity<List<DiaryListResponse>> getDiariesByDate(
            //@RequestParam Long userId,
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(diaryService.getDiariesByDate(userId, date));
    }

    //일기 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<DiaryDetailResponse> getDiary(
            @PathVariable Long id,
            //@RequestParam Long userId
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(diaryService.getDiary(userId, id));
    }

    //일기 작성
    @PostMapping
    public ResponseEntity<Long> create(
            //@RequestParam Long userId,
            @AuthenticationPrincipal Long userId,
            @RequestBody DiaryCreateRequest request) {
        Long diaryId = diaryService.create(userId, request);
        return ResponseEntity.ok(diaryId);
    }

    //일기 수정
    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            //@RequestParam Long userId,
            @AuthenticationPrincipal Long userId,
            @RequestBody DiaryUpdateRequest request) {
        diaryService.update(userId, id, request);
        return ResponseEntity.ok("수정되었습니다.");
    }

    //일기 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id,
            //@RequestParam Long userId
            @AuthenticationPrincipal Long userId) {
        diaryService.delete(userId, id);
        return ResponseEntity.ok("삭제되었습니다.");
    }
}