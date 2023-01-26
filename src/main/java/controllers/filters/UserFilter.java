package controllers.filters;

import com.google.inject.Inject;

import controllers.util.AuthenticationHelper;
import ninja.Context;

public class UserFilter extends AbstractAuthenticationFilter {

    @Inject
    private AuthenticationHelper authenticationHelper;

    @Override
    protected boolean authorize(Context context) {
        return authenticationHelper.validUser(context);
    }
}
