package ru.practicum.ewm.utils;

import ru.practicum.ewm.controllers.dtos.UserDto;
import ru.practicum.ewm.entities.User;

import java.util.ArrayList;
import java.util.List;

public class UserTestUtils {

    public static final long USER_ID = 1L;
    public static final String USER_NAME = "testUser";
    public static final String USER_EMAIL = "testUser@user.ru";

    public static User getDefaultUser() {
        return User.builder()
                .id(USER_ID)
                .name(USER_NAME)
                .email(USER_EMAIL)
                .build();
    }

    public static UserDto getDefaultUserDto() {
        return UserDto.builder()
                .id(USER_ID)
                .name(USER_NAME)
                .email(USER_EMAIL)
                .build();
    }

    public static List<User> generateUsers(final int count) {
        List<User> users = new ArrayList<>();

        for (long i = 1; i <= count; i++) {
            User user = User.builder()
                    .id(USER_ID)
                    .name(USER_NAME)
                    .email(i + USER_EMAIL)
                    .build();
            users.add(user);
        }

        return users;
    }
}
