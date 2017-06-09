/*******************************************************************************
 *
 *   Copyright 2017 Walmart, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *******************************************************************************/
package com.oneops.log;

import com.oneops.utils.Platform;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.*;

/**
 * A Java logging formatter that writes more compact output than the default.
 *
 * @author Suresh
 */
public class BriefLogFormatter extends Formatter {

    /**
     * Message format.
     */
    private final MessageFormat messageFormat = new MessageFormat("{4,date,HH:mm:ss} [" + Platform.getUser() + "] {0} {1}{2}.{3}: {5}\n{6}");

    /**
     * OpenJDK made a questionable, backwards incompatible change to the Logger implementation.
     * It internally uses weak references now which means simply fetching the logger and changing
     * its configuration won't work. We must keep a reference to our custom logger around.
     */
    private static List<Logger> loggerRefs = new ArrayList<>();

    /**
     * Log directory.
     */
    public static final Path logDir = Paths.get(Platform.getUserHome() + "/keywhiz-cli/log");

    /**
     * Used to check whether the logger is already initialized.
     */
    private static boolean initialized = false;

    @Override
    public String format(@Nonnull LogRecord logRecord) {
        Object[] arguments = new Object[7];

        arguments[0] = logRecord.getThreadID();
        arguments[1] = "";
        if (logRecord.getLevel() == Level.SEVERE) {
            arguments[1] = " **ERROR** ";
        } else if (logRecord.getLevel() == Level.WARNING) {
            arguments[1] = " (warning) ";
        }

        String fullClassName = logRecord.getSourceClassName();
        int dollarIndex = fullClassName.indexOf('$');
        if (dollarIndex == -1) {
            dollarIndex = fullClassName.length();
        }

        String className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1, dollarIndex);
        arguments[2] = className;
        arguments[3] = logRecord.getSourceMethodName();
        arguments[4] = new Date(logRecord.getMillis());
        String message = logRecord.getMessage();
        if (logRecord.getParameters() != null) {
            message = MessageFormat.format(logRecord.getMessage(), logRecord.getParameters());
        }
        arguments[5] = message;
        if (logRecord.getThrown() != null) {
            StringWriter result = new StringWriter();
            logRecord.getThrown().printStackTrace(new PrintWriter(result));
            arguments[6] = result.toString();
        } else {
            arguments[6] = "";
        }
        return messageFormat.format(arguments);
    }


    /**
     * Configures JDK root logging to use this class for everything.
     * This will remove the default console logger and adds a custom
     * file handler.
     */
    public static void init() throws IOException {
        Logger logger = Logger.getLogger("");
        if (!initialized) {
            if (!logDir.toFile().exists()) logDir.toFile().mkdirs();

            FileHandler fileHandler = new FileHandler(logDir.resolve("OneOps-KeywhizCli-%g.log").toString(), 10 * 1024 * 1024, 2, true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new BriefLogFormatter());

            logger.setUseParentHandlers(false);
            logger.removeHandler(logger.getHandlers()[0]);
            logger.addHandler(fileHandler);
            loggerRefs.add(logger);

            logger.info("");
            logger.info(">>>>> Starting Keywhiz CLI... <<<<<");
            logger.info(() -> "Initialized the logger on" + DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()));
            initialized = true;
        } else {
            logger.info("Logger is already initialized!");
        }
    }
}