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
package org.openmeetings.axis.services;

import javax.servlet.ServletContext;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.openmeetings.app.OpenmeetingsVariables;
import org.openmeetings.app.persistence.beans.rooms.Rooms;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This class provides method entry points necessary for OM to Jabber integration.
 * 
 * @author solomax
 *
 */
public class JabberWebServiceFacade {
	private static final Logger log = Red5LoggerFactory
			.getLogger(JabberWebServiceFacade.class,
					OpenmeetingsVariables.webAppRootKey);

	private ServletContext getServletContext() throws Exception {
		MessageContext mc = MessageContext.getCurrentMessageContext();
		return ((ServletContext) mc
				.getProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT));
	}

	private JabberWebService getJabberServiceProxy() {
		try {
			ApplicationContext context = WebApplicationContextUtils
					.getWebApplicationContext(getServletContext());

			return ((JabberWebService) context.getBean("jabberWebService"));
		} catch (Exception err) {
			log.error("[getJabberServiceProxy]", err);
		}
		return null;
	}

	/**
	 * Get array of all rooms available to the user.
	 * No admin rights are necessary for this call
	 * 
	 * @param SID The SID from {@link UserWebService.getSession}
	 * @return array of Rooms
	 */
	public Rooms[] getAvailableRooms(String SID) {
		return getJabberServiceProxy().getAvailableRooms(SID).toArray(new Rooms[0]);
	}

	/**
	 * Returns the count of users currently in the Room with given id
	 * No admin rights are necessary for this call
	 * 
	 * @param SID The SID from {@link UserWebService.getSession}
	 * @param roomId id of the room to get users
	 * @return number of users as int
	 */
	public int getUserCount(String SID, Long roomId) {
		return getJabberServiceProxy().getUserCount(SID, roomId);
	}

	/**
	 * Get invitation hash for the room with given id
	 * No admin rights are necessary for this call
	 * 
	 * @param SID The SID from {@link UserWebService.getSession}
	 * @param username The name of invited user, will be displayed in the rooms user list
	 * @param room_id id of the room to get users
	 * @return hash to enter the room
	 */
	public String getInvitationHash(String SID, String username, Long room_id) {
		return getJabberServiceProxy()
				.getInvitationHash(SID, username, room_id);
	}
}