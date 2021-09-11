package org.min.userservice.controller;

import io.micrometer.core.annotation.Timed;
import org.min.userservice.dto.UserDto;
import org.min.userservice.jpa.UserEntity;
import org.min.userservice.service.UserService;
import org.min.userservice.vo.Greeting;
import org.min.userservice.vo.RequestUser;
import org.min.userservice.vo.ResponseUser;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class UserController {

    private Environment env;
    private UserService userService;

    @Autowired
    private Greeting greeting;

    public UserController(Environment env, UserService userService) {
        this.env = env;
        this.userService = userService;
    }

    @GetMapping("/health_check")
    @Timed(value = "users.status", longTask = true)
    public String status() {
        StringBuilder builder = new StringBuilder();
        builder.append("It's working on User-Service port(local.server.port)= ")
                .append(env.getProperty("local.server.port"))
                .append(" port(server.port)= ")
                .append(env.getProperty("server.port"))
                .append(" token secret= ")
                .append(env.getProperty("token.secret"))
                .append(" token expiration time= ")
                .append(env.getProperty("token.expiration_time"));
        return builder.toString();
    }

    @GetMapping("/welcome")
    @Timed(value = "users.welcome", longTask = true)
    public String welcome() {
//        return env.getProperty("greeting.message");
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity createUser(@RequestBody RequestUser user) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserDto userDto = mapper.map(user,UserDto.class);
        userService.createUser(userDto);
        ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers() {
        Iterable<UserEntity> userList = userService.getUserByAll();

        List<ResponseUser> result = new ArrayList<>();
        userList.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseUser.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
     @GetMapping("/users/{userId}")
        public ResponseEntity<ResponseUser> getUsers(@PathVariable String userId) {
            UserDto userDto = userService.getUserByUserId(userId);

            ResponseUser returnValue = new ModelMapper().map(userDto, ResponseUser.class);

            return ResponseEntity.status(HttpStatus.OK).body(returnValue);
        }

}
