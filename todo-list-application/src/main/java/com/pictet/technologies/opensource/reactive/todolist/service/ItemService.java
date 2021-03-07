package com.pictet.technologies.opensource.reactive.todolist.service;

import com.pictet.technologies.opensource.reactive.todolist.exception.ItemNotFoundException;
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

    public Mono<Void> deleteById(final String id) {

        return verifyExistence(id)
                .flatMap(exists -> itemRepository.deleteById(id));
    }

    /**
     * Find an item
     *
     * @param id identifier of the item
     * @return the item
     * @throws ItemNotFoundException if the item with the provided identifier does not exist
     */
    public Mono<Item> findById(final String id) {

        return itemRepository.findById(id)
                .switchIfEmpty(Mono.error(new ItemNotFoundException(id)));
    }

    private Mono<Boolean> verifyExistence(String id) {

        return itemRepository.existsById(id).handle((exists, sink) -> {
            if (! exists) {
                sink.error(new ItemNotFoundException(id));
            } else {
                sink.next(true);
            }
        });
    }

}
