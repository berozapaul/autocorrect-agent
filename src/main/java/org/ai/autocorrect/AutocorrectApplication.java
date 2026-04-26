package org.ai.autocorrect;

import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;

@SpringBootApplication(exclude = {SpringAiRetryAutoConfiguration.class})
public class AutocorrectApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutocorrectApplication.class, args);
    }

    @Bean
    public RetryTemplate retryTemplate() {
        return new RetryTemplate();
    }
}
