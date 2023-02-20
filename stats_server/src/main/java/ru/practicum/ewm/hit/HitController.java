package ru.practicum.ewm.hit;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.HitDto;

@Slf4j
@RestController
@RequestMapping(path = "/hit")
@RequiredArgsConstructor
public class HitController {

    private final HitService service;

    @PostMapping
    public ResponseEntity<Object> create(@Validated @RequestBody HitDto hitDto) {
        Hit saved = service.add(HitMapper.toHit(hitDto));
        log.info("Saved: {}", saved.toString());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
