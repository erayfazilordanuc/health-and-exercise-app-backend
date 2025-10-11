package exercise.User.services;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import exercise.User.dtos.UserDTO;
import exercise.Consent.entities.Consent;
import exercise.Consent.enums.ConsentPurpose;
import exercise.Consent.enums.ConsentStatus;
import exercise.Consent.repositories.ConsentRepository;
import exercise.Group.entities.Group;
import exercise.Group.enums.MemberSort;
import exercise.Group.enums.SortDir;
import exercise.Group.repositories.GroupRepository;
import exercise.User.dtos.UpdateMeasurementsDTO;
import exercise.User.dtos.UpdateUserDTO;
import exercise.User.entities.User;
import exercise.User.mappers.UserMapper;
import exercise.User.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private GroupRepository groupRepo;

    @Autowired
    private ConsentRepository consentRepo;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean checkUserConsentState(Long userId) {
        // isAdmin eklenip ona göre kontrol eklenebilir yoksa her seferinde userRepodan
        // çekmek sunucuyu çok yorar
        // // Şuanlık admin için bir sözleşme kısıtlaması yok
        // if (user.getRole().equals("ROLE_ADMIN"))
        // admine özel kontrol
        // return true;

        List<Consent> consents = consentRepo.findByUser_Id(userId);
        if (consents == null || consents.isEmpty())
            return false;

        // withdrawn/iptal edilenleri ele
        List<Consent> active = consents.stream()
                .filter(c -> c.getWithdrawnAt() == null)
                .toList();

        // her purpose için EN GÜNCEL kaydı al (grantedAt varsa onu, yoksa createdAt)
        Map<ConsentPurpose, Consent> latestByPurpose = active.stream()
                .collect(Collectors.toMap(
                        Consent::getPurpose,
                        c -> c,
                        (a, b) -> {
                            var atA = a.getGrantedAt() != null ? a.getGrantedAt() : a.getCreatedAt();
                            var atB = b.getGrantedAt() != null ? b.getGrantedAt() : b.getCreatedAt();
                            return atA.after(atB) ? a : b;
                        }));

        // KVKK bilgilendirmesi "okudum"
        boolean kvkkOk = Optional.ofNullable(latestByPurpose.get(ConsentPurpose.KVKK_NOTICE_ACK))
                .map(Consent::getStatus)
                .map(s -> s == ConsentStatus.ACKNOWLEDGED) // senin enum’una göre
                .orElse(false);

        // Sağlık verileri için açık rıza
        boolean healthOk = Optional.ofNullable(latestByPurpose.get(ConsentPurpose.HEALTH_DATA_PROCESSING_ACK))
                .map(Consent::getStatus)
                .map(s -> s == ConsentStatus.ACCEPTED) // hangisini kullanıyorsan
                .orElse(false);

        // Egzersiz verileri için açık rıza
        boolean exerciseOk = Optional.ofNullable(latestByPurpose.get(ConsentPurpose.EXERCISE_DATA_PROCESSING_ACK))
                .map(Consent::getStatus)
                .map(s -> s == ConsentStatus.ACCEPTED) // hangisini kullanıyorsan
                .orElse(false);

        boolean studyOk = Optional.ofNullable(latestByPurpose.get(ConsentPurpose.STUDY_CONSENT_ACK))
                .map(Consent::getStatus)
                .map(s -> s == ConsentStatus.ACCEPTED) // hangisini kullanıyorsan
                .orElse(false);

        return kvkkOk && healthOk && exerciseOk && studyOk;
    }

    @Transactional(readOnly = true)
    public UserDTO getUserDTO(User user) {
        UserDTO userDTO = userMapper.entityToDTO(user);
        return userDTO;
    }

    public UserDTO getUserDTO(Long id) {
        User user = userRepo.findById(id).get();
        UserDTO userDTO = userMapper.entityToDTO(user);
        return userDTO;
    }

    public UserDTO getPublicUserDTO(Long id, User user) {
        User targetUser = userRepo.findById(id).get();
        if (user.getRole().equals("ROLE_USER")) {
            return new UserDTO(targetUser);
        }

        if (user.getRole().equals("ROLE_ADMIN")) {
            return userMapper.entityToDTO(targetUser);
        }

        return null;
    }

    public UserDTO getPublicUserDTO(String username, User user) {
        User targetUser = userRepo.findByUsername(username);
        if (user.getRole().equals("ROLE_USER")) {
            return new UserDTO(targetUser);
        }

        if (user.getRole().equals("ROLE_ADMIN")) {
            return userMapper.entityToDTO(targetUser);
        }

        return null;
    }

    public List<UserDTO> getUsersByGroupId(Long id, MemberSort sort, SortDir dir) {
        List<User> users = userRepo.findByGroupId(id);
        List<UserDTO> userDTOs = users.stream()
                .map(userMapper::entityToDTO)
                .collect(Collectors.toList());
        boolean hasAdmin = userDTOs.stream()
                .anyMatch(u -> u.getRole().equals("ROLE_ADMIN"));
        if (!hasAdmin) {
            Group group = groupRepo.findById(id).get();
            User user = userRepo.findById(group.getAdminId()).get();
            userDTOs.add(new UserDTO(user));
        }
        userDTOs.sort(Comparator.comparing((UserDTO u) -> !"ROLE_ADMIN".equals(u.getRole())));
        return userDTOs;
    }

    public UserDTO updateUserAndGetDTO(UpdateUserDTO newUserDTO, User user) {
        User updatedUser = updateUser(newUserDTO, user);
        UserDTO userDTO = new UserDTO(updatedUser);
        return userDTO;
    }

    public UserDTO updateTheme(String themeKey, User user) {
        user.setTheme(themeKey);
        User updatedUser = userRepo.save(user);
        UserDTO userDTO = new UserDTO(updatedUser);
        return userDTO;
    }

    public UserDTO updateUserAvatarAndGetDTO(String key, User user) {
        user.setAvatar(key);
        User updatedUser = userRepo.save(user);
        UserDTO userDTO = new UserDTO(updatedUser);
        return userDTO;
    }

    public UserDTO updateMeasurements(UpdateMeasurementsDTO dto, User user) {
        user.setHeight(dto.getHeight());
        user.setWeight(dto.getWeight());
        User updatedUser = userRepo.save(user);
        UserDTO userDTO = new UserDTO(updatedUser);
        return userDTO;
    }

    public UserDTO joinGroup(Long groupId, User user) {
        user.setGroupId(groupId);
        User updatedUser = userRepo.save(user);
        UserDTO userDTO = new UserDTO(updatedUser);
        return userDTO;
    }

    public User updateUser(UpdateUserDTO dto, User user) {

        if (dto == null)
            return user;

        if (dto.getId() != null && !dto.getId().equals(user.getId()))
            throw new IllegalArgumentException("ID mismatch");

        if (dto.getUsername() != null)
            user.setUsername(dto.getUsername());

        if (dto.getEmail() != null)
            user.setEmail(dto.getEmail());

        if (dto.getPassword() != null)
            user.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getFullName() != null)
            user.setFullName(dto.getFullName());

        if (dto.getHeight() != null)
            user.setHeight(dto.getHeight());

        if (dto.getWeight() != null)
            user.setWeight(dto.getWeight());

        if (dto.getGroupId() != null)
            user.setGroupId(dto.getGroupId());

        if (dto.getTheme() != null)
            user.setTheme(dto.getTheme());

        return userRepo.save(user);
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
