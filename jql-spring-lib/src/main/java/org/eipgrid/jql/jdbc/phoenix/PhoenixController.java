package org.eipgrid.jql.jdbc.phoenix;

//@Controller
//@RequestMapping("/api/phoenix")
public class PhoenixController {

    private final PhoenixRepository phoenixRepository;


    public PhoenixController(PhoenixRepository phoenixRepository) {
        this.phoenixRepository = phoenixRepository;

    }
}
