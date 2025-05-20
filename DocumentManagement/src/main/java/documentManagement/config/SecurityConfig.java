package documentManagement.config;

import documentManagement.security.JwtAuthenticationFilter;
import documentManagement.service.CustomUserDetailsService;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.util.List.of;

// Configuration Class for Spring Security
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // Enable method-level security
class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final AuthenticationEntryPoint authenticationEntryPoint;

    // Declare it as a static inner class
    private static class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint { // Changed extends to implements
        private final HttpStatus httpStatus;

        public CustomAuthenticationEntryPoint(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
        }
        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
            response.sendError(httpStatus.value(), httpStatus.getReasonPhrase());
        }
    }

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = new CustomAuthenticationEntryPoint(HttpStatus.UNAUTHORIZED); // Use the static inner class
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/auth/register").permitAll()
                .antMatchers("/auth/login").permitAll()
                .antMatchers("/favicon.ico").permitAll()
                .antMatchers(
                        "/v3/api-docs/**",             // OpenAPI 3 documentation endpoint
                        "/swagger-ui/**",              // Swagger UI resources (e.g., swagger-ui.html)
                        "/swagger-ui.html",            // Old swagger-ui.html (sometimes used)
                        "/webjars/**",                 // Webjars for Swagger UI assets
                        "/swagger-resources/**",       // For older Swagger versions
                        "/configuration/ui",
                        "/configuration/security"
                ).permitAll()
                .antMatchers("/api/documents/**").hasAnyRole("ADMIN", "EDITOR", "VIEWER")
                .antMatchers("/auth/user/**").hasAnyRole("ADMIN", "EDITOR")
                .anyRequest().authenticated()
                .and()
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Add this logout configuration
                .logout()
                .logoutUrl("/auth/logout") // Specify the logout URL, should match your form action
                .logoutSuccessUrl("/auth/login?logout=true") // Redirect after successful logout
                .invalidateHttpSession(true) // Invalidate the session
                .deleteCookies("JSESSIONID") // Delete the session cookie
                .permitAll(); // Allow all users to access the logout URL
    }

    /* Removed the @Bean for AuthenticationEntryPoint
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }
    */

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

