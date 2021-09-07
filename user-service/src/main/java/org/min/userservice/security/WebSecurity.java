package org.min.userservice.security;

import org.min.userservice.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

    private final UserService userService;
    private final Environment env;
    private final PasswordEncoder passwordEncoder;

    public WebSecurity(UserService userService, Environment env, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.env = env;
        this.passwordEncoder = passwordEncoder;
    }


    public AuthenticationFilter getAuthenticationFilter() throws Exception {
        AuthenticationFilter filter =
                new AuthenticationFilter(authenticationManager(),userService,env);
//        filter.setAuthenticationManager(authenticationManager());
        return filter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
//                .authorizeRequests().antMatchers("/users/**").permitAll();
                .authorizeRequests().antMatchers("/**")
                .hasIpAddress("192.168.1.39")
                .and()
                .addFilter(getAuthenticationFilter());

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
    }
}
