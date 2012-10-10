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
package org.openmeetings.app.data.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.openmeetings.app.OpenmeetingsVariables;
import org.openmeetings.app.data.file.dao.FileExplorerItemDaoImpl;
import org.openmeetings.app.persistence.beans.files.FileExplorerItem;
import org.openmeetings.app.remote.red5.ScopeApplicationAdapter;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class FileUtils {

	private static final Logger log = Red5LoggerFactory.getLogger(
			FileProcessor.class, OpenmeetingsVariables.webAppRootKey);

	@Autowired
	private FileExplorerItemDaoImpl fileExplorerItemDao;

	public long getSizeOfDirectoryAndSubs(FileExplorerItem fileExplorerItem) {
		try {

			long fileSize = 0;

			File base = new File(new File(ScopeApplicationAdapter.webAppPath, OpenmeetingsVariables.UPLOAD_DIR), "files");
			if (fileExplorerItem.getIsImage()) {

				File tFile = new File(base, fileExplorerItem.getFileHash());
				if (tFile.exists()) {
					fileSize += tFile.length();
				}

				File thumbFile = new File(base, "_thumb_"
						+ fileExplorerItem.getFileHash());
				if (thumbFile.exists()) {
					fileSize += thumbFile.length();
				}

			}

			if (fileExplorerItem.getIsPresentation()) {

				File tFolder = new File(base, fileExplorerItem.getFileHash());

				if (tFolder.exists()) {
					fileSize += getSize(tFolder);
				}

			}

			log.debug("calling [1] FileExplorerItemDaoImpl.updateFileOrFolder()");
			fileExplorerItemDao.updateFileOrFolder(fileExplorerItem);

			FileExplorerItem[] childElements = fileExplorerItemDao
					.getFileExplorerItemsByParent(fileExplorerItem
							.getFileExplorerItemId());

			for (FileExplorerItem childExplorerItem : childElements) {

				fileSize += this.getSizeOfDirectoryAndSubs(childExplorerItem);

			}

			return fileSize;

		} catch (Exception err) {
			log.error("[getSizeOfDirectoryAndSubs] ", err);
		}
		return 0;
	}

	public static String getHumanSize(File dir) {
		long size = getSize(dir);

		if(size <= 0) return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	public static long getSize(File dir) {
		long size = 0;
		if (dir.isFile()) {
			size = dir.length();
		} else {
			File[] subFiles = dir.listFiles();

			for (File file : subFiles) {
				if (file.isFile()) {
					size += file.length();
				} else {
					size += getSize(file);
				}

			}
		}
		return size;
	}

	public void setFileToOwnerOrRoomByParent(FileExplorerItem fileExplorerItem,
			Long users_id, Long room_id) {
		try {

			fileExplorerItem.setOwnerId(users_id);
			fileExplorerItem.setRoom_id(room_id);

			log.debug("calling [2] FileExplorerItemDaoImpl.updateFileOrFolder()");
			fileExplorerItemDao.updateFileOrFolder(fileExplorerItem);

			FileExplorerItem[] childElements = fileExplorerItemDao
					.getFileExplorerItemsByParent(fileExplorerItem
							.getFileExplorerItemId());

			for (FileExplorerItem childExplorerItem : childElements) {

				this.setFileToOwnerOrRoomByParent(childExplorerItem, users_id,
						room_id);

			}

		} catch (Exception err) {
			log.error("[setFileToOwnerOrRoomByParent] ", err);
		}
	}

	public String formatDate(Date date) {
		SimpleDateFormat formatter;
		String pattern = "dd/MM/yy H:mm:ss";
		Locale locale = new Locale("en", "US");
		formatter = new SimpleDateFormat(pattern, locale);
		return formatter.format(date);
	}

	public void copyFile(String sourceFile, String targetFile) {
		try {
			File f1 = new File(sourceFile);
			File f2 = new File(targetFile);
			InputStream in = new FileInputStream(f1);

			// For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			System.out.println("File copied.");
		} catch (Exception e) {
			log.error("[copyfile]", e);
		}
	}
}
