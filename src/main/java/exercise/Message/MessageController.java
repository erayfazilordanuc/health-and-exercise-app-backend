package exercise.Message;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import java.util.List;
import java.util.Objects;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import exercise.Message.dtos.MessageDTO;
import exercise.Message.entities.Message;
import exercise.Message.services.MessageService;
import exercise.User.dtos.UserDTO;
import exercise.User.entities.User;

@RestController
@RequestMapping("api/messages")
@Tags(value = @Tag(name = "Message Operations"))
public class MessageController {

  @Autowired
  public MessageService messageService;

  @PutMapping("/id/{id}")
  public Message save(@RequestBody MessageDTO messageDTO) {
    Message message = messageService.save(messageDTO);
    return message;
  }

  @GetMapping("/id/{id}")
  public Message getById(@PathVariable Long id) {
    Message message = messageService.getMessageById(id);
    return message;
  }

  @GetMapping("/room/id/{id}")
  public List<Message> getByRoomId(@PathVariable Long id) {
    List<Message> messages = messageService.getMessagesByRoomId(id);
    return messages;
  }

  @GetMapping("/sender/{sender}")
  public List<Message> getBySender(@PathVariable String sender) {
    List<Message> messages = messageService.getMessagesBySender(sender);
    return messages;
  }

  @GetMapping("/receiver/{receiver}")
  public List<Message> getByReceiver(@PathVariable String receiver) {
    List<Message> messages = messageService.getMessagesBySender(receiver);
    return messages;
  }
}
