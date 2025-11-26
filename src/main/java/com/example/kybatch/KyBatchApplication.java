package com.example.kybatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
//@EnableBatchProcessing
public class KyBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(KyBatchApplication.class, args);
    }

/*    @Bean
    public ApplicationRunner runner(JobLauncher jobLauncher, Job helloJob) {
        return args -> jobLauncher.run(helloJob, new JobParameters());
    } */


}
