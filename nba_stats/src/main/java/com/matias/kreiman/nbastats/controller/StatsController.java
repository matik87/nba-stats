package com.matias.kreiman.nbastats.controller;

import com.matias.kreiman.nbastats.dto.PlayerStatsBatchRequest;
import com.matias.kreiman.nbastats.kafka.StatsProducer;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/stats")
@Validated
public class StatsController {

    private final StatsProducer producer;

    public StatsController(StatsProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void ingestStats(@Valid @RequestBody PlayerStatsBatchRequest request) {
        producer.send(request.getStats());
    }
}