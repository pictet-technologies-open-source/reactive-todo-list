package com.pictet.technologies.opensource.reactive.todolist.rest.mapper;

import com.pictet.technologies.opensource.reactive.todolist.rest.api.ItemResource;
import com.pictet.technologies.opensource.reactive.todolist.rest.api.ItemUpdateResource;
import com.pictet.technologies.opensource.reactive.todolist.rest.api.NewItemResource;
import com.pictet.technologies.opensource.reactive.todolist.model.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    ItemResource toResource(Item item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    Item toModel(NewItemResource item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    void update(ItemUpdateResource updateResource, @MappingTarget Item item);

}
