package exercise.User.mappers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import exercise.Symptoms.dtos.SymptomsDTO;
import exercise.Symptoms.entities.Symptoms;
import exercise.Symptoms.repositories.SymptomsRepository;
import exercise.User.dtos.UserDTO;
import exercise.User.dtos.UpdateUserDTO;
import exercise.User.entities.User;

@Component
public class UserMapper {

    @Autowired
    SymptomsRepository symptomsRepository;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User DTOToEntity(UserDTO userDTO, User user) {
        User userEntity = new User(user.getId(), userDTO.getUsername(), userDTO.getEmail(), userDTO.getFullName(),
                passwordEncoder.encode(user.getPassword()), userDTO.getGroupId());

        return userEntity;
    }

    public User updateDTOToEntity(UpdateUserDTO userDTO, User user) {
        User userEntity = new User(user.getId(), userDTO.getUsername(), userDTO.getEmail(), userDTO.getFullName(),
                /* passwordEncoder.encode(userDTO.getPassword()) */user.getPassword(), userDTO.getGroupId());

        return userEntity;
    }

    public UserDTO entityToDTO(User user) {
        UserDTO userDTO = new UserDTO(user);

        List<Symptoms> symptomsList = symptomsRepository.findByUserId(user.getId());
        List<SymptomsDTO> symptomDTOs = symptomsList.stream()
                .map(s -> new SymptomsDTO(s))
                .toList();

        userDTO.setSymptomList(symptomDTOs);

        return userDTO;
    }
}
