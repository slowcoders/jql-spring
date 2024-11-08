package org.slowcoders.hyperql.sample.jpa.bookstore_jpa.service;

import org.slowcoders.hyperql.HyperStorage;
import org.slowcoders.hyperql.jdbc.JdbcStorage;
import org.slowcoders.hyperql.sample.jpa.bookstore_jpa.model.Customer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class BookStoreJpaService  {

    private final JdbcStorage storage;

    public BookStoreJpaService(JdbcStorage storage) {
        this.storage = storage;
    }

    public HyperStorage getStorage() {
        return this.storage;
    }

//    @PostConstruct
//    void initData() throws IOException {
//        long cntCustomer = storage.loadJpaTable(Customer.class).count(null);
//        if (cntCustomer == 0) {
//            loadData();
//        }
//    }
//    public void loadData() throws IOException {
//        String dbType = storage.getDbType();
//        ClassPathResource resource = new ClassPathResource("db/" + dbType + "/bookstore_jpa-data.sql");
//        BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
//        StringBuilder sql = new StringBuilder();
//        for (String s = null; (s = br.readLine()) != null; ) {
//            sql.append(s);
//            if (s.trim().endsWith(";")) {
//                storage.getJdbcTemplate().update(sql.toString());
//                sql.setLength(0);
//            }
//        }
//    }

}
