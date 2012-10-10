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
package org.openmeetings.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.apache.openjpa.jdbc.meta.MappingTool;
import org.openmeetings.app.OpenmeetingsVariables;
import org.openmeetings.app.data.file.FileUtils;
import org.openmeetings.app.documents.InstallationDocumentHandler;
import org.openmeetings.app.installation.ImportInitvalues;
import org.openmeetings.app.installation.InstallationConfig;
import org.openmeetings.app.remote.red5.ScopeApplicationAdapter;
import org.openmeetings.servlet.outputhandler.BackupExport;
import org.openmeetings.servlet.outputhandler.BackupImportController;
import org.openmeetings.utils.ImportHelper;
import org.openmeetings.utils.OMContextListener;
import org.openmeetings.utils.mail.MailUtil;
import org.openmeetings.utils.math.CalendarPatterns;
import org.quartz.SchedulerException;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

public class Admin {
	private static final Logger log = Red5LoggerFactory.getLogger(Admin.class);
	
	private boolean verbose = false;
	private InstallationConfig cfg = null;
	private Options opts = null;
	private CommandLine cmdl = null;
	private File omHome = null;
	private ClassPathXmlApplicationContext ctx = null; 

	private Admin() {
		cfg = new InstallationConfig();
		opts = buildOptions();
	}
	
	private Options buildOptions() {
		Options options = new Options();
		OptionGroup group = new OptionGroup()
			.addOption(new OmOption("h", 0, "h", "help", false, "prints this message"))
			.addOption(new OmOption("b", 1, "b", "backup", false, "Backups OM"))
			.addOption(new OmOption("r", 2, "r", "restore", false, "Restores OM"))
			.addOption(new OmOption("i", 3, "i", "install", false, "Fill DB table, and make OM usable"))
			.addOption(new OmOption("f", 4, "f", "files", false, "File operations - statictics/cleanup"));
		group.setRequired(true); 
		options.addOptionGroup(group);
		//general
		options.addOption(new OmOption(null, "v", "verbose", false, "verbose error messages"));
		//backup/restore
		options.addOption(new OmOption("b", null, "exclude-files", false, "should backup exclude files [default: include]", true));
		options.addOption(new OmOption("b,r,i", "file", null, true, "file used for backup/restore/install", "b"));
		//install
		options.addOption(new OmOption("i", "user", null, true, "Login name of the default user, minimum " + InstallationConfig.USER_LOGIN_MINIMUM_LENGTH + " characters (mutually exclusive with 'file')"));
		options.addOption(new OmOption("i", "email", null, true, "Email of the default user (mutually exclusive with 'file')"));
		options.addOption(new OmOption("i", "group", null, true, "The name of the default user group (mutually exclusive with 'file')"));
		options.addOption(new OmOption("i", "tz", null, true, "Default server time zone, and time zone for the selected user (mutually exclusive with 'file')"));
		options.addOption(new OmOption("i", null, "password", true, "Password of the default user, minimum " + InstallationConfig.USER_LOGIN_MINIMUM_LENGTH + " characters (will be prompted if not set)", true));
		options.addOption(new OmOption("i", null, "system-email-address", true, "System e-mail address [default: " + cfg.mailReferer + "]", true));
		options.addOption(new OmOption("i", null, "smtp-server", true, "SMTP server for outgoing e-mails [default: " + cfg.smtpServer + "]", true));
		options.addOption(new OmOption("i", null, "smtp-port", true, "SMTP server for outgoing e-mails [default: " + cfg.smtpPort + "]", true));
		options.addOption(new OmOption("i", null, "email-auth-user", true, "Email auth username (anonymous connection will be used if not set)", true));
		options.addOption(new OmOption("i", null, "email-auth-pass", true, "Email auth password (anonymous connection will be used if not set)", true));
		options.addOption(new OmOption("i", null, "email-use-tls", false, "Is secure e-mail connection [default: no]", true));
		options.addOption(new OmOption("i", null, "skip-default-rooms", false, "Do not create default rooms [created by default]", true));
		options.addOption(new OmOption("i", null, "disable-frontend-register", false, "Do not allow front end register [allowed by default]", true));

		options.addOption(new OmOption("i", null, "db-type", true, "The type of the DB to be used", true));
		options.addOption(new OmOption("i", null, "db-host", true, "DNS name or IP address of database", true));
		options.addOption(new OmOption("i", null, "db-port", true, "Database port", true));
		options.addOption(new OmOption("i", null, "db-name", true, "The name of Openmeetings database", true));
		options.addOption(new OmOption("i", null, "db-user", true, "User with write access to the DB specified", true));
		options.addOption(new OmOption("i", null, "db-pass", true, "Password of the user with write access to the DB specified", true));
		options.addOption(new OmOption("i", null, "drop", false, "Drop database before installation", true));
		options.addOption(new OmOption("i", null, "force", false, "Install without checking the existence of old data in the database.", true));
		
		return options;
	}
	
	private enum Command {
		install
		, backup
		, restore
		, files
		, usage
	}
	
	private void usage() {
		OmHelpFormatter formatter = new OmHelpFormatter();
		formatter.setWidth(100);
		formatter.printHelp("admin", opts);
	}
	
	private void handleError(String msg, Exception e) {
		handleError(msg, e, false);
	}
	
	private void handleError(String msg, Exception e, boolean printUsage) {
		if (printUsage) {
			usage();
		}
		if (verbose) {
			log.error(msg, e);
		} else {
			log.error(msg + " " + e.getMessage());
		}
		System.exit(1);
	}
	
	private ClassPathXmlApplicationContext getApplicationContext(final String ctxName) {
		if (ctx == null) {
			OMContextListener omcl = new OMContextListener();
			omcl.contextInitialized(new ServletContextEvent(new ServletContext() {
				public void setAttribute(String arg0, Object arg1) {
				}
				
				public void removeAttribute(String arg0) {
				}
				
				public void log(String arg0, Throwable arg1) {
				}
				
				public void log(Exception arg0, String arg1) {
				}
				
				public void log(String arg0) {
				}
				
				@SuppressWarnings("rawtypes")
				public Enumeration getServlets() {
					return null;
				}
				
				@SuppressWarnings("rawtypes")
				public Enumeration getServletNames() {
					return null;
				}
				
				public String getServletContextName() {
					return null;
				}
				
				public Servlet getServlet(String arg0) throws ServletException {
					return null;
				}
				
				public String getServerInfo() {
					return null;
				}
				
				@SuppressWarnings("rawtypes")
				public Set getResourcePaths(String arg0) {
					return null;
				}
				
				public InputStream getResourceAsStream(String arg0) {
					return null;
				}
				
				public URL getResource(String arg0) throws MalformedURLException {
					return null;
				}
				
				public RequestDispatcher getRequestDispatcher(String arg0) {
					return null;
				}
				
				public String getRealPath(String arg0) {
					return null;
				}
				
				public RequestDispatcher getNamedDispatcher(String arg0) {
					return null;
				}
				
				public int getMinorVersion() {
					return 0;
				}
				
				public String getMimeType(String arg0) {
					return null;
				}
				
				public int getMajorVersion() {
					return 0;
				}
				
				@SuppressWarnings("rawtypes")
				public Enumeration getInitParameterNames() {
					return null;
				}
				
				public String getInitParameter(String arg0) {
					return null;
				}
				
				public String getContextPath() {
					return ctxName;
				}
				
				public ServletContext getContext(String arg0) {
					return null;
				}
				
				@SuppressWarnings("rawtypes")
				public Enumeration getAttributeNames() {
					return null;
				}
				
				public Object getAttribute(String arg0) {
					return null;
				}
			}));
			try {
				ctx = new ClassPathXmlApplicationContext("openmeetings-applicationContext.xml");
			} catch (Exception e) {
				handleError("Unable to obtain application context", e);
			}
		}
		return ctx;
	}
	
	private void process(String[] args) {
		String ctxName = System.getProperty("context", "openmeetings");
		File home = new File(System.getenv("RED5_HOME"));
		omHome = new File(new File(home, "webapps"), ctxName);
		File omUploadTemp = new File(omHome, OpenmeetingsVariables.UPLOAD_TEMP_DIR);
		
		Parser parser = new PosixParser();
		try {
			cmdl = parser.parse(opts, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			usage();
			System.exit(1);
		}
		verbose = cmdl.hasOption('v');

		Command cmd = Command.usage;
		if (cmdl.hasOption('i')) {
			cmd = Command.install;
		} else if (cmdl.hasOption('b')) {
			cmd = Command.backup;
		} else if (cmdl.hasOption('r')) {
			cmd = Command.restore;
		} else if (cmdl.hasOption('f')) {
			cmd = Command.files;
		}

		String file = cmdl.getOptionValue("file", "");
		switch(cmd) {
			case install:
				try {
					if (cmdl.hasOption("file") && (cmdl.hasOption("user") || cmdl.hasOption("email") || cmdl.hasOption("group"))) {
						System.out.println("Please specify even 'file' option or 'admin user'.");
						System.exit(1);
					}
					boolean force = cmdl.hasOption("force");
					if (cmdl.hasOption("skip-default-rooms")) {
						cfg.createDefaultRooms = "0";
					}
					if (cmdl.hasOption("disable-frontend-register")) {
						cfg.allowFrontendRegister = "0";
					}
					if (cmdl.hasOption("system-email-address")) {
						cfg.mailReferer = cmdl.getOptionValue("system-email-address");
					}
					if (cmdl.hasOption("smtp-server")) {
						cfg.smtpServer = cmdl.getOptionValue("smtp-server");
					}
					if (cmdl.hasOption("smtp-port")) {
						cfg.smtpPort = cmdl.getOptionValue("smtp-port");
					}
					if (cmdl.hasOption("email-auth-user")) {
						cfg.mailAuthName = cmdl.getOptionValue("email-auth-user");
					}
					if (cmdl.hasOption("email-auth-pass")) {
						cfg.mailAuthPass = cmdl.getOptionValue("email-auth-pass");
					}
					if (cmdl.hasOption("email-use-tls")) {
						cfg.mailUseTls = "1";
					}
					String langPath = new File(omHome, ImportInitvalues.languageFolderName).getAbsolutePath(); //FIXME need to be moved to helper
					ConnectionProperties connectionProperties = new ConnectionProperties();
					File conf = new File(omHome, "WEB-INF/classes/META-INF/persistence.xml");
					if (!conf.exists() || cmdl.hasOption("db-type") || cmdl.hasOption("db-host") || cmdl.hasOption("db-port") || cmdl.hasOption("db-name") || cmdl.hasOption("db-user") || cmdl.hasOption("db-pass")) {
						String dbType = cmdl.getOptionValue("db-type", "derby");
						File srcConf = new File(omHome, "WEB-INF/classes/META-INF/" + dbType + "_persistence.xml");
						ConnectionPropertiesPatcher.getPatcher(dbType).patch(
								srcConf
								, conf
								, cmdl.getOptionValue("db-host", "localhost")
								, cmdl.getOptionValue("db-port", null)
								, cmdl.getOptionValue("db-name", null)
								, cmdl.getOptionValue("db-user", null)
								, cmdl.getOptionValue("db-pass", null)
								, connectionProperties
								);
					} else {
						//get properties from existent persistence.xml
						connectionProperties = ConnectionPropertiesPatcher.getConnectionProperties(conf);
					}
					if (cmdl.hasOption("file")) {
						File backup = checkRestoreFile(file);
						dropDB(connectionProperties);
						
						shutdownScheduledJobs(ctxName);
						ImportInitvalues importInit = getApplicationContext(ctxName).getBean(ImportInitvalues.class);
						importInit.loadSystem(langPath, cfg, force); 
						restoreOm(ctxName, backup);
					} else {
						AdminUserDetails admin = checkAdminDetails(ctxName, langPath);
						dropDB(connectionProperties);
						
						shutdownScheduledJobs(ctxName);
						ImportInitvalues importInit = getApplicationContext(ctxName).getBean(ImportInitvalues.class);
						importInit.loadAll(langPath, cfg, admin.login, admin.pass, admin.email, admin.group, admin.tz, force);
					}					
					
					File installerFile = new File(new File(omHome, ScopeApplicationAdapter.configDirName), InstallationDocumentHandler.installFileName);
					InstallationDocumentHandler.getInstance().createDocument(installerFile.getAbsolutePath(), 3);
				} catch(Exception e) {
					handleError("Install failed", e);
				}
				break;
			case backup:
				try {
					if (!cmdl.hasOption("file")) {
						file = "backup_" + CalendarPatterns.getTimeForStreamId(new Date()) + ".zip";
						System.out.println("File name was not specified, '" + file + "' will be used");
					}
					boolean includeFiles = Boolean.parseBoolean(cmdl.getOptionValue("exclude-files", "true"));
					File backup_dir = new File(omUploadTemp, "" + System.currentTimeMillis());
					backup_dir.mkdirs();
					
					shutdownScheduledJobs(ctxName);
					BackupExport export = getApplicationContext(ctxName).getBean(BackupExport.class);
					export.performExport(file, backup_dir, includeFiles, omHome.getAbsolutePath());
					export.deleteDirectory(backup_dir);
					backup_dir.delete();
				} catch (Exception e) {
					handleError("Backup failed", e);
				}
				break;
			case restore:
				try {
					shutdownScheduledJobs(ctxName);
					restoreOm(ctxName, checkRestoreFile(file));
				} catch (Exception e) {
					handleError("Restore failed", e);
				}
				break;
			case files:
				try {
					File omUpload = new File(omHome, OpenmeetingsVariables.UPLOAD_DIR);
					File omStreams = new File(omHome, OpenmeetingsVariables.STREAMS_DIR);
					System.out.println("Temporary upload files allocates: " + FileUtils.getHumanSize(omUploadTemp));
					System.out.println("Upload allocates: " + FileUtils.getHumanSize(omUpload));
					System.out.println("Recordings allocates: " + FileUtils.getHumanSize(omStreams));
					/*
					omHome
					
					ClassPathXmlApplicationContext ctx = getApplicationContext(ctxName);
					//user pictures
					//dist/red5/webapps/openmeetings/upload/profiles/profile_<id> (check if ends with filename)
					UsersDaoImpl udao = ctx.getBean(UsersDaoImpl.class);
					for (Users u : udao.getAllUsersDeleted()) {
						System.out.println("id == " + u.getUser_id() + "; deleted ? " + u.getDeleted() + "; uri -> " + u.getPictureuri());
					}
					
					*/
					//Upload backup ???
					
					//Upload import ???
					
					//public/private files
					//Object: fileexploreritem (filehash == document file/folder)
					//webapps/openmeetings/upload/files (check if ends with filename)
					
					//public/private recordings
					//Object: flvrecording
					//webapps/openmeetings/streams/<room_id>/rec_<id>*				-->temporary files
					//webapps/openmeetings/streams/hibernate/flvRecording_<id>*		-->files
				} catch (Exception e) {
					handleError("Files failed", e);
				}
				break;
			case usage:
			default:
				usage();
				break;
		}
		
		System.out.println("... Done");
		System.exit(0);
	}
	
	private class AdminUserDetails {
		String login = null;
		String email = null;
		String group = null;
		String pass = null;
		String tz = null;
	}
	
	private AdminUserDetails checkAdminDetails(String ctxName, String langPath) throws Exception {
		AdminUserDetails admin = new AdminUserDetails();
		admin.login = cmdl.getOptionValue("user");
		admin.email = cmdl.getOptionValue("email");
		admin.group = cmdl.getOptionValue("group");
		if (admin.login == null || admin.login.length() < InstallationConfig.USER_LOGIN_MINIMUM_LENGTH) {
			System.out.println("User login was not provided, or too short, should be at least " + InstallationConfig.USER_LOGIN_MINIMUM_LENGTH + " character long.");
			System.exit(1);
		}
		
		try {
			if (!MailUtil.matches(admin.email)) {
			    throw new AddressException("Invalid address");
			}
			new InternetAddress(admin.email, true);
		} catch (AddressException ae) {
			System.out.println("Please provide non-empty valid email: '" + admin.email + "' is not valid.");
			System.exit(1);
		}
		if (admin.group == null || admin.group.length() < 1) {
			System.out.println("User group was not provided, or too short, should be at least 1 character long: " + admin.group);
			System.exit(1);
		}
		admin.pass = cmdl.getOptionValue("password");
		if (checkPassword(admin.pass)) {
			System.out.print("Please enter password for the user '" + admin.login + "':");
			admin.pass = new BufferedReader(new InputStreamReader(System.in)).readLine();
			if (checkPassword(admin.pass)) {
				System.out.println("Password was not provided, or too short, should be at least " + InstallationConfig.USER_PASSWORD_MINIMUM_LENGTH + " character long.");
				System.exit(1);
			}
		}
		ImportInitvalues importInit = getApplicationContext(ctxName).getBean(ImportInitvalues.class);
		Map<String, String> tzMap = ImportHelper.getAllTimeZones(importInit.getTimeZones(langPath));
		admin.tz = null;
		if (cmdl.hasOption("tz")) {
			admin.tz = cmdl.getOptionValue("tz");
			admin.tz = tzMap.containsKey(admin.tz) ? admin.tz : null;
		}
		if (admin.tz == null) {
			System.out.println("Please enter timezone, Possible timezones are:");
			
			for (String tzIcal : tzMap.keySet()) {
				System.out.println(String.format("%1$-25s%2$s", "\"" + tzIcal + "\"", tzMap.get(tzIcal)));
			}
			System.exit(1);
		}
		return admin;
	}
	
	private boolean checkPassword(String pass) {
		return (pass == null || pass.length() < InstallationConfig.USER_PASSWORD_MINIMUM_LENGTH);
	}
	
	private void shutdownScheduledJobs(String ctxName) throws SchedulerException {
		SchedulerFactoryBean sfb =  getApplicationContext(ctxName).getBean(SchedulerFactoryBean.class);
		sfb.getScheduler().shutdown(false);
	}
	
	private void dropDB(ConnectionProperties props) throws Exception {
		if(cmdl.hasOption("drop")) {	
			String[] args = {
					"-schemaAction", "retain,drop"
					, "-properties", omHome.getAbsolutePath() + "/WEB-INF/classes/META-INF/persistence.xml"
					, "-connectionDriverName", props.getDriver()
					, "-connectionURL", props.getURL()
					, "-connectionUserName", props.getLogin()
					, "-connectionPassword", props.getPassword()
					, "-ignoreErrors", "true"};
			MappingTool.main(args);
		}
	}
	
	private File checkRestoreFile(String file) {
		File backup = new File(file);
		if (!cmdl.hasOption("file") || !backup.exists() || !backup.isFile()) {
			System.out.println("File should be specified, and point the existent zip file");
			usage();
			System.exit(1);
		}
		
		return backup;
	}
	
	private void restoreOm(String ctxName, File backup) {
		try {
			BackupImportController importCtrl = getApplicationContext(ctxName).getBean(BackupImportController.class);
			importCtrl.performImport(new FileInputStream(backup), omHome.getAbsolutePath());
		} catch (Exception e) {
			handleError("Restore failed", e);
		}
	}
	
	public static void main(String[] args) {
		new Admin().process(args);
	}
}
