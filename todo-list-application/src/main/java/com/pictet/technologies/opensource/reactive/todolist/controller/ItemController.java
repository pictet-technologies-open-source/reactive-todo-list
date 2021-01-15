package com.pictet.technologies.opensource.reactive.todolist.controller;

import com.pictet.technologies.opensource.reactive.todolist.api.ItemPatchResource;
import com.pictet.technologies.opensource.reactive.todolist.api.ItemResource;
import com.pictet.technologies.opensource.reactive.todolist.api.ItemUpdateResource;
import com.pictet.technologies.opensource.reactive.todolist.api.NewItemResource;
import com.pictet.technologies.opensource.reactive.todolist.api.event.EventMessage;
import com.pictet.technologies.opensource.reactive.todolist.api.event.HeartBeat;
import com.pictet.technologies.opensource.reactive.todolist.config.ServerSentEventConfig;
import com.pictet.technologies.opensource.reactive.todolist.service.ItemService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Duration;

import static org.springframework.http.HttpHeaders.IF_MATCH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ServerSentEventConfig sseConfig;
    private final ItemService itemService;

    @ApiOperation("Get a the list of items")
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ItemResource> getAllItems() {
        return itemService.findAll();
    }

    @ApiOperation("Get the item event stream")
    @GetMapping(value = "events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<EventMessage>> getEventStream() {

        Flux<EventMessage> eventMessageFlux = itemService.listenToEvents();

        // Send a heart beart every x ms to keep the connection alive
        if(sseConfig.getHeartBeatDelayMs() > 0) {
            log.info("Send a heart beat every {}ms ", sseConfig.getHeartBeatDelayMs());
            final Flux<EventMessage> beats = Flux.interval(Duration.ofMillis(sseConfig.getHeartBeatDelayMs()))
                .map(sequence -> new EventMessage(HeartBeat.class.getSimpleName(),
                        new HeartBeat()));
            eventMessageFlux = Flux.merge(beats, eventMessageFlux);
        }

        return eventMessageFlux
                .map(event -> ServerSentEvent.<EventMessage>builder()
                    .retry(Duration.ofMillis(sseConfig.getReconnectionDelayMs()))
                    .data(event).build())
                .doFinally(signal -> log.info("Item event stream - {}", signal));
    }

    @ApiOperation("Create a new item")
    @PostMapping
    public Mono<ItemResource> create(@Valid @RequestBody final NewItemResource item) {
        return itemService.create(item);
    }

    @ApiOperation("Update an existing item")
    @PutMapping(value = "/{id}")
    public Mono<ItemResource> update(@PathVariable @NotNull final String id,
                                     @RequestHeader(name = IF_MATCH, required = false) Long version,
                                     @Valid @RequestBody ItemUpdateResource itemUpdateResource) {

        return itemService.update(id, version, itemUpdateResource);
    }

    @ApiOperation("Patch an existing item following the patch merge RCF (https://tools.ietf.org/html/rfc7386)")
    @PatchMapping(value = "/{id}")
    public Mono<ItemResource> update(@PathVariable @NotNull final String id,
                                     @RequestHeader(name = IF_MATCH, required = false) Long version,
                                     @Valid @RequestBody ItemPatchResource itemPatchResource) {

        return itemService.patch(id, version, itemPatchResource);
    }


    @ApiOperation("Find an item by its id")
    @GetMapping(value = "/{id}", produces = {APPLICATION_JSON_VALUE})
    public Mono<ItemResource> findById(@PathVariable String id) {
        return itemService.findById(id, null);
    }

    @ApiOperation("Delete an item")
    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable final String id,
                             @RequestHeader(name = IF_MATCH, required = false) Long version) {

        return itemService.deleteById(id, version);
    }

}
