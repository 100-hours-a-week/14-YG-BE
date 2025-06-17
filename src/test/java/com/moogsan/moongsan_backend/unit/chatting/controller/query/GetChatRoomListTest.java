package com.moogsan.moongsan_backend.unit.chatting.controller.query;

import com.moogsan.moongsan_backend.domain.chatting.controller.command.LeaveChatRoomController;
import com.moogsan.moongsan_backend.domain.chatting.controller.query.GetChatRoomListController;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(controllers = GetChatRoomListController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class GetChatRoomListTest {
}
