package org.slowcoders.hyperql.sample.jdbc.starwars.controller;

import org.slowcoders.hyperql.HyperStorage;
import org.slowcoders.hyperql.HyperStorageController;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hql/starwars")
public class StarWarsController extends HyperStorageController.CRUD implements HyperStorageController.ListAll {

    public StarWarsController(HyperStorage storage, ConversionService conversionService) {
        super(storage, "starwars.", conversionService);
    }

}
