package com.moogsan.moongsan_backend.unit.groupbuy.controller.command;

import com.moogsan.moongsan_backend.domain.groupbuy.controller.GroupBuyCommandController;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.GenerateDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GroupBuyCommandController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GenerateDescriptionTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private GenerateDescription generateDescription;

}
