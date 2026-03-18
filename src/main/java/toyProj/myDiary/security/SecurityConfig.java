package toyProj.myDiary.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/*
    [Spring Security 설정]
    JWT 방식을 쓰면 세션을 사용하지 않음 (STATELESS)
    -> 서버가 상태를 저장하지 않고 토큰만으로 인증 처리
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //CORS 설정 추가: 브라우저에서 fetch로 백엔드 API 호출할 때 이 설정이 없으면 브라우저가 응답을 차단함
                //FE, BE 같은 8080 포트여도 Spring Security가 preflight 요청 막음
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                //CSRF 비활성화: REST API + JWT 방식에서는 불필요 (CSRF는 세션/쿠키 기반 인증의 취약점을 막는 용도)
                .csrf(AbstractHttpConfigurer::disable)

                //세션 사용 안 함 (JWT는 STATELESS)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        //회원가입, 로그인, 프론트엔드 정적 리소스는 접근 허용해야 함
                        .requestMatchers(HttpMethod.POST, "/api/users/join").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                        .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        .anyRequest().authenticated() //그 외 모든 요청은 인증 필요
                )

                //JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 삽입
                // -> Spring Security 기본 폼 로그인 필터보다 먼저 JWT 검사
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /*
        CORS 설정
        -개발 중: localhost 모든 포트 허용
        -배포 시: allowedOrigins를 실제 도메인으로 교체
    */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        //허용할 출처 (개발용)
        config.setAllowedOriginPatterns(List.of("*"));

        //허용할 HTTP 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        //허용할 헤더
        config.setExposedHeaders(List.of("*"));

        //Authorization 헤더 노출 (클라이언트에서 읽을 수 있게)
        config.setExposedHeaders(List.of("Authorization"));

        //자격 증명 포함 허용 (쿠키 등, JWT는 필수는 아니지만 관례상 추가)
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); //모든 경로에 적용
        return source;
    }

    /*
        비밀번호 암호화 Bean
        -BCrypt: 단방향 해시 + salt 자동 적용
        -같은 비밀번호여도 매번 다른 해시값 생성 -> 레인보우 테이블 공격 방어
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
