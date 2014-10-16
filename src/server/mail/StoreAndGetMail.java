package server.mail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.fleety.util.pool.timer.FleetyTimerTask;
import com.fleety.util.pool.timer.TimerPool;

import server.threadgroup.ThreadPoolGroupServer;

public class StoreAndGetMail {
	private TimerPool timer = null;
	private long loginMaxDelay = 15 * 1000;

	public long polling_interval = 10 * 60 * 1000L;
	public int interval_days = 2;
	public boolean is_delete_history_mail = true;
	public int save_mail_days = 20;
	public String mail_save_path = "mailStore";
	
	public static SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat HHmmssSSS = new SimpleDateFormat("HH-mm-ss-SSS");

	public  MailServer mailServer= null;

	public void start() {
		//邮件发送服务，必然作为守护线程的存在，因为必然由其它模块进行邮件的投送。
		this.timer = ThreadPoolGroupServer.getSingleInstance().createTimerPool(StoreAndGetMail.class.getName()+"["+this.hashCode()+"]");
		// 检测之前发送未成功的
		SendAllFailStroeMailTask beat = new SendAllFailStroeMailTask();
		this.timer.schedule(beat, loginMaxDelay, polling_interval);
	}
	
	public void stop(){
		if(this.timer != null){
			this.timer.cancel();
		}
	}

	private String initDir() {
		File tempFile = new File(mail_save_path);
		try {
			if (!tempFile.exists()) {
				tempFile.mkdir();
			}
		} catch (Exception e) {
			System.out.println("新建基础目录操作出错");
			e.printStackTrace();
		}

		String monthPath = mail_save_path + File.separator
				+ YYYY_MM_DD.format(new Date()).substring(0, 7);

		tempFile = new File(monthPath);
		try {
			if (!tempFile.exists()) {
				tempFile.mkdir();
			}
		} catch (Exception e) {
			System.out.println("新建月份目录操作出错");
			e.printStackTrace();
		}
		String dayPath = monthPath + File.separator
				+ YYYY_MM_DD.format(new Date()).substring(8);

		tempFile = new File(dayPath);
		try {
			if (!tempFile.exists()) {
				tempFile.mkdir();
			}
		} catch (Exception e) {
			System.out.println("新建日目录操作出错");
			e.printStackTrace();
		}
		return dayPath;
	}

	/**
	 * 保存邮件
	 */
	public synchronized void storeMail(MailObj obj) {
		try {
			String dayPath = initDir();
			String dirPath = dayPath + File.separator + HHmmssSSS.format(new Date());

			File myFolderPath = new File(dirPath);
			try {
				if (!myFolderPath.exists()) {
					myFolderPath.mkdir();
				} else {
					dirPath = dayPath + File.separator + HHmmssSSS.format(new Date());
					myFolderPath = new File(dirPath);
					if (!myFolderPath.exists()) {
						myFolderPath.mkdir();
					}
				}
			} catch (Exception e) {
				System.out.println("新建目录操作出错");
				e.printStackTrace();
			}
			// 保存文件内容
			File headFile = new File(dirPath + File.separator + "head");
			BufferedWriter out = new BufferedWriter(new FileWriter(headFile));
			out.write(obj.getHead());// 
			out.flush();
			out.close();
			File contentFile = new File(dirPath + File.separator + "content");
			out = new BufferedWriter(new FileWriter(contentFile));
			if (obj.getContent() != null)
				out.write((String) obj.getContent());// 
			out.flush();
			out.close();
			// 保存收件人
			for (int i = 0; i < obj.getReceiverArr().length; i++) {
				File emailFile = new File(dirPath + File.separator
						+ obj.getReceiverArr()[i] + ";" + obj.getState());
				FileWriter write = new FileWriter(emailFile);
				write.close();
			}
			// 保存附件
			if (obj.getFile() != null) {
				File appendPath = new File(myFolderPath.getAbsolutePath()
						+ File.separator + "append");
				List appendFile = new ArrayList();
				appendPath.mkdir();
				for (int i = 0; i < obj.getFile().length; i++) {
					File temp = obj.getFile()[i];
					if (!temp.exists())
						continue;
					FileInputStream input = new FileInputStream(temp);
					String newPath = appendPath.getAbsolutePath()
							+ File.separator + (temp.getName()).toString();
					FileOutputStream output = new FileOutputStream(newPath);
					byte[] b = new byte[5120];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
					appendFile.add(new File(newPath));
				}
				File[] file = new File[appendFile.size()];
				for (int i = 0; i < appendFile.size(); i++) {
					file[i] = (File) appendFile.get(i);
				}
				obj.setFile(file);
			}
			obj.setStorePath(myFolderPath.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void storeMailState(MailObj obj) {
		File file = new File(obj.getStorePath());
		for (int i = 0; i < obj.getReceiverArr().length; i++) {
			final String thisEmail = obj.getReceiverArr()[i];
			FileFilter filter = new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.getName().startsWith(thisEmail);
				}
			};

			File[] fs = file.listFiles(filter);
			for (int j = 0; j < fs.length; j++) {
				File oldFile = fs[j];
				File newFile = new File(oldFile.getAbsolutePath().substring(0,
						oldFile.getAbsolutePath().length() - 1)
						+ obj.getState());
				oldFile.renameTo(newFile);
			}
		}
	}

	public synchronized void deleteStoreMail(MailObj obj) {
		delFolder(obj.getStorePath());
	}

	public void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); // 删除空文件夹
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 删除指定文件夹下所有文件
	// param path 文件夹完整绝对路径

	public boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}

	public void getMailAndSend(File myFolderPath) {
		try {
			String headTag = "head";
			String contentTag = "content";
			File[] fileArr = myFolderPath.listFiles();
			File contentFile = null;
			File headFile = null;
			File[] appendList = null;
			int thisMailState = 0;
			List emailList = new ArrayList();
			List stateList = new ArrayList();
			if(fileArr==null)
			{
				System.err.println("文件夹内无内容:"+myFolderPath.getAbsolutePath());
				myFolderPath.delete();
				return;
			}
			for (int i = 0; i < fileArr.length; i++) {
				if (fileArr[i].isFile() && fileArr[i].getName().equals(headTag)) {
					headFile = fileArr[i];
				} else if (fileArr[i].isFile()
						&& fileArr[i].getName().equals(contentTag)) {
					contentFile = fileArr[i];
				} else if (fileArr[i].isDirectory()) {
					appendList = fileArr[i].listFiles();
				} else if (fileArr[i].isFile()) {
					String emailStr = fileArr[i].getName();
					String email = emailStr.substring(0, emailStr.length() - 2);
					thisMailState = Integer.parseInt(emailStr
							.substring(emailStr.length() - 1));
					if (thisMailState != MailObj.STATE_INIT
							&& thisMailState != MailObj.STATE_FAIL) {
						continue;
					}
					emailList.add(email);
					stateList.add(new Integer(thisMailState));
				}
			}
			if (emailList.size() == 0)
				return;
			StringBuffer content = new StringBuffer("");
			if (contentFile != null) {
				FileInputStream io = new FileInputStream(contentFile);
				int avail = io.available();
				while (avail > 0) {
					byte[] b1 = new byte[avail];
					io.read(b1, 0, b1.length);
					content.append(new String(b1));
					avail = io.available();
				}
				io.close();
			}
			StringBuffer head = new StringBuffer("");
			if (headFile != null) {
				FileInputStream io = new FileInputStream(headFile);
				int avail = io.available();
				while (avail > 0) {
					byte[] b1 = new byte[avail];
					io.read(b1, 0, b1.length);
					head.append(new String(b1));
					avail = io.available();
				}
				io.close();
			}
			if (mailServer.is_Bulk == true) {
				String[] receiveArr = new String[emailList.size()];
				for (int i = 0; i < emailList.size(); i++) {
					receiveArr[i] = (String) emailList.get(i);
				}
				MailObj obj = new MailObj(receiveArr, head.toString(), content,
						appendList);
				obj.setState(((Integer) stateList.get(0)).intValue());
				obj.setStorePath(myFolderPath.getAbsolutePath());
				mailServer.addMail(obj);
			} else {
				for (int i = 0; i < emailList.size(); i++) {
					MailObj obj = new MailObj(new String[] { (String) emailList
							.get(i) }, head.toString(), content, appendList);
					obj.setState(((Integer) stateList.get(i)).intValue());
					obj.setStorePath(myFolderPath.getAbsolutePath());
					mailServer.addMail(obj);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendAllStroe(int state) {
		try {
			initDir();
			File fDir = new File(mail_save_path);

			if (is_delete_history_mail == true) {
				// 删除过期的月份文件
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_YEAR, 0 - save_mail_days + 1);
				final String saveMonth = YYYY_MM_DD.format(cal.getTime())
						.substring(0, 7);
				System.out.println("saveMonth::" + saveMonth);
				FileFilter filter = new FileFilter() {
					public boolean accept(File pathname) {
						return (pathname.getName().compareTo(saveMonth) < 0);
					}
				};

				File[] fs = fDir.listFiles(filter);
				for (int i = 0; i < fs.length; i++) {
					System.out.println("fs[i].getAbsolutePath()::"
							+ fs[i].getAbsolutePath());
					delFolder(fs[i].getAbsolutePath());
				}

				File monthFile = new File(mail_save_path + File.separator
						+ saveMonth);

				// 如果临界月份文件存在，则删除过期的日期文件夹
				if (monthFile.exists()) {
					final String thisMonthDate = YYYY_MM_DD.format(
							cal.getTime()).substring(8);
					System.out.println("thisMonthDate::" + thisMonthDate);
					FileFilter filterDate = new FileFilter() {
						public boolean accept(File pathname) {
							return (pathname.getName().compareTo(thisMonthDate) < 0);
						}
					};

					File[] dayFiles = monthFile.listFiles(filterDate);
					for (int i = 0; i < dayFiles.length; i++) {
						delFolder(dayFiles[i].getAbsolutePath());
					}
				}
			}

			// 重新发送未过期的邮件，state=0表示发送未发送的，state=3表示发送以前发送失败的
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_YEAR, 0 - interval_days + 1);

			final String saveMonth = YYYY_MM_DD.format(cal.getTime())
					.substring(0, 7);
			System.out.println("saveMonth::" + saveMonth);
			FileFilter filter = new FileFilter() {
				public boolean accept(File pathname) {
					return (pathname.getName().compareTo(saveMonth) > 0);
				}
			};

			File[] fs = fDir.listFiles(filter);
			for (int i = 0; i < fs.length; i++) {
				// 月列表
				File[] fs2 = fs[i].listFiles();
				for (int j = 0; j < fs2.length; j++) {
					// 日列表
					File[] fs3 = fs2[j].listFiles();
					for (int k = 0; k < fs3.length; k++) {
						// 邮件文件夹列表
						getMailAndSend(fs3[k]);
					}

				}
			}

			File monthFile = new File(mail_save_path + File.separator
					+ saveMonth);
			// begin

			if (monthFile.exists()) {
				final String thisMonthDate = YYYY_MM_DD.format(cal.getTime())
						.substring(8);
				System.out.println("thisMonthDate::" + thisMonthDate);
				FileFilter filterDate = new FileFilter() {
					public boolean accept(File pathname) {
						return (pathname.getName().compareTo(thisMonthDate) >= 0);
					}
				};
				File[] dayFiles = monthFile.listFiles(filterDate);
				for (int j = 0; j < dayFiles.length; j++) {

					File[] emailFiles = dayFiles[j].listFiles();
					for (int k = 0; k < emailFiles.length; k++) {
						getMailAndSend(emailFiles[k]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// end
	}

	private class SendAllFailStroeMailTask extends FleetyTimerTask {
		public void run() {
			try {
				mailServer.reSendAllFailMail();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
