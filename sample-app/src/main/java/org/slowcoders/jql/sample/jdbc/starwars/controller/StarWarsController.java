package org.slowcoders.jql.sample.jdbc.starwars.controller;

import org.slowcoders.jql.JqlStorage;
import org.slowcoders.jql.JqlStorageController;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jql/starwars")
public class StarWarsController extends JqlStorageController.CRUD implements JqlStorageController.ListAll {

    public StarWarsController(JqlStorage storage, ConversionService conversionService) {
        super(storage, "starwars.", conversionService);
    }

}
