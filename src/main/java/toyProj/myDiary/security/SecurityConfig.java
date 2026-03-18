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
                //CSRF 비활성화: REST API + JWT 방식에서는 불필요 (CSRF는 세션/쿠키 기반 인증의 취약점을 막는 용도)
                .csrf(AbstractHttpConfigurer::disable)

                //세션 사용 안 함 (JWT는 STATELESS)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        //회원가입, 로그인, 프론트엔드 정적 리소스는 누구나 접근 가능
                        .requestMatchers(HttpMethod.POST, "/api/users/join").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                        .requestMatchers("/", "index.html", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated() //그 외 모든 요청은 인증 필요
                )

                //JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 삽입
                // -> Spring Security 기본 폼 로그인 필터보다 먼저 JWT 검사
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
