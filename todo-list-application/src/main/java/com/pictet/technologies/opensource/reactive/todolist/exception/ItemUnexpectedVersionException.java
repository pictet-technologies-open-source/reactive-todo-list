package com.pictet.technologies.opensource.reactive.todolist.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class ItemUnexpectedVersionException extends NotFoundException {

    public ItemUnexpectedVersionException(Long expectedVersion, Long foundVersion) {
        super(String.format("The item has a different version than the expected one. Expected [%s], found [%s]",
                expectedVersion, foundVersion));
    }

}
