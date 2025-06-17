package com.moogsan.moongsan_backend.unit.chatting.controller.command;

import com.moogsan.moongsan_backend.domain.chatting.controller.command.CreateChatMessageController;
import com.moogsan.moongsan_backend.domain.chatting.controller.command.JoinChatRoomController;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(controllers = JoinChatRoomController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class JoinChatRoomTest {
}
