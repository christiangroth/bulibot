/**
 * Copyright (C) 2012-2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the License.
 */

package controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import configuration.DataConfig;
import ninja.Context;
import ninja.Renderable;
import ninja.Result;
import ninja.Results;
import ninja.metrics.Timed;
import ninja.utils.HttpCacheToolkit;
import ninja.utils.MimeTypes;
import ninja.utils.NinjaProperties;
import ninja.utils.ResponseStreams;

@Singleton
public class TeamImageAssetsController {

    private final static Logger logger = LoggerFactory.getLogger(TeamImageAssetsController.class);

    public final static String FILENAME_PATH_PARAM = "fileName";

    private final MimeTypes mimeTypes;
    private final HttpCacheToolkit httpCacheToolkit;
    private final File media;

    @Inject
    public TeamImageAssetsController(HttpCacheToolkit httpCacheToolkit, MimeTypes mimeTypes, NinjaProperties ninjaProperties) {
        this.httpCacheToolkit = httpCacheToolkit;
        this.mimeTypes = mimeTypes;
        media = new File(ninjaProperties.getOrDie(DataConfig.MEDIA_DIR.getKey()));
    }

    @Timed
    public Result serveStatic() {
        Object renderable = new Renderable() {
            @Override
            public void render(Context context, Result result) {
                String fileName = getFileNameFromPathOrReturnRequestPath(context);
                URL url = getStaticFileFromAssetsDir(fileName);
                streamOutUrlEntity(url, context, result);
            }
        };
        return Results.ok().render(renderable);
    }

    private void streamOutUrlEntity(URL url, Context context, Result result) {

        // check if stream exists. if not print a notfound exception
        if (url == null) {
            context.finalizeHeadersWithoutFlashAndSessionCookie(Results.notFound());
        } else {

            // CHANGED moved before try
            URLConnection urlConnection = null;
            try {
                urlConnection = url.openConnection();
                Long lastModified = urlConnection.getLastModified();
                httpCacheToolkit.addEtag(context, result, lastModified);

                if (result.getStatusCode() == Result.SC_304_NOT_MODIFIED) {
                    // Do not stream anything out. Simply return 304
                    context.finalizeHeadersWithoutFlashAndSessionCookie(result);
                } else {
                    result.status(200);

                    // Try to set the mimetype:
                    String mimeType = mimeTypes.getContentType(context, url.getFile());

                    if (mimeType != null && !mimeType.isEmpty()) {
                        result.contentType(mimeType);
                    }

                    ResponseStreams responseStreams = context.finalizeHeadersWithoutFlashAndSessionCookie(result);
                    try (InputStream inputStream = urlConnection.getInputStream(); OutputStream outputStream = responseStreams.getOutputStream()) {
                        ByteStreams.copy(inputStream, outputStream);
                    }
                }
            } catch (IOException e) {
                logger.error("error streaming file", e);
            } finally {

                // CHANGED added finally to be able to close input stream
                if (urlConnection != null) {
                    try {
                        urlConnection.getInputStream().close();
                    } catch (IOException e) {
                        logger.info("ignoring exception on stream close: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Loads files from media directory.
     */
    private URL getStaticFileFromAssetsDir(String fileName) {
        try {
            return new File(media, fileName).toURI().toURL();
        } catch (MalformedURLException e) {
            logger.error("unable to serve team image media: " + fileName, e);
            return null;
        }
    }

    private static String getFileNameFromPathOrReturnRequestPath(Context context) {

        String fileName = context.getPathParameter(FILENAME_PATH_PARAM);

        if (fileName == null) {
            fileName = context.getRequestPath();
        }
        return fileName;
    }
}
