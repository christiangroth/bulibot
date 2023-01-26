package controllers;

import javax.inject.Singleton;

import com.google.inject.Inject;

import conf.Routes;
import controllers.filters.AdminFilter;
import controllers.filters.UserFilter;
import controllers.util.AuthenticationHelper;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.metrics.Timed;
import services.InfoService;

@Singleton
public class TemplateController {

    @Inject
    private InfoService infoService;

    @Inject
    private AuthenticationHelper authenticationHelper;

    @Timed
    public Result login(Context context) {
        if (authenticationHelper.validUser(context)) {
            return Results.redirect(Routes.PATH_INTERNAL_MAIN);
        }

        return render(context, Results.html());
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result rankings(Context context) {
        return render(context, Results.html());
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result editor(Context context) {
        return render(context, Results.html());
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result matches(Context context) {
        return render(context, Results.html());
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result profile(Context context) {
        return render(context, Results.html());
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result build(Context context) {
        return render(context, Results.html());
    }

    @Timed
    @FilterWith({ UserFilter.class })
    public Result releasenotes(Context context) {
        return render(context, Results.html());
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result users(Context context) {
        return render(context, Results.html());
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result pendingBulibotExecutions(Context context) {
        return render(context, Results.html());
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result smartcrons(Context context) {
        return render(context, Results.html());
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result persistence(Context context) {
        return render(context, Results.html());
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result api(Context context) {
        return render(context, Results.html());
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result killswitch(Context context) {
        return render(context, Results.html());
    }

    @Timed
    @FilterWith({ AdminFilter.class })
    public Result config(Context context) {
        return render(context, Results.html());
    }

    private Result render(Context context, Result result) {

        // add build info
        result.render("buildVersion", infoService.getBuildVersion());

        // add current user
        result.render("activeUser", authenticationHelper.activeUser(context));

        // done
        return result;
    }
}
