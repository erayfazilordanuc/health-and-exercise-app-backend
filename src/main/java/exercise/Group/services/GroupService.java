package exercise.Group.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exercise.Group.dtos.GroupDTO;
import exercise.Group.entities.Group;
import exercise.Group.mappers.GroupMapper;
import exercise.Group.repositories.GroupRepository;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepo;

    @Autowired
    private GroupMapper groupMapper;

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

    public Group createGroup(GroupDTO groupDTO) {
        Group newGroup = groupMapper.DTOToEntity(groupDTO);
        Group savedGroup = groupRepo.save(newGroup);
        return savedGroup;
    }
}
