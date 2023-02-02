package org.eipgrid.jql.sample.jpa.petclinic.controller;

import org.eipgrid.jql.JqlService;
import org.eipgrid.jql.JqlStorageController;
import org.eipgrid.jql.jdbc.JdbcJqlService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jql/petclinic")
public class PetClinicController extends JqlStorageController.CRUD implements JqlStorageController.ListAll {

    public PetClinicController(JqlService service) {
        super(service, "petclinic.");
    }

}
