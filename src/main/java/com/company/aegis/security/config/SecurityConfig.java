package com.company.aegis.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 基础安全链：负责非 OAuth2 协议的请求（如管理端 API、登录表单）
     */
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/actuator/**", "/login").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.permitAll()) // 开启默认登录页
            .csrf(csrf -> csrf.disable()); // 开发阶段暂时关闭 CSRF

        return http.build();
    }

    /**
     * 临时内存用户，用于测试项目启动是否成功
     * Phase 2 时会被数据库 UserDetailsService 替换
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}