package toyProj.myDiary.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
/*
     전역 예외 처리기
     -@RestControllerAdvice: 모든 Controller에서 발생하는 예외를 한 곳에서 처리
     -예외 종류에 따라 적절한 HTTP 상태 코드 + 메시지 반환
     -예외 처리 통합 안 하면 IllegalArgumentException가 터졌을 때 스프링이
     기본적으로 500 에러 내려보냄. 프론트에서 500으로만 와서 어떤 오류인지 판별 어려움.
     -> @RestControllerAdvice 붙이면
     { "message": "존재하지 않는 일기입니다." }  // 400 Bad Request
     위처럼 에러 메시지가 나오고 500대신 400으로 정확하게 내려옴.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
         비즈니스 로직 예외 (잘못된 요청)
         -중복 아이디, 비밀번호 불일치, 존재하지 않는 리소스 등
         -500 → 400 Bad Request로 변환
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }

    /*
         그 외 예상치 못한 서버 예외
         -사용자에게 내부 오류 메시지 노출 방지
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "서버 오류가 발생했습니다."));
    }

}