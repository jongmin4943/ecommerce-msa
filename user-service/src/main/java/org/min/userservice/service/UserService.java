package org.min.userservice.service;

import org.min.userservice.dto.UserDto;

public interface UserService {
    UserDto createUser(UserDto userDto);
}
