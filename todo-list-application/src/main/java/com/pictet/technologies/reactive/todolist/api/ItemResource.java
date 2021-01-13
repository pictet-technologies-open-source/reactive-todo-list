package com.pictet.technologies.reactive.todolist.api;

import com.pictet.technologies.reactive.todolist.model.ItemStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain = true)
public class ItemResource {

    private String id;
    private Long version;

    private String description;
    private ItemStatus status;

    private Instant createdDate;
    private Instant lastModifiedDate;

}
