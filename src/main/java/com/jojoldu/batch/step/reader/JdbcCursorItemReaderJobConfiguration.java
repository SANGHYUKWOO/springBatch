package com.jojoldu.batch.step.reader;

import com.jojoldu.batch.entity.City;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.Date;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class JdbcCursorItemReaderJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource; // DataSource DI

    private static final int chunkSize = 10;
    private int payTemp =0;

    @Bean
    public Job jdbcCursorItemReaderJob() {
        SimpleDateFormat format1 = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
        Date time = new Date();
        String time1 = format1.format(time);
        log.info("  time1  > "+time1);

        Job build = jobBuilderFactory.get("jdbcCursorItemReaderJob" + time1)
                .start(jdbcCursorItemReaderStep(time1))
                .build();

        log.info("여기 여기 오는가? >"+payTemp);

        return build;

        /*return jobBuilderFactory.get("jdbcCursorItemReaderJob"+time1)
                .start(jdbcCursorItemReaderStep(time1))
                .build();*/

    }

    @Bean
    public Step jdbcCursorItemReaderStep(String time) {
        log.info("스테이");
        return stepBuilderFactory.get("jdbcCursorItemReaderStep")
                .<City, City>chunk(chunkSize)
                .reader(jdbcCursorItemReader(time))
                //.writer(jdbcCursorItemWriter(time))
                .writer(jdbcBatchItemWriter(time))
                .build();
    }

    @Bean
    public JdbcCursorItemReader<City> jdbcCursorItemReader(String time) {
        return new JdbcCursorItemReaderBuilder<City>()
                .fetchSize(chunkSize)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(City.class))
                //.sql("SELECT id, amount, tx_name, tx_date_time FROM pay")
                .sql("SELECT id, name as cityName  FROM city")
                .name("jdbcCursorItemReader"+time)
                .build();
    }

    /**
     * reader에서 넘어온 데이터를 하나씩 출력하는 writer
     * 비지니스 로직을 여기에....
     */
    /*private ItemWriter<City> jdbcCursorItemWriter(String time) {
        return list -> {
            for (City pay: list) {
                log.info("Current Pay={}", pay);
                payTemp += pay.getId();
            }

        };

        /*return list -> {
            for (City pay: list) {
                log.info("Current Pay={}", pay);
            }
        };*/
                /*return new JdbcBatchItemWriterBuilder<City>()
                .dataSource(dataSource)
                .sql("insert into city2(id,name ) values ('1', 'seoul')")
                .beanMapped()
                .build();/
    }*/
    @Bean // beanMapped()을 사용할때는 필수
    public JdbcBatchItemWriter<City> jdbcBatchItemWriter(String time) {
        return new JdbcBatchItemWriterBuilder<City>()
                .dataSource(dataSource)
                .sql("insert into city2(id, name) values (:id, :cityName)")
                .beanMapped()
                .build();
    }
}
