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

import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.openmeetings.app.OpenmeetingsVariables;
import org.openmeetings.app.data.beans.basic.SearchResult;
import org.openmeetings.app.persistence.beans.flvrecord.FlvRecording;
import org.openmeetings.app.persistence.beans.rooms.RoomTypes;
import org.openmeetings.app.persistence.beans.rooms.Rooms;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class RoomWebServiceFacade {

	private static final Logger log = Red5LoggerFactory.getLogger(
			RoomWebServiceFacade.class, OpenmeetingsVariables.webAppRootKey);

	private ServletContext getServletContext() throws Exception {
		MessageContext mc = MessageContext.getCurrentMessageContext();
		return (ServletContext) mc
				.getProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT);
	}

	private RoomWebService getRoomServiceProxy() {
		try {
			ApplicationContext context = WebApplicationContextUtils
					.getWebApplicationContext(getServletContext());
			return context.getBean("roomWebService", RoomWebService.class);
		} catch (Exception err) {
			log.error("[getRoomServiceProxy]", err);
		}
		return null;
	}

	// TODO: Not implemented yet
	// public List<Rooms_Organisation> getRoomsByOrganisationAndType(String SID,
	// long organisation_id, long roomtypes_id) {
	// return conferenceService.getRoomsByOrganisationAndType(SID,
	// organisation_id, roomtypes_id);
	// }

	public Rooms[] getRoomsPublic(String SID, Long roomtypes_id)
			throws AxisFault {
		return this.getRoomServiceProxy().getRoomsPublic(SID, roomtypes_id);
	}

	/**
	 * Deletes a flv recording 
	 * @param SID The SID of the User. This SID must be marked as Loggedin 
	 * @param flvRecordingId the id of the recording 
	 * @return
	 * @throws AxisFault
	 */
	public boolean deleteFlvRecording(String SID, Long flvRecordingId)
			throws AxisFault {
		return this.getRoomServiceProxy().deleteFlvRecording(SID, flvRecordingId);
	}

	public FLVRecordingReturn[] getFlvRecordingByExternalUserId(String SID,
			String externalUserId) throws AxisFault {
		return this.getRoomServiceProxy().getFlvRecordingByExternalUserId(SID,
				externalUserId);
	}

	public FLVRecordingReturn[] getFlvRecordingByExternalRoomTypeAndCreator(
			String SID, String externalRoomType, Long insertedBy)
			throws AxisFault {
		return this.getRoomServiceProxy()
				.getFlvRecordingByExternalRoomTypeAndCreator(SID,
						externalRoomType, insertedBy);
	}

	public List<FlvRecording> getFlvRecordingByExternalRoomTypeByList(
			String SID, String externalRoomType) throws AxisFault {
		return this.getRoomServiceProxy()
				.getFlvRecordingByExternalRoomTypeByList(SID, externalRoomType);
	}

	public FlvRecording[] getFlvRecordingByExternalRoomType(String SID,
			String externalRoomType) throws AxisFault {
		return this.getRoomServiceProxy().getFlvRecordingByExternalRoomType(SID,
				externalRoomType);
	}

	public FlvRecording[] getFlvRecordingByRoomId(String SID, Long roomId)
			throws AxisFault {
		return this.getRoomServiceProxy().getFlvRecordingByRoomId(SID, roomId);
	}

	public RoomTypes[] getRoomTypes(String SID) throws AxisFault {
		return this.getRoomServiceProxy().getRoomTypes(SID);
	}

	public RoomCountBean[] getRoomCounters(String SID, Integer roomId1,
			Integer roomId2, Integer roomId3, Integer roomId4, Integer roomId5,
			Integer roomId6, Integer roomId7, Integer roomId8, Integer roomId9,
			Integer roomId10) throws AxisFault {
		return this.getRoomServiceProxy().getRoomCounters(SID, roomId1, roomId2,
				roomId3, roomId4, roomId5, roomId6, roomId7, roomId8, roomId9,
				roomId10);
	}

	public Rooms getRoomById(String SID, long rooms_id) {
		return this.getRoomServiceProxy().getRoomById(SID, rooms_id);
	}

	/**
	 * @deprecated this function is intend to be deleted
	 * @param SID
	 * @param rooms_id
	 * @return
	 */
	@Deprecated
	public Rooms getRoomWithCurrentUsersById(String SID, long rooms_id)
			throws AxisFault {
		return this.getRoomServiceProxy().getRoomWithCurrentUsersById(SID,
				rooms_id);
	}

	public RoomReturn getRoomWithClientObjectsById(String SID, long rooms_id)
			throws AxisFault {
		return this.getRoomServiceProxy().getRoomWithClientObjectsById(SID,
				rooms_id);
	}

	public SearchResult<Rooms> getRooms(String SID, int start, int max,
			String orderby, boolean asc) throws AxisFault {
		return this.getRoomServiceProxy()
				.getRooms(SID, start, max, orderby, asc);
	}

	public SearchResult<Rooms> getRoomsWithCurrentUsers(String SID, int start,
			int max, String orderby, boolean asc) throws AxisFault {
		return this.getRoomServiceProxy().getRoomsWithCurrentUsers(SID, start,
				max, orderby, asc);
	}

	/**
	 * TODO: Fix Organization Issue
	 * 
	 * @deprecated use addRoomWithModeration instead
	 * 
	 * @param SID
	 * @param name
	 * @param roomtypes_id
	 * @param comment
	 * @param numberOfPartizipants
	 * @param ispublic
	 * @param videoPodWidth
	 * @param videoPodHeight
	 * @param videoPodXPosition
	 * @param videoPodYPosition
	 * @param moderationPanelXPosition
	 * @param showWhiteBoard
	 * @param whiteBoardPanelXPosition
	 * @param whiteBoardPanelYPosition
	 * @param whiteBoardPanelHeight
	 * @param whiteBoardPanelWidth
	 * @param showFilesPanel
	 * @param filesPanelXPosition
	 * @param filesPanelYPosition
	 * @param filesPanelHeight
	 * @param filesPanelWidth
	 * @return
	 */
	@Deprecated
	public Long addRoom(String SID, String name, Long roomtypes_id,
			String comment, Long numberOfPartizipants, Boolean ispublic,
			Integer videoPodWidth, Integer videoPodHeight,
			Integer videoPodXPosition, Integer videoPodYPosition,
			Integer moderationPanelXPosition, Boolean showWhiteBoard,
			Integer whiteBoardPanelXPosition, Integer whiteBoardPanelYPosition,
			Integer whiteBoardPanelHeight, Integer whiteBoardPanelWidth,
			Boolean showFilesPanel, Integer filesPanelXPosition,
			Integer filesPanelYPosition, Integer filesPanelHeight,
			Integer filesPanelWidth) throws AxisFault {
		return this.getRoomServiceProxy().addRoom(SID, name, roomtypes_id,
				comment, numberOfPartizipants, ispublic, videoPodWidth,
				videoPodHeight, videoPodXPosition, videoPodYPosition,
				moderationPanelXPosition, showWhiteBoard,
				whiteBoardPanelXPosition, whiteBoardPanelYPosition,
				whiteBoardPanelHeight, whiteBoardPanelWidth, showFilesPanel,
				filesPanelXPosition, filesPanelYPosition, filesPanelHeight,
				filesPanelWidth);
	}

	public Long addRoomWithModeration(String SID, String name,
			Long roomtypes_id, String comment, Long numberOfPartizipants,
			Boolean ispublic, Boolean appointment, Boolean isDemoRoom,
			Integer demoTime, Boolean isModeratedRoom) throws AxisFault {
		return this.getRoomServiceProxy().addRoomWithModeration(SID, name,
				roomtypes_id, comment, numberOfPartizipants, ispublic,
				appointment, isDemoRoom, demoTime, isModeratedRoom);
	}

	/**
	 * this SOAP Method has an additional param to enable or disable the buttons
	 * to apply for moderation this does only work in combination with the
	 * room-type restricted
	 * 
	 * @param SID
	 * @param name
	 * @param roomtypes_id
	 * @param comment
	 * @param numberOfPartizipants
	 * @param ispublic
	 * @param appointment
	 * @param isDemoRoom
	 * @param demoTime
	 * @param isModeratedRoom
	 * @param allowUserQuestions
	 * @return
	 */
	public Long addRoomWithModerationAndQuestions(String SID, String name,
			Long roomtypes_id, String comment, Long numberOfPartizipants,
			Boolean ispublic, Boolean appointment, Boolean isDemoRoom,
			Integer demoTime, Boolean isModeratedRoom,
			Boolean allowUserQuestions) throws AxisFault {
		return this.getRoomServiceProxy().addRoomWithModerationAndQuestions(SID,
				name, roomtypes_id, comment, numberOfPartizipants, ispublic,
				appointment, isDemoRoom, demoTime, isModeratedRoom,
				allowUserQuestions);
	}

	public Long addRoomWithModerationQuestionsAndAudioType(String SID,
			String name, Long roomtypes_id, String comment,
			Long numberOfPartizipants, Boolean ispublic, Boolean appointment,
			Boolean isDemoRoom, Integer demoTime, Boolean isModeratedRoom,
			Boolean allowUserQuestions, Boolean isAudioOnly) throws AxisFault {
		return this.getRoomServiceProxy()
				.addRoomWithModerationQuestionsAndAudioType(SID, name,
						roomtypes_id, comment, numberOfPartizipants, ispublic,
						appointment, isDemoRoom, demoTime, isModeratedRoom,
						allowUserQuestions, isAudioOnly);
	}

	/**
	 * 
	 * @param SID
	 * @param name
	 * @param roomtypes_id
	 * @param comment
	 * @param numberOfPartizipants
	 * @param ispublic
	 * @param appointment
	 * @param isDemoRoom
	 * @param demoTime
	 * @param isModeratedRoom
	 * @param externalRoomId
	 * @param externalUserType
	 * @return
	 */
	public Long getRoomIdByExternalId(String SID, String name,
			Long roomtypes_id, String comment, Long numberOfPartizipants,
			Boolean ispublic, Boolean appointment, Boolean isDemoRoom,
			Integer demoTime, Boolean isModeratedRoom, Long externalRoomId,
			String externalRoomType) throws AxisFault {
		return this.getRoomServiceProxy().getRoomIdByExternalId(SID, name,
				roomtypes_id, comment, numberOfPartizipants, ispublic,
				appointment, isDemoRoom, demoTime, isModeratedRoom,
				externalRoomId, externalRoomType);
	}

	/**
	 * TODO: Fix Organization Issue
	 * 
	 * @deprecated use updateRoomWithModeration
	 * 
	 * @param SID
	 * @param rooms_id
	 * @param name
	 * @param roomtypes_id
	 * @param comment
	 * @param numberOfPartizipants
	 * @param ispublic
	 * @param videoPodWidth
	 * @param videoPodHeight
	 * @param videoPodXPosition
	 * @param videoPodYPosition
	 * @param moderationPanelXPosition
	 * @param showWhiteBoard
	 * @param whiteBoardPanelXPosition
	 * @param whiteBoardPanelYPosition
	 * @param whiteBoardPanelHeight
	 * @param whiteBoardPanelWidth
	 * @param showFilesPanel
	 * @param filesPanelXPosition
	 * @param filesPanelYPosition
	 * @param filesPanelHeight
	 * @param filesPanelWidth
	 * @return
	 */
	@Deprecated
	public Long updateRoom(String SID, Long rooms_id, String name,
			Long roomtypes_id, String comment, Long numberOfPartizipants,
			Boolean ispublic, Integer videoPodWidth, Integer videoPodHeight,
			Integer videoPodXPosition, Integer videoPodYPosition,
			Integer moderationPanelXPosition, Boolean showWhiteBoard,
			Integer whiteBoardPanelXPosition, Integer whiteBoardPanelYPosition,
			Integer whiteBoardPanelHeight, Integer whiteBoardPanelWidth,
			Boolean showFilesPanel, Integer filesPanelXPosition,
			Integer filesPanelYPosition, Integer filesPanelHeight,
			Integer filesPanelWidth, Boolean appointment) throws AxisFault {
		return this.getRoomServiceProxy().updateRoom(SID, rooms_id, name,
				roomtypes_id, comment, numberOfPartizipants, ispublic,
				videoPodWidth, videoPodHeight, videoPodXPosition,
				videoPodYPosition, moderationPanelXPosition, showWhiteBoard,
				whiteBoardPanelXPosition, whiteBoardPanelYPosition,
				whiteBoardPanelHeight, whiteBoardPanelWidth, showFilesPanel,
				filesPanelXPosition, filesPanelYPosition, filesPanelHeight,
				filesPanelWidth, appointment);
	}

	public Long updateRoomWithModeration(String SID, Long room_id, String name,
			Long roomtypes_id, String comment, Long numberOfPartizipants,
			Boolean ispublic, Boolean appointment, Boolean isDemoRoom,
			Integer demoTime, Boolean isModeratedRoom) throws AxisFault {
		return this.getRoomServiceProxy().updateRoomWithModeration(SID, room_id,
				name, roomtypes_id, comment, numberOfPartizipants, ispublic,
				appointment, isDemoRoom, demoTime, isModeratedRoom);
	}

	public Long updateRoomWithModerationAndQuestions(String SID, Long room_id,
			String name, Long roomtypes_id, String comment,
			Long numberOfPartizipants, Boolean ispublic, Boolean appointment,
			Boolean isDemoRoom, Integer demoTime, Boolean isModeratedRoom,
			Boolean allowUserQuestions) throws AxisFault {
		return this.getRoomServiceProxy().updateRoomWithModeration(SID, room_id,
				name, roomtypes_id, comment, numberOfPartizipants, ispublic,
				appointment, isDemoRoom, demoTime, isModeratedRoom);
	}

	public Long deleteRoom(String SID, long rooms_id) throws AxisFault {
		return this.getRoomServiceProxy().deleteRoom(SID, rooms_id);
	}

	public Boolean kickUser(String SID_Admin, Long room_id) throws AxisFault {
		return this.getRoomServiceProxy().kickUser(SID_Admin, room_id);
	}

	public Long addRoomWithModerationAndExternalType(String SID, String name,
			Long roomtypes_id, String comment, Long numberOfPartizipants,
			Boolean ispublic, Boolean appointment, Boolean isDemoRoom,
			Integer demoTime, Boolean isModeratedRoom, String externalRoomType)
			throws AxisFault {
		return this.getRoomServiceProxy().addRoomWithModerationAndExternalType(
				SID, name, roomtypes_id, comment, numberOfPartizipants,
				ispublic, appointment, isDemoRoom, demoTime, isModeratedRoom,
				externalRoomType);
	}

	public Long addRoomWithModerationExternalTypeAndAudioType(String SID,
			String name, Long roomtypes_id, String comment,
			Long numberOfPartizipants, Boolean ispublic, Boolean appointment,
			Boolean isDemoRoom, Integer demoTime, Boolean isModeratedRoom,
			String externalRoomType, Boolean allowUserQuestions,
			Boolean isAudioOnly) throws AxisFault {
		return this.getRoomServiceProxy()
				.addRoomWithModerationExternalTypeAndAudioType(SID, name,
						roomtypes_id, comment, numberOfPartizipants, ispublic,
						appointment, isDemoRoom, demoTime, isModeratedRoom,
						externalRoomType, allowUserQuestions, isAudioOnly);
	}

	public Long addRoomWithModerationAndRecordingFlags(String SID, String name,
			Long roomtypes_id, String comment, Long numberOfPartizipants,
			Boolean ispublic, Boolean appointment, Boolean isDemoRoom,
			Integer demoTime, Boolean isModeratedRoom, String externalRoomType,
			Boolean allowUserQuestions, Boolean isAudioOnly,
			Boolean waitForRecording, Boolean allowRecording) throws AxisFault {
		return this.getRoomServiceProxy()
				.addRoomWithModerationAndRecordingFlags(SID, name,
						roomtypes_id, comment, numberOfPartizipants, ispublic,
						appointment, isDemoRoom, demoTime, isModeratedRoom,
						externalRoomType, allowUserQuestions, isAudioOnly,
						waitForRecording, allowRecording);
	}

	public Long addRoomWithModerationExternalTypeAndTopBarOption(String SID,
			String name, Long roomtypes_id, String comment,
			Long numberOfPartizipants, Boolean ispublic, Boolean appointment,
			Boolean isDemoRoom, Integer demoTime, Boolean isModeratedRoom,
			String externalRoomType, Boolean allowUserQuestions,
			Boolean isAudioOnly, Boolean waitForRecording,
			Boolean allowRecording, Boolean hideTopBar) throws AxisFault {
		return this.getRoomServiceProxy()
				.addRoomWithModerationExternalTypeAndTopBarOption(SID, name,
						roomtypes_id, comment, numberOfPartizipants, ispublic,
						appointment, isDemoRoom, demoTime, isModeratedRoom,
						externalRoomType, allowUserQuestions, isAudioOnly,
						waitForRecording, allowRecording, hideTopBar);
	}

	/**
	 * 
	 * Create a Invitation hash and optionally send it by mail the From to Date
	 * is as String as some SOAP libraries do not accept Date Objects in SOAP
	 * Calls Date is parsed as dd.mm.yyyy, time as hh:mm (don't forget the
	 * leading zero's)
	 * 
	 * @param SID
	 *            a valid Session Token
	 * @param username
	 *            the username of the User that he will get
	 * @param room_id
	 *            the conference room id of the invitation
	 * @param isPasswordProtected
	 *            if the invitation is password protected
	 * @param invitationpass
	 *            the password for accessing the conference room via the
	 *            invitation hash
	 * @param valid
	 *            the type of validation for the hash 1: endless, 2: from-to
	 *            period, 3: one-time
	 * @param validFromDate
	 *            Date in Format of dd.mm.yyyy only of interest if valid is type
	 *            2
	 * @param validFromTime
	 *            time in Format of hh:mm only of interest if valid is type 2
	 * @param validToDate
	 *            Date in Format of dd.mm.yyyy only of interest if valid is type
	 *            2
	 * @param validToTime
	 *            time in Format of hh:mm only of interest if valid is type 2
	 * @return a HASH value that can be made into a URL with
	 *         http://$OPENMEETINGS_HOST
	 *         :$PORT/openmeetings/?invitationHash="+invitationsHash;
	 * @throws AxisFault
	 */
	public String getInvitationHash(String SID, String username, Long room_id,
			Boolean isPasswordProtected, String invitationpass, Integer valid,
			String validFromDate, String validFromTime, String validToDate,
			String validToTime) throws AxisFault {
		return this.getRoomServiceProxy().getInvitationHash(SID, username,
				room_id, isPasswordProtected, invitationpass, valid,
				validFromDate, validFromTime, validToDate, validToTime);

	}

	/**
	 * Create a Invitation hash and optionally send it by mail the From to Date
	 * is as String as some SOAP libraries do not accept Date Objects in SOAP
	 * Calls Date is parsed as dd.mm.yyyy, time as hh:mm (don't forget the
	 * leading zero's)
	 * 
	 * @param SID
	 *            a valid Session Token
	 * @param username
	 *            the Username of the User that he will get
	 * @param message
	 *            the Message in the Email Body send with the invitation if
	 *            sendMail is true
	 * @param baseurl
	 *            the baseURL for the Infivations link in the Mail Body if
	 *            sendMail is true
	 * @param email
	 *            the Email to send the invitation to if sendMail is true
	 * @param subject
	 *            the subject of the Email send with the invitation if sendMail
	 *            is true
	 * @param room_id
	 *            the conference room id of the invitation
	 * @param conferencedomain
	 *            the domain of the room (keep empty)
	 * @param isPasswordProtected
	 *            if the invitation is password protected
	 * @param invitationpass
	 *            the password for accessing the conference room via the
	 *            invitation hash
	 * @param valid
	 *            the type of validation for the hash 1: endless, 2: from-to
	 *            period, 3: one-time
	 * @param validFromDate
	 *            Date in Format of dd.mm.yyyy only of interest if valid is type
	 *            2
	 * @param validFromTime
	 *            time in Format of hh:mm only of interest if valid is type 2
	 * @param validToDate
	 *            Date in Format of dd.mm.yyyy only of interest if valid is type
	 *            2
	 * @param validToTime
	 *            time in Format of hh:mm only of interest if valid is type 2
	 * @param language_id
	 *            the language id of the EMail that is send with the invitation
	 *            if sendMail is true
	 * @param sendMail
	 *            if sendMail is true then the RPC-Call will send the invitation
	 *            to the email
	 * @return a HASH value that can be made into a URL with
	 *         http://$OPENMEETINGS_HOST
	 *         :$PORT/openmeetings/?invitationHash="+invitationsHash;
	 * @throws AxisFault
	 */
	public String sendInvitationHash(String SID, String username,
			String message, String baseurl, String email, String subject,
			Long room_id, String conferencedomain, Boolean isPasswordProtected,
			String invitationpass, Integer valid, String validFromDate,
			String validFromTime, String validToDate, String validToTime,
			Long language_id, Boolean sendMail) throws AxisFault {
		return this.getRoomServiceProxy().sendInvitationHash(SID, username,
				message, baseurl, email, subject, room_id, conferencedomain,
				isPasswordProtected, invitationpass, valid, validFromDate,
				validFromTime, validToDate, validToTime, language_id, sendMail);
	}

	/**
	 * Create a Invitation hash and optionally send it by mail the From to Date
	 * is as String as some SOAP libraries do not accept Date Objects in SOAP
	 * Calls Date is parsed as dd.mm.yyyy, time as hh:mm (don't forget the
	 * leading zero's)
	 * 
	 * @param SID
	 *            a valid Session Token
	 * @param username
	 *            the Username of the User that he will get
	 * @param message
	 *            the Message in the Email Body send with the invitation if
	 *            sendMail is true
	 * @param baseurl
	 *            the baseURL for the Infivations link in the Mail Body if
	 *            sendMail is true
	 * @param email
	 *            the Email to send the invitation to if sendMail is true
	 * @param subject
	 *            the subject of the Email send with the invitation if sendMail
	 *            is true
	 * @param room_id
	 *            the conference room id of the invitation
	 * @param conferencedomain
	 *            the domain of the room (keep empty)
	 * @param isPasswordProtected
	 *            if the invitation is password protected
	 * @param invitationpass
	 *            the password for accessing the conference room via the
	 *            invitation hash
	 * @param valid
	 *            the type of validation for the hash 1: endless, 2: from-to
	 *            period, 3: one-time
	 * @param fromDate
	 *            Date as Date Object only of interest if valid is type 2
	 * @param toDate
	 *            Date as Date Object only of interest if valid is type 2
	 * @param language_id
	 *            the language id of the EMail that is send with the invitation
	 *            if sendMail is true
	 * @param sendMail
	 *            if sendMail is true then the RPC-Call will send the invitation
	 *            to the email
	 * @return a HASH value that can be made into a URL with
	 *         http://$OPENMEETINGS_HOST
	 *         :$PORT/openmeetings/?invitationHash="+invitationsHash;
	 * @throws AxisFault
	 */
	public String sendInvitationHashWithDateObject(String SID, String username,
			String message, String baseurl, String email, String subject,
			Long room_id, String conferencedomain, Boolean isPasswordProtected,
			String invitationpass, Integer valid, Date fromDate, Date toDate,
			Long language_id, Boolean sendMail) throws AxisFault {
		return this.getRoomServiceProxy().sendInvitationHashWithDateObject(SID,
				username, message, baseurl, email, subject, room_id,
				conferencedomain, isPasswordProtected, invitationpass, valid,
				fromDate, toDate, language_id, sendMail);
	}

	public List<RoomReturn> getRoomsWithCurrentUsersByList(String SID,
			int start, int max, String orderby, boolean asc) throws AxisFault {
		return this.getRoomServiceProxy().getRoomsWithCurrentUsersByList(SID,
				start, max, orderby, asc);
	}

	public List<RoomReturn> getRoomsWithCurrentUsersByListAndType(String SID,
			int start, int max, String orderby, boolean asc,
			String externalRoomType) throws AxisFault {
		return this.getRoomServiceProxy().getRoomsWithCurrentUsersByListAndType(
				SID, start, max, orderby, asc, externalRoomType);
	}

	public Long addRoomWithModerationAndExternalTypeAndStartEnd(String SID,
			String name, Long roomtypes_id, String comment,
			Long numberOfPartizipants, Boolean ispublic, Boolean appointment,
			Boolean isDemoRoom, Integer demoTime, Boolean isModeratedRoom,
			String externalRoomType, String validFromDate,
			String validFromTime, String validToDate, String validToTime,
			Boolean isPasswordProtected, String password, Long reminderTypeId,
			String redirectURL) throws AxisFault {
		return this.getRoomServiceProxy()
				.addRoomWithModerationAndExternalTypeAndStartEnd(SID, name,
						roomtypes_id, comment, numberOfPartizipants, ispublic,
						appointment, isDemoRoom, demoTime, isModeratedRoom,
						externalRoomType, validFromDate, validFromTime,
						validToDate, validToTime, isPasswordProtected,
						password, reminderTypeId, redirectURL);
	}

	public Long addMeetingMemberRemindToRoom(String SID, Long room_id,
			String firstname, String lastname, String email, String baseUrl,
			Long language_id) throws AxisFault {
		return this.getRoomServiceProxy().addMeetingMemberRemindToRoom(SID,
				room_id, firstname, lastname, email, baseUrl, language_id);
	}

	public Long addExternalMeetingMemberRemindToRoom(String SID, Long room_id,
			String firstname, String lastname, String email, String baseUrl,
			Long language_id, String jNameTimeZone, String invitorName)
			throws AxisFault {
		return this.getRoomServiceProxy().addExternalMeetingMemberRemindToRoom(
				SID, room_id, firstname, lastname, email, baseUrl, language_id,
				jNameTimeZone, invitorName);
	}

	public int closeRoom(String SID, Long room_id, Boolean status)
			throws AxisFault {
		return this.getRoomServiceProxy().closeRoom(SID, room_id, status);
	}

}
