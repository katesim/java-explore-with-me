package ru.practicum.ewm.hit;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HitController {

    @PostMapping
    public ResponseEntity<Object> create(@Validated @RequestBody HitDto hitDto) {
        log.info(hitDto.toString());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
