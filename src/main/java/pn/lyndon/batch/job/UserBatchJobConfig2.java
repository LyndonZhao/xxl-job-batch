package pn.lyndon.batch.job;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pn.lyndon.batch.listener.JobListener;
import pn.lyndon.batch.tasklet.MyTasklet;
import pn.lyndon.batch.user.dao.UserDao;

/**
 * Description:
 *
 * @author Administrator
 * @create 2019-12-01 11:42
 */
@Configuration
@Slf4j
public class UserBatchJobConfig2 {
    /**
     *
     用于构建JOB
     */
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    /**
     *
     用于构建Step
     */
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    /**
     *简单的JOB listener
     */
    @Autowired
    private JobListener jobListener;

    @Autowired
    DataSource dataSource;

    @Autowired
    UserDao userDao;

    /**
     * 一个简单基础的Job通常由一个或者多个Step组成
     */
    @Bean
    public Job userHandleJob2() {
        return jobBuilderFactory.get("userHandleJob2")
                .incrementer(new RunIdIncrementer())
                //start是JOB执行的第一个step
                .start(handleDataStep2())
                .listener(jobListener)
                .build();
    }

    /**
     * 一个简单的基于tasklet的Step
     */
    @Bean
    public Step handleDataStep2() {
        return stepBuilderFactory.get("getData").
                tasklet(new MyTasklet(dataSource, userDao)).
                build();
    }


}