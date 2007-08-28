/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell.util;

import org.codehaus.groovy.tools.shell.Groovysh;
import org.codehaus.groovy.tools.shell.IO;

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.prefs.PreferenceChangeEvent;

/**
 * Provides a very, very basic logging API.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public final class Logger
{
    public static IO io;

    public final String name;

    private Logger(final String name) {
        assert name != null;

        this.name = name;
    }
    
    private void log(final String level, Object msg, Throwable cause) throws Exception {
        assert level != null;
        assert msg != null;
        
        if (io == null) {
            io = new IO();
        }

        // Allow the msg to be a Throwable, and handle it properly if no cause is given
        if (cause == null) {
            if (msg instanceof Throwable) {
                cause = (Throwable) msg;
                msg = cause.getMessage();
            }
        }

        StringBuffer buff = new StringBuffer();
        
        int color = ANSI.Code.BOLD;
        if (WARN.equals(level) || ERROR.equals(level)) {
            color = ANSI.Code.RED;
        }

        buff.append(ANSI.Renderer.encode(level, color));

        buff.append(" [");
        buff.append(name);
        buff.append("] ");
        buff.append(msg);

        io.out.println(buff);

        if (cause != null) {
            cause.printStackTrace(io.out);
        }
        
        io.flush();
    }
    
    //
    // Level helpers
    //
    
    private static final String DEBUG = "DEBUG";
    
    public static boolean debug;

    static {
        //
        // HACK: For now put in some ugly preferences API muck until we can find a more elegant solution
        //
        
        Preferences prefs = Preferences.userNodeForPackage(Groovysh.class);

        debug = prefs.getBoolean("debug", false);

        prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
            public void preferenceChange(final PreferenceChangeEvent event) {
                if (event.getKey().equals("debug")) {
                    debug = event.getNode().getBoolean("debug", false);
                }
            }
        });
    }

    public boolean isDebugEnabled() {
        return debug;
    }
    
    public void debug(final Object msg) throws Exception {
        if (debug) {
            log(DEBUG, msg, null);
        }
    }
    
    public void debug(final Object msg, final Throwable cause) throws Exception {
        if (debug) {
            log(DEBUG, msg, cause);
        }
    }

    private static final String WARN = "WARN";

    public void warn(final Object msg) throws Exception {
        log(WARN, msg, null);
    }

    public void warn(final Object msg, final Throwable cause) throws Exception {
        log(WARN, msg, cause);
    }
    
    private static final String ERROR = "ERROR";

    public void error(final Object msg) throws Exception {
        log(ERROR, msg, null);
    }

    public void error(final Object msg, final Throwable cause) throws Exception {
        log(ERROR, msg, cause);
    }

    //
    // Factory access
    //
    
    public static Logger create(final Class type) {
        return new Logger(type.getName());
    }

    public static Logger create(final Class type, final String suffix) {
        return new Logger(type.getName() + "." + suffix);
    }
}
