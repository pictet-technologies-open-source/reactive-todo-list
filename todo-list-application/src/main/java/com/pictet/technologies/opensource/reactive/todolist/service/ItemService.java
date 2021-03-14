package com.pictet.technologies.opensource.reactive.todolist.service;

import com.mongodb.client.model.changestream.OperationType;
import com.pictet.technologies.opensource.reactive.todolist.api.ItemPatchResource;
import com.pictet.technologies.opensource.reactive.todolist.api.ItemResource;
import com.pictet.technologies.opensource.reactive.todolist.api.ItemUpdateResource;
import com.pictet.technologies.opensource.reactive.todolist.api.NewItemResource;
import com.pictet.technologies.opensource.reactive.todolist.api.event.Event;
import com.pictet.technologies.opensource.reactive.todolist.exception.ItemNotFoundException;
import com.pictet.technologies.opensource.reactive.todolist.exception.UnexpectedItemVersionException;
import com.pictet.technologies.opensource.reactive.todolist.mapper.ItemMapper;
import com.pictet.technologies.opensource.reactive.todolist.model.Item;
import com.pictet.technologies.opensource.reactive.todolist.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ItemService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.by("lastModifiedDate"));

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public Mono<ItemResource> create(final NewItemResource item) {

        return itemRepository.save(itemMapper.toModel(item))
                .map(itemMapper::toResource);
    }

    public Flux<ItemResource> findAll() {
        return itemRepository.findAll(DEFAULT_SORT)
                .map(itemMapper::toResource);
    }

    public Mono<ItemResource> findById(final String id, final Long version) {

        return findItemById(id, version)
                .map(itemMapper::toResource);
    }

    public Flux<Event> listenToEvents() {
        final ChangeStreamOptions changeStreamOptions = ChangeStreamOptions.builder()
                .returnFullDocumentOnUpdate()
                .filter(Aggregation.newAggregation(Item.class,
                        Aggregation.match(Criteria.where("operationType")
                                .in(OperationType.INSERT.getValue(),
                                    OperationType.REPLACE.getValue(),
                                    OperationType.UPDATE.getValue(),
                                    OperationType.DELETE.getValue()))))
                .build();

        return reactiveMongoTemplate.changeStream("item", changeStreamOptions, Item.class)
                .map(itemMapper::toEvent);
    }

    public Mono<ItemResource> update(final String id, final Long version, final ItemUpdateResource itemUpdateResource) {

        return findItemById(id, version)
                .flatMap(item -> {
                    itemMapper.update(itemUpdateResource, item);
                    return itemRepository.save(item);
                })
                .map(itemMapper::toResource);
    }

    @SuppressWarnings({"OptionalAssignedToNull", "OptionalGetWithoutIsPresent"})
    public Mono<ItemResource> patch(final String id, final Long version, final ItemPatchResource itemPatchResource) {

        return findItemById(id, version)
                .flatMap(item -> {
                    if (itemPatchResource.getDescription() != null) {
                        // The description has been provided in the patch
                        item.setDescription(itemPatchResource.getDescription().get());
                    }

                    if (itemPatchResource.getStatus() != null) {
                        // The status has been provided in the patch
                        item.setStatus(itemPatchResource.getStatus().get());
                    }
                    return itemRepository.save(item);
                })
                .map(itemMapper::toResource);
    }

    public Mono<Void> deleteById(final String id, final Long version) {

        return findItemById(id, version)
                .flatMap(itemRepository::delete);
    }

    /**
     * Find an item
     *
     * @param id              Id of the item
     * @param expectedVersion Expected version of the item (optional)
     * @return the item mono
     * @throws ItemNotFoundException          if the item with the provided Id does not exist
     * @throws UnexpectedItemVersionException if the item has a different version
     */
    private Mono<Item> findItemById(final String id, final Long expectedVersion) {

        return itemRepository.findById(id)
                .switchIfEmpty(Mono.error(new ItemNotFoundException(id)))
                .handle((item, sink) -> {
                    if (expectedVersion != null && !expectedVersion.equals(item.getVersion())) {
                        sink.error(new UnexpectedItemVersionException(expectedVersion, item.getVersion()));
                    } else {
                        sink.next(item);
                    }
                });
    }

}
