package com.example.demo.Configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    @Bean
    public FilterRegistrationBean<HiddenHttpMethodFilter> hiddenHttpMethodFilterRegistration() {
        FilterRegistrationBean<HiddenHttpMethodFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(hiddenHttpMethodFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}
