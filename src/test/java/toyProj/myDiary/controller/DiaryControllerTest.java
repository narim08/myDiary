package toyProj.myDiary.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import toyProj.myDiary.dto.diary.DiaryDetailResponse;
import toyProj.myDiary.dto.diary.DiaryListResponse;
import toyProj.myDiary.security.JwtTokenProvider;
import toyProj.myDiary.service.DiaryService;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
     @WebMvcTest
     -Controller + Security 필터 체인만 로딩
     -Service, Repository 등은 @MockBean으로 대체
     -실제 HTTP 요청/응답 흐름을 MockMvc로 테스트

     @MockBean
     -Spring Context에 Mock Bean을 등록
     -@WebMvcTest에서는 @Mock 대신 이것 사용
 */
@WebMvcTest(DiaryController.class)
class DiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // JSON 직렬화/역직렬화

    @MockitoBean
    private DiaryService diaryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider; // Security 설정에서 필요

    /**
         인증된 요청 만들기
         -JWT 필터 대신 Spring Security Test의 authentication() 사용
         -principal에 userId(1L)를 직접 세팅
     */
    private org.springframework.security.authentication.UsernamePasswordAuthenticationToken
            mockAuth(Long userId) {
        return new org.springframework.security.authentication
                .UsernamePasswordAuthenticationToken(userId, null, List.of());
    }

    @Test
    @DisplayName("날짜별 일기 목록 조회 API - 200 OK")
    void getDiariesByDate_success() throws Exception {
        // given
        DiaryListResponse item = createListResponse(10L, "테스트 제목");
        given(diaryService.getDiariesByDate(eq(1L), any(LocalDate.class)))
                .willReturn(List.of(item));

        // when & then
        mockMvc.perform(get("/api/diaries")
                        .param("date", "2026-03-17")
                        .with(authentication(mockAuth(1L))) // 인증 정보 세팅
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].title").value("테스트 제목"));
    }

    @Test
    @DisplayName("일기 상세 조회 API - 200 OK")
    void getDiary_success() throws Exception {
        // given
        DiaryDetailResponse detail = createDetailResponse(
                10L, "테스트 제목", "테스트 내용", LocalDate.of(2026, 3, 17));
        given(diaryService.getDiary(1L, 10L)).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/diaries/10")
                        .with(authentication(mockAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 제목"))
                .andExpect(jsonPath("$.content").value("테스트 내용"));
    }

    @Test
    @DisplayName("일기 작성 API - 200 OK, 생성된 id 반환")
    void create_success() throws Exception {
        // given
        given(diaryService.create(eq(1L), any())).willReturn(10L);

        Map<String, Object> body = Map.of(
                "title", "새 일기",
                "content", "내용입니다",
                "diaryDate", "2026-03-17"
        );

        // when & then
        mockMvc.perform(post("/api/diaries")
                        .with(authentication(mockAuth(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(10L));
    }

    @Test
    @DisplayName("일기 수정 API - 200 OK")
    void update_success() throws Exception {
        // given
        willDoNothing().given(diaryService).update(eq(1L), eq(10L), any());

        Map<String, String> body = Map.of("title", "수정 제목", "content", "수정 내용");

        // when & then
        mockMvc.perform(put("/api/diaries/10")
                        .with(authentication(mockAuth(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().string("수정되었습니다."));
    }

    @Test
    @DisplayName("일기 삭제 API - 200 OK")
    void delete_success() throws Exception {
        // given
        willDoNothing().given(diaryService).delete(1L, 10L);

        // when & then
        mockMvc.perform(delete("/api/diaries/10")
                        .with(authentication(mockAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(content().string("삭제되었습니다."));
    }

    @Test
    @DisplayName("인증 없이 요청 시 - 403 Forbidden")
    void request_without_auth() throws Exception {
        // 인증 정보 없이 요청 → Security 설정에 의해 차단
        mockMvc.perform(get("/api/diaries")
                        .param("date", "2026-03-17"))
                .andExpect(status().isForbidden());
    }

    /* 헬퍼 메서드 */
    private DiaryListResponse createListResponse(Long id, String title) {
        try {
            DiaryListResponse dto = new DiaryListResponse();
            setField(dto, "id", id);
            setField(dto, "title", title);
            setField(dto, "createdAt", LocalDateTime.now());
            return dto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DiaryDetailResponse createDetailResponse(
            Long id, String title, String content, LocalDate date) {
        try {
            DiaryDetailResponse dto = new DiaryDetailResponse();
            setField(dto, "id", id);
            setField(dto, "title", title);
            setField(dto, "content", content);
            setField(dto, "diaryDate", date);
            setField(dto, "createdAt", LocalDateTime.now());
            setField(dto, "updatedAt", LocalDateTime.now());
            return dto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
