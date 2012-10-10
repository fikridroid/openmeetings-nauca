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
package org.openmeetings.servlet.outputhandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.openmeetings.app.OpenmeetingsVariables;
import org.openmeetings.app.data.basic.FieldLanguageDaoImpl;
import org.openmeetings.app.data.basic.Fieldmanagment;
import org.openmeetings.app.data.basic.Sessionmanagement;
import org.openmeetings.app.data.user.Usermanagement;
import org.openmeetings.app.persistence.beans.lang.FieldLanguage;
import org.openmeetings.app.persistence.beans.lang.Fieldlanguagesvalues;
import org.openmeetings.app.remote.red5.ScopeApplicationAdapter;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 
 * @author sebastianwagner
 * 
 */
public class LangExport extends HttpServlet {
	private static final long serialVersionUID = 243294279856160463L;
	private static final Logger log = Red5LoggerFactory.getLogger(
			LangExport.class, OpenmeetingsVariables.webAppRootKey);

	public Sessionmanagement getSessionManagement() {
		try {
			if (ScopeApplicationAdapter.initComplete) {
				ApplicationContext context = WebApplicationContextUtils
						.getWebApplicationContext(getServletContext());
				return context.getBean("sessionManagement", Sessionmanagement.class);
			}
		} catch (Exception err) {
			log.error("[getSessionManagement]", err);
		}
		return null;
	}

	public Usermanagement getUserManagement() {
		try {
			if (ScopeApplicationAdapter.initComplete) {
				ApplicationContext context = WebApplicationContextUtils
						.getWebApplicationContext(getServletContext());
				return context.getBean("userManagement", Usermanagement.class);
			}
		} catch (Exception err) {
			log.error("[getUserManagement]", err);
		}
		return null;
	}

	public Fieldmanagment getFieldmanagment() {
		try {
			if (ScopeApplicationAdapter.initComplete) {
				ApplicationContext context = WebApplicationContextUtils
						.getWebApplicationContext(getServletContext());
				return context.getBean("fieldmanagment", Fieldmanagment.class);
			}
		} catch (Exception err) {
			log.error("[getFieldmanagment]", err);
		}
		return null;
	}

	public FieldLanguageDaoImpl getFieldLanguageDaoImpl() {
		try {
			if (ScopeApplicationAdapter.initComplete) {
				ApplicationContext context = WebApplicationContextUtils
						.getWebApplicationContext(getServletContext());
				return context.getBean("fieldLanguageDaoImpl", FieldLanguageDaoImpl.class);
			}
		} catch (Exception err) {
			log.error("[getFieldLanguageDaoImpl]", err);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void service(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException,
			IOException {

		try {

			if (getUserManagement() == null
					|| getFieldLanguageDaoImpl() == null
					|| getFieldmanagment() == null
					|| getSessionManagement() == null) {
				return;
			}

			String sid = httpServletRequest.getParameter("sid");
			if (sid == null) {
				sid = "default";
			}
			log.debug("sid: " + sid);

			String language = httpServletRequest.getParameter("language");
			if (language == null) {
				language = "0";
			}
			Long language_id = Long.valueOf(language).longValue();
			log.debug("language_id: " + language_id);

			Long users_id = getSessionManagement().checkSession(sid);
			Long user_level = getUserManagement().getUserLevelByID(users_id);

			log.debug("users_id: " + users_id);
			log.debug("user_level: " + user_level);

			if (user_level != null && user_level > 0) {
				FieldLanguage fl = getFieldLanguageDaoImpl()
						.getFieldLanguageById(language_id);

				List<Fieldlanguagesvalues> flvList = getFieldmanagment().getMixedFieldValuesList(language_id);

				if (fl != null && flvList != null) {
					Document doc = createDocument(flvList, getFieldmanagment().getUntranslatedFieldValuesList(language_id));

					String requestedFile = fl.getName() + ".xml";

					httpServletResponse.reset();
					httpServletResponse.resetBuffer();
					OutputStream out = httpServletResponse.getOutputStream();
					httpServletResponse
							.setContentType("APPLICATION/OCTET-STREAM");
					httpServletResponse.setHeader("Content-Disposition",
							"attachment; filename=\"" + requestedFile + "\"");
					// httpServletResponse.setHeader("Content-Length", ""+
					// rf.length());

					this.serializetoXML(out, "UTF-8", doc);

					out.flush();
					out.close();
				}
			} else {
				log.debug("ERROR LangExport: not authorized FileDownload "
						+ (new Date()));
			}

		} catch (Exception er) {
			log.error("ERROR ", er);
			System.out.println("Error exporting: " + er);
			er.printStackTrace();
		}
	}

	public Document createDocument(List<Fieldlanguagesvalues> flvList, List<Fieldlanguagesvalues> untranslatedList) throws Exception {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("UTF-8");
		document.addComment("###############################################\n"
				+ "This File is auto-generated by the LanguageEditor \n"
				+ "to add new Languages or modify/customize it use the LanguageEditor \n"
				+ "see http://incubator.apache.org/openmeetings/LanguageEditor.html for Details \n"
				+ "###############################################");

		Element root = document.addElement("language");

		for (Fieldlanguagesvalues flv : flvList) {
			Element eTemp = root.addElement("string")
					.addAttribute("id", flv.getFieldvalues().getFieldvalues_id().toString())
					.addAttribute("name", flv.getFieldvalues().getName());
			Element value = eTemp.addElement("value");
			value.addText(flv.getValue());
		}

		//untranslated
		if (untranslatedList.size() > 0) {
			root.addComment("Untranslated strings");
			for (Fieldlanguagesvalues flv : untranslatedList) {
				Element eTemp = root.addElement("string")
						.addAttribute("id", flv.getFieldvalues().getFieldvalues_id().toString())
						.addAttribute("name", flv.getFieldvalues().getName());
				Element value = eTemp.addElement("value");
				value.addText(flv.getValue());
			}
		}

		return document;
	}

	public void serializetoXML(OutputStream out, String aEncodingScheme,
			Document doc) throws Exception {
		OutputFormat outformat = OutputFormat.createPrettyPrint();
		outformat.setEncoding(aEncodingScheme);
		XMLWriter writer = new XMLWriter(out, outformat);
		writer.write(doc);
		writer.flush();
	}

}
