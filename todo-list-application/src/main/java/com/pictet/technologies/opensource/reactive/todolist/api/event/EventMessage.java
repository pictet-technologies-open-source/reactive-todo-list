package com.pictet.technologies.opensource.reactive.todolist.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventMessage {

    private String eventType;
    private Event event;

}
