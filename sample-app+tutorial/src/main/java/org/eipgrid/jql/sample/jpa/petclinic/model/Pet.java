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

import lombok.Getter;
import lombok.Setter;
import org.eipgrid.jql.JqlService;
import org.eipgrid.jql.jpa.JPARepositoryBase;
import org.eipgrid.jql.sample.jpa.petclinic.model.base.NamedEntity;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Simple business object representing a pet.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@Entity
@Table(schema = "petclinic", name = "pets")
public class Pet extends NamedEntity {


	@Column(name = "birth_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Getter @Setter
	private LocalDate birthDate;

	@ManyToOne()
	@JoinColumn(name = "type_id")
	@Getter @Setter
	private PetType type;

//	@ManyToOne()
//	private Owner owner;
//
//	@Transient
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "pet_id")
	@Getter
	private Set<Visit> visits = new LinkedHashSet<>();

	public Collection<Visit> getVisits() {
		return this.visits;
	}

	public void addVisit(Visit visit) {
		getVisits().add(visit);
	}

	@org.springframework.stereotype.Repository
	public static class Repository extends JPARepositoryBase<Pet, Integer> {

		public Repository(JqlService service) {
			super(service, Pet.class);
		}

		@Override
		public Integer getEntityId(Pet pet) {
			return pet.getId();
		}
	}
}
