package com.moogsan.moongsan_backend.unit.chatting.participant.controller.command;

import com.moogsan.moongsan_backend.domain.chatting.participant.controller.command.CreateChatMessageController;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(controllers = CreateChatMessageController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class CreateChatMessageTest {
}
