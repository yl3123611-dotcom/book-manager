package com.book.manager.config;

import com.book.manager.service.UserService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.authorization.AuthorityAuthorizationManager;

import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserService userService,
                                                               PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider provider) {
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 登录配置
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/user/login")
                        // ❌ 删除 successForwardUrl，改为自定义 JSON 处理器
                        .successHandler((request, response, authentication) -> {
                            response.setContentType("application/json;charset=utf-8");
                            PrintWriter out = response.getWriter();
                            // 返回成功 JSON
                            R r = R.success(CodeEnum.SUCCESS, "登录成功");
                            out.write(new ObjectMapper().writeValueAsString(r));
                            out.flush();
                            out.close();
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setContentType("application/json;charset=utf-8");
                            PrintWriter out = response.getWriter();
                            // 返回失败 JSON
                            R r = R.fail(CodeEnum.NAME_OR_PASS_ERROR);
                            out.write(new ObjectMapper().writeValueAsString(r));
                            out.flush();
                            out.close();
                        })
                        .permitAll()
                )

                // 授权配置
                .authorizeHttpRequests(auth -> auth
                        // 放行静态资源 + 登录注册
                        .requestMatchers(
                                "/upload/**",
                                "/javaex/**",
                                "/ajax/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",

                                "/",
                                "/login",
                                "/user/login",
                                "/register",
                                "/user/register",

                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // ✅ 管理员页面 / 管理员接口（你截图里那几个功能点）
                        .requestMatchers(
                                "/home/home-manage",
                                "/user/user-manage",
                                "/user/admin-add",
                                "/book/book-add",
                                "/admin/**",

                                // ✅ 专题管理接口仅管理员可用
                                "/topic/admin/**",

                                // 馆藏管理后台接口仅管理员可用
                                "/nav/admin/**"
                        ).access(AuthorityAuthorizationManager.hasAnyAuthority("ROLE_0", "ROLE_1"))

                        // 公告管理后台接口仅管理员可用（新增/编辑/删除/上下线/置顶等）
                        .requestMatchers("/announcement/admin/**")
                        .access(AuthorityAuthorizationManager.hasAuthority("ROLE_0"))

                        // 其他请求：只要登录就行（普通用户也能进系统）
                        .anyRequest().authenticated()
                )


                // logout
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/")
                )

                // 关闭 csrf (前后端分离或混合模式建议开启，但为了兼容你现有代码先关闭)
                .csrf(csrf -> csrf.disable())

                // 允许 iframe
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // remember-me
                .rememberMe(remember -> remember.rememberMeParameter("remember"))

                // 兼容默认配置
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}