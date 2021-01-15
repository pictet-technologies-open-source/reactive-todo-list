package com.pictet.technologies.opensource.reactive.todolist.repository;

import com.pictet.technologies.opensource.reactive.todolist.model.Item;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ItemRepository extends ReactiveMongoRepository<Item, String> {


}
