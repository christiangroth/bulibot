package services;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import configuration.AuthConfig;
import configuration.BulibotConfig;
import configuration.NotificationEventConfig;
import de.svenkubiak.ninja.auth.services.Authentications;
import model.user.BulibotVersion;
import model.user.User;
import ninja.Context;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.utils.NinjaProperties;
import services.exception.MailException;

@Singleton
public class AuthenticationService {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    @Inject
    private MailService mailService;

    @Inject
    private BulibotService bulibotService;

    @Inject
    private DataService dataService;

    @Inject
    private Authentications authentications;

    @Inject
    private NotificationService notificationService;

    @Inject
    private NinjaProperties ninjaProperties;

    private List<String> verifiedAdminEmails;
    private String defaultVersionName;
    private String defaultSource;

    @Start
    public void startup() {
        LOG.info("configuring authentication service...");
        verifiedAdminEmails = Arrays.asList(ninjaProperties.getStringArray(AuthConfig.ADMIN_VERIFIED_EMAIL.getKey()));
        defaultVersionName = ninjaProperties.getOrDie(BulibotConfig.BULIBOT_DEFAULT_VERSION_NAME.getKey());
        defaultSource = ninjaProperties.getOrDie(BulibotConfig.BULIBOT_DEFAULT_SOURCE.getKey());
        LOG.info("started authentication service.");
    }

    public void createUser(String email, String name, String password) throws MailException {

        // create user and verification phrase
        User user = new User();
        user.setId(dataService.nextUserId());
        user.setName(name);
        user.setEmail(email);
        if (password != null) {
            user.setPassword(hashPassword(password));
        }
        user.setSince(LocalDateTime.now());
        user.setVerified(false);
        user.setAdmin(verifiedAdminEmails.contains(email));
        user.setGenerated(false);

        // start verification process
        startVerification(user);

        // notify
        notificationService.send(NotificationEventConfig.USER_CREATED, email);
    }

    public void startVerification(User user) throws MailException {

        // set verification hash
        user.setVerified(false);
        user.setVerificationPhrase(UUID.randomUUID().toString());
        dataService.user(user);

        // send reset mail
        mailService.sendVerificationMail(user);
    }

    public void finishVerification(User user) throws MailException {

        // set verified
        user.setVerified(true);
        user.setVerifiedSince(LocalDateTime.now());

        // set random buibot name
        Random r = new Random();
        user.setBulibotName("" + (char) (r.nextInt(26) + 'A') + (char) (r.nextInt(26) + 'A') + (r.nextInt(29) + 1));

        // prepare default bulibot version
        BulibotVersion version = new BulibotVersion();
        version.setName(defaultVersionName);
        version.setSource(defaultSource);
        version.setLive(true);
        user.getBulibotVersions().add(version);

        // save
        LOG.info("user " + user.getEmail() + " verified, default data created.");
        dataService.user(user);

        // trigger rankings recalculation
        bulibotService.updateRankings();

        // reset password if not set - this happens for invites
        if (StringUtils.isBlank(user.getPassword())) {
            resetPassword(user);
        }

        // notify
        notificationService.send(NotificationEventConfig.USER_VERIFIED, user.getEmail());
    }

    public void resetPassword(User user) throws MailException {

        // set password hash
        String password = UUID.randomUUID().toString();
        user.setPassword(hashPassword(password));
        dataService.user(user);

        // send reset mail
        mailService.sendPasswordReset(user, password);
    }

    private String hashPassword(String password) {
        return authentications.getHashedPassword(password);
    }

    public boolean authenticate(User user, String password) {
        return user != null && !user.isGenerated() && authentications.authenticate(password, user.getPassword());
    }

    public void login(Context context, User user) {
        authentications.login(context, "" + user.getId(), true);
    }

    public void changePassword(User user, String password) {
        user.setPassword(authentications.getHashedPassword(password));
        dataService.user(user);
    }

    public void updateFlags(User user, boolean locked, boolean admin) {
        user.setLocked(locked);
        user.setAdmin(admin);
        dataService.user(user);
    }

    public void logout(Context context) {
        authentications.logout(context);
    }

    @Dispose
    public void shutdown() {
        LOG.info("stopped authentication service.");
    }
}
