/*
 * 
 * Datei übernommen von https://raw.githubusercontent.com/omnifaces/omnifaces/4.x/src/main/java/org/omnifaces/util/Utils.java
 * 
 * Nur die Methoden übernommen die für die Klasse FileServlet.java relevant sind.
 * Umstellung von File zu Path.
 * 
 */
/*
 * Copyright OmniFaces
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.mycore.jspdocportal.diskcache.servlet;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletResponse;

public class FileDownloadServletUtils {
    private static final int DEFAULT_STREAM_BUFFER_SIZE = 10240;

    /**
     * Returns the first non-<code>null</code> object of the argument list, or <code>null</code> if there is no such
     * element.
     * @param <T> The generic object type.
     * @param objects The argument list of objects to be tested for non-<code>null</code>.
     * @return The first non-<code>null</code> object of the argument list, or <code>null</code> if there is no such
     * element.
     */
    @SafeVarargs
    public static <T> T coalesce(T... objects) {
        for (T object : objects) {
            if (object != null) {
                return object;
            }
        }

        return null;
    }

    /**
     * URL-encode the given string using UTF-8.
     * @param string The string to be URL-encoded using UTF-8.
     * @return The given string, URL-encoded using UTF-8, or <code>null</code> if <code>null</code> was given.
     * @throws UnsupportedOperationException When this platform does not support UTF-8.
     * @since 1.4
     */
    public static String encodeURL(String string) {
        if (string == null) {
            return null;
        }

        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    /**
     * Returns <code>true</code> if the given string starts with one of the given prefixes.
     * @param string The object to be checked if it starts with one of the given prefixes.
     * @param prefixes The argument list of prefixes to be checked
     * @return <code>true</code> if the given string starts with one of the given prefixes.
     * @since 1.4
     */
    public static boolean startsWithOneOf(String string, String... prefixes) {
        for (String prefix : prefixes) {
            if (string.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Stream the given input to the given output via NIO {@link Channels} and a directly allocated NIO
     * {@link ByteBuffer}. Both the input and output streams will implicitly be closed after streaming,
     * regardless of whether an exception is been thrown or not.
     * @param input The input stream.
     * @param output The output stream.
     * @return The length of the written bytes.
     * @throws IOException When an I/O error occurs.
     */
    public static long stream(InputStream input, OutputStream output) throws IOException {
        try (ReadableByteChannel inputChannel = Channels.newChannel(input);
            WritableByteChannel outputChannel = Channels.newChannel(output)) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(DEFAULT_STREAM_BUFFER_SIZE);
            long size = 0;

            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                size += outputChannel.write(buffer);
                buffer.clear();
            }

            return size;
        }
    }

    /**
     * Stream a specified range of the given path to the given output via NIO {@link Channels} and a directly allocated
     * NIO {@link ByteBuffer}. The output stream will only implicitly be closed after streaming when the specified range
     * represents the whole path, regardless of whether an exception is been thrown or not.
     * @param file The Path.
     * @param output The output stream.
     * @param start The start position (offset).
     * @param length The (intented) length of written bytes.
     * @return The (actual) length of the written bytes. This may be smaller when the given length is too large.
     * @throws IOException When an I/O error occurs.
     * @since 2.2
     */
    public static long stream(Path file, OutputStream output, long start, long length) throws IOException {
        if (start == 0 && length >= Files.size(file)) {
            return stream(Files.newInputStream(file), output);
        }

        try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(file, StandardOpenOption.READ)) {
            WritableByteChannel outputChannel = Channels.newChannel(output);
            ByteBuffer buffer = ByteBuffer.allocateDirect(DEFAULT_STREAM_BUFFER_SIZE);
            long size = 0;

            while (fileChannel.read(buffer, start + size) != -1) {
                buffer.flip();

                if (size + buffer.limit() > length) {
                    buffer.limit((int) (length - size));
                }

                size += outputChannel.write(buffer);

                if (size >= length) {
                    break;
                }

                buffer.clear();
            }

            return size;
        }
    }

    /**
     * <p>Set the cache headers. If the <code>expires</code> argument is larger than 0 seconds, then the following headers
     * will be set:
     * <ul>
     * <li><code>Cache-Control: public,max-age=[expiration time in seconds],must-revalidate</code></li>
     * <li><code>Expires: [expiration date of now plus expiration time in seconds]</code></li>
     * </ul>
     * <p>Else the method will delegate to {@link #setNoCacheHeaders(HttpServletResponse)}.
     * @param response The HTTP servlet response to set the headers on.
     * @param expires The expire time in seconds (not milliseconds!).
     * @since 2.2
     */
    public static void setCacheHeaders(HttpServletResponse response, long expires) {
        if (expires > 0) {
            response.setHeader("Cache-Control", "public,max-age=" + expires + ",must-revalidate");
            response.setDateHeader("Expires", System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expires));
            response.setHeader("Pragma", ""); // Explicitly set pragma to prevent container from overriding it.
        } else {
            setNoCacheHeaders(response);
        }
    }

    /**
     * <p>Set the no-cache headers. The following headers will be set:
     * <ul>
     * <li><code>Cache-Control: no-cache,no-store,must-revalidate</code></li>
     * <li><code>Expires: [expiration date of 0]</code></li>
     * <li><code>Pragma: no-cache</code></li>
     * </ul>
     * Set the no-cache headers.
     * @param response The HTTP servlet response to set the headers on.
     * @since 2.2
     */
    public static void setNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-cache"); // Backwards compatibility for HTTP 1.0.
    }

    /**
     * <p>Format an UTF-8 compatible content disposition header for the given filename and whether it's an attachment.
     * @param filename The filename to appear in "Save As" dialogue.
     * @param attachment Whether the content should be provided as an attachment or inline.
     * @return An UTF-8 compatible content disposition header.
     * @since 2.6
     */
    public static String formatContentDispositionHeader(String filename, boolean attachment) {
        return String.format(Locale.US, "%s;filename=\"%2$s\"; filename*=UTF-8''%2$s", 
            (attachment ? "attachment" : "inline"), encodeURI(filename));
    }

    /**
     * URI-encode the given string using UTF-8. URIs (paths and filenames) have different encoding rules as compared to
     * URL query string parameters. {@link URLEncoder} is actually only for www (HTML) form based query string parameter
     * values (as used when a webbrowser submits a HTML form). URI encoding has a lot in common with URL encoding, but
     * the space has to be %20 and some chars doesn't necessarily need to be encoded.
     * <p>
     * Since version 4.2 this method is using <a href="https://datatracker.ietf.org/doc/html/rfc3986">RFC 3986</a> rules.
     * Previously it was using <a href="https://datatracker.ietf.org/doc/html/rfc2396">RFC 2396</a> rules. The result is
     * therefore not per definition exactly the same, but this is supposed to be backwards compatible in modern clients.
     * @param string The string to be URI-encoded using UTF-8.
     * @return The given string, URI-encoded using UTF-8, or <code>null</code> if <code>null</code> was given.
     * @throws UnsupportedOperationException When this platform does not support UTF-8.
     * @since 2.4
     */
    public static String encodeURI(String string) {
        if (string == null) {
            return null;
        }

        return encodeURL(string)
            .replace("+", "%20")
            .replace("*", "%2A")
            .replace("%7E", "~");
    }

}
