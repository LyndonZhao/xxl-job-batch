package pn.lyndon.batch.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Description: 配置TaskExecutor
 *
 * @author Lyndon
 * @create 2019-12-01 11:37
 */
@Configuration
public class ExecutorConfiguration {

    @Bean
    public ThreadPoolTaskExecutor batchThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(50);
        threadPoolTaskExecutor.setMaxPoolSize(200);
        threadPoolTaskExecutor.setQueueCapacity(1000);
        threadPoolTaskExecutor.setThreadNamePrefix("batch-Job");
        return threadPoolTaskExecutor;
    }
}