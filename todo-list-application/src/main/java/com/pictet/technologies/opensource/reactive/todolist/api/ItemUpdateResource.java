package com.pictet.technologies.opensource.reactive.todolist.api;

import com.pictet.technologies.opensource.reactive.todolist.model.ItemStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class ItemUpdateResource {

    @NotBlank
    private String description;

    @NotNull
    private ItemStatus status;

}
