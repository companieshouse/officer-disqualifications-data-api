package uk.gov.companieshouse.disqualifiedofficersdataapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import uk.gov.companieshouse.api.filter.CustomCorsFilter;
import uk.gov.companieshouse.disqualifiedofficersdataapi.auth.EricTokenAuthenticationFilter;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    /**
     * Configure Http Security.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.httpBasic(AbstractHttpConfigurer::disable)
                //REST APIs not enabled for cross site script headers
                .csrf(AbstractHttpConfigurer::disable) //NO SONAR
                .cors(AbstractHttpConfigurer::disable)
                .addFilterBefore(new CustomCorsFilter(externalMethods()), CsrfFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAt(new EricTokenAuthenticationFilter(), BasicAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/disqualified-officers/healthcheck");
    }

    @Bean
    public List<String> externalMethods() {
        return Arrays.asList(HttpMethod.GET.name());
    }
}
