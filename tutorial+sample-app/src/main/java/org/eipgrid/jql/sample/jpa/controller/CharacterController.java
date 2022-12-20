package org.eipgrid.jql.sample.jpa.controller;

import org.eipgrid.jql.sample.jpa.domain.Character;
import org.eipgrid.jql.spring.JQLController;
import org.eipgrid.jql.spring.JQLService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jql-jpa/starwars")
@Profile("jpa")
public class CharacterController extends JQLController.SearchAndUpdate<Character, Long> {

    CharacterController(JQLService service) {
        super(new Character.Repository(service));
    }
}
