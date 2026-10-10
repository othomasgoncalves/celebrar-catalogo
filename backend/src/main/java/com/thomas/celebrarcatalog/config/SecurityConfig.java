package com.thomas.celebrarcatalog.config;

import com.thomas.celebrarcatalog.auth.JwtCookieAuthFilter;
import com.thomas.celebrarcatalog.auth.JwtService;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private static final String CSP = "default-src 'self'; img-src 'self' data:; font-src 'self' data:; "
            + "script-src 'self'; style-src 'self' 'unsafe-inline'; "
            + "frame-ancestors 'none'; object-src 'none'; base-uri 'self'";

    private static final String PERMISSIONS_POLICY = "camera=(), microphone=(), geolocation=()";

    private static final long HSTS_UM_ANO_EM_SEGUNDOS = 31_536_000L;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    JwtService jwtService,
                                    SessaoCookie sessaoCookie,
                                    JwtProperties jwtProperties) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers(PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.POST, "/api/auth/login")))

                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(autorizacao -> autorizacao
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.ASYNC).permitAll()

                        // Frontend (SPA) empacotado no mesmo jar: os arquivos estaticos e as
                        // rotas do React Router. A protecao do admin fica na API, nao no HTML.
                        .requestMatchers(HttpMethod.GET,
                                "/", "/index.html", "/assets/**", "/favicon.ico",
                                "/montar", "/admin", "/admin/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/categorias", "/api/produtos", "/api/cestas", "/api/imagens/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/eu").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().denyAll())

                .exceptionHandling(excecoes -> excecoes
                        .authenticationEntryPoint(RespostasSeguranca.naoAutenticado())
                        .accessDeniedHandler(RespostasSeguranca.acessoNegado()))

                .headers(cabecalhos -> cabecalhos
                        .contentSecurityPolicy(csp -> csp.policyDirectives(CSP))
                        .frameOptions(frame -> frame.deny())
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .permissionsPolicyHeader(permissoes -> permissoes.policy(PERMISSIONS_POLICY))
                        .httpStrictTransportSecurity(hsts -> {
                            if (jwtProperties.cookieSecure()) {
                                hsts.includeSubDomains(true).maxAgeInSeconds(HSTS_UM_ANO_EM_SEGUNDOS);
                            } else {
                                hsts.disable();
                            }
                        }))

                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .addFilterBefore(new JwtCookieAuthFilter(jwtService, sessaoCookie),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
