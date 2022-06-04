package org.ibctf.security;

import org.ibctf.service.AuthenticationService;
import org.ibctf.util.WebConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(
                        "/login", "/logout", "/register", "/otp",
                        "/bootstrap.min.css", "/main.css", "/favicon.ico"
                ).permitAll()
                .anyRequest().authenticated()
                .and()
                .logout()
                .logoutUrl("/logout")
                .addLogoutHandler((request, response, authentication) -> {
                    response.addCookie(authenticationService.nullCookie(WebConst.AUTHENTICATION_COOKIE_NAME));
                    response.addCookie(authenticationService.nullCookie(WebConst.SESSION_COOKIE_NAME));
                    SecurityContextHolder.getContext().setAuthentication(null);
                })
                .logoutSuccessUrl("/login?logout")
                .and()
                .addFilterAt(new PartnerTokenFilter(authenticationService), UsernamePasswordAuthenticationFilter.class)
                .csrf().disable();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ElevatedTokenInterceptor()).addPathPatterns("/template**", "/process**");
        registry.addInterceptor(new LoggedInInterceptor()).addPathPatterns("/login**", "/register**");
    }
}
