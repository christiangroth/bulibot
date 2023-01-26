package services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import configuration.NotificationEventConfig;
import configuration.SystemConfig;
import model.user.User;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.postoffice.Mail;
import ninja.postoffice.Postoffice;
import ninja.utils.NinjaProperties;
import services.exception.MailException;

// TODO library for html email templates
@Singleton
public class MailService {
    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

    private static final String CHARSET_UTF_8 = "utf-8";

    @Inject
    private Provider<Mail> mailProvider;

    @Inject
    private Postoffice postoffice;

    @Inject
    private NinjaProperties ninjaProperties;

    @Inject
    private NotificationService notificationService;

    private String from;

    @Start
    public void startup() {
        LOG.info("configuring mail service...");
        from = ninjaProperties.getOrDie(SystemConfig.SMTP_USER.getKey());
        LOG.info("started mail service.");
    }

    public void sendVerificationMail(User user) throws MailException {

        // create mail
        LOG.info("generating verification mail for user " + user.getEmail());
        Mail mail = createMail(user.getEmail(), "Bulibot - Verifiziere deinen Account");

        // set content
        String text = "Bestätige bitte deine E-Mail Adresse: ";
        String link = getHost() + "/verify/" + user.getVerificationPhrase();
        mail.setBodyText(text + link);
        mail.setBodyHtml(text + "<a href=\"" + link + "\">" + link + "</a>");

        // send mail
        send(mail);
    }

    public void sendPasswordReset(User user, String password) throws MailException {

        // create mail
        LOG.info("generating reset password mail for user " + user.getEmail());
        Mail mail = createMail(user.getEmail(), "Bulibot - Passwort zurückgesetzt");

        // set content
        String text = "Dein Passwort wurde zurückgesetzt, bitte ändere es nach dem nächsten Login. Dein neues Passwort ist: " + password;
        mail.setBodyText(text);
        mail.setBodyHtml(text);

        // send mail
        send(mail);
    }

    private Mail createMail(String to, String subject) {

        // create mail
        Mail mail = mailProvider.get();

        // header information
        mail.setFrom(from);
        mail.addTo(to);
        mail.setSubject(subject);

        // content
        mail.setCharset(CHARSET_UTF_8);

        // done
        return mail;
    }

    private void send(Mail mail) throws MailException {
        try {
            postoffice.send(mail);
        } catch (Exception e) {
            LOG.error("unable to send mail: " + e.getMessage(), e);
            notificationService.send(NotificationEventConfig.MAIL_FAILURE, e.getClass().getName(), e.getMessage());
        }
    }

    private String getHost() {
        return ninjaProperties.getOrDie(SystemConfig.HOST.getKey());
    }

    @Dispose
    public void shutdown() {
        LOG.info("stopped mail service.");
    }
}
