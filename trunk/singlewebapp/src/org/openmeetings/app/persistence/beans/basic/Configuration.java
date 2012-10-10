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
package org.openmeetings.app.persistence.beans.basic;

import java.io.Serializable;
import java.util.Date;

import org.openmeetings.app.persistence.beans.user.Users;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "configuration")
public class Configuration implements Serializable {
	
	private static final long serialVersionUID = -6129473946508963339L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	@Column(name="configuration_id")
	private Long configuration_id;
	@Column(name="conf_key")
	private String conf_key;
	@Column(name="conf_value")
	private String conf_value;	
	@Column(name="starttime")
	private Date starttime;
	@Column(name="updatetime")
	private Date updatetime;
	@Lob
	@Column(name="comment_field", length=2048)
	private String comment;
	@Column(name="deleted")
	private String deleted;
	@Column(name="user_id")
	private Long user_id;

	@Transient
	private Users users;
	
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getConf_key() {
        return conf_key;
    }
    public void setConf_key(String conf_key) {
        this.conf_key = conf_key;
    }
    
    public String getConf_value() {
        return conf_value;
    }
    public void setConf_value(String conf_value) {
        this.conf_value = conf_value;
    }
    
    public Long getConfiguration_id() {
        return configuration_id;
    }
    public void setConfiguration_id(Long configuration_id) {
        this.configuration_id = configuration_id;
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
	
    public Long getUser_id() {
		return user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

 public Users getUsers() {
     return users;
 }
 public void setUsers(Users users) {
     this.users = users;
 }

	
}
