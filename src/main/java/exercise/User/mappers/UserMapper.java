package exercise.User.mappers;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import exercise.Exercise.repositories.ExerciseRepository;
import exercise.Symptoms.dtos.SymptomsDTO;
import exercise.Symptoms.entities.Symptoms;
import exercise.Symptoms.repositories.SymptomsRepository;
import exercise.User.dtos.UserDTO;
import exercise.User.dtos.UpdateUserDTO;
import exercise.User.entities.User;

@Component
public class UserMapper {

        @Autowired
        SymptomsRepository symptomsRepo;

        @Autowired
        private ExerciseRepository exerciseRepo;

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        public User DTOToEntity(UserDTO userDTO, User user) {
                User userEntity = new User(user.getId(), userDTO.getUsername(), userDTO.getEmail(),
                                userDTO.getFullName(),
                                passwordEncoder.encode(user.getPassword()), userDTO.getGroupId(), userDTO.getTheme());

                return userEntity;
        }

        public User updateDTOToEntity(UpdateUserDTO userDTO, User user) {
                return new User(
                                user.getId(),
                                Objects.requireNonNullElse(userDTO.getUsername(), user.getUsername()),
                                Objects.requireNonNullElse(userDTO.getEmail(), user.getEmail()),
                                Objects.requireNonNullElse(userDTO.getFullName(), user.getFullName()),
                                user.getPassword(),
                                Objects.requireNonNullElse(userDTO.getGroupId(), user.getGroupId()),
                                Objects.requireNonNullElse(userDTO.getTheme(), user.getTheme()));

                // List<Achievement> achievements = userDTO.getAchievementDTOs().stream()
                // .map(aDto -> {
                // Achievement achievement = new Achievement();
                // achievement.setId(aDto.getId());
                // achievement.setUser(userEntity);
                // achievement.setExercise(exerciseRepo.findById(aDto.getExerciseId())
                // .orElseThrow(() -> new RuntimeException("Exercise not found")));
                // return achievement;
                // })
                // .collect(Collectors.toList());
                // userEntity.setAchievements(achievements);

                // User userEntity = new User(user.getId(), userDTO.getUsername(),
                // userDTO.getEmail(),
                // userDTO.getFullName(),
                // /* passwordEncoder.encode(userDTO.getPassword()) */user.getPassword(),
                // userDTO.getGroupId());

                // return userEntity;
        }

        public UserDTO entityToDTO(User user) {
                UserDTO userDTO = new UserDTO(user);

                List<Symptoms> symptomsList = symptomsRepo.findByUserId(user.getId());
                List<SymptomsDTO> symptomDTOs = symptomsList.stream()
                                .map(s -> new SymptomsDTO(s))
                                .toList();

                // userDTO.setSymptomList(symptomDTOs);

                return userDTO;
        }
}
