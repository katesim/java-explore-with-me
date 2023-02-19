package ru.practicum.ewm.stats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.transaction.AfterTransaction;
import ru.practicum.ewm.hit.Hit;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static ru.practicum.ewm.hit.HitTestUtils.getDefaultHit;

@DataJpaTest
public class StatsRepositoryTest {

    private static final LocalDateTime DEFAULT_TIMESTAMP = LocalDateTime.of(2022, 9, 1, 0, 0, 0);
    private static final LocalDateTime START_TIMESTAMP = LocalDateTime.of(2022, 9, 1, 0, 0, 0);
    private static final LocalDateTime END_TIMESTAMP = LocalDateTime.of(2022, 10, 1, 0, 0, 0);

    private static final String URI_1 = "events/1";
    private static final String URI_2 = "events/2";
    private static final String IP_1 = "111.1.0.7";
    private static final String IP_2 = "193.1.14.7";

    @Autowired
    private TestEntityManager em;
    @Autowired
    private StatsRepository repo;

    @AfterTransaction
    public void showCountAfterTransaction() {
        System.out.println("Item count after tx: " + repo.count());
    }

    private List<Hit> getHits() {
        return Arrays.asList(
                getDefaultHit().toBuilder()
                        .uri(URI_1)
                        .ip(IP_1)
                        .timestamp(DEFAULT_TIMESTAMP)
                        .build(),
                getDefaultHit().toBuilder()
                        .uri(URI_2)
                        .ip(IP_2)
                        .timestamp(DEFAULT_TIMESTAMP)
                        .build(),
                getDefaultHit().toBuilder()
                        .uri(URI_2)
                        .ip(IP_2)
                        .timestamp(DEFAULT_TIMESTAMP.plusSeconds(1))
                        .build()
        );
    }

    @BeforeEach
    void fillData() {
        getHits().forEach((hit) -> em.persist(hit));
    }

    @Test
    void count() {
        List<HitCount> result = repo.countHits(START_TIMESTAMP, END_TIMESTAMP);

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getHits(), is(2L));
        assertThat(result.get(0).getUri(), is(URI_2));
        assertThat(result.get(1).getHits(), is(1L));
        assertThat(result.get(1).getUri(), is(URI_1));
    }

    @Test
    void count_withUriFilter() {
        List<HitCount> result = repo.countHits(START_TIMESTAMP, END_TIMESTAMP, List.of(URI_1));

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getHits(), is(1L));
        assertThat(result.get(0).getUri(), is(URI_1));
    }

    @Test
    void countUniq() {
        List<HitCount> result = repo.countHitsUniq(START_TIMESTAMP, END_TIMESTAMP);

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getHits(), is(1L));
        assertThat(result.get(1).getHits(), is(1L));
        assertThat(result.stream()
                .map(HitCount::getUri)
                .collect(Collectors.toList()),
                containsInAnyOrder(URI_1, URI_2));
    }

    @Test
    void countUniq_withUriFilter() {
        List<HitCount> result = repo.countHitsUniq(START_TIMESTAMP, END_TIMESTAMP, List.of(URI_2));

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getHits(), is(1L));
        assertThat(result.get(0).getUri(), is(URI_2));
    }
}
