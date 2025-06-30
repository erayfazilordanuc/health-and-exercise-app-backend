package exercise.User.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import exercise.User.dtos.UserDTO;
import exercise.User.dtos.UpdateUserDTO;
import exercise.User.entities.User;
import exercise.User.mappers.UserMapper;
import exercise.User.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserMapper userMapper;

    public UserDTO getUserDTO(User user) {
        UserDTO userDTO = userMapper.entityToDTO(user);
        return userDTO;
    }

    public UserDTO getUserDTO(Long id) {
        User user = userRepo.findById(id).get();
        UserDTO userDTO = userMapper.entityToDTO(user);
        return userDTO;
    }

    public UserDTO getPublicUserDTO(Long id, User requestUser) {
        User user = userRepo.findById(id).get();
        if (user.getRole().equals("ROLE_USER")) {
            if (requestUser.getRole().equals("ROLE_USER"))
                throw new RuntimeException("You can not view other user's info");
        }
        UserDTO userDTO = userMapper.entityToDTO(user);
        return userDTO;
    }

    public List<UserDTO> getUsersByGroupId(Long id) {
        List<User> users = userRepo.findByGroupId(id);
        List<UserDTO> userDTOs = users.stream().map(u -> userMapper.entityToDTO(u)).toList();
        return userDTOs;
    }

    public User updateUser(UpdateUserDTO newUserDTO, User user) {
        User updatedUser = userMapper.updateDTOToEntity(newUserDTO, user);

        userRepo.save(updatedUser);

        return updatedUser;
    }

    public User updateUser(UpdateUserDTO newUserDTO, Long id) {
        User user = userRepo.findById(id).get();
        User updatedUser = userMapper.updateDTOToEntity(newUserDTO, user);

        userRepo.save(updatedUser);

        return updatedUser;
    }

    public String deleteUser(User user) {
        userRepo.delete(user);

        return "User \"" + user.getUsername() + "\" has been deleted";
    }

    public String deleteUser(Long id) {
        User user = userRepo.findById(id).get();
        userRepo.delete(user);

        return "User \"" + user.getUsername() + "\" has been deleted";
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return userRepo.findByUsername(username);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Unimplemented method 'loadUserByUsername'");
        }
    }

}
