package exercise.Group.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exercise.Group.dtos.CreateGroupDTO;
import exercise.Group.dtos.GroupDTO;
import exercise.Group.entities.Group;
import exercise.Group.mappers.GroupMapper;
import exercise.Group.repositories.GroupRepository;
import exercise.User.dtos.UserDTO;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepo;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private UserRepository userRepo;

    public List<Group> getAll() {
        List<Group> groups = groupRepo.findAll();
        return groups;
    }

    public Group getGroupById(Long id) {
        Optional<Group> optionalGroup = groupRepo.findById(id);
        if (!optionalGroup.isPresent()) {
            throw new RuntimeException("Group not found with this id");
        }
        Group group = optionalGroup.get();
        return group;
    }

    public Integer countMembers(Long groupId) {
        List<User> users = userRepo.findByGroupId(groupId);
        Integer count = users.size();
        return count;
    }

    public UserDTO getAdmin(Long groupId) {
        Group group = groupRepo.findById(groupId).get();
        User user = userRepo.findById(group.getAdminId()).get();
        UserDTO userDTO = new UserDTO(user);
        return userDTO;
    }

    public Group createGroup(CreateGroupDTO createGroupDTO) {
        Group existGroup = groupRepo.findByAdminId(createGroupDTO.getAdminId());
        if (Objects.nonNull(existGroup))
            throw new RuntimeException("An admin can own only one group.");
        Group newGroup = groupMapper.DTOToEntity(createGroupDTO);
        Group savedGroup = groupRepo.save(newGroup);
        User user = userRepo.findById(createGroupDTO.getAdminId()).get();
        user.setGroupId(savedGroup.getId());
        userRepo.save(user);
        return savedGroup;
    }

    public Group updateGroup(GroupDTO groupDTO) {
        Optional<Group> optionalGroup = groupRepo.findById(groupDTO.getId());
        if (!optionalGroup.isPresent())
            throw new RuntimeException("Group not found");
        Group group = optionalGroup.get();
        group.setName(groupDTO.getName());
        Group savedGroup = groupRepo.save(group);
        return savedGroup;
    }
}
