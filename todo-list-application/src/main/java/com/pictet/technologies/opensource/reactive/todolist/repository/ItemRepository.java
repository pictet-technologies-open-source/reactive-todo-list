package com.pictet.technologies.opensource.reactive.todolist.repository;

import com.pictet.technologies.opensource.reactive.todolist.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends MongoRepository<Item, String> {


}
