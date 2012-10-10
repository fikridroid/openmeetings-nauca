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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.openmeetings.app.OpenmeetingsVariables;
import org.openmeetings.app.data.basic.Configurationmanagement;
import org.openmeetings.app.data.basic.dao.LdapConfigDaoImpl;
import org.openmeetings.app.data.basic.dao.OmTimeZoneDaoImpl;
import org.openmeetings.app.data.calendar.daos.AppointmentCategoryDaoImpl;
import org.openmeetings.app.data.calendar.daos.AppointmentDaoImpl;
import org.openmeetings.app.data.calendar.daos.AppointmentReminderTypDaoImpl;
import org.openmeetings.app.data.calendar.daos.MeetingMemberDaoImpl;
import org.openmeetings.app.data.conference.PollManagement;
import org.openmeetings.app.data.conference.Roommanagement;
import org.openmeetings.app.data.conference.dao.RoomModeratorsDaoImpl;
import org.openmeetings.app.data.file.dao.FileExplorerItemDaoImpl;
import org.openmeetings.app.data.flvrecord.FlvRecordingDaoImpl;
import org.openmeetings.app.data.flvrecord.FlvRecordingMetaDataDaoImpl;
import org.openmeetings.app.data.user.Organisationmanagement;
import org.openmeetings.app.data.user.Statemanagement;
import org.openmeetings.app.data.user.dao.PrivateMessageFolderDaoImpl;
import org.openmeetings.app.data.user.dao.PrivateMessagesDaoImpl;
import org.openmeetings.app.data.user.dao.UserContactsDaoImpl;
import org.openmeetings.app.data.user.dao.UsersDaoImpl;
import org.openmeetings.app.persistence.beans.adresses.States;
import org.openmeetings.app.persistence.beans.basic.Configuration;
import org.openmeetings.app.persistence.beans.basic.LdapConfig;
import org.openmeetings.app.persistence.beans.basic.OmTimeZone;
import org.openmeetings.app.persistence.beans.calendar.Appointment;
import org.openmeetings.app.persistence.beans.calendar.MeetingMember;
import org.openmeetings.app.persistence.beans.domain.Organisation;
import org.openmeetings.app.persistence.beans.domain.Organisation_Users;
import org.openmeetings.app.persistence.beans.files.FileExplorerItem;
import org.openmeetings.app.persistence.beans.flvrecord.FlvRecording;
import org.openmeetings.app.persistence.beans.flvrecord.FlvRecordingMetaData;
import org.openmeetings.app.persistence.beans.poll.RoomPoll;
import org.openmeetings.app.persistence.beans.poll.RoomPollAnswers;
import org.openmeetings.app.persistence.beans.rooms.RoomModerators;
import org.openmeetings.app.persistence.beans.rooms.Rooms;
import org.openmeetings.app.persistence.beans.rooms.Rooms_Organisation;
import org.openmeetings.app.persistence.beans.sip.asterisk.AsteriskSipUsers;
import org.openmeetings.app.persistence.beans.sip.asterisk.Extensions;
import org.openmeetings.app.persistence.beans.sip.asterisk.MeetMe;
import org.openmeetings.app.persistence.beans.user.PrivateMessageFolder;
import org.openmeetings.app.persistence.beans.user.PrivateMessages;
import org.openmeetings.app.persistence.beans.user.UserContacts;
import org.openmeetings.app.persistence.beans.user.UserSipData;
import org.openmeetings.app.persistence.beans.user.Users;
import org.openmeetings.app.remote.red5.ScopeApplicationAdapter;
import org.openmeetings.app.sip.api.impl.asterisk.dao.AsteriskDAOImpl;
import org.openmeetings.utils.math.CalendarPatterns;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class BackupImportController extends AbstractUploadController {

	private static final Logger log = Red5LoggerFactory.getLogger(
			BackupImportController.class, OpenmeetingsVariables.webAppRootKey);

	@Autowired
	private AppointmentDaoImpl appointmentDao;
	@Autowired
	private Statemanagement statemanagement;
	@Autowired
	private OmTimeZoneDaoImpl omTimeZoneDaoImpl;
	@Autowired
	private Organisationmanagement organisationmanagement;
	@Autowired
	private Roommanagement roommanagement;
	@Autowired
	private AppointmentCategoryDaoImpl appointmentCategoryDaoImpl;
	@Autowired
	private AppointmentReminderTypDaoImpl appointmentReminderTypDaoImpl;
	@Autowired
	private UsersDaoImpl usersDao;
	@Autowired
	private FlvRecordingDaoImpl flvRecordingDao;
	@Autowired
	private FlvRecordingMetaDataDaoImpl flvRecordingMetaDataDao;
	@Autowired
	private PrivateMessageFolderDaoImpl privateMessageFolderDao;
	@Autowired
	private PrivateMessagesDaoImpl privateMessagesDao;
	@Autowired
	private MeetingMemberDaoImpl meetingMemberDao;
	@Autowired
	private LdapConfigDaoImpl ldapConfigDao;
	@Autowired
	private RoomModeratorsDaoImpl roomModeratorsDao;
	@Autowired
	private FileExplorerItemDaoImpl fileExplorerItemDao;
	@Autowired
	private UserContactsDaoImpl userContactsDao;
	@Autowired
	private ScopeApplicationAdapter scopeApplicationAdapter;
	@Autowired
	private PollManagement pollManagement;
	@Autowired
	private Configurationmanagement cfgManagement;
	@Autowired
	private AsteriskDAOImpl asteriskDAOImpl;

	private final HashMap<Long, Long> usersMap = new HashMap<Long, Long>();
	private final HashMap<Long, Long> organisationsMap = new HashMap<Long, Long>();
	private final HashMap<Long, Long> appointmentsMap = new HashMap<Long, Long>();
	private final HashMap<Long, Long> roomsMap = new HashMap<Long, Long>();
	private final HashMap<Long, Long> messageFoldersMap = new HashMap<Long, Long>();
	private final HashMap<Long, Long> userContactsMap = new HashMap<Long, Long>();
	private final HashMap<Long, Long> fileExplorerItemsMap = new HashMap<Long, Long>();

	private enum Maps {
		USERS, ORGANISATIONS, APPOINTMENTS, ROOMS, MESSAGEFOLDERS, USERCONTACTS, FILEEXPLORERITEMS
	};

	public void performImport(InputStream is, String current_dir) throws Exception {
		File working_dir = new File(current_dir, OpenmeetingsVariables.UPLOAD_DIR
				+ File.separatorChar + "import");
		if (!working_dir.exists()) {
			working_dir.mkdir();
		}

		File f = new File(working_dir, "import_" + CalendarPatterns.getTimeForStreamId(new Date()));

		int recursiveNumber = 0;
		do {
			if (f.exists()) {
				f = new File(f.getAbsolutePath() + (recursiveNumber++));
			}
		} while (f.exists());
		f.mkdir();

		log.debug("##### WRITE FILE TO: " + f);
		
		ZipInputStream zipinputstream = new ZipInputStream(is);
		byte[] buf = new byte[1024];

		ZipEntry zipentry = zipinputstream.getNextEntry();

		while (zipentry != null) {
			// for each entry to be extracted
			int n;
			FileOutputStream fileoutputstream;
			File fentryName = new File(f, zipentry.getName());

			if (zipentry.isDirectory()) {
				if (!fentryName.mkdir()) {
					break;
				}
				zipentry = zipinputstream.getNextEntry();
				continue;
			}

			File fparent = new File(fentryName.getParent());

			if (!fparent.exists()) {

				File fparentparent = new File(fparent.getParent());

				if (!fparentparent.exists()) {

					File fparentparentparent = new File(
							fparentparent.getParent());

					if (!fparentparentparent.exists()) {

						fparentparentparent.mkdir();
						fparentparent.mkdir();
						fparent.mkdir();

					} else {

						fparentparent.mkdir();
						fparent.mkdir();

					}

				} else {

					fparent.mkdir();

				}

			}

			fileoutputstream = new FileOutputStream(fentryName);

			while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
				fileoutputstream.write(buf, 0, n);
			}

			fileoutputstream.close();
			zipinputstream.closeEntry();
			zipentry = zipinputstream.getNextEntry();

		}// while

		zipinputstream.close();

		/*
		 * ##################### Import Organizations
		 */
		File orgFile = new File(f, "organizations.xml");
		if (!orgFile.exists()) {
			throw new Exception("organizations.xml missing");
		}
		this.importOrganizsations(orgFile);

		log.info("Organizations import complete, starting user import");

		/*
		 * ##################### Import Users
		 */
		File userFile = new File(f, "users.xml");
		if (!userFile.exists()) {
			throw new Exception("users.xml missing");
		}
		this.importUsers(userFile);

		log.info("Users import complete, starting room import");

		/*
		 * ##################### Import Rooms
		 */
		File roomFile = new File(f, "rooms.xml");
		if (!roomFile.exists()) {
			throw new Exception("rooms.xml missing");
		}
		this.importRooms(roomFile);

		log.info("Room import complete, starting room organizations import");

		/*
		 * ##################### Import Room Organisations
		 */
		File orgRoomListFile = new File(f, "rooms_organisation.xml");
		if (!orgRoomListFile.exists()) {
			throw new Exception("rooms_organisation.xml missing");
		}
		this.importOrgRooms(orgRoomListFile);

		log.info("Room organizations import complete, starting appointement import");

		/*
		 * ##################### Import Appointements
		 */
		File appointementListFile = new File(f, "appointements.xml");
		if (!appointementListFile.exists()) {
			throw new Exception("appointements.xml missing");
		}
		this.importAppointements(appointementListFile);

		log.info("Appointement import complete, starting meeting members import");

		/*
		 * ##################### Import MeetingMembers
		 * 
		 * Reminder Invitations will be NOT send!
		 */
		File meetingmembersListFile = new File(f, "meetingmembers.xml");
		if (!meetingmembersListFile.exists()) {
			throw new Exception("meetingmembersListFile missing");
		}
		this.importMeetingmembers(meetingmembersListFile);

		log.info("Meeting members import complete, starting ldap config import");

		/*
		 * ##################### Import LDAP Configs
		 */
		File ldapConfigListFile = new File(f, "ldapconfigs.xml");
		if (!ldapConfigListFile.exists()) {
			log.debug("meetingmembersListFile missing");
			// throw new Exception
			// ("meetingmembersListFile missing");
		} else {
			this.importLdapConfig(ldapConfigListFile);
		}

		log.info("Ldap config import complete, starting recordings import");

		/*
		 * ##################### Import Recordings
		 */
		File flvRecordingsListFile = new File(f, "flvRecordings.xml");
		if (!flvRecordingsListFile.exists()) {
			log.debug("flvRecordingsListFile missing");
			// throw new Exception
			// ("meetingmembersListFile missing");
		} else {
			this.importFlvRecordings(flvRecordingsListFile);
		}

		log.info("FLVrecording import complete, starting private message folder import");

		/*
		 * ##################### Import Private Message Folders
		 */
		File privateMessageFoldersFile = new File(f, "privateMessageFolder.xml");
		if (!privateMessageFoldersFile.exists()) {
			log.debug("privateMessageFoldersFile missing");
			// throw new Exception
			// ("meetingmembersListFile missing");
		} else {
			this.importPrivateMessageFolders(privateMessageFoldersFile);
		}

		log.info("Private message folder import complete, starting private message import");

		/*
		 * ##################### Import Private Messages
		 */
		File privateMessagesFile = new File(f, "privateMessages.xml");
		if (!privateMessagesFile.exists()) {
			log.debug("privateMessagesFile missing");
			// throw new Exception
			// ("meetingmembersListFile missing");
		} else {
			this.importPrivateMessages(privateMessagesFile);
		}

		log.info("Private message import complete, starting usercontact import");

		/*
		 * ##################### Import User Contacts
		 */
		File userContactsFile = new File(f, "userContacts.xml");
		if (!userContactsFile.exists()) {
			log.debug("userContactsFile missing");
			// throw new Exception
			// ("meetingmembersListFile missing");
		} else {
			this.importUserContacts(userContactsFile);
		}

		log.info("Usercontact import complete, starting file explorer item import");

		/*
		 * ##################### Import File-Explorer Items
		 */
		File fileExplorerListFile = new File(f, "fileExplorerItems.xml");
		if (!fileExplorerListFile.exists()) {
			log.debug("fileExplorerListFile missing");
			// throw new Exception
			// ("meetingmembersListFile missing");
		} else {
			this.importFileExplorerItems(fileExplorerListFile);
		}

		log.info("File explorer item import complete, starting file poll import");

		/*
		 * ##################### Import Room Polls
		 */
		File roomPollListFile = new File(f, "roompolls.xml");
		if (!roomPollListFile.exists()) {
			log.debug("roomPollListFile missing");
		} else {
			this.importRoomPolls(roomPollListFile);
		}
		log.info("Poll import complete, starting configs import");

		/*
		 * ##################### Import Configs
		 */
		File configsFile = new File(f, "configs.xml");
		if (!configsFile.exists()) {
			log.debug("configsFile missing");
		} else {
			importConfigs(configsFile);
		}
		log.info("Configs import complete, starting asteriskSipUsersFile import");
		
		/*
		 * ##################### Import AsteriskSipUsers
		 */
		File asteriskSipUsersFile = new File(f, "asterisksipusers.xml");
		if (!asteriskSipUsersFile.exists()) {
			log.debug("asteriskSipUsersFile missing");
		} else {
			importAsteriskSipUsers(asteriskSipUsersFile);
		}
		log.info("AsteriskSipUsers import complete, starting extensions import");
		
		/*
		 * ##################### Import Extensions
		 */
		File extensionsFile = new File(f, "extensions.xml");
		if (!extensionsFile.exists()) {
			log.debug("extensionsFile missing");
		} else {
			importExtensions(extensionsFile);
		}
		log.info("Extensions import complete, starting members import");
		
		/*
		 * ##################### Import Extensions
		 */
		File membersFile = new File(f, "members.xml");
		if (!membersFile.exists()) {
			log.debug("membersFile missing");
		} else {
			importMembers(membersFile);
		}
		log.info("Members import complete, starting copy of files and folders");

		/*
		 * ##################### Import real files and folders
		 */
		importFolders(current_dir, f);

		log.info("File explorer item import complete, clearing temp files");
		
		deleteDirectory(f);
	}
	
	@RequestMapping(value = "/backup.upload", method = RequestMethod.POST)
	public void service(HttpServletRequest request,
			HttpServletResponse httpServletResponse)
			throws ServletException, IOException {

    	UploadInfo info = validate(request, true);
    	try {
			String current_dir = context.getRealPath("/");
			MultipartFile multipartFile = info.file;
			InputStream is = multipartFile.getInputStream();
			performImport(is, current_dir);

			LinkedHashMap<String, Object> hs = new LinkedHashMap<String, Object>();
			hs.put("user", usersDao.getUser(info.userId));
			hs.put("message", "library");
			hs.put("action", "import");
			hs.put("error", "");
			hs.put("fileName", info.filename);

			scopeApplicationAdapter.sendMessageWithClientByPublicSID(
					hs, info.publicSID);

		} catch (Exception e) {

			log.error("[ImportExport]", e);

			e.printStackTrace();
			throw new ServletException(e);
		}

		return;
	}

	private void importRoomPolls(File roomPollListFile) {
		try {

			List<RoomPoll> roomPolls = this.getRoomPolls(roomPollListFile);

			for (RoomPoll roomPoll : roomPolls) {

				pollManagement.savePollBackup(roomPoll);

			}

		} catch (Exception err) {
			log.error("[getRoomPolls]", err);
		}

	}
	
	private void importMembers(File membersFile) throws Exception {
		SAXReader reader = new SAXReader();
		Document document = reader.read(membersFile);

		Element root = document.getRootElement();
		Element extensions = root.element("members");
		for (@SuppressWarnings("unchecked")
			Iterator<Element> iter = extensions.elementIterator("member"); iter.hasNext();) {
			
			Element extensionElem = iter.next();
			String confno = extensionElem.elementText("confno");
			try {
				MeetMe meetMe = new MeetMe();
				meetMe.setConfno(unformatString(extensionElem
								.element("confno").getText()));
				meetMe.setPin(unformatString(extensionElem
						.element("pin").getText()));
				meetMe.setAdminpin(unformatString(extensionElem
						.element("adminpin").getText()));
				meetMe.setMembers(importIntegerType(unformatString(extensionElem
						.element("members").getText())));
				
				asteriskDAOImpl.saveMeetMe(meetMe);
			} catch (Exception e) {
				log.debug("failed to add/update members confno: " + confno, e);
			}
		}
	}
	
	private void importExtensions(File extensionsFile) throws Exception {
		SAXReader reader = new SAXReader();
		Document document = reader.read(extensionsFile);

		Element root = document.getRootElement();
		Element extensions = root.element("extensions");
		for (@SuppressWarnings("unchecked")
			Iterator<Element> iter = extensions.elementIterator("extension"); iter.hasNext();) {
			
			Element extensionElem = iter.next();
			String id = extensionElem.elementText("id");
			try {
				Extensions extension = new Extensions();
				//the primary key must be null for new objects if its an auto-increment
				extension.setExten(unformatString(extensionElem
						.element("exten").getText()));
				extension.setPriority(importIntegerType(unformatString(extensionElem
						.element("priority").getText())));
				extension.setApp(unformatString(extensionElem
						.element("app").getText()));
				extension.setAppdata(unformatString(extensionElem
						.element("appdata").getText()));
				
				asteriskDAOImpl.saveExtensions(extension);
			} catch (Exception e) {
				log.debug("failed to add/update extensions id: " + id, e);
			}
		}
	}
	
	private void importAsteriskSipUsers(File asteriskSipUsersFile) throws Exception {
		SAXReader reader = new SAXReader();
		Document document = reader.read(asteriskSipUsersFile);

		Element root = document.getRootElement();
		Element asterisksipusers = root.element("asterisksipusers");
		for (@SuppressWarnings("unchecked")
			Iterator<Element> iter = asterisksipusers.elementIterator("asterisksipuser"); iter.hasNext();) {
			
			Element asterisksipuserElem = iter.next();
			
			String id = asterisksipuserElem.elementText("id");
			
			try {
			
				AsteriskSipUsers asterisksipuser = new AsteriskSipUsers();
				//the primary key must be null for new objects if its an auto-increment
				asterisksipuser.setAccountcode(unformatString(asterisksipuserElem
						.element("accountcode").getText()));
				asterisksipuser.setDisallow(unformatString(asterisksipuserElem
						.element("disallow").getText()));
				asterisksipuser.setAllow(unformatString(asterisksipuserElem
						.element("allow").getText()));
				asterisksipuser.setAllowoverlap(unformatString(asterisksipuserElem
						.element("allowoverlap").getText()));
				asterisksipuser.setAllowsubscribe(unformatString(asterisksipuserElem
						.element("allowsubscribe").getText()));
				asterisksipuser.setAllowtransfer(unformatString(asterisksipuserElem
						.element("allowtransfer").getText()));
				asterisksipuser.setAmaflags(unformatString(asterisksipuserElem
						.element("amaflags").getText()));
				asterisksipuser.setAutoframing(unformatString(asterisksipuserElem
						.element("autoframing").getText()));
				asterisksipuser.setAuth(unformatString(asterisksipuserElem
						.element("auth").getText()));
				asterisksipuser.setBuggymwi(unformatString(asterisksipuserElem
						.element("buggymwi").getText()));
				asterisksipuser.setCallgroup(unformatString(asterisksipuserElem
						.element("callgroup").getText()));
				asterisksipuser.setCallerid(unformatString(asterisksipuserElem
						.element("callerid").getText()));
				asterisksipuser.setCid_number(unformatString(asterisksipuserElem
						.element("cid_number").getText()));
				asterisksipuser.setFullname(unformatString(asterisksipuserElem
						.element("fullname").getText()));
				asterisksipuser.setCallingpres(unformatString(asterisksipuserElem
						.element("callingpres").getText()));
				asterisksipuser.setCanreinvite(unformatString(asterisksipuserElem
						.element("canreinvite").getText()));
				asterisksipuser.setContext(unformatString(asterisksipuserElem
						.element("context").getText()));
				asterisksipuser.setDefaultip(unformatString(asterisksipuserElem
						.element("defaultip").getText()));
				asterisksipuser.setDtmfmode(unformatString(asterisksipuserElem
						.element("dtmfmode").getText()));
				asterisksipuser.setFromuser(unformatString(asterisksipuserElem
						.element("fromuser").getText()));
				asterisksipuser.setFromdomain(unformatString(asterisksipuserElem
						.element("fromdomain").getText()));
				asterisksipuser.setFullcontact(unformatString(asterisksipuserElem
						.element("fullcontact").getText()));
				asterisksipuser.setG726nonstandard(unformatString(asterisksipuserElem
						.element("g726nonstandard").getText()));
				asterisksipuser.setHost(unformatString(asterisksipuserElem
						.element("host").getText()));
				asterisksipuser.setInsecure(unformatString(asterisksipuserElem
						.element("insecure").getText()));
				asterisksipuser.setIpaddr(unformatString(asterisksipuserElem
						.element("ipaddr").getText()));
				asterisksipuser.setLanguage(unformatString(asterisksipuserElem
						.element("language").getText()));
				asterisksipuser.setLastms(unformatString(asterisksipuserElem
						.element("lastms").getText()));
				asterisksipuser.setMailbox(unformatString(asterisksipuserElem
						.element("mailbox").getText()));
				asterisksipuser.setMaxcallbitrate(importIntegerType(unformatString(asterisksipuserElem
						.element("maxcallbitrate").getText())));
				asterisksipuser.setMohsuggest(unformatString(asterisksipuserElem
						.element("mohsuggest").getText()));
				asterisksipuser.setMd5secret(unformatString(asterisksipuserElem
						.element("md5secret").getText()));
				asterisksipuser.setMusiconhold(unformatString(asterisksipuserElem
						.element("musiconhold").getText()));
				asterisksipuser.setName(unformatString(asterisksipuserElem
						.element("name").getText()));
				asterisksipuser.setNat(unformatString(asterisksipuserElem
						.element("nat").getText()));
				asterisksipuser.setOutboundproxy(unformatString(asterisksipuserElem
						.element("outboundproxy").getText()));
				asterisksipuser.setDeny(unformatString(asterisksipuserElem
						.element("deny").getText()));
				asterisksipuser.setPermit(unformatString(asterisksipuserElem
						.element("permit").getText()));
				asterisksipuser.setPickupgroup(unformatString(asterisksipuserElem
						.element("pickupgroup").getText()));
				asterisksipuser.setPort(unformatString(asterisksipuserElem
						.element("port").getText()));
				asterisksipuser.setProgressinband(unformatString(asterisksipuserElem
						.element("progressinband").getText()));
				asterisksipuser.setPromiscredir(unformatString(asterisksipuserElem
						.element("promiscredir").getText()));
				asterisksipuser.setQualify(unformatString(asterisksipuserElem
						.element("qualify").getText()));
				asterisksipuser.setRegexten(unformatString(asterisksipuserElem
						.element("regexten").getText()));
				asterisksipuser.setRegseconds(importIntegerType(unformatString(asterisksipuserElem
						.element("regseconds").getText())));
				asterisksipuser.setRfc2833compensate(unformatString(asterisksipuserElem
						.element("rfc2833compensate").getText()));
				asterisksipuser.setRtptimeout(unformatString(asterisksipuserElem
						.element("rtptimeout").getText()));
				asterisksipuser.setRtpholdtimeout(unformatString(asterisksipuserElem
						.element("rtpholdtimeout").getText()));
				asterisksipuser.setSecret(unformatString(asterisksipuserElem
						.element("secret").getText()));
				asterisksipuser.setSendrpid(unformatString(asterisksipuserElem
						.element("sendrpid").getText()));
				asterisksipuser.setSetvar(unformatString(asterisksipuserElem
						.element("setvar").getText()));
				asterisksipuser.setSubscribecontext(unformatString(asterisksipuserElem
						.element("subscribecontext").getText()));
				asterisksipuser.setSubscribemwi(unformatString(asterisksipuserElem
						.element("subscribemwi").getText()));
				asterisksipuser.setT38pt_udptl(unformatString(asterisksipuserElem
						.element("t38pt_udptl").getText()));
				asterisksipuser.setTrustrpid(unformatString(asterisksipuserElem
						.element("trustrpid").getText()));
				asterisksipuser.setType(unformatString(asterisksipuserElem
						.element("type").getText()));
				asterisksipuser.setUseclientcode(unformatString(asterisksipuserElem
						.element("useclientcode").getText()));
				asterisksipuser.setUsername(unformatString(asterisksipuserElem
						.element("username").getText()));
				asterisksipuser.setUsereqphone(unformatString(asterisksipuserElem
						.element("usereqphone").getText()));
				asterisksipuser.setVideosupport(unformatString(asterisksipuserElem
						.element("videosupport").getText()));
				asterisksipuser.setVmexten(unformatString(asterisksipuserElem
						.element("vmexten").getText()));
				
				asteriskDAOImpl.saveAsteriskSipUsers(asterisksipuser);
				
			} catch (Exception e) {
				log.debug("failed to add/update asterisksipuser id: "+id, e);
			}
		}
	}
	
	private void importConfigs(File configsFile) throws Exception {
		SAXReader reader = new SAXReader();
		Document document = reader.read(configsFile);

		Element root = document.getRootElement();
		Element configs = root.element("configs");
		for (@SuppressWarnings("unchecked")
			Iterator<Element> iter = configs.elementIterator("config"); iter.hasNext();) {
			
			Element cfgElem = iter.next();
			String key = cfgElem.elementText("key");
			try {
				Configuration cfg = cfgManagement.getConfKey(3L, key);
				if (cfg == null) {
					cfg = new Configuration();
					cfg.setConf_key(key);
				}
				cfg.setConf_value(cfgElem.elementText("value"));
				cfg.setUpdatetime(new Date());
				cfg.setDeleted(cfgElem.elementText("deleted"));
				cfg.setComment(cfgElem.elementText("comment"));
				cfgManagement.updateConfig(cfg);
			} catch (Exception e) {
				log.debug("failed to add/update configuration: " + key, e);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<RoomPoll> getRoomPolls(File roomPollListFile) throws Exception {

		List<RoomPoll> roomPollList = new LinkedList<RoomPoll>();

		SAXReader reader = new SAXReader();
		Document document = reader.read(roomPollListFile);

		Element root = document.getRootElement();

		for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {

			Element itemObject = i.next();

			if (itemObject.getName().equals("roompolls")) {

				for (Iterator<Element> innerIter = itemObject
						.elementIterator("roompoll"); innerIter.hasNext();) {

					Element roompollObject = innerIter.next();

					String pollname = unformatString(roompollObject.element(
							"pollname").getText());
					String pollquestion = unformatString(roompollObject
							.element("pollquestion").getText());
					Boolean archived = importBooleanType(unformatString(roompollObject
							.element("archived").getText()));
					Date created = CalendarPatterns
							.parseImportDate(unformatString(roompollObject
									.element("created").getText()));
					Long createdbyuserid = importLongType(unformatString(roompollObject
							.element("createdbyuserid").getText()));
					Long polltypeid = importLongType(unformatString(roompollObject
							.element("polltypeid").getText()));
					Long roomid = importLongType(unformatString(roompollObject
							.element("roomid").getText()));
					
					RoomPoll roomPoll = new RoomPoll();
					roomPoll.setPollName(pollname);
					roomPoll.setPollQuestion(pollquestion);
					if (archived != null) {
						roomPoll.setArchived(archived.booleanValue());
					} else {
						roomPoll.setArchived(true);
					}
					roomPoll.setCreated(created);
					roomPoll.setCreatedBy(usersDao.getUser(getNewId(createdbyuserid, Maps.USERS)));
					roomPoll.setPollType(pollManagement.getPollType(polltypeid));
					roomPoll.setRoom(roommanagement.getRoomById(getNewId(roomid, Maps.ROOMS)));
					roomPoll.setRoomPollAnswerList(new LinkedList<RoomPollAnswers>());
					
					Element roompollanswers = roompollObject
							.element("roompollanswers");

					for (Iterator<Element> innerIterAnswers = roompollanswers
							.elementIterator("roompollanswer"); innerIterAnswers
							.hasNext();) {

						Element innerIterAnswerObj = innerIterAnswers.next();

						Integer pointlist = importIntegerType(unformatString(innerIterAnswerObj
								.element("pointlist").getText()));
						Boolean answer = importBooleanType(unformatString(innerIterAnswerObj
								.element("answer").getText()));
						Date votedate = CalendarPatterns
								.parseImportDate(unformatString(innerIterAnswerObj
										.element("votedate").getText()));
						Long voteduserid = importLongType(unformatString(innerIterAnswerObj
								.element("voteduserid").getText()));
						
						RoomPollAnswers roomPollAnswers = new RoomPollAnswers();
						roomPollAnswers.setPointList(pointlist);
						roomPollAnswers.setAnswer(answer);
						roomPollAnswers.setVoteDate(votedate);
						roomPollAnswers.setVotedUser(usersDao.getUser(getNewId(voteduserid, Maps.USERS)));
						
						roomPoll.getRoomPollAnswerList().add(roomPollAnswers);
					}
					
					roomPollList.add(roomPoll);

				}

			}

		}

		return roomPollList;

	}

	public void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {

		// log.debug("^^^^ "+sourceLocation.getName()+" || "+targetLocation.getName());

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	public void copyFile(File sourceLocation, File targetLocation)
			throws IOException {

		InputStream in = new FileInputStream(sourceLocation);
		OutputStream out = new FileOutputStream(targetLocation);

		// Copy the bits from instream to outstream
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	public boolean deleteDirectory(File path) throws IOException {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	private void importUsers(File userFile) throws Exception {

		this.getUsersByXML(userFile);

	}

	@SuppressWarnings("unchecked")
	private void getUsersByXML(File userFile) {
		try {

			SAXReader reader = new SAXReader();
			Document document = reader.read(userFile);

			Element root = document.getRootElement();

			for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
				Element itemObject = i.next();
				if (itemObject.getName().equals("users")) {

					for (Iterator<Element> innerIter = itemObject
							.elementIterator("user"); innerIter.hasNext();) {

						Element itemUsers = innerIter.next();

						Users us = new Users();
						Long userId = Long.valueOf(unformatString(itemUsers
								.element("user_id").getText()));

						us.setAge(CalendarPatterns
								.parseImportDate(unformatString(itemUsers
										.element("age").getText())));
						us.setAvailible(importIntegerType(unformatString(itemUsers
								.element("availible").getText())));
						us.setDeleted(unformatString(itemUsers.element(
								"deleted").getText()));
						us.setFirstname(unformatString(itemUsers.element(
								"firstname").getText()));
						us.setLastname(unformatString(itemUsers.element(
								"lastname").getText()));
						us.setLogin(unformatString(itemUsers.element("login")
								.getText()));
						us.setPassword(unformatString(itemUsers.element("pass")
								.getText()));
						us.setDeleted(itemUsers.element("deleted").getText());

						if (itemUsers.element("activatehash") != null) {
							us.setActivatehash(unformatString(itemUsers
									.element("activatehash").getText()));
						} else {
							us.setActivatehash("");
						}
						if (itemUsers.element("externalUserType") != null) {
							us.setExternalUserType(unformatString(itemUsers
									.element("externalUserType").getText()));
						} else {
							us.setExternalUserType("");
						}
						if (itemUsers.element("externalUserId") != null) {
							us.setExternalUserId(unformatString(itemUsers
									.element("externalUserId").getText()));
						} else {
							us.setExternalUserId(null);
						}
						if (itemUsers.element("resethash") != null) {
							us.setResethash(unformatString(itemUsers.element(
									"resethash").getText()));
						} else {
							us.setResethash(null);
						}
						if (itemUsers.element("userOffers") != null) {
							us.setUserOffers(unformatString(itemUsers.element(
									"userOffers").getText()));
						} else {
							us.setUserOffers("");
						}
						if (itemUsers.element("userSearchs") != null) {
							us.setUserSearchs(unformatString(itemUsers.element(
									"userSearchs").getText()));
						} else {
							us.setUserSearchs("");
						}
						if (itemUsers.element("forceTimeZoneCheck") != null) {
							us.setForceTimeZoneCheck(importBooleanType(unformatString(itemUsers
									.element("forceTimeZoneCheck").getText())));
						} else {
							us.setForceTimeZoneCheck(null);
						}
						if (itemUsers.element("lasttrans") != null) {
							us.setLasttrans(importLongType(unformatString(itemUsers
									.element("lasttrans").getText())));
						} else {
							us.setLasttrans(null);
						}
						if (itemUsers.element("showContactData") != null) {
							us.setShowContactData(importBooleanType(unformatString(itemUsers
									.element("showContactData").getText())));
						} else {
							us.setShowContactData(null);
						}
						if (itemUsers.element("showContactDataToContacts") != null) {
							us.setShowContactDataToContacts(importBooleanType(unformatString(itemUsers
									.element("showContactDataToContacts")
									.getText())));
						} else {
							us.setShowContactDataToContacts(null);
						}

						us.setPictureuri(unformatString(itemUsers.element(
								"pictureuri").getText()));
						if (unformatString(
								itemUsers.element("language_id").getText())
								.length() > 0)
							us.setLanguage_id(Long
									.valueOf(unformatString(itemUsers.element(
											"language_id").getText())));

						us.setStatus(importIntegerType(unformatString(itemUsers
								.element("status").getText())));
						us.setRegdate(CalendarPatterns
								.parseImportDate(unformatString(itemUsers
										.element("regdate").getText())));
						us.setTitle_id(importIntegerType(unformatString(itemUsers
								.element("title_id").getText())));
						us.setLevel_id(importLongType(unformatString(itemUsers
								.element("level_id").getText())));

						// UserSIP Data
						if (itemUsers.element("sip_username") != null
								&& itemUsers.element("sip_userpass") != null
								&& itemUsers.element("sip_authid") != null) {
							UserSipData userSipData = new UserSipData();
							userSipData.setUsername(unformatString(itemUsers
									.element("sip_username").getText()));
							userSipData.setUsername(unformatString(itemUsers
									.element("sip_userpass").getText()));
							userSipData.setUsername(unformatString(itemUsers
									.element("sip_authid").getText()));
							us.setUserSipData(userSipData);
						}

						String additionalname = unformatString(itemUsers
								.element("additionalname").getText());
						String comment = unformatString(itemUsers.element(
								"comment").getText());
						// A User can not have a deleted Adress, you cannot
						// delete the
						// Adress of an User
						// String deleted = u.getAdresses().getDeleted()
						// Phone Number not done yet
						String fax = unformatString(itemUsers.element("fax")
								.getText());
						Long state_id = importLongType(unformatString(itemUsers
								.element("state_id").getText()));
						String street = unformatString(itemUsers.element(
								"street").getText());
						String town = unformatString(itemUsers.element("town")
								.getText());
						String zip = unformatString(itemUsers.element("zip")
								.getText());

						if (itemUsers.element("omTimeZone") != null) {
							OmTimeZone omTimeZone = omTimeZoneDaoImpl
									.getOmTimeZone(unformatString(itemUsers
											.element("omTimeZone").getText()));

							us.setOmTimeZone(omTimeZone);
							us.setForceTimeZoneCheck(false);
						} else {

							String jNameTimeZone = cfgManagement.getConfValue("default.timezone", String.class, "Europe/Berlin");
							OmTimeZone omTimeZone = omTimeZoneDaoImpl
									.getOmTimeZone(jNameTimeZone);
							us.setOmTimeZone(omTimeZone);
							us.setForceTimeZoneCheck(true);
						}

						String phone = "";
						if (itemUsers.element("phone") != null) {
							phone = unformatString(itemUsers.element("phone")
									.getText());
						}

						String email = "";
						if (itemUsers.element("mail") != null) {
							email = unformatString(itemUsers.element("mail")
									.getText());
						}

						States st = statemanagement.getStateById(state_id);
						if (st == null) {
							st = statemanagement.getStateById(1L);
						}

						us.setAdresses(street, zip, town,
								st, additionalname, comment, fax,
								phone, email);

						HashSet<Organisation_Users> orgUsers = new HashSet<Organisation_Users>();

						for (Iterator<Element> organisationsIterator = itemUsers
								.elementIterator("organisations"); organisationsIterator
								.hasNext();) {

							Element organisations = organisationsIterator
									.next();

							for (Iterator<Element> organisationIterator = organisations
									.elementIterator("user_organisation"); organisationIterator
									.hasNext();) {

								Element organisationObject = organisationIterator
										.next();

								Long organisation_id = getNewId(
										importLongType(unformatString(organisationObject
												.element("organisation_id")
												.getText())),
										Maps.ORGANISATIONS);
								Boolean isModerator = importBooleanType(unformatString(organisationObject
										.element("isModerator").getText()));
								String deleted = unformatString(organisationObject
										.element("deleted").getText());

								Organisation_Users orgUser = new Organisation_Users();
								orgUser.setOrganisation(organisationmanagement
										.getOrganisationByIdBackup(organisation_id));
								orgUser.setIsModerator(isModerator);
								orgUser.setStarttime(new Date());
								orgUser.setDeleted(deleted);

								orgUsers.add(orgUser);

							}

						}

						log.debug("Import User ID " + userId);
						us.setStarttime(new Date());
						Long actualNewUserId = userManagement.addUserBackup(us);
						usersMap.put(userId, actualNewUserId);

						for (Iterator<Organisation_Users> orgUserIterator = orgUsers
								.iterator(); orgUserIterator.hasNext();) {

							Organisation_Users organisationUsers = orgUserIterator
									.next();

							organisationmanagement
									.addOrganisationUserObj(actualNewUserId, organisationUsers);

						}

					}

				}
			}

		} catch (Exception err) {
			log.error("[getUsersByXML]", err);
		}
	}

	private void importFlvRecordings(File flvRecordingsListFile)
			throws Exception {

		List<FlvRecording> flvRecordings = this
				.getFlvRecordings(flvRecordingsListFile);

		for (FlvRecording flvRecording : flvRecordings) {

			Long flvRecordingId = flvRecordingDao
					.addFlvRecordingObj(flvRecording);

			for (FlvRecordingMetaData flvRecordingMetaData : flvRecording
					.getFlvRecordingMetaData()) {

				FlvRecording flvRecordingSaved = flvRecordingDao
						.getFlvRecordingById(flvRecordingId);

				flvRecordingMetaData.setFlvRecording(flvRecordingSaved);

				flvRecordingMetaDataDao
						.addFlvRecordingMetaDataObj(flvRecordingMetaData);

			}

		}

	}

	@SuppressWarnings("unchecked")
	private List<FlvRecording> getFlvRecordings(File flvRecordingsListFile) {
		try {

			List<FlvRecording> flvList = new LinkedList<FlvRecording>();

			SAXReader reader = new SAXReader();
			Document document = reader.read(flvRecordingsListFile);

			Element root = document.getRootElement();

			for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {

				Element itemObject = i.next();

				if (itemObject.getName().equals("flvrecordings")) {

					for (Iterator<Element> innerIter = itemObject
							.elementIterator("flvrecording"); innerIter
							.hasNext();) {

						Element flvObject = innerIter.next();

						String alternateDownload = unformatString(flvObject
								.element("alternateDownload").getText());
						String comment = unformatString(flvObject.element(
								"comment").getText());
						String deleted = unformatString(flvObject.element(
								"deleted").getText());
						String fileHash = unformatString(flvObject.element(
								"fileHash").getText());
						String fileName = unformatString(flvObject.element(
								"fileName").getText());
						String previewImage = unformatString(flvObject.element(
								"previewImage").getText());
						String recorderStreamId = unformatString(flvObject
								.element("recorderStreamId").getText());
						Long fileSize = importLongType(unformatString(flvObject
								.element("fileSize").getText()));
						Integer flvHeight = importIntegerType(unformatString(flvObject
								.element("flvHeight").getText()));
						Integer flvWidth = importIntegerType(unformatString(flvObject
								.element("flvWidth").getText()));
						Integer height = importIntegerType(unformatString(flvObject
								.element("height").getText()));
						Integer width = importIntegerType(unformatString(flvObject
								.element("width").getText()));
						Long insertedBy = getNewId(
								importLongType(unformatString(flvObject
										.element("insertedBy").getText())),
								Maps.USERS);
						Long organization_id = getNewId(
								importLongType(unformatString(flvObject
										.element("organization_id").getText())),
								Maps.ORGANISATIONS);
						Long ownerId = getNewId(
								importLongType(unformatString(flvObject
										.element("ownerId").getText())),
								Maps.USERS);
						Long parentFileExplorerItemId = getNewId(
								importLongType(unformatString(flvObject
										.element("parentFileExplorerItemId")
										.getText())), Maps.FILEEXPLORERITEMS);
						Integer progressPostProcessing = importIntegerType(unformatString(flvObject
								.element("progressPostProcessing").getText()));
						Long room_id = getNewId(
								importLongType(unformatString(flvObject
										.element("room_id").getText())),
								Maps.ROOMS);
						Date inserted = CalendarPatterns
								.parseImportDate(unformatString(flvObject
										.element("inserted").getText()));
						Boolean isFolder = importBooleanType(unformatString(flvObject
								.element("isFolder").getText()));
						Boolean isImage = importBooleanType(unformatString(flvObject
								.element("isImage").getText()));
						Boolean isInterview = importBooleanType(unformatString(flvObject
								.element("isInterview").getText()));
						Boolean isPresentation = importBooleanType(unformatString(flvObject
								.element("isPresentation").getText()));
						Boolean isRecording = importBooleanType(unformatString(flvObject
								.element("isRecording").getText()));
						Date recordEnd = CalendarPatterns
								.parseImportDate(unformatString(flvObject
										.element("recordEnd").getText()));
						Date recordStart = CalendarPatterns
								.parseImportDate(unformatString(flvObject
										.element("recordStart").getText()));

						FlvRecording flvRecording = new FlvRecording();
						flvRecording.setAlternateDownload(alternateDownload);
						flvRecording.setComment(comment);
						flvRecording.setFileHash(fileHash);
						flvRecording.setFileName(fileName);
						flvRecording.setPreviewImage(previewImage);
						flvRecording.setRecorderStreamId(recorderStreamId);
						flvRecording.setFileSize(fileSize);
						flvRecording.setFlvHeight(flvHeight);
						flvRecording.setFlvWidth(flvWidth);
						flvRecording.setHeight(height);
						flvRecording.setWidth(width);
						flvRecording.setInsertedBy(insertedBy);
						flvRecording.setOrganization_id(organization_id);
						flvRecording.setOwnerId(ownerId);
						flvRecording
								.setParentFileExplorerItemId(parentFileExplorerItemId);
						flvRecording
								.setProgressPostProcessing(progressPostProcessing);
						flvRecording.setRoom_id(room_id);
						flvRecording.setInserted(inserted);
						flvRecording.setIsFolder(isFolder);
						flvRecording.setIsImage(isImage);
						flvRecording.setIsInterview(isInterview);
						flvRecording.setIsPresentation(isPresentation);
						flvRecording.setIsRecording(isRecording);
						flvRecording.setRecordEnd(recordEnd);
						flvRecording.setRecordStart(recordStart);
						flvRecording.setDeleted(deleted);

						flvRecording
								.setFlvRecordingMetaData(new LinkedList<FlvRecordingMetaData>());

						Element flvrecordingmetadatas = flvObject
								.element("flvrecordingmetadatas");

						for (Iterator<Element> innerIterMetas = flvrecordingmetadatas
								.elementIterator("flvrecordingmetadata"); innerIterMetas
								.hasNext();) {

							Element flvrecordingmetadataObj = innerIterMetas
									.next();

							String freeTextUserName = unformatString(flvrecordingmetadataObj
									.element("freeTextUserName").getText());
							String fullWavAudioData = unformatString(flvrecordingmetadataObj
									.element("fullWavAudioData").getText());
							String streamName = unformatString(flvrecordingmetadataObj
									.element("streamName").getText());
							String wavAudioData = unformatString(flvrecordingmetadataObj
									.element("wavAudioData").getText());
							Integer initialGapSeconds = importIntegerType(unformatString(flvrecordingmetadataObj
									.element("initialGapSeconds").getText()));
							Long insertedBy1 = importLongType(unformatString(flvrecordingmetadataObj
									.element("insertedBy").getText()));
							Integer interiewPodId = importIntegerType(unformatString(flvrecordingmetadataObj
									.element("interiewPodId").getText()));
							Boolean audioIsValid = importBooleanType(unformatString(flvrecordingmetadataObj
									.element("audioIsValid").getText()));
							Date inserted1 = CalendarPatterns
									.parseImportDate(unformatString(flvrecordingmetadataObj
											.element("inserted").getText()));
							Boolean isAudioOnly = importBooleanType(unformatString(flvrecordingmetadataObj
									.element("isAudioOnly").getText()));
							Boolean isScreenData = importBooleanType(unformatString(flvrecordingmetadataObj
									.element("isScreenData").getText()));
							Boolean isVideoOnly = importBooleanType(unformatString(flvrecordingmetadataObj
									.element("isVideoOnly").getText()));
							Date recordEnd1 = CalendarPatterns
									.parseImportDate(unformatString(flvrecordingmetadataObj
											.element("recordEnd").getText()));
							Date recordStart1 = CalendarPatterns
									.parseImportDate(unformatString(flvrecordingmetadataObj
											.element("recordStart").getText()));
							Date updated = CalendarPatterns
									.parseImportDate(unformatString(flvrecordingmetadataObj
											.element("updated").getText()));

							FlvRecordingMetaData flvrecordingmetadata = new FlvRecordingMetaData();
							flvrecordingmetadata
									.setFreeTextUserName(freeTextUserName);
							flvrecordingmetadata
									.setFullWavAudioData(fullWavAudioData);
							flvrecordingmetadata.setStreamName(streamName);
							flvrecordingmetadata.setWavAudioData(wavAudioData);
							flvrecordingmetadata
									.setInitialGapSeconds(initialGapSeconds);
							flvrecordingmetadata.setInsertedBy(insertedBy1);
							flvrecordingmetadata
									.setInteriewPodId(interiewPodId);
							flvrecordingmetadata.setAudioIsValid(audioIsValid);
							flvrecordingmetadata.setInserted(inserted1);
							flvrecordingmetadata.setIsAudioOnly(isAudioOnly);
							flvrecordingmetadata.setIsScreenData(isScreenData);
							flvrecordingmetadata.setIsVideoOnly(isVideoOnly);
							flvrecordingmetadata.setRecordEnd(recordEnd1);
							flvrecordingmetadata.setRecordStart(recordStart1);
							flvrecordingmetadata.setUpdated(updated);
							flvrecordingmetadata.setDeleted("false");

							flvRecording.getFlvRecordingMetaData().add(
									flvrecordingmetadata);

						}

						flvList.add(flvRecording);

					}

				}
			}

			return flvList;

		} catch (Exception err) {
			log.error("[getFlvRecordings]", err);
		}
		return null;
	}

	private void importPrivateMessageFolders(File privateMessageFoldersFile)
			throws Exception {

		List<PrivateMessageFolder> privateMessageFolders = this
				.getPrivateMessageFoldersByXML(privateMessageFoldersFile);

		for (PrivateMessageFolder privateMessageFolder : privateMessageFolders) {

			Long folderId = privateMessageFolder.getPrivateMessageFolderId();
			PrivateMessageFolder storedFolder = privateMessageFolderDao
					.getPrivateMessageFolderById(folderId);
			if (storedFolder == null) {
				privateMessageFolder.setPrivateMessageFolderId(0);
				Long newFolderId = privateMessageFolderDao
						.addPrivateMessageFolderObj(privateMessageFolder);
				messageFoldersMap.put(folderId, newFolderId);
			}
		}

	}

	@SuppressWarnings("unchecked")
	private List<PrivateMessageFolder> getPrivateMessageFoldersByXML(
			File privateMessageFoldersFile) {
		try {

			List<PrivateMessageFolder> pmfList = new LinkedList<PrivateMessageFolder>();

			SAXReader reader = new SAXReader();
			Document document = reader.read(privateMessageFoldersFile);

			Element root = document.getRootElement();

			for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
				Element itemObject = i.next();
				if (itemObject.getName().equals("privatemessagefolders")) {

					for (Iterator<Element> innerIter = itemObject
							.elementIterator("privatemessagefolder"); innerIter
							.hasNext();) {

						Element pmfObject = innerIter.next();

						String folderName = unformatString(pmfObject.element(
								"folderName").getText());
						Long userId = getNewId(
								importLongType(unformatString(pmfObject
										.element("userId").getText())),
								Maps.USERS);
						Long privateMessageFolderId = importLongType(unformatString(pmfObject
								.element("privateMessageFolderId").getText()));

						PrivateMessageFolder privateMessageFolder = new PrivateMessageFolder();
						privateMessageFolder.setFolderName(folderName);
						privateMessageFolder.setUserId(userId);
						privateMessageFolder
								.setPrivateMessageFolderId(privateMessageFolderId);

						pmfList.add(privateMessageFolder);

					}

				}
			}

			return pmfList;

		} catch (Exception err) {
			log.error("[getPrivateMessageFoldersByXML]", err);
		}
		return null;
	}

	private void importPrivateMessages(File privateMessagesFile)
			throws Exception {

		List<PrivateMessages> pmList = this
				.getPrivateMessagesByXML(privateMessagesFile);

		for (PrivateMessages pm : pmList) {
			privateMessagesDao.addPrivateMessageObj(pm);
		}

	}

	@SuppressWarnings("unchecked")
	private List<PrivateMessages> getPrivateMessagesByXML(
			File privateMessagesFile) {
		try {

			List<PrivateMessages> pmList = new LinkedList<PrivateMessages>();

			SAXReader reader = new SAXReader();
			Document document = reader.read(privateMessagesFile);

			Element root = document.getRootElement();

			for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
				Element itemObject = i.next();
				if (itemObject.getName().equals("privatemessages")) {

					for (Iterator<Element> innerIter = itemObject
							.elementIterator("privatemessage"); innerIter
							.hasNext();) {

						Element pmObject = innerIter.next();

						String message = unformatString(pmObject.element(
								"message").getText());
						String subject = unformatString(pmObject.element(
								"subject").getText());
						Long privateMessageFolderId = getNewId(
								importLongType(unformatString(pmObject.element(
										"privateMessageFolderId").getText())),
								Maps.MESSAGEFOLDERS);
						Long userContactId = getNewId(
								importLongType(unformatString(pmObject.element(
										"userContactId").getText())),
								Maps.USERCONTACTS);
						Long parentMessage = importLongType(unformatString(pmObject
								.element("parentMessage").getText()));
						Boolean bookedRoom = importBooleanType(unformatString(pmObject
								.element("bookedRoom").getText()));
						Users from = userManagement.getUserById(getNewId(
								importLongType(unformatString(pmObject.element(
										"from").getText())), Maps.USERS));
						Users to = userManagement.getUserById(getNewId(
								importLongType(unformatString(pmObject.element(
										"to").getText())), Maps.USERS));
						Date inserted = CalendarPatterns
								.parseImportDate(unformatString(pmObject
										.element("inserted").getText()));
						Boolean isContactRequest = importBooleanType(unformatString(pmObject
								.element("isContactRequest").getText()));
						Boolean isRead = importBooleanType(unformatString(pmObject
								.element("isRead").getText()));
						Boolean isTrash = importBooleanType(unformatString(pmObject
								.element("isTrash").getText()));
						Users owner = userManagement.getUserById(getNewId(
								importLongType(unformatString(pmObject.element(
										"owner").getText())), Maps.USERS));
						Rooms room = roommanagement.getRoomById(getNewId(
								importLongType(unformatString(pmObject.element(
										"room").getText())), Maps.ROOMS));

						PrivateMessages pm = new PrivateMessages();
						pm.setMessage(message);
						pm.setSubject(subject);
						pm.setPrivateMessageFolderId(privateMessageFolderId);
						pm.setUserContactId(userContactId);
						pm.setParentMessage(parentMessage);
						pm.setBookedRoom(bookedRoom);
						pm.setFrom(from);
						pm.setTo(to);
						pm.setInserted(inserted);
						pm.setIsContactRequest(isContactRequest);
						pm.setIsRead(isRead);
						pm.setIsTrash(isTrash);
						pm.setOwner(owner);
						pm.setRoom(room);

						pmList.add(pm);

					}

				}
			}

			return pmList;

		} catch (Exception err) {
			log.error("[getPrivateMessagesByXML]", err);
		}
		return null;
	}

	private void importUserContacts(File userContactsFile) throws Exception {

		List<UserContacts> ucList = this.getUserContactsByXML(userContactsFile);

		for (UserContacts uc : ucList) {
			Long userContactId = uc.getUserContactId();
			UserContacts storedUC = userContactsDao
					.getUserContacts(userContactId);

			if (storedUC == null) {
				uc.setUserContactId(0);
				Long newId = userContactsDao.addUserContactObj(uc);
				this.userContactsMap.put(userContactId, newId);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<UserContacts> getUserContactsByXML(File userContactsFile) {
		try {

			List<UserContacts> ucList = new LinkedList<UserContacts>();

			SAXReader reader = new SAXReader();
			Document document = reader.read(userContactsFile);

			Element root = document.getRootElement();

			for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
				Element itemObject = i.next();
				if (itemObject.getName().equals("usercontacts")) {

					for (Iterator<Element> innerIter = itemObject
							.elementIterator("usercontact"); innerIter
							.hasNext();) {

						Element usercontact = innerIter.next();

						String hash = unformatString(usercontact
								.element("hash").getText());
						Users contact = userManagement.getUserById(getNewId(
								importLongType(unformatString(usercontact
										.element("contact").getText())),
								Maps.USERS));
						Users owner = userManagement.getUserById(getNewId(
								importLongType(unformatString(usercontact
										.element("owner").getText())),
								Maps.USERS));
						Boolean pending = importBooleanType(unformatString(usercontact
								.element("pending").getText()));
						Boolean shareCalendar = importBooleanType(unformatString(usercontact
								.element("shareCalendar").getText()));
						Long userContactId = importLongType(unformatString(usercontact
								.element("userContactId").getText()));

						UserContacts userContacts = new UserContacts();
						userContacts.setHash(hash);
						userContacts.setContact(contact);
						userContacts.setOwner(owner);
						userContacts.setPending(pending);
						userContacts.setShareCalendar(shareCalendar);
						userContacts.setUserContactId(userContactId);

						ucList.add(userContacts);

					}

				}
			}

			return ucList;

		} catch (Exception err) {
			log.error("[getUserContactsByXML]", err);
		}
		return null;
	}

	private void importOrganizsations(File orgFile) {
		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(orgFile);

			Element root = document.getRootElement();

			for (@SuppressWarnings("unchecked")
			Iterator<Element> i = root.elementIterator(); i.hasNext();) {
				Element itemObject = i.next();
				if (itemObject.getName().equals("organisations")) {
					for (@SuppressWarnings("unchecked")
					Iterator<Element> innerIter = itemObject
							.elementIterator("organisation"); innerIter
							.hasNext();) {

						Element orgObject = innerIter.next();

						Long organisation_id = importLongType(unformatString(orgObject
								.element("organisation_id").getText()));
						String name = unformatString(orgObject.element("name")
								.getText());
						String deleted = unformatString(orgObject.element(
								"deleted").getText());

						Organisation organisation = new Organisation();
						organisation.setName(name);
						organisation.setDeleted(deleted);

						Long newOrgID = organisationmanagement
								.addOrganisationObj(organisation);
						organisationsMap.put(organisation_id, newOrgID);
					}
				}
			}
		} catch (Exception err) {
			log.error("[getOrganisationsByXML]", err);
		}
	}

	private String unformatString(String str) {
		str = str.replaceAll(Pattern.quote("<![CDATA["), "");
		str = str.replaceAll(Pattern.quote("]]>"), "");
		return str;
	}

	private void importMeetingmembers(File meetingmembersListFile)
			throws Exception {

		List<MeetingMember> meetingmembersList = this
				.getMeetingmembersListByXML(meetingmembersListFile);

		for (MeetingMember ma : meetingmembersList) {

			// We need to reset this as openJPA reject to store them otherwise
			ma.setMeetingMemberId(null);

			meetingMemberDao.addMeetingMemberByObject(ma);
		}

	}

	@SuppressWarnings("unchecked")
	private List<MeetingMember> getMeetingmembersListByXML(
			File meetingmembersListFile) {
		try {

			List<MeetingMember> meetingmembersList = new LinkedList<MeetingMember>();

			SAXReader reader = new SAXReader();
			Document document = reader.read(meetingmembersListFile);

			Element root = document.getRootElement();

			for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
				Element itemObject = i.next();
				if (itemObject.getName().equals("meetingmembers")) {

					for (Iterator<Element> innerIter = itemObject
							.elementIterator("meetingmember"); innerIter
							.hasNext();) {

						Element appointmentsObject = innerIter.next();

						Long meetingMemberId = importLongType(unformatString(appointmentsObject
								.element("meetingMemberId").getText()));
						Long userid = getNewId(
								importLongType(unformatString(appointmentsObject
										.element("userid").getText())),
								Maps.USERS);
						Long appointment = getNewId(
								importLongType(unformatString(appointmentsObject
										.element("appointment").getText())),
								Maps.APPOINTMENTS);
						String firstname = unformatString(appointmentsObject
								.element("firstname").getText());
						String lastname = unformatString(appointmentsObject
								.element("lastname").getText());
						String memberStatus = unformatString(appointmentsObject
								.element("memberStatus").getText());
						String appointmentStatus = unformatString(appointmentsObject
								.element("appointmentStatus").getText());
						String email = unformatString(appointmentsObject
								.element("email").getText());
						Boolean deleted = importBooleanType(unformatString(appointmentsObject
								.element("deleted").getText()));
						Boolean invitor = importBooleanType(unformatString(appointmentsObject
								.element("invitor").getText()));

						MeetingMember meetingMember = new MeetingMember();
						meetingMember.setMeetingMemberId(meetingMemberId);
						meetingMember.setUserid(usersDao.getUser(userid));
						meetingMember.setAppointment(appointmentDao
								.getAppointmentByIdBackup(appointment));
						meetingMember.setFirstname(firstname);
						meetingMember.setLastname(lastname);
						meetingMember.setMemberStatus(memberStatus);
						meetingMember.setAppointmentStatus(appointmentStatus);
						meetingMember.setEmail(email);
						meetingMember.setDeleted(deleted);
						meetingMember.setInvitor(invitor);

						meetingmembersList.add(meetingMember);

					}

				}
			}

			return meetingmembersList;

		} catch (Exception err) {
			log.error("[meetingmembersList]", err);
		}
		return null;
	}

	private void importLdapConfig(File ldapConfigListFile) throws Exception {

		List<LdapConfig> ldapConfigList = this
				.getLdapConfigListByXML(ldapConfigListFile);

		for (LdapConfig ldapConfig : ldapConfigList) {
			ldapConfigDao.addLdapConfigByObject(ldapConfig);
		}

	}

	@SuppressWarnings("unchecked")
	private List<LdapConfig> getLdapConfigListByXML(File ldapConfigListFile) {
		try {

			List<LdapConfig> ldapConfigsList = new LinkedList<LdapConfig>();

			SAXReader reader = new SAXReader();
			Document document = reader.read(ldapConfigListFile);

			Element root = document.getRootElement();

			for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
				Element itemObject = i.next();
				if (itemObject.getName().equals("ldapconfigs")) {

					for (Iterator<Element> innerIter = itemObject
							.elementIterator("ldapconfig"); innerIter.hasNext();) {

						Element ldapconfigObject = innerIter.next();

						String name = unformatString(ldapconfigObject.element(
								"name").getText());
						String configFileName = unformatString(ldapconfigObject
								.element("configFileName").getText());
						Boolean addDomainToUserName = importBooleanType(unformatString(ldapconfigObject
								.element("addDomainToUserName").getText()));
						String domain = unformatString(ldapconfigObject
								.element("domain").getText());
						Boolean isActive = importBooleanType(unformatString(ldapconfigObject
								.element("isActive").getText()));

						LdapConfig ldapConfig = new LdapConfig();
						ldapConfig.setName(name);
						ldapConfig.setConfigFileName(configFileName);
						ldapConfig.setAddDomainToUserName(addDomainToUserName);
						ldapConfig.setDomain(domain);
						ldapConfig.setIsActive(isActive);

						ldapConfigsList.add(ldapConfig);

					}

				}
			}

			return ldapConfigsList;

		} catch (Exception err) {
			log.error("[getLdapConfigListByXML]", err);
		}
		return null;
	}

	private void importAppointements(File appointementListFile)
			throws Exception {

		List<Appointment> appointmentList = this
				.getAppointmentListByXML(appointementListFile);

		for (Appointment appointment : appointmentList) {
			Long appId = appointment.getAppointmentId();

			// We need to reset this as openJPA reject to store them otherwise
			appointment.setAppointmentId(null);

			Long newAppId = appointmentDao.addAppointmentObj(appointment);
			appointmentsMap.put(appId, newAppId);

		}

	}

	@SuppressWarnings("unchecked")
	private List<Appointment> getAppointmentListByXML(File appointementListFile) {
		try {

			List<Appointment> appointmentList = new LinkedList<Appointment>();

			SAXReader reader = new SAXReader();
			Document document = reader.read(appointementListFile);

			Element root = document.getRootElement();

			for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
				Element itemObject = i.next();
				if (itemObject.getName().equals("appointments")) {

					for (Iterator<Element> innerIter = itemObject
							.elementIterator("appointment"); innerIter
							.hasNext();) {

						Element appointmentsObject = innerIter.next();

						Long appointmentId = importLongType(unformatString(appointmentsObject
								.element("appointmentId").getText()));
						String appointmentName = unformatString(appointmentsObject
								.element("appointmentName").getText());
						String appointmentLocation = unformatString(appointmentsObject
								.element("appointmentLocation").getText());
						String appointmentDescription = unformatString(appointmentsObject
								.element("appointmentDescription").getText());
						Long categoryId = importLongType(unformatString(appointmentsObject
								.element("categoryId").getText()));
						Date appointmentStarttime = CalendarPatterns
								.parseImportDate(unformatString(appointmentsObject
										.element("appointmentStarttime")
										.getText()));
						Date appointmentEndtime = CalendarPatterns
								.parseImportDate(unformatString(appointmentsObject
										.element("appointmentEndtime")
										.getText()));
						String deleted = unformatString(appointmentsObject
								.element("deleted").getText());
						Long typId = importLongType(unformatString(appointmentsObject
								.element("typId").getText()));
						Boolean isDaily = importBooleanType(unformatString(appointmentsObject
								.element("isDaily").getText()));
						Boolean isWeekly = importBooleanType(unformatString(appointmentsObject
								.element("isWeekly").getText()));
						Boolean isMonthly = importBooleanType(unformatString(appointmentsObject
								.element("isMonthly").getText()));
						Boolean isYearly = importBooleanType(unformatString(appointmentsObject
								.element("isYearly").getText()));
						Long room_id = getNewId(
								importLongType(unformatString(appointmentsObject
										.element("room_id").getText())),
								Maps.ROOMS);
						String icalId = unformatString(appointmentsObject
								.element("icalId").getText());
						Long language_id = importLongType(unformatString(appointmentsObject
								.element("language_id").getText()));
						Boolean isPasswordProtected = importBooleanType(unformatString(appointmentsObject
								.element("isPasswordProtected").getText()));
						String password = unformatString(appointmentsObject
								.element("password").getText());
						Long users_id = getNewId(
								importLongType(unformatString(appointmentsObject
										.element("users_id").getText())),
								Maps.USERS);

						Appointment app = new Appointment();
						app.setAppointmentId(appointmentId);
						app.setAppointmentLocation(appointmentLocation);
						app.setAppointmentName(appointmentName);
						app.setAppointmentDescription(appointmentDescription);
						app.setAppointmentCategory(appointmentCategoryDaoImpl
								.getAppointmentCategoryById(categoryId));
						app.setAppointmentStarttime(appointmentStarttime);
						app.setAppointmentEndtime(appointmentEndtime);
						app.setDeleted(deleted);
						app.setRemind(appointmentReminderTypDaoImpl
								.getAppointmentReminderTypById(typId));
						app.setIsDaily(isDaily);
						app.setIsWeekly(isWeekly);
						app.setIsMonthly(isMonthly);
						app.setIsYearly(isYearly);
						app.setRoom(roommanagement.getRoomById(room_id));
						app.setIcalId(icalId);
						app.setLanguage_id(language_id);
						app.setIsPasswordProtected(isPasswordProtected);
						app.setPassword(password);
						app.setUserId(userManagement.getUserById(users_id));

						appointmentList.add(app);

					}

				}
			}

			return appointmentList;

		} catch (Exception err) {
			log.error("[getRoomListByXML]", err);
		}
		return null;
	}

	private void importOrgRooms(File orgRoomListFile) throws Exception {

		List<Rooms_Organisation> roomOrgList = this
				.getOrgRoomListByXML(orgRoomListFile);

		for (Rooms_Organisation rooms_Organisation : roomOrgList) {

			// We need to reset this as openJPA reject to store them otherwise
			rooms_Organisation.setRooms_organisation_id(null);

			roommanagement.addRoomOrganisation(rooms_Organisation);

		}

	}

	@SuppressWarnings("unchecked")
	private List<Rooms_Organisation> getOrgRoomListByXML(File orgRoomListFile) {
		try {

			List<Rooms_Organisation> orgRoomList = new LinkedList<Rooms_Organisation>();

			SAXReader reader = new SAXReader();
			Document document = reader.read(orgRoomListFile);

			Element root = document.getRootElement();

			for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
				Element itemObject = i.next();
				if (itemObject.getName().equals("room_organisations")) {

					for (Iterator<Element> innerIter = itemObject
							.elementIterator("room_organisation"); innerIter
							.hasNext();) {

						Element orgRoomObject = innerIter.next();

						Long rooms_organisation_id = importLongType(unformatString(orgRoomObject
								.element("rooms_organisation_id").getText()));
						Long organisation_id = getNewId(
								importLongType(unformatString(orgRoomObject
										.element("organisation_id").getText())),
								Maps.ORGANISATIONS);
						Long rooms_id = getNewId(
								importLongType(unformatString(orgRoomObject
										.element("rooms_id").getText())),
								Maps.ROOMS);
						String deleted = unformatString(orgRoomObject.element(
								"deleted").getText());

						Rooms_Organisation rooms_Organisation = new Rooms_Organisation();
						rooms_Organisation
								.setRooms_organisation_id(rooms_organisation_id);
						rooms_Organisation
								.setOrganisation(organisationmanagement
										.getOrganisationById(organisation_id));
						rooms_Organisation.setRoom(roommanagement
								.getRoomById(rooms_id));
						rooms_Organisation.setDeleted(deleted);

						orgRoomList.add(rooms_Organisation);

					}

				}
			}

			return orgRoomList;

		} catch (Exception err) {
			log.error("[getRoomListByXML]", err);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void importRooms(File roomFile) throws Exception {
		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(roomFile);

			Element root = document.getRootElement();

			Element rooms = root.element("rooms");
			for (Iterator<Element> innerIter = rooms
					.elementIterator("room"); innerIter.hasNext();) {

				Element roomObject = innerIter.next();

				Long rooms_id = importLongType(unformatString(roomObject
						.element("rooms_id").getText()));
				String name = unformatString(roomObject.element("name")
						.getText());
				String deleted = unformatString(roomObject.element(
						"deleted").getText());
				String comment = unformatString(roomObject.element(
						"comment").getText());
				Long numberOfPartizipants = importLongType(unformatString((roomObject
						.element("numberOfPartizipants").getText())));
				Boolean appointment = importBooleanType(unformatString(roomObject
						.element("appointment").getText()));
				Long externalRoomId = importLongType(unformatString(roomObject
						.element("externalRoomId").getText()));
				String externalRoomType = unformatString(roomObject
						.element("externalRoomType").getText());
				Long roomtypes_id = importLongType(unformatString(roomObject
						.element("roomtypeId").getText()));
				
				Boolean isDemoRoom = false;
				if (roomObject
						.element("isDemoRoom") != null) {
					isDemoRoom = importBooleanType(unformatString(roomObject
						.element("isDemoRoom").getText()));
				}
				
				Integer demoTime = null;
				if (roomObject
						.element("demoTime") != null) {
					demoTime = importIntegerType(unformatString(roomObject
						.element("demoTime").getText()));
				}
				
				Boolean isModeratedRoom = false;
				if (roomObject.element("isModeratedRoom") != null) {
					isModeratedRoom = importBooleanType(unformatString(roomObject
						.element("isModeratedRoom").getText()));
				}
				
				Boolean allowUserQuestions = true;
				if (roomObject.element("allowUserQuestions") != null) {
					allowUserQuestions = importBooleanType(unformatString(roomObject
						.element("allowUserQuestions").getText()));
				}
				
				
				Boolean isAudioOnly = false;
				if (roomObject.element("isAudioOnly") != null) {
					isAudioOnly = importBooleanType(unformatString(roomObject
						.element("isAudioOnly").getText()));
				}
				
				String sipNumber = "";
				if (roomObject.element("sipNumber") != null) {
					sipNumber = unformatString(roomObject.element(
						"sipNumber").getText());
				}
				
				String conferencePin = "";
				if (roomObject.element("conferencePin") != null) {
					conferencePin = unformatString(roomObject
							.element("conferencePin").getText());
				}
				
				Boolean showMicrophoneStatus = false;
				if (roomObject.element("showMicrophoneStatus") != null) {
					showMicrophoneStatus = importBooleanType(unformatString(roomObject
							.element("showMicrophoneStatus").getText()));
				}

				Long ownerId = null;
				if (roomObject.element("ownerid") != null) {
					ownerId = getNewId(
							importLongType(unformatString(roomObject
									.element("ownerid").getText())),
							Maps.USERS);
				}

				Boolean ispublic = false;
				if (roomObject.element("ispublic") != null) {
					ispublic = importBooleanType(unformatString(roomObject
							.element("ispublic").getText()));
				}

				Boolean waitForRecording = false;
				if (roomObject.element("waitForRecording") != null) {
					waitForRecording = importBooleanType(unformatString(roomObject
							.element("waitForRecording").getText()));
				}

				Boolean hideTopBar = false;
				if (roomObject.element("hideTopBar") != null) {
					hideTopBar = importBooleanType(unformatString(roomObject
							.element("hideTopBar").getText()));
				}

				Boolean isClosed = false;
				if (roomObject.element("isClosed") != null) {
					isClosed = importBooleanType(unformatString(roomObject
							.element("isClosed").getText()));
				}

				Boolean allowRecording = false;
				if (roomObject.element("allowRecording") != null) {
					allowRecording = importBooleanType(unformatString(roomObject
							.element("allowRecording").getText()));
				}

				String redirectURL = "";
				if (roomObject.element("redirectURL") != null) {
					redirectURL = unformatString(roomObject.element(
							"redirectURL").getText());
				}
				
				Boolean hideActionsMenu = false;
				if (roomObject.element("hideActionsMenu") != null) {
					hideTopBar = importBooleanType(unformatString(roomObject
							.element("hideActionsMenu").getText()));
				}
				
				Boolean hideActivitiesAndActions = false;
				if (roomObject.element("hideActivitiesAndActions") != null) {
					hideTopBar = importBooleanType(unformatString(roomObject
							.element("hideActivitiesAndActions").getText()));
				}
				
				Boolean hideChat = false;
				if (roomObject.element("hideChat") != null) {
					hideTopBar = importBooleanType(unformatString(roomObject
							.element("hideChat").getText()));
				}
				
				Boolean hideFilesExplorer = false;
				if (roomObject.element("hideFilesExplorer") != null) {
					hideTopBar = importBooleanType(unformatString(roomObject
							.element("hideFilesExplorer").getText()));
				}
				
				Boolean hideScreenSharing = false;
				if (roomObject.element("hideScreenSharing") != null) {
					hideTopBar = importBooleanType(unformatString(roomObject
							.element("hideScreenSharing").getText()));
				}
				
				Boolean hideWhiteboard = false;
				if (roomObject.element("hideWhiteboard") != null) {
					hideTopBar = importBooleanType(unformatString(roomObject
							.element("hideWhiteboard").getText()));
				}

				Rooms room = new Rooms();
				room.setRooms_id(rooms_id);
				room.setOwnerId(ownerId);
				room.setName(name);
				room.setDeleted(deleted);
				room.setComment(comment);
				room.setNumberOfPartizipants(numberOfPartizipants);
				room.setAppointment(appointment);
				room.setExternalRoomId(externalRoomId);
				room.setExternalRoomType(externalRoomType);
				room.setRoomtype(roommanagement
						.getRoomTypesById(roomtypes_id));
				room.setIsDemoRoom(isDemoRoom);
				room.setDemoTime(demoTime);
				room.setIsModeratedRoom(isModeratedRoom);
				room.setAllowUserQuestions(allowUserQuestions);
				room.setIsAudioOnly(isAudioOnly);
				room.setSipNumber(sipNumber);
				room.setConferencePin(conferencePin);
				room.setIspublic(ispublic);
				room.setIsClosed(isClosed);
				room.setRedirectURL(redirectURL);
				room.setWaitForRecording(waitForRecording);
				room.setHideTopBar(hideTopBar);
				room.setAllowRecording(allowRecording);
				room.setShowMicrophoneStatus(showMicrophoneStatus);						
				room.setHideActionsMenu(hideActionsMenu);
				room.setHideActivitiesAndActions(hideActivitiesAndActions);
				room.setHideChat(hideChat);
				room.setHideFilesExplorer(hideFilesExplorer);
				room.setHideScreenSharing(hideScreenSharing);
				room.setHideWhiteboard(hideWhiteboard);

				Long roomId = room.getRooms_id();

				// We need to reset this as openJPA reject to store them
				// otherwise
				room.setRooms_id(null);

				Long newRoomId = roommanagement.addRoom(room);
				roomsMap.put(roomId, newRoomId);

				for (Iterator<Element> iterMods = roomObject
						.elementIterator("room_moderators"); iterMods
						.hasNext();) {

					Element room_moderators = iterMods.next();

					for (Iterator<Element> iterMod = room_moderators
							.elementIterator("room_moderator"); iterMod
							.hasNext();) {

						Element room_moderator = iterMod.next();

						RoomModerators roomModerators = new RoomModerators();

						Long user_id = getNewId(
								importLongType(unformatString(room_moderator
										.element("user_id").getText())),
								Maps.USERS);
						Boolean is_supermoderator = importBooleanType(unformatString(room_moderator
								.element("is_supermoderator").getText()));

						roomModerators.setDeleted("false");
						roomModerators.setRoomId(getNewId(rooms_id,
								Maps.ROOMS));
						roomModerators.setUser(userManagement
								.getUserById(user_id));
						roomModerators
								.setIsSuperModerator(is_supermoderator);

						roomModeratorsDao
								.addRoomModeratorByObj(roomModerators);

					}
				}
			}

		} catch (Exception err) {
			log.error("[getRoomListByXML]", err);
		}
	}

	private void importFileExplorerItems(File fileExplorerItemsListFile)
			throws Exception {

		List<FileExplorerItem> fileExplorerItems = this
				.getFileExplorerItems(fileExplorerItemsListFile);

		for (FileExplorerItem fileExplorerItem : fileExplorerItems) {

			// We need to reset this as openJPA reject to store them otherwise
			long itemId = fileExplorerItem.getFileExplorerItemId();

			fileExplorerItem.setFileExplorerItemId(0);
			long newItemId = fileExplorerItemDao
					.addFileExplorerItem(fileExplorerItem);
			fileExplorerItemsMap.put(itemId, newItemId);

		}

	}

	@SuppressWarnings("unchecked")
	private List<FileExplorerItem> getFileExplorerItems(
			File fileExplorerItemsListFile) {
		try {

			List<FileExplorerItem> fileExplorerItemsList = new LinkedList<FileExplorerItem>();

			SAXReader reader = new SAXReader();
			Document document = reader.read(fileExplorerItemsListFile);

			Element root = document.getRootElement();

			for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {

				Element itemObject = i.next();

				if (itemObject.getName().equals("fileExplorerItems")) {

					for (Iterator<Element> innerIter = itemObject
							.elementIterator("fileExplorerItem"); innerIter
							.hasNext();) {

						Element fileExplorerItemObj = innerIter.next();

						Long fileExplorerItemId = importLongType(unformatString(fileExplorerItemObj
								.element("fileExplorerItemId").getText()));
						String fileName = unformatString(fileExplorerItemObj
								.element("fileName").getText());
						String fileHash = unformatString(fileExplorerItemObj
								.element("fileHash").getText());
						Long parentFileExplorerItemId = importLongType(unformatString(fileExplorerItemObj
								.element("parentFileExplorerItemId").getText()));
						Long room_id = getNewId(
								importLongType(unformatString(fileExplorerItemObj
										.element("room_id").getText())),
								Maps.ROOMS);
						Long ownerId = getNewId(
								importLongType(unformatString(fileExplorerItemObj
										.element("ownerId").getText())),
								Maps.USERS);
						Boolean isFolder = importBooleanType(unformatString(fileExplorerItemObj
								.element("isFolder").getText()));
						Boolean isImage = importBooleanType(unformatString(fileExplorerItemObj
								.element("isImage").getText()));
						Boolean isPresentation = importBooleanType(unformatString(fileExplorerItemObj
								.element("isPresentation").getText()));
						Boolean isVideo = importBooleanType(unformatString(fileExplorerItemObj
								.element("isVideo").getText()));
						Long insertedBy = getNewId(
								importLongType(unformatString(fileExplorerItemObj
										.element("insertedBy").getText())),
								Maps.USERS);
						Date inserted = CalendarPatterns
								.parseImportDate(unformatString(fileExplorerItemObj
										.element("inserted").getText()));
						Date updated = CalendarPatterns
								.parseImportDate(unformatString(fileExplorerItemObj
										.element("updated").getText()));
						String deleted = unformatString(fileExplorerItemObj
								.element("deleted").getText());
						Long fileSize = importLongType(unformatString(fileExplorerItemObj
								.element("fileSize").getText()));
						Integer flvWidth = importIntegerType(unformatString(fileExplorerItemObj
								.element("flvWidth").getText()));
						Integer flvHeight = importIntegerType(unformatString(fileExplorerItemObj
								.element("flvHeight").getText()));
						String previewImage = unformatString(fileExplorerItemObj
								.element("previewImage").getText());
						String wmlFilePath = unformatString(fileExplorerItemObj
								.element("wmlFilePath").getText());
						Boolean isStoredWmlFile = importBooleanType(unformatString(fileExplorerItemObj
								.element("isStoredWmlFile").getText()));
						Boolean isChart = importBooleanType(unformatString(fileExplorerItemObj
								.element("isChart").getText()));

						FileExplorerItem fileExplorerItem = new FileExplorerItem();
						fileExplorerItem
								.setFileExplorerItemId(fileExplorerItemId);
						fileExplorerItem.setFileName(fileName);
						fileExplorerItem.setFileHash(fileHash);
						fileExplorerItem
								.setParentFileExplorerItemId(parentFileExplorerItemId);
						fileExplorerItem.setRoom_id(room_id);
						fileExplorerItem.setOwnerId(ownerId);
						fileExplorerItem.setIsFolder(isFolder);
						fileExplorerItem.setIsImage(isImage);
						fileExplorerItem.setIsPresentation(isPresentation);
						fileExplorerItem.setIsVideo(isVideo);
						fileExplorerItem.setInsertedBy(insertedBy);
						fileExplorerItem.setInserted(inserted);
						fileExplorerItem.setUpdated(updated);
						fileExplorerItem.setDeleted(deleted);
						fileExplorerItem.setFileSize(fileSize);
						fileExplorerItem.setFlvWidth(flvWidth);
						fileExplorerItem.setFlvHeight(flvHeight);
						fileExplorerItem.setPreviewImage(previewImage);
						fileExplorerItem.setWmlFilePath(wmlFilePath);
						fileExplorerItem.setIsStoredWmlFile(isStoredWmlFile);
						fileExplorerItem.setIsChart(isChart);

						fileExplorerItemsList.add(fileExplorerItem);

					}

				}
			}

			return fileExplorerItemsList;

		} catch (Exception err) {
			log.error("[getFileExplorerItems]", err);
		}
		return null;
	}

	private void importFolders(String current_dir, File importBaseDir)
			throws IOException {

		// Now check the room files and import them
		File roomFilesFolder = new File(importBaseDir, "roomFiles");

		File library_dir = new File(current_dir, OpenmeetingsVariables.UPLOAD_DIR);

		log.debug("roomFilesFolder PATH " + roomFilesFolder.getAbsolutePath());

		if (roomFilesFolder.exists()) {

			File[] files = roomFilesFolder.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {

					File parentPathFile = new File(library_dir, file.getName());

					if (!parentPathFile.exists()) {
						parentPathFile.mkdir();
					}

					File[] roomOrProfileFiles = file.listFiles();
					for (File roomOrProfileFileOrFolder : roomOrProfileFiles) {

						if (roomOrProfileFileOrFolder.isDirectory()) {

							String fileOrFolderName = roomOrProfileFileOrFolder
									.getName();
							int beginIndex = fileOrFolderName
									.indexOf(ScopeApplicationAdapter.profilesPrefix);
							// Profile folder should be renamed if new user id
							// is differ from current id.
							if (beginIndex > -1) {
								beginIndex = beginIndex
										+ ScopeApplicationAdapter.profilesPrefix
												.length();
								Long profileId = importLongType(fileOrFolderName
										.substring(beginIndex));
								Long newProfileID = getNewId(profileId,
										Maps.USERS);
								if (profileId != newProfileID) {
									fileOrFolderName = fileOrFolderName
											.replaceFirst(
													ScopeApplicationAdapter.profilesPrefix
															+ profileId,
													ScopeApplicationAdapter.profilesPrefix
															+ newProfileID);
								}
							}
							File roomDocumentFolder = new File(parentPathFile, fileOrFolderName);

							if (!roomDocumentFolder.exists()) {
								roomDocumentFolder.mkdir();

								File[] roomDocumentFiles = roomOrProfileFileOrFolder
										.listFiles();

								for (File roomDocumentFile : roomDocumentFiles) {

									if (roomDocumentFile.isDirectory()) {
										log.error("Folder detected in Documents space! Folder "
												+ roomDocumentFolder);
									} else {
										copyFile(roomDocumentFile,
											new File(roomDocumentFolder, roomDocumentFile.getName()));
									}
								}
							} else {
								log.debug("Document already exists :: ",
										roomDocumentFolder);
							}
						} else {
							File roomFileOrProfileFile = new File(parentPathFile, roomOrProfileFileOrFolder.getName());
							if (!roomFileOrProfileFile.exists()) {
								this.copyFile(roomOrProfileFileOrFolder,
										roomFileOrProfileFile);
							} else {
								log.debug("File does already exist :: ", roomFileOrProfileFile);
							}
						}
					}
				}
			}
		}

		// Now check the recordings and import them

		File sourceDirRec = new File(importBaseDir, "recordingFiles");

		log.debug("sourceDirRec PATH " + sourceDirRec.getAbsolutePath());

		if (sourceDirRec.exists()) {

			File targetDirRec = new File(current_dir, OpenmeetingsVariables.STREAMS_DIR
					+ File.separatorChar + "hibernate" + File.separatorChar);

			copyDirectory(sourceDirRec, targetDirRec);

		}

	}

	private Integer importIntegerType(String value) {

		if (value.equals("null") || value.equals("")) {
			return null;
		}

		return Integer.valueOf(value).intValue();

	}

	private Long importLongType(String value) {

		if (value.equals("null") || value.equals("")) {
			return null;
		}

		return Long.valueOf(value).longValue();

	}

	private Boolean importBooleanType(String value) {

		if (value.equals("null") || value.equals("")) {
			return null;
		}

		return Boolean.valueOf(value).booleanValue();

	}

	private Long getNewId(Long oldId, Maps map) {
		Long newId = oldId;
		switch (map) {
		case USERS:
			if (usersMap.get(oldId) != null)
				newId = usersMap.get(oldId);
			break;
		case ORGANISATIONS:
			if (organisationsMap.get(oldId) != null)
				newId = organisationsMap.get(oldId);
			break;
		case APPOINTMENTS:
			if (appointmentsMap.get(oldId) != null)
				newId = appointmentsMap.get(oldId);
			break;
		case ROOMS:
			if (roomsMap.get(oldId) != null)
				newId = roomsMap.get(oldId);
			break;
		case MESSAGEFOLDERS:
			if (messageFoldersMap.get(oldId) != null)
				newId = messageFoldersMap.get(oldId);
			break;
		case USERCONTACTS:
			if (userContactsMap.get(oldId) != null)
				newId = userContactsMap.get(oldId);
			break;
		case FILEEXPLORERITEMS:
			if (fileExplorerItemsMap.get(oldId) != null)
				newId = fileExplorerItemsMap.get(oldId);
			break;
		default:
			break;
		}
		return newId;
	}

}
