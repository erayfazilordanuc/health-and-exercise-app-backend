package exercise.Group.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import exercise.Group.dtos.CreateGroupDTO;
import exercise.Group.dtos.GroupDTO;
import exercise.Group.dtos.GroupRequestDTO;
import exercise.Group.entities.Group;
import exercise.Group.entities.GroupRequest;
import exercise.Group.mappers.GroupMapper;
import exercise.Group.repositories.GroupRequestRepository;
import exercise.User.dtos.UserDTO;
import exercise.User.repositories.UserRepository;

@Component
public class GroupMapper {

  public Group DTOToEntity(CreateGroupDTO groupDTO) {
    Group group = new Group(null, groupDTO.getName(),
        groupDTO.getAdminId());

    return group;
  }

  public Group DTOToEntity(GroupDTO groupDTO) {
    Group group = new Group(null, groupDTO.getName(),
        groupDTO.getAdminId());

    return group;
  }
}
