package toyProj.myDiary.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; //lombok 말고 이거!
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/*
    JWT 토큰 생성, 검증, 파싱 담당

    JWT 구조: Header.Payload.Signature
    -Header: 알고리즘 정보 (HS256)
    -Payload: claim (userId 등 담고 싶은 데이터)
    -Signature: 위변조 방지 서명
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        //문자열 secret -> HMAC-SHA 알고리즘용 Key 객체로 변환
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /*
        [토큰 생성]
        -subject에 userId를 문자열로 저장
        -발급 시간, 만료 시간 자동 세팅
     */
    public String createToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId)) //payload에 userId 저장
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)    //서명 (위변조 방지)
                .compact();
    }

    /*
        [토큰에서 userId 추출]
        -subject에서 꺼내서 Long으로 변환
     */
    public Long getUserId(String token) {
        return Long.parseLong(
                getClaims(token).getSubject()
        );
    }

    /*
        [토큰 유효성 검증]
        -만료, 위변조, 형식 오류 등을 각각 catch해서 로그 남김
        -유효하면 true, 그 외 모두 false
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token); //파싱 자체가 곧 검증
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 형식: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있음: {}", e.getMessage());
        }
        return false;
    }

    /*
        [공통 내부 메서드]
        -토큰 파싱 -> Claims 반환
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
