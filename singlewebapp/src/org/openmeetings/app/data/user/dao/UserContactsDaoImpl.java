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
package org.openmeetings.app.data.user.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.openmeetings.app.OpenmeetingsVariables;
import org.openmeetings.app.data.user.Usermanagement;
import org.openmeetings.app.persistence.beans.user.UserContacts;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class UserContactsDaoImpl {
	
	private static final Logger log = Red5LoggerFactory.getLogger(UserContactsDaoImpl.class, OpenmeetingsVariables.webAppRootKey);
	@PersistenceContext
	private EntityManager em;
    @Autowired
    private Usermanagement userManagement;

	public Long addUserContact(Long user_id, Long ownerId, Boolean pending, String hash) {
		try {
			
			UserContacts userContact = new UserContacts();
			userContact.setInserted(new Date());
			userContact.setOwner(userManagement.getUserById(ownerId));
			userContact.setContact(userManagement.getUserById(user_id));
			userContact.setPending(pending);
			userContact.setHash(hash);
			
			userContact = em.merge(userContact);
			Long userContactId = userContact.getUserContactId();
			
			return userContactId;			
		} catch (Exception e) {
			log.error("[addUserContact]",e);
		}
		return null;
	}
	
	public Long addUserContactObj(UserContacts userContact) {
		try {
			
			userContact.setInserted(new Date());
			
			userContact = em.merge(userContact);
			Long userContactId = userContact.getUserContactId();
			
			return userContactId;			
		} catch (Exception e) {
			log.error("[addUserContact]",e);
		}
		return null;
	}
	
	public Integer deleteUserContact(Long userContactDeleteId) {
		try {
			
			String hql = "delete from UserContacts u where u.userContactId = :userContactDeleteId";
			
			Query query = em.createQuery(hql);
			query.setParameter("userContactDeleteId", userContactDeleteId);
	        int rowCount = query.executeUpdate();
			
			return rowCount;			
		} catch (Exception e) {
			log.error("[deleteUserContact]",e);
		}
		return null;
	}
	
	public Integer deleteAllUserContacts(Long ownerId) {
		try {
			
			String hql = "delete from UserContacts u where u.owner.user_id = :ownerId";
			
			Query query = em.createQuery(hql);
	        query.setParameter("ownerId",ownerId);
	        int rowCount = query.executeUpdate();
			
			return rowCount;			
		} catch (Exception e) {
			log.error("[deleteAllUserContacts]",e);
		}
		return null;
	}
	
	public Long checkUserContacts(Long user_id, Long ownerId) {
		try {
			
			String hql = "select count(c.userContactId) from UserContacts c " +
							"where c.contact.user_id = :user_id AND c.owner.user_id = :ownerId ";
			
			TypedQuery<Long> query = em.createQuery(hql, Long.class); 
			query.setParameter("user_id", user_id);
			query.setParameter("ownerId", ownerId);
			List<Long> ll = query.getResultList();
			
			log.info("checkUserContacts" + ll.get(0));
			
			return ll.get(0);
			
		} catch (Exception e) {
			log.error("[checkUserContacts]",e);
		}
		return null;
	}
	
	public UserContacts getContactsByHash(String hash) {
		try {
			
			String hql = "select c from UserContacts c " +
							"where c.hash like :hash ";
			
			TypedQuery<UserContacts> query = em.createQuery(hql, UserContacts.class); 
			query.setParameter("hash", hash);
			List<UserContacts> ll = query.getResultList();
			
			if (ll.size() > 0) {
				return ll.get(0);
			}
			
			
		} catch (Exception e) {
			log.error("[getContactsByHash]",e);
		}
		return null;
	}
	
	public List<UserContacts> getContactsByUserAndStatus(Long ownerId, Boolean pending) {
		try {
			
			String hql = "select c from UserContacts c " +
							"where c.owner.user_id = :ownerId " +
							"AND c.pending = :pending " +
							"AND c.contact.deleted <> 'true'";
			
			TypedQuery<UserContacts> query = em.createQuery(hql, UserContacts.class);
			query.setParameter("ownerId", ownerId);
			query.setParameter("pending", pending);
			List<UserContacts> ll = query.getResultList();
			
			return ll;
			
		} catch (Exception e) {
			log.error("[getContactsByUserAndStatus]",e);
		}
		return null;
	}
	
	public UserContacts getUserContactByShareCalendar(Long contactId,
			Boolean shareCalendar, Long userId) {
		try {

			String hql = "select c from UserContacts c "
					+ "where c.contact.user_id = :userId "
					+ "AND c.owner.user_id = :contactId "
					+ "AND c.shareCalendar = :shareCalendar "
					+ "AND c.contact.deleted <> 'true'";

			TypedQuery<UserContacts> query = em.createQuery(hql,
					UserContacts.class);
			query.setParameter("contactId", contactId);
			query.setParameter("userId", userId);
			query.setParameter("shareCalendar", shareCalendar);
			List<UserContacts> ll = query.getResultList();

			if (ll.size() > 0) {
				return ll.get(0);
			}

		} catch (Exception e) {
			log.error("[getUserContactByShareCalendar]", e);
		}
		return null;
	}

	public List<UserContacts> getContactsByShareCalendar(Long contactId, Boolean shareCalendar) {
		try {
			
			String hql = "select c from UserContacts c " +
							"where c.contact.user_id = :contactId " +
							"AND c.shareCalendar = :shareCalendar " +
							"AND c.contact.deleted <> 'true'";
			
			TypedQuery<UserContacts> query = em.createQuery(hql, UserContacts.class); 
			query.setParameter("contactId", contactId);
			query.setParameter("shareCalendar", shareCalendar);
			List<UserContacts> ll = query.getResultList();
			
			return ll;
			
		} catch (Exception e) {
			log.error("[getContactsByShareCalendar]",e);
		}
		return null;
	}
	
	public List<UserContacts> getContactRequestsByUserAndStatus(Long user_id, Boolean pending) {
		try {
			
			String hql = "select c from UserContacts c " +
							"where c.contact.user_id = :user_id " +
							"AND c.pending = :pending " +
							"AND c.contact.deleted <> 'true'";
			
			TypedQuery<UserContacts> query = em.createQuery(hql, UserContacts.class); 
			query.setParameter("user_id", user_id);
			query.setParameter("pending", pending);
			List<UserContacts> ll = query.getResultList();
			
			return ll;
			
		} catch (Exception e) {
			log.error("[getContactRequestsByUserAndStatus]",e);
		}
		return null;
	}
	
	public UserContacts getUserContacts(Long userContactId) {
		try {
			
			String hql = "select c from UserContacts c " +
							"where c.userContactId = :userContactId";
			
			TypedQuery<UserContacts> query = em.createQuery(hql, UserContacts.class); 
			query.setParameter("userContactId", userContactId);
			UserContacts userContacts = null;
			try {
				userContacts = query.getSingleResult();
		    } catch (NoResultException ex) {
		    }
			
			return userContacts;
			
		} catch (Exception e) {
			log.error("[getUserContacts]",e);
		}
		return null;
	}
	
	public List<UserContacts> getUserContacts() {
		try {
			
			String hql = "select c from UserContacts c ";
			
			TypedQuery<UserContacts> query = em.createQuery(hql, UserContacts.class); 
			List<UserContacts> userContacts = query.getResultList();
			
			return userContacts;
			
		} catch (Exception e) {
			log.error("[getUserContacts]",e);
		}
		return null;
	}
	
	public Long updateContactStatus(Long userContactId, Boolean pending) {
		try {
			
			UserContacts userContacts = this.getUserContacts(userContactId);
			
			if (userContacts == null) {
				return null;
			}
			userContacts.setPending(pending);
			userContacts.setUpdated(new Date());
			
			if (userContacts.getUserContactId() == 0) {
				em.persist(userContacts);
		    } else {
		    	if (!em.contains(userContacts)) {
		    		em.merge(userContacts);
			    }
			}
			
			return userContactId;
			
		} catch (Exception e) {
			log.error("[updateContactStatus]",e);
		}
		return null;
	}
	
	public void updateContact(UserContacts userContacts) {
		try {
			userContacts.setUpdated(new Date());
			
			if (userContacts.getUserContactId() == 0) {
				em.persist(userContacts);
		    } else {
		    	if (!em.contains(userContacts)) {
		    		em.merge(userContacts);
			    }
			}
			
		} catch (Exception e) {
			log.error("[updateContact]",e);
		}
	}

}
