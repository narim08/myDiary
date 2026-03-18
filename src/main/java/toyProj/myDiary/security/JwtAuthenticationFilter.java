package toyProj.myDiary.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/*
    JWT 인증 필터
    -OncePerRequestFilter: 요청당 딱 한 번만 실행됨을 보장

    처리 흐름
    1. 요청 헤더에서 "Authorization: Bearer {token}" 추출
    2. 토큰 유효성 검증
    3. 유효하면 SecurityContext에 인증 정보 저장
    4. 이후 Controller에서 @AuthenticationPrincipal로 userId 꺼내 씀
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //1. 헤더에서 토큰 추출
        String token = resolveToken(request);

        //2. 토큰 유효성 검증 후 SecurityContext에 저장
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserId(token);

            /*
                UsernamePasswordAuthenticationToken(principal, credentials, authorities)
                -principal: 인증된 사용자 식별 정보 -> userId를 그대로 저장
                -credentials: 비밀번호 (인증 후에는 null로 처리)
                -authorities: 권한 목록 (학습용이라 빈 리스트)
             */
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of());

            //SecurityContext에 저장 -> 이후 어디서든 꺼낼 수 있음
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("SecurityContext에 인증 정보 저장 완료. userId: {}", userId);
        }

        //3. 다음 필터로 넘김 (인증 실패해도 넘김 - securityConfig에서 접근 제어)
        filterChain.doFilter(request, response);
    }


    /*
        Authorization 헤더에서 Bearer 토큰 파싱
        -"Bearer eyjhbGc..." -> "eyjhbGc..." 부분만 추출
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); //"Bearer "이후 문자열 추출
        }
        return null;
    }
}
