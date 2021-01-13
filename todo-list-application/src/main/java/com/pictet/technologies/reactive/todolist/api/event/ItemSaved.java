package com.pictet.technologies.reactive.todolist.api.event;

import com.pictet.technologies.reactive.todolist.api.ItemResource;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ItemSaved implements Event {

    private ItemResource item;

}
