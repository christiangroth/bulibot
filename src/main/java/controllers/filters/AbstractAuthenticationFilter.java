package controllers.filters;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import de.svenkubiak.ninja.auth.enums.Constants;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.NinjaDefault;
import ninja.Result;
import ninja.Results;
import ninja.utils.NinjaProperties;

public abstract class AbstractAuthenticationFilter implements Filter {

    @Inject
    private NinjaProperties ninjaProperties;

    @Inject
    private NinjaDefault ninjaDefault;

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        if (!authorize(context)) {
            String redirect = ninjaProperties.get(Constants.AUTH_REDIRECT_URL.get());
            return StringUtils.isBlank(redirect) ? ninjaDefault.getUnauthorizedResult(context) : Results.redirect(redirect);
        }

        return filterChain.next(context);
    }

    protected abstract boolean authorize(Context context);
}
