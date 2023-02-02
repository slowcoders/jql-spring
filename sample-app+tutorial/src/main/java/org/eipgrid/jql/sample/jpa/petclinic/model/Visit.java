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

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;
import org.eipgrid.jql.JqlService;
import org.eipgrid.jql.jpa.JPARepositoryBase;
import org.eipgrid.jql.sample.jpa.petclinic.model.base.BaseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;

/**
 * Simple JavaBean domain object representing a visit.
 *
 * @author Ken Krebs
 * @author Dave Syer
 */
@Entity
@Table(schema = "petclinic", name = "visits")
public class Visit extends BaseEntity {

	@Column(name = "visit_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Getter @Setter
	private LocalDate date;

	@NotEmpty
	@Getter @Setter
	private String description;

	/**
	 * Creates a new instance of Visit for the current date
	 */
	public Visit() {
		this.date = LocalDate.now();
	}

	@org.springframework.stereotype.Repository
	public static class Repository extends JPARepositoryBase<Visit, Integer> {

		public Repository(JqlService service) {
			super(service, Visit.class);
		}

		@Override
		public Integer getEntityId(Visit visit) {
			return visit.getId();
		}
	}
}
