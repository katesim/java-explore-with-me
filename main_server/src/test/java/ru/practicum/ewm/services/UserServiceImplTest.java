package ru.practicum.ewm.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.entities.User;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static ru.practicum.ewm.utils.UserTestUtils.USER_ID;
import static ru.practicum.ewm.utils.UserTestUtils.generateUsers;
import static ru.practicum.ewm.utils.UserTestUtils.getDefaultUser;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final int PAGE_START_FROM = 0;
    private static final int PAGE_SIZE = 10;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl subject;

    @Test
    void create() {
        User user = getDefaultUser();
        when(userRepository.save(user)).thenReturn(user);

        User result = subject.create(user);

        assertEquals(user, result);
        verify(userRepository, times(1)).save(user);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void get_UserExists_returnCategory() {
        User user = getDefaultUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User result = subject.get(user.getId());

        assertEquals(user, result);
        verify(userRepository, times(1)).findById(user.getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void get_whenUserNotExists_throwNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> subject.get(USER_ID));
        verify(userRepository, times(1)).findById(USER_ID);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getAll() {
        List<User> users = generateUsers(10);
        Pageable pageable = PageRequest.of(PAGE_START_FROM, PAGE_SIZE);

        when(userRepository.findAll(eq(pageable)))
                .thenReturn(new PageImpl<>(users));

        Page<User> result = subject.getAll(PAGE_START_FROM, PAGE_SIZE);

        assertEquals(result.stream().collect(Collectors.toList()), users);
        verify(userRepository, times(1)).findAll(eq(pageable));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getAll_whenFilterByIds_returnFiltered() {
        User user = getDefaultUser().toBuilder()
                .id(2L)
                .build();
        List<User> users = List.of(user);
        List<Long> ids = List.of(2L);
        Pageable pageable = PageRequest.of(PAGE_START_FROM, PAGE_SIZE);

        when(userRepository.findByIdInOrderByIdDesc(eq(ids), eq(pageable)))
                .thenReturn(new PageImpl<>(users));

        Page<User> result = subject.getAllWithUserIds(ids, PAGE_START_FROM, PAGE_SIZE);

        assertEquals(result.stream().collect(Collectors.toList()), users);
        verify(userRepository, times(1))
                .findByIdInOrderByIdDesc(eq(ids), eq(pageable));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void delete_whenUserExists_deleteUser() {
        User user = getDefaultUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(user.getId());

        subject.delete(user.getId());

        verify(userRepository, times(1)).deleteById(user.getId());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void delete_whenUserNotExists_throwNotFound() {
        User user = getDefaultUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> subject.delete(user.getId()));
        verify(userRepository, times(1)).findById(user.getId());
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(userRepository);
    }
}
