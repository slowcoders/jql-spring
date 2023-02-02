/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eipgrid.jql.sample.jpa.petclinic.model;

import org.eipgrid.jql.JqlService;
import org.eipgrid.jql.jpa.JPARepositoryBase;
import org.eipgrid.jql.sample.jpa.petclinic.model.base.NamedEntity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Models a {@link Vet Vet's} specialty (for example, dentistry).
 *
 * @author Juergen Hoeller
 */
@Entity
@Table(schema = "petclinic", name = "specialties")
public class Specialty extends NamedEntity implements Serializable {
    @org.springframework.stereotype.Repository
    public static class Repository extends JPARepositoryBase<Specialty, Integer> {

        public Repository(JqlService service) {
            super(service, Specialty.class);
        }

        @Override
        public Integer getEntityId(Specialty specialty) {
            return specialty.getId();
        }
    }

}
