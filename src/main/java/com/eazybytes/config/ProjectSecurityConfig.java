package com.eazybytes.config;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class ProjectSecurityConfig {
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        /**
         * Below is the custom security configuration
         */
        http.authorizeHttpRequests((requests) ->
                requests.requestMatchers("/myAccount","/myBalance","/myLoan","/myCard").authenticated()
                        .requestMatchers("/notice","/contact").permitAll());
        http.formLogin(withDefaults());
        http.httpBasic(withDefaults());
        return http.build();


        /**
         * Configure to deny all request
         */
        /*http.authorizeHttpRequests((requests) ->
                requests.anyRequest().denyAll());
        http.formLogin(withDefaults());
        http.httpBasic(withDefaults());
        return http.build();*/

        /**
         * Configuration to permit all requests
         */

        /*http.authorizeHttpRequests((requests) ->
                requests.anyRequest().permitAll());
        http.formLogin(withDefaults());
        http.httpBasic(withDefaults());
        return http.build();*/


    }
}