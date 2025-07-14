package exercise.Message.services;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    public Message save(MessageDTO messageDTO) {
        Message message = new Message(messageDTO);
        Message savedMessage = messageRepo.save(message);
        return savedMessage;
    }

    public Boolean isRoomExist(Long id) {
        List<Message> messages = messageRepo.findByRoomId(id);
        return messages.size() > 0;
    }

    public Long isRoomExistBySenderAndReceiver(String sender, String receiver) {
        // Sender ve receiver yer değiştirip iki kez sorgu yapılır
        // ki iki kullanıcının odası bulunsun
        List<Message> messagesPrimary = messageRepo.findBySenderAndReceiver(sender, receiver);
        if (messagesPrimary.size() > 0)
            return messagesPrimary.get(0).getRoomId();
        List<Message> messagesSecondary = messageRepo.findBySenderAndReceiver(receiver, sender);
        return messagesSecondary.size() > 0 ? messagesSecondary.get(0).getRoomId() : 0;
    }

    public Long getLastRoomId() {
        return messageRepo.findLastRoomId();
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

    public List<Message> getMessagesBySenderAndReceiver(String sender, String receiver) {
        List<Message> messagesPrimary = messageRepo.findBySenderAndReceiver(sender, receiver);
        List<Message> messagesSecondary = messageRepo.findBySenderAndReceiver(receiver, sender);

        List<Message> combinedSortedMessages = Stream.concat(messagesPrimary.stream(), messagesSecondary.stream())
                .sorted(Comparator.comparing(Message::getCreatedAt))
                .collect(Collectors.toList());
        return combinedSortedMessages;
    }

    public Message getLastMessageBySenderAndReceiver(String sender, String receiver) {
        List<Message> messages = messageRepo.findBySenderAndReceiverOrderByCreatedAtDesc(sender, receiver);
        return messages.isEmpty() ? null : messages.get(0);
    }

    public void delete(Long id, User user) {
        Optional<Message> optionalMessage = messageRepo.findById(id);
        if (!optionalMessage.isPresent()) {
            throw new RuntimeException("Message not found with this id");
        } else {
            Message message = optionalMessage.get();
            if (!(message.getSender().equals(user.getUsername()) || message.getReceiver()
                    .equals(user.getUsername()))) {
                throw new RuntimeException("You can not get someone else's message");
            }
            messageRepo.delete(message);
        }
    }
}
