package com.k12.resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SearchApiSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void hotKeywordsHttpShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/search/hot-keywords").param("limit", "3"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void searchAllHttpShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/search/all").param("q", "主题班会").param("page", "1").param("size", "5"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
