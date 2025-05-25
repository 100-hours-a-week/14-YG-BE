package com.moogsan.moongsan_backend.unit.groupbuy.controller.query;

import com.moogsan.moongsan_backend.domain.groupbuy.controller.GroupBuyQueryController;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService.GetGroupBuyHostedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GroupBuyQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GetGroupBuyHostedListTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private GetGroupBuyHostedList getGroupBuyHostedList;

}
