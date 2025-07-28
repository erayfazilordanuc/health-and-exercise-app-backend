package exercise.Group.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exercise.Group.dtos.CreateGroupDTO;
import exercise.Group.dtos.GroupDTO;
import exercise.Group.dtos.GroupRequestDTO;
import exercise.Group.entities.Group;
import exercise.Group.entities.GroupRequest;
import exercise.Group.mappers.GroupMapper;
import exercise.Group.repositories.GroupRepository;
import exercise.Group.repositories.GroupRequestRepository;
import exercise.User.dtos.UserDTO;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepo;
    @Autowired
    private GroupRequestRepository groupRequestRepo;
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private GroupMapper groupMapper;

    public GroupRequestDTO createJoinRequest(Long groupId, Long userId) {
        GroupRequest newGroupRequest = new GroupRequest(null, userId, groupId);
        GroupRequest savedGroupRequest = groupRequestRepo.save(newGroupRequest);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        GroupDTO groupDTO = new GroupDTO(groupId, group.getName(), group.getAdminId());

        GroupRequestDTO groupRequestDTO = new GroupRequestDTO(savedGroupRequest.getId(), userDTO, groupDTO);
        return groupRequestDTO;
    }

    public void deleteJoinRequest(Long groupRequestId, Long userId) {
        GroupRequest groupRequest = groupRequestRepo.findById(groupRequestId)
                .orElseThrow(() -> new RuntimeException("Group request not found with id: " + groupRequestId));
        if (groupRequest.getUserId().equals(userId)) {
            groupRequestRepo.delete(groupRequest);
        } else {
            throw new RuntimeException("You can not delete the group request that not yours");
        }
    }

    private GroupRequestDTO toDto(GroupRequest gr) {
        User user = userRepo.findById(gr.getUserId()).get();
        UserDTO userDTO = new UserDTO(user);
        Group group = groupRepo.findById(gr.getGroupId()).get();
        GroupDTO groupDTO = new GroupDTO(gr.getId(), group.getName(), group.getAdminId());

        GroupRequestDTO groupRequestDTO = new GroupRequestDTO(gr.getId(), userDTO, groupDTO);

        return groupRequestDTO;
    }

    public List<GroupRequestDTO> getGroupRequestsByGroupId(Long groupId) {
        List<GroupRequest> groupRequests = groupRequestRepo.findByGroupId(groupId);

        List<GroupRequestDTO> groupRequestDTOs = groupRequests.stream().map(this::toDto).toList();
        return groupRequestDTOs;
    }

    public GroupRequestDTO getGroupRequestByUserId(Long userId) {
        GroupRequest groupRequest = groupRequestRepo.findByUserId(userId);

        if (groupRequest == null)
            return null;

        GroupRequestDTO groupRequestDTO = toDto(groupRequest);
        return groupRequestDTO;
    }

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

    public void respondToGroupJoinRequest(Long groupRequestId, Boolean approved) {
        GroupRequest groupRequest = groupRequestRepo.findById(groupRequestId).get();

        if (approved) {
            User user = userRepo.findById(groupRequest.getUserId()).get();
            user.setGroupId(groupRequest.getGroupId());
            userRepo.save(user);
        }

        groupRequestRepo.delete(groupRequest);
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
