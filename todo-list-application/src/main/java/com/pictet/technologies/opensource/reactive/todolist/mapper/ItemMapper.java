package com.pictet.technologies.opensource.reactive.todolist.mapper;

import com.pictet.technologies.opensource.reactive.todolist.api.ItemResource;
import com.pictet.technologies.opensource.reactive.todolist.api.ItemUpdateResource;
import com.pictet.technologies.opensource.reactive.todolist.api.NewItemResource;
import com.pictet.technologies.opensource.reactive.todolist.api.event.Event;
import com.pictet.technologies.opensource.reactive.todolist.api.event.ItemDeleted;
import com.pictet.technologies.opensource.reactive.todolist.api.event.ItemSaved;
import com.pictet.technologies.opensource.reactive.todolist.model.Item;
import org.bson.BsonObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.mongodb.core.ChangeStreamEvent;

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

    default Event toEvent(final ChangeStreamEvent<Item> changeStreamEvent) {

        if(changeStreamEvent.getOperationType() == null) {
            return null;
        }

        final Event event;

        switch (changeStreamEvent.getOperationType()) {
            case DELETE:
                // In case of deletion, the body is not set so we need to extract the objectId from the raw document
                event = new ItemDeleted().setItemId(((BsonObjectId) changeStreamEvent.getRaw()
                        .getDocumentKey().get("_id")).getValue().toString());
                break;
            case INSERT:
            case UPDATE:
            case REPLACE:
                // Item saved
                event = new ItemSaved().setItem(toResource(changeStreamEvent.getBody()));
                break;
            default:
                throw new UnsupportedOperationException(
                        String.format("The Mongo operation type [%s] is not supported", changeStreamEvent.getOperationType()));
        }

        return event;
    }

}
