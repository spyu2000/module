/**
 * ¾øÃÜ Created on 2010-5-21 by edmund
 */
package server.mail;

import javax.mail.Message;

public interface IMailListener{
	public void mailReceived(Message mail) throws Exception;
}
