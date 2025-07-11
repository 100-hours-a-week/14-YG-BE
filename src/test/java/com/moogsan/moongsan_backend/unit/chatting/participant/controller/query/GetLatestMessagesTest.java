package com.moogsan.moongsan_backend.unit.chatting.participant.controller.query;

import com.moogsan.moongsan_backend.domain.chatting.participant.controller.query.GetLatestMessagesController;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(controllers = GetLatestMessagesController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class GetLatestMessagesTest {
}
