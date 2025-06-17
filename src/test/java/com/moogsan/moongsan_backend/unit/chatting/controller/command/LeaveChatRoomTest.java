package com.moogsan.moongsan_backend.unit.chatting.controller.command;

import com.moogsan.moongsan_backend.domain.chatting.controller.command.JoinChatRoomController;
import com.moogsan.moongsan_backend.domain.chatting.controller.command.LeaveChatRoomController;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(controllers = LeaveChatRoomController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class LeaveChatRoomTest {
}
