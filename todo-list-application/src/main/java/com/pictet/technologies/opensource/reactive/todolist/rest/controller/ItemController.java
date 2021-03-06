package com.pictet.technologies.opensource.reactive.todolist.rest.controller;

import com.pictet.technologies.opensource.reactive.todolist.rest.api.ItemPatchResource;
import com.pictet.technologies.opensource.reactive.todolist.rest.api.ItemResource;
import com.pictet.technologies.opensource.reactive.todolist.rest.api.ItemUpdateResource;
import com.pictet.technologies.opensource.reactive.todolist.rest.api.NewItemResource;
import com.pictet.technologies.opensource.reactive.todolist.rest.mapper.ItemMapper;
import com.pictet.technologies.opensource.reactive.todolist.model.Item;
import com.pictet.technologies.opensource.reactive.todolist.service.ItemService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.http.HttpHeaders.IF_MATCH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;

@RestController
@RequestMapping(value = "/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @ApiOperation("Create a new item")
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody final NewItemResource item) {

        final String id = itemService.save(itemMapper.toModel(item)).getId();

        return created(linkTo(ItemController.class).slash(id).toUri()).build();
    }

    @ApiOperation("Update an existing item")
    @PutMapping(value = "/{id}")
    public ResponseEntity<Void> update(@PathVariable @NotNull final String id,
                                     @Valid @RequestBody ItemUpdateResource itemUpdateResource) {

        // Find the item and update the instance
        final Item item = itemService.findById(id);
        itemMapper.update(itemUpdateResource, item);

        // Save the updated instance
        itemService.save(item);

        return noContent().build();
    }

    @ApiOperation("Patch an existing item following the patch merge RCF (https://tools.ietf.org/html/rfc7396)")
    @PatchMapping(value = "/{id}")
    @SuppressWarnings({"OptionalAssignedToNull", "OptionalGetWithoutIsPresent"})
    public ResponseEntity<Void> patch(@PathVariable @NotNull final String id,
                                     @Valid @RequestBody ItemPatchResource patch) {

        final Item item = itemService.findById(id);
        if (patch.getDescription() != null) {
            // The description has been provided in the patch
            item.setDescription(patch.getDescription().get());
        }

        if (patch.getStatus() != null) {
            // The status has been provided in the patch
            item.setStatus(patch.getStatus().get());
        }

        itemService.save(item);

        return noContent().build();
    }

    @ApiOperation("Find an item by its id")
    @GetMapping(value = "/{id}", produces = {APPLICATION_JSON_VALUE})
    public ItemResource findById(@PathVariable String id) {

        return itemMapper.toResource(itemService.findById(id));
    }

    @ApiOperation("Get a the list of items")
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<ItemResource> getAllItems() {

        return itemService.findAll().stream()
                .map(itemMapper::toResource)
                .collect(Collectors.toList());
    }


    @ApiOperation("Delete an item")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final String id,
                             @RequestHeader(name = IF_MATCH, required = false) Long version) {

        itemService.deleteById(id);
        return noContent().build();
    }

}
