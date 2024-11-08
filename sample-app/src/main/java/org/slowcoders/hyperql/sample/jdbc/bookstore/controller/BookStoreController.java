package org.slowcoders.hyperql.sample.jdbc.bookstore.controller;

import org.slowcoders.hyperql.HyperStorage;
import org.slowcoders.hyperql.HyperStorageController;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hql/bookstore")
public class BookStoreController extends HyperStorageController.CRUD implements HyperStorageController.ListAll {

    public BookStoreController(HyperStorage storage, ConversionService conversionService) {
        super(storage, "bookstore", conversionService);
    }

}
