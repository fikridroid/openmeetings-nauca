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
package org.openmeetings.app.data.basic;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.openmeetings.app.OpenmeetingsVariables;
import org.openmeetings.app.persistence.beans.lang.FieldLanguage;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author sebastianwagner
 * 
 */
@Transactional
public class FieldLanguageDaoImpl {

	private static final Logger log = Red5LoggerFactory.getLogger(
			FieldLanguageDaoImpl.class, OpenmeetingsVariables.webAppRootKey);

	@PersistenceContext
	private EntityManager em;

	public Long addLanguage(String langName, Boolean langRtl, String code) {
		try {

			FieldLanguage fl = new FieldLanguage();
			fl.setStarttime(new Date());
			fl.setDeleted("false");
			fl.setName(langName);
			fl.setRtl(langRtl);
			fl.setCode(code);

			fl = em.merge(fl);
			Long languages_id = fl.getLanguage_id();

			return languages_id;
		} catch (Exception ex2) {
			log.error("[addLanguage]: ", ex2);
		}
		return null;
	}

	public void emptyFieldLanguage() {
		try {

			// TODO delete hql query doesn't work, must be repared
			em.createQuery("delete from FieldLanguage");
		} catch (Exception ex2) {
			log.error("[getConfKey]: ", ex2);
		}
	}

	public Long updateFieldLanguage(Long language_id, String langName,
			String code, String deleted) {
		try {
			FieldLanguage fl = this.getFieldLanguageById(language_id);
			fl.setUpdatetime(new Date());
			if (langName.length() > 0)
				fl.setName(langName);
			if (code.length() > 0)
				fl.setCode(code);
			fl.setDeleted(deleted);
			this.updateLanguage(fl);
			return language_id;
		} catch (Exception ex2) {
			log.error("[updateLanguage]: ", ex2);
		}
		return new Long(-1);
	}

	private void updateLanguage(FieldLanguage fl) throws Exception {
		if (fl.getLanguage_id() == null) {
			em.persist(fl);
		} else {
			if (!em.contains(fl)) {
				em.merge(fl);
			}
		}
	}

	public FieldLanguage getFieldLanguageById(Long language_id) {
		try {
			String hql = "select c from FieldLanguage as c "
					+ "WHERE c.deleted <> :deleted "
					+ "AND c.language_id = :language_id";
			TypedQuery<FieldLanguage> query = em.createQuery(hql, FieldLanguage.class);
			query.setParameter("deleted", "true");
			query.setParameter("language_id", language_id);
			FieldLanguage fl = null;
			try {
				fl = query.getSingleResult();
			} catch (NoResultException ex) {
			}
			return fl;
		} catch (Exception ex2) {
			log.error("[getLanguageById]: ", ex2);
		}
		return null;
	}

	public List<FieldLanguage> getLanguages() {
		try {
			String hql = "select c from FieldLanguage as c "
					+ "WHERE c.deleted <> :deleted ";
			TypedQuery<FieldLanguage> query = em.createQuery(hql, FieldLanguage.class);
			query.setParameter("deleted", "true");
			List<FieldLanguage> ll = query.getResultList();
			return ll;
		} catch (Exception ex2) {
			log.error("[getLanguages]: ", ex2);
		}
		return null;
	}

}
