package com.pictet.technologies.opensource.reactive.todolist.service;

import com.pictet.technologies.opensource.reactive.todolist.exception.ItemNotFoundException;
import com.pictet.technologies.opensource.reactive.todolist.exception.UnexpectedItemVersionException;
import com.pictet.technologies.opensource.reactive.todolist.model.Item;
import com.pictet.technologies.opensource.reactive.todolist.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ItemService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.by("lastModifiedDate"));

    private final ItemRepository itemRepository;

    public Flux<Item> findAll() {
        return itemRepository.findAll(DEFAULT_SORT);
    }

    public Mono<Item> save(final Item item) {

        if(item.getId() != null) {
            // Update
            return verifyExistence(item.getId())
                    .flatMap(exists -> itemRepository.save(item));
        }

        // Create
        return itemRepository.save(item);
    }

    public Mono<Void> deleteById(final String id, final Long version) {

        return findById(id, version)
                .flatMap(itemRepository::delete);
    }

    /**
     * Find an item
     *
     * @param id identifier of the item
     * @param version expected version of the item
     *
     * @return the item
     * @throws ItemNotFoundException if the item with the provided identifier does not exist
     * @throws UnexpectedItemVersionException if the actual version is different from the actual one
     */
    public Mono<Item> findById(final String id, final Long version) {

        return itemRepository.findById(id)
                .switchIfEmpty(Mono.error(new ItemNotFoundException(id)))
                .handle((item, sink) -> {
                    if(version != null && ! item.getVersion().equals(version)) {
                        sink.error(new UnexpectedItemVersionException(version, item.getVersion()));
                    } else {
                        sink.next(item);
                    }
                });
    }

    private Mono<Boolean> verifyExistence(String id) {

        return itemRepository.existsById(id).handle((exists, sink) -> {
            if (Boolean.FALSE.equals(exists)) {
                sink.error(new ItemNotFoundException(id));
            } else {
                sink.next(true);
            }
        });
    }

}
