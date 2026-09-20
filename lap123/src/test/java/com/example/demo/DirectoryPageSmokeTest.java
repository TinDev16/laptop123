package com.example.demo;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DirectoryPageSmokeTest {
    @Autowired MockMvc mvc;

    @Test
    void supplierAndEmployeeDirectoriesRenderForManager() throws Exception {
        mvc.perform(get("/ncc").with(user("manager").roles("QUAN_LY_KHO")))
                .andExpect(status().isOk());
        mvc.perform(get("/nhanvien").with(user("manager").roles("QUAN_LY_KHO")))
                .andExpect(status().isOk());
    }
}
