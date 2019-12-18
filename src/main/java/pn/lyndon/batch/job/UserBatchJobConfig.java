package pn.lyndon.batch.job;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pn.lyndon.batch.model.UserRowMapper;
import pn.lyndon.batch.listener.JobListener;
import pn.lyndon.batch.user.entity.User;

/**
 * Description:
 *
 * @author Administrator
 * @create 2019-12-01 11:42
 */
@Configuration
@Slf4j
public class UserBatchJobConfig {
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

    /**
     * 一个简单基础的Job通常由一个或者多个Step组成
     */
    @Bean
    public Job userHandleJob() {
        return jobBuilderFactory.get("userHandleJob")
                .incrementer(new RunIdIncrementer())
                //start是JOB执行的第一个step
                .start(handleDataStep())
                //.next(xxxStep())
                //.next(xxxStep())
                //设置了一个简单JobListener
                .listener(jobListener)
                .build();
    }

    /**
     * 一个简单基础的Step主要分为三个部分
     * ItemReader : 用于读取数据
     * ItemProcessor : 用于处理数据
     * ItemWriter : 用于写数据
     */
    @Bean
    public Step handleDataStep() {
        return stepBuilderFactory.get("getData").
                // <输入,输出> 。chunk通俗的讲类似于SQL的commit; 这里表示处理(processor)100条后写入(writer)一次。SX
                <User, User>chunk(100).
                //捕捉到异常就重试,重试100次还是异常,JOB就停止并标志失败
                faultTolerant().retryLimit(3).retry(Exception.class).skipLimit(100).skip(Exception.class).
                //指定ItemReader
                reader(getDataReader()).
                //指定ItemProcessor
                processor(getDataProcessor()).
                //指定ItemWriter
                writer(getDataWriter()).
                build();
    }

    @Bean
    public ItemReader<? extends User> getDataReader() {
        JdbcPagingItemReader<User> reader = new JdbcPagingItemReader<>();
        // 设置数据源
        reader.setDataSource(dataSource);
        // 设置一次最大读取条数
        reader.setFetchSize(100);
        // 把数据库中的每条数据映射到Person对中
        reader.setRowMapper(new UserRowMapper());
        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        // 设置查询的列
        queryProvider.setSelectClause("id, name, age");
        // 设置要查询的表
        queryProvider.setFromClause("from users");
        // 定义一个集合用于存放排序列
        Map<String, Order> sortKeys = new HashMap<String, Order>();
        // 按照升序排序
        sortKeys.put("id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        // 设置排序列
        reader.setQueryProvider(queryProvider);
        return reader;
    }

    @Bean
    public ItemProcessor<User, User> getDataProcessor() {
        return user -> {
            log.info("processor data : " + user.toString());
            return user;
        };
    }

    @Bean
    public ItemWriter<User> getDataWriter() {
        return list -> {
            for (User user : list) {
                //模拟 假装写数据 ,这里写真正写入数据的逻辑
                log.info("write data : " + user);
            }
        };
    }
}