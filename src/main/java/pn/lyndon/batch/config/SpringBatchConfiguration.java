package pn.lyndon.batch.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pn.lyndon.batch.job.UserBatchJobConfig2;
import pn.lyndon.batch.job.UserBatchJobConfig;

/**
 * Description:
 *
 * @author Administrator
 * @create 2019-12-02 22:15
 */

@Configuration
@EnableAutoConfiguration
@EnableBatchProcessing(modular = true)
public class SpringBatchConfiguration {
    @Bean
    public ApplicationContextFactory userDataBatchApplicatonCtxFactory() {
        return new GenericApplicationContextFactory(UserBatchJobConfig.class);
    }
    @Bean
    public ApplicationContextFactory userDataTaskBatchApplicatonCtxFactory() {
        return new GenericApplicationContextFactory(UserBatchJobConfig2.class);
    }
}