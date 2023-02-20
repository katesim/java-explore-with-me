package ru.practicum.ewm.hit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.transaction.AfterTransaction;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static ru.practicum.ewm.hit.HitTestUtils.getDefaultHit;

@DataJpaTest
public class HitRepositoryTest {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private HitRepository hitRepository;

    @AfterTransaction
    public void showCountAfterTransaction() {
        System.out.println("Item count after tx: " + hitRepository.count());
    }

    @Test
    void save() {
        Hit hit = getDefaultHit();

        Hit result = hitRepository.save(hit);

        assertThat(result.getId(),  is(notNullValue()));
        assertThat(result.getId(),  greaterThan(0L));
        assertThat(result.getApp(), is(hit.getApp()));
        assertThat(result.getUri(), is(hit.getUri()));
        assertThat(result.getIp(), is(hit.getIp()));
        assertThat(result.getTimestamp(), is(hit.getTimestamp()));
    }
}
