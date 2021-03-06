package com.pictet.technologies.opensource.reactive.todolist.service;

import com.pictet.technologies.opensource.reactive.todolist.exception.ItemNotFoundException;
import com.pictet.technologies.opensource.reactive.todolist.model.Item;
import com.pictet.technologies.opensource.reactive.todolist.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.by("lastModifiedDate"));

    private final ItemRepository itemRepository;

    public List<Item> findAll() {
        return itemRepository.findAll(DEFAULT_SORT);
    }


    public Item save(final Item item) {
        if(item.getId() != null) {
            // Update
            verifyExistence(item.getId());
        }
        return itemRepository.save(item);
    }

    public void deleteById(final String id) {

        verifyExistence(id);
        itemRepository.deleteById(id);
    }

    /**
     * Find an item
     *
     * @param id identifier of the item
     * @return the item
     * @throws ItemNotFoundException if the item with the provided identifier does not exist
     */
    public Item findById(final String id) {

        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }

    private void verifyExistence(String id) {
        if(! itemRepository.existsById(id)) {
            throw new ItemNotFoundException(id);
        }
    }

}
