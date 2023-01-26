package controllers.util;

import org.apache.commons.lang.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.svenkubiak.ninja.auth.services.Authentications;
import model.user.User;
import ninja.Context;
import services.DataService;

@Singleton
public class AuthenticationHelper {

    @Inject
    private Authentications authentications;

    @Inject
    private DataService dataService;

    public boolean validUser(Context context) {
        User user = activeUser(context);
        return user != null && !user.isLocked() && user.isVerified();
    }

    public boolean validAdmin(Context context) {
        User user = activeUser(context);
        return user != null && !user.isLocked() && user.isVerified() && user.isAdmin();
    }

    public User activeUser(Context context) {
        String authenticatedUserId = authentications.getAuthenticatedUser(context);
        return StringUtils.isBlank(authenticatedUserId) ? null : dataService.userById(Long.valueOf(authenticatedUserId));
    }
}
