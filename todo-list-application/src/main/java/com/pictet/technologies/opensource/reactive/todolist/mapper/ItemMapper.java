package com.pictet.technologies.opensource.reactive.todolist.mapper;

import com.pictet.technologies.opensource.reactive.todolist.api.ItemResource;
import com.pictet.technologies.opensource.reactive.todolist.api.ItemUpdateResource;
import com.pictet.technologies.opensource.reactive.todolist.api.NewItemResource;
import com.pictet.technologies.opensource.reactive.todolist.api.event.EventMessage;
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

    default EventMessage toEventMessage(ChangeStreamEvent<Item> event) {

        if(event.getOperationType() == null) {
            return null;
        }

        final EventMessage eventMessage = new EventMessage();

        switch (event.getOperationType()) {
            case DELETE:
                // In case of deletion, the body is not set so we need to extract the objectId from the raw document
                eventMessage.setEvent(new ItemDeleted().setItemId(((BsonObjectId) event.getRaw()
                        .getDocumentKey().get("_id")).getValue().toString()));
                eventMessage.setEventType(ItemDeleted.class.getSimpleName());
                break;
            case INSERT:
            case UPDATE:
            case REPLACE:
                // Item saved
                eventMessage.setEvent(new ItemSaved().setItem(toResource(event.getBody())));
                eventMessage.setEventType(ItemSaved.class.getSimpleName());
                break;
            default:
                throw new UnsupportedOperationException(
                        String.format("The Mongo operation type [%s] is not supported", event.getOperationType()));
        }

        return eventMessage;
    }

}
