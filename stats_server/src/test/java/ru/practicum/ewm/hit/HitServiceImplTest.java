package ru.practicum.ewm.hit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static ru.practicum.ewm.hit.HitTestUtils.getDefaultHit;

@ExtendWith(MockitoExtension.class)
class HitServiceImplTest {

    @Mock
    private HitRepository hitRepository;

    @InjectMocks
    private HitServiceImpl subject;

    @Test
    void add_hitSaved() {
        Hit hit = getDefaultHit();
        when(hitRepository.save(hit)).thenReturn(hit);

        Hit result = subject.add(hit);

        assertThat(result, is(hit));
        verify(hitRepository, times(1)).save(hit);
        verifyNoMoreInteractions(hitRepository);
    }
}
