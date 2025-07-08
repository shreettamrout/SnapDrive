package com.shreettam.mini_google_drive.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ExceptionHandlingConfig implements WebMvcConfigurer {
    /*
    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(new CustomExceptionResolver());
    }
    */
}
