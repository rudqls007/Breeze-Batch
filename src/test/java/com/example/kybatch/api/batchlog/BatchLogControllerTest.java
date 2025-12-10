package com.example.kybatch.api.batchlog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc

class BatchLogControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void jobLog_API_정상조회() throws Exception {
        mvc.perform(get("/api/batch-logs/jobs")
                        .param("jobName", "fullAutoPipelineJob"))
                .andExpect(status().isOk());
    }

    @Test
    void stepLog_API_정상조회() throws Exception {
        mvc.perform(get("/api/batch-logs/steps")
                        .param("stepName", "dailyAggregationStep"))
                .andExpect(status().isOk());
    }
}
