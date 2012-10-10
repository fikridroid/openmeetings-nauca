/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") +  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openmeetings.app.persistence.beans.lang;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@NamedQueries({
	@NamedQuery(name = "getFieldCount", query = "SELECT COUNT(fv) FROM Fieldvalues flv WHERE flv.deleted LIKE 'false' ")
})
@Table(name = "fieldvalues")
public class Fieldvalues implements Serializable {
	private static final long serialVersionUID = -3439614511218028085L;
	@Id
	@Column(name="fieldvalues_id")
	private Long fieldvalues_id;
	@Column(name="name")
	private String name;
	@Column(name="starttime")
	private Date starttime;
	@Column(name="updatetime")
	private Date updatetime;
	@Column(name="deleted")
	private String deleted;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name="fieldvalues_id")
	private Set<Fieldlanguagesvalues> fieldlanguagesvalues;
	
	@Transient
	private Fieldlanguagesvalues fieldlanguagesvalue;
	
	public Fieldvalues() {
		super();
	}
	
	public Long getFieldvalues_id() {
		return fieldvalues_id;
	}
	public void setFieldvalues_id(Long fieldvalues_id) {
		this.fieldvalues_id = fieldvalues_id;
	}	

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Date getStarttime() {
		return starttime;
	}
	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}
    
	public Date getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}
	
	public String getDeleted() {
		return deleted;
	}
	public void setDeleted(String deleted) {
		this.deleted = deleted;
	}

	public Set<Fieldlanguagesvalues> getFieldlanguagesvalues() {
		return fieldlanguagesvalues;
	}
	public void setFieldlanguagesvalues(Set<Fieldlanguagesvalues> fieldlanguagesvalues) {
		this.fieldlanguagesvalues = fieldlanguagesvalues;
	}

	public Fieldlanguagesvalues getFieldlanguagesvalue() {
		return fieldlanguagesvalue;
	}
	public void setFieldlanguagesvalue(Fieldlanguagesvalues fieldlanguagesvalue) {
		this.fieldlanguagesvalue = fieldlanguagesvalue;
	}
}
