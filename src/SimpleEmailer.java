import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.simpleemail.AWSJavaMailTransport;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.DeleteIdentityRequest;
import com.amazonaws.services.simpleemail.model.ListVerifiedEmailAddressesResult;
import com.amazonaws.services.simpleemail.model.VerifyEmailAddressRequest;
public class SimpleEmailer {
	public static void main(String[] args)
	{
        AWSCredentials credentials = new ClasspathPropertiesFileCredentialsProvider().getCredentials();
		AmazonSimpleEmailService ses = new AmazonSimpleEmailServiceClient(credentials);
		String to = "";
		String input="";
		Scanner vScan=new Scanner(System.in);
		System.out.println("Welcome to a simple email service using amazon.");
		System.out.println("Please enter a command.");
		while(true)
		{
			input=vScan.nextLine();
			if(input.equalsIgnoreCase("verify"))
			{
				System.out.println("Enter the email to be verified.");
				String verEmail=vScan.nextLine();
				ListVerifiedEmailAddressesResult verifiedEmails = ses.listVerifiedEmailAddresses();
				if (verifiedEmails.getVerifiedEmailAddresses().contains(verEmail)) 
				{
					//email is already verified.
					System.out.println("Email is already a verified sender!");
				}
				else
				{
				ses.verifyEmailAddress(new VerifyEmailAddressRequest().withEmailAddress(verEmail));
				System.out.println("Please check the email address " + verEmail + " to verify it");
				}
			}
			if(input.equalsIgnoreCase("unverify"))
			{
				System.out.println("Enter the email address to be unverified.");
				String unVer=vScan.nextLine();
				ListVerifiedEmailAddressesResult verifiedEmails = ses.listVerifiedEmailAddresses();
				if (verifiedEmails.getVerifiedEmailAddresses().contains(unVer)) 
				{
					ses.deleteIdentity(new DeleteIdentityRequest().withIdentity(unVer));
					System.out.println("Verified email " + unVer + "removed from verified senders.");
				}
				else
				{
					System.out.println("The given email address was not a verified email, so it could not be deleted.");
				}
				
			}
			if(input.equalsIgnoreCase("send"))
			{
				/*
				 * Setting mail.aws.user and mail.aws.password are optional. Setting
				 * these will allow you to send mail using the static transport send()
				 * convince method.  It will also allow you to call connect() with no
				 * parameters. Otherwise, a user name and password must be specified
				 * in connect.
				 */
				Properties props = new Properties();
				props.setProperty("mail.transport.protocol", "aws");
				props.setProperty("mail.aws.user", credentials.getAWSAccessKeyId());
				props.setProperty("mail.aws.password", credentials.getAWSSecretKey());
				Session session = Session.getInstance(props);
				try {
					// Create a new Message
					System.out.println("Enter the destination email address.");
					String TO=vScan.nextLine();
					System.out.println("Enter the email address you are sending from (Verified addresses only)");
					String FROM=vScan.nextLine();
					ListVerifiedEmailAddressesResult verifiedEmails = ses.listVerifiedEmailAddresses();
					if (!verifiedEmails.getVerifiedEmailAddresses().contains(FROM)) 
					{
						//email is not verified.
						System.out.println("Email is not a verified sender!");
					}
					else
					{
						Message msg = new MimeMessage(session);
						msg.setFrom(new InternetAddress(FROM));
						msg.addRecipient(Message.RecipientType.TO, new InternetAddress(TO));
						System.out.print("Do you want to add any CCs or BCCs? (y/n)");
						String response=vScan.nextLine();
						if(response.equalsIgnoreCase("y"))
						{
							while(true)
							{
								System.out.print("Add a CC (or type \"end\" to finish adding CCs: ");
								response=vScan.nextLine();
								if(response.equalsIgnoreCase("end"))
								{
									break;
								}
								else
								{
									msg.addRecipient(Message.RecipientType.CC, new InternetAddress(response));
								}
							}
							while(true)
							{
								System.out.println("Add a BCC (or type \"end\" to finish adding BCCs: ");
								response=vScan.nextLine();
								if(response.equalsIgnoreCase("end"))
								{
									break;
								}
								else
								{
									msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(response));
								}
							}
							
						}
							
						System.out.println("Enter the subject line of the message:");
						String SUBJECT=vScan.nextLine();
						msg.setSubject(SUBJECT);
						System.out.println("Enter the subject body of the email.");
						String BODY=vScan.nextLine();
						msg.setText(BODY);
						msg.saveChanges();

						// Reuse one Transport object for sending all your messages
						// for better performance
						Transport t = new AWSJavaMailTransport(session, null);
						t.connect();
						t.sendMessage(msg, null);

						// Close your transport when you're completely done sending
						// all your messages
						t.close();
						System.out.println("Email sent.");
					}
				} catch (AddressException e) {
					e.printStackTrace();
					System.out.println("Caught an AddressException, which means one or more of your "
							+ "addresses are improperly formatted.");
				} catch (MessagingException e) {
					e.printStackTrace();
					System.out.println("Caught a MessagingException, which means that there was a "
							+ "problem sending your message to Amazon's E-mail Service check the "
							+ "stack trace for more information.");
				}
			}
			if(input.equalsIgnoreCase("getSendQuota"))
			{
				System.out.println("Your current send quota is:");
				System.out.println(ses.getSendQuota());
			}
			if(input.equalsIgnoreCase("getStats"))
			{
				System.out.println("The current sending statistics are:");
				System.out.println(ses.getSendStatistics());
			}
			if(input.equalsIgnoreCase("listVerified"))
			{
				System.out.println(ses.listVerifiedEmailAddresses());
			}
			if(input.equalsIgnoreCase("exit"))
			{
				break;
			}
		}
	}
}
