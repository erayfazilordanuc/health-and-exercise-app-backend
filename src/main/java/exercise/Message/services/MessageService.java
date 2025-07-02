package exercise.Message.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exercise.Message.dtos.MessageDTO;
import exercise.Message.entities.Message;
import exercise.Message.mappers.MessageMapper;
import exercise.Message.repositories.MessageRepository;
import exercise.User.dtos.UserDTO;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserRepository userRepo;

    public Message save(MessageDTO messageDTO) {
        Message message = new Message(messageDTO);
        Message savedMessage = messageRepo.save(message);
        return savedMessage;
    }

    public Message getMessageById(Long id) {
        Optional<Message> optionalMessage = messageRepo.findById(id);
        if (!optionalMessage.isPresent()) {
            throw new RuntimeException("Message not found with this id");
        }
        Message message = optionalMessage.get();
        return message;
    }

    public List<Message> getMessagesByRoomId(Long roomId) {
        List<Message> messages = messageRepo.findByRoomId(roomId);
        return messages;
    }

    public List<Message> getMessagesBySender(String sender) {
        List<Message> messages = messageRepo.findBySender(sender);
        return messages;
    }

    public List<Message> getMessagesByReceiver(String receiver) {
        List<Message> messages = messageRepo.findByReceiver(receiver);
        return messages;
    }
}
