package com.pictet.technologies.reactive.todolist.api;

import com.pictet.technologies.reactive.todolist.model.ItemStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Data
@Accessors(chain = true)
public class ItemPatchResource {

    private Optional<@NotBlank String> description;
    private Optional<@NotNull ItemStatus> status;

}
