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
package com.oneops.cli;

import com.google.common.base.Strings;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.Attributes;

import static com.oneops.utils.Color.*;
import static com.oneops.utils.Common.center;
import static java.util.jar.Attributes.Name.IMPLEMENTATION_VERSION;

/**
 * Default configuration for Keywhiz CLI app.
 *
 * @author Suresh
 */
public class Config {

    /**
     * Holds all manifest attributes in the jar file.
     */
    public static final Attributes jarManifest;

    static {
        jarManifest = readJarManifest();
    }

    private Config() {
    }

    /**
     * Returns the jar manifest of the class. Returns <code>empty</code> attributes
     * if the class is not bundled in a jar (Classes in an unpacked class hierarchy).
     */
    private static Attributes readJarManifest() {
        try {
            URL res = Config.class.getResource(Config.class.getSimpleName() + ".class");
            URLConnection conn = res.openConnection();
            if (conn instanceof JarURLConnection) {
                return ((JarURLConnection) conn).getManifest().getMainAttributes();
            } else {
                return new Attributes(0);
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Formatted banner string for CLI
     */
    public static String banner() {
        int width = 60;
        return "+" + Strings.repeat("-", width) + "+" + "\n" +
                "|" + cyan(bold(center("Welcome to OneOps Keywhiz CLI", width))) + "|" + "\n" +
                "|" + gray(center("Version: " + jarManifest.getValue(IMPLEMENTATION_VERSION), width)) + "|" + "\n" +
                "|" + gray(center("Built on " + jarManifest.getValue("Built-Date"), width)) + "|" + "\n" +
                "+" + Strings.repeat("-", width) + "+" + "\n" +
                "Type " + green("help") + " or " + green("?") +
                " for help, " + green("exit") + " to exit.";
    }
}