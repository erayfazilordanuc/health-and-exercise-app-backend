package exercise.Message;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import exercise.Message.dtos.MessageDTO;
import exercise.Message.entities.Message;
import exercise.Message.services.MessageService;
import exercise.User.entities.User;

@RestController
@RequestMapping("api/messages")
@Tags(value = @Tag(name = "Message Operations"))
public class MessageController {

  @Autowired
  public MessageService messageService;

  @PutMapping
  public Message save(@RequestBody MessageDTO messageDTO, @AuthenticationPrincipal User user) {
    Message message = messageService.save(messageDTO);
    return message;
  }

  @GetMapping("/exists/room/id/{id}")
  public Boolean isRoomExist(@PathVariable Long id) {
    Boolean isRoomExist = messageService.isRoomExist(id);
    return isRoomExist;
  }

  @GetMapping("/room/sender/{sender}/receiver/{receiver}")
  public Long getRoomBySenderAndReceiver(@PathVariable String sender, @PathVariable String receiver) {
    Long isRoomExist = messageService.isRoomExistBySenderAndReceiver(sender, receiver);
    return isRoomExist;
  }

  @GetMapping("/room/next-id")
  public Long getNextRoomId() {
    Long lastRoomId = messageService.getLastRoomId();
    return lastRoomId + 1;
  }

  @GetMapping("/id/{id}")
  public Message getById(@PathVariable Long id, @AuthenticationPrincipal User user) {
    Message message = messageService.getMessageById(id);
    if (!(message.getSender().equals(user.getUsername()) || message.getReceiver()
        .equals(user.getUsername()))) {
      throw new RuntimeException("You can not get someone else's message");
    }
    return message;
  }

  @GetMapping("/room/id/{id}")
  public List<Message> getByRoomId(@PathVariable Long id, @AuthenticationPrincipal User user) {
    List<Message> messages = messageService.getMessagesByRoomId(id);
    if (messages.size() > 0 && !(messages.get(0).getSender().equals(user.getUsername()) || messages.get(0).getReceiver()
        .equals(user.getUsername()))) {
      throw new RuntimeException("You can not get someone else's messages");
    }
    return messages;
  }

  // @GetMapping("/sender/{sender}")
  // public List<Message> getBySender(@PathVariable String sender,
  // @AuthenticationPrincipal User user) {
  // List<Message> messages = messageService.getMessagesBySender(sender);
  // return messages;
  // }

  // @GetMapping("/receiver/{receiver}")
  // public List<Message> getByReceiver(@PathVariable String receiver,
  // @AuthenticationPrincipal User user) {
  // List<Message> messages = messageService.getMessagesByReceiver(receiver);
  // return messages;
  // }

  @GetMapping("/sender/{sender}/receiver/{receiver}")
  public List<Message> getBySenderAndReceiver(@PathVariable String sender, @PathVariable String receiver,
      @AuthenticationPrincipal User user) {
    List<Message> messages = messageService.getMessagesBySenderAndReceiver(sender, receiver);
    if (messages.size() > 0 && !(messages.get(0).getSender().equals(user.getUsername()) || messages.get(0).getReceiver()
        .equals(user.getUsername()))) {
      throw new RuntimeException("You can not get someone else's messages");
    }
    return messages;
  }

  @GetMapping("/sender/{sender}/receiver/{receiver}/last")
  public Message getLastBySenderAndReceiver(@PathVariable String sender, @PathVariable String receiver,
      @AuthenticationPrincipal User user) {
    Message lastMessage = messageService.getLastMessageBySenderAndReceiver(sender, receiver);
    if (lastMessage == null)
      return null;
      
    if (!(lastMessage.getSender().equals(user.getUsername()) || lastMessage.getReceiver()
        .equals(user.getUsername()))) {
      throw new RuntimeException("You can not get someone else's messages");
    }
    return lastMessage;
  }

  @DeleteMapping("/id/{id}")
  public ResponseEntity<?> save(@PathVariable Long id, @AuthenticationPrincipal User user) {
    messageService.delete(id, user);
    return ResponseEntity.ok().build();
  }

}
