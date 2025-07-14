package exercise.User.mappers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import exercise.Exercise.dtos.AchievementDTO;
import exercise.Exercise.entities.Achievement;
import exercise.Exercise.repositories.AchievementRepository;
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
        AchievementRepository achievementRepo;

        @Autowired
        private ExerciseRepository exerciseRepo;

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        public User DTOToEntity(UserDTO userDTO, User user) {
                List<Achievement> achievements = userDTO.getAchievements().stream()
                                .map(dto -> {
                                        Achievement a = new Achievement();
                                        a.setId(dto.getId());
                                        a.setUser(user);
                                        a.setExercise(exerciseRepo.findById(dto.getExerciseId()).get());
                                        return a;
                                })
                                .collect(Collectors.toList());

                User userEntity = new User(user.getId(), userDTO.getUsername(), userDTO.getEmail(),
                                userDTO.getFullName(),
                                passwordEncoder.encode(user.getPassword()), userDTO.getGroupId(),
                                achievements);

                return userEntity;
        }

        public User updateDTOToEntity(UpdateUserDTO userDTO, User user) {
                User userEntity = new User(user.getId(), userDTO.getUsername(), userDTO.getEmail(),
                                userDTO.getFullName(),
                                /* passwordEncoder.encode(userDTO.getPassword()) */user.getPassword(),
                                userDTO.getGroupId(),
                                user.getAchievements());

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

                return userEntity;
        }

        public UserDTO entityToDTO(User user) {
                UserDTO userDTO = new UserDTO(user);

                List<Symptoms> symptomsList = symptomsRepo.findByUserId(user.getId());
                List<SymptomsDTO> symptomDTOs = symptomsList.stream()
                                .map(s -> new SymptomsDTO(s))
                                .toList();

                userDTO.setSymptomList(symptomDTOs);

                List<Achievement> achievements = achievementRepo.findByUserId(user.getId());

                userDTO.setAchievements(achievements.stream()
                                .map(AchievementDTO::new)
                                .collect(Collectors.toList()));

                return userDTO;
        }
}
