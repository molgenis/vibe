package org.molgenis.vibe.core.exceptions;

import java.io.IOException;

/**
 * An {@link IOException} specific for JSON-parsing. Can be used when a {@link com.google.gson.JsonParseException} is
 * caused by an input file/stream.
 */
public class JsonIOParseException extends IOException {
    public JsonIOParseException() {
        super();
    }

    public JsonIOParseException(String message) {
        super(message);
    }

    public JsonIOParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonIOParseException(Throwable cause) {
        super(cause);
    }
}
