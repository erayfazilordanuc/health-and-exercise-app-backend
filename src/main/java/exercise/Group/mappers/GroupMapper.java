package exercise.Group.mappers;

import org.springframework.stereotype.Component;

import exercise.Group.dtos.GroupDTO;
import exercise.Group.entities.Group;
import exercise.Group.mappers.GroupMapper;

@Component
public class GroupMapper {

  public Group DTOToEntity(GroupDTO groupDTO) {
    Group group = new Group(null, groupDTO.getName(),
        groupDTO.getAdminId());

    return group;
  }
}
