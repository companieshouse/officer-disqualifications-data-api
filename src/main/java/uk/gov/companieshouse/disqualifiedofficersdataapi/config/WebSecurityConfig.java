package uk.gov.companieshouse.disqualifiedofficersdataapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import uk.gov.companieshouse.disqualifiedofficersdataapi.auth.EricTokenAuthenticationFilter;
import uk.gov.companieshouse.logging.Logger;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    /**
     * Configure Http Security.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, Logger logger) throws Exception {
        return httpSecurity.httpBasic(AbstractHttpConfigurer::disable)
                //REST APIs not enabled for cross site script headers
                .csrf(AbstractHttpConfigurer::disable) //NO SONAR
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAt(new EricTokenAuthenticationFilter(logger), BasicAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/disqualified-officers/healthcheck");
    }
}
