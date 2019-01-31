/*
 * Copyright (c) 2017, 2018, Oracle and/or its affiliates.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.graalvm.vm.x86.launcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.graalvm.launcher.AbstractLanguageLauncher;
import org.graalvm.nativeimage.ImageInfo;
import org.graalvm.nativeimage.RuntimeOptions;
import org.graalvm.options.OptionCategory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.vm.util.log.Trace;

public class AMD64Launcher extends AbstractLanguageLauncher {
    public static void main(String[] args) {
        Trace.setupConsoleApplication(Level.INFO);
        if (ImageInfo.inImageCode()) {
            // set default thresholds
            RuntimeOptions.set("TruffleOSRCompilationThreshold", 10);
            RuntimeOptions.set("TruffleCompilationThreshold", 10);
        }
        new AMD64Launcher().launch(args);
    }

    private String[] programArgs;
    private File file;
    private VersionAction versionAction = VersionAction.None;

    @Override
    protected void launch(Context.Builder contextBuilder) {
        System.exit(execute(contextBuilder));
    }

    @Override
    protected String getLanguageId() {
        return "amd64";
    }

    @Override
    protected List<String> preprocessArguments(List<String> arguments, Map<String, String> polyglotOptions) {
        final List<String> unrecognizedOptions = new ArrayList<>();

        ListIterator<String> iterator = arguments.listIterator();
        while (iterator.hasNext()) {
            String option = iterator.next();
            if (option.length() < 2 || !option.startsWith("-")) {
                iterator.previous();
                break;
            }
            // Ignore fall through
            switch (option) {
                case "--": // --
                    break;
                case "--show-version":
                    versionAction = VersionAction.PrintAndContinue;
                    break;
                case "--version":
                    versionAction = VersionAction.PrintAndExit;
                    break;
                default:
                    // options with argument
                    String argument;
                    int equalsIndex = option.indexOf('=');
                    if (equalsIndex > 0) {
                        argument = option.substring(equalsIndex + 1);
                    } else if (iterator.hasNext()) {
                        argument = iterator.next();
                    } else {
                        argument = null;
                    }
                    // ignore unknown options
                    unrecognizedOptions.add(option);
                    if (equalsIndex < 0 && argument != null) {
                        iterator.previous();
                    }
                    break;
            }
        }

        // collect the file:
        if (file == null && iterator.hasNext()) {
            file = Paths.get(iterator.next()).toFile();
        }

        // collect the program args:
        if (iterator.previousIndex() < 0) {
            programArgs = new String[0];
        } else {
            List<String> programArgumentsList = arguments.subList(iterator.previousIndex(), arguments.size());
            programArgs = programArgumentsList.toArray(new String[programArgumentsList.size()]);
        }

        return unrecognizedOptions;
    }

    @Override
    protected void validateArguments(Map<String, String> polyglotOptions) {
        if (file == null && versionAction != VersionAction.PrintAndExit) {
            throw abort("No program file provided.", 1);
        }
    }

    @Override
    protected void printHelp(OptionCategory maxCategory) {
        // @formatter:off
        System.out.println("Usage: vmx86 [OPTIONS]... FILE [PROGRAM ARGS]");
        System.out.println("Run x86_64 programs for Linux on the GraalVM's x86_64 interpreter.\n");
        System.out.println("Mandatory arguments to long options are mandatory for short options too.\n");
        System.out.println("Options:");
        printOption("--version",         "print the version and exit");
        printOption("--show-version",    "print the version and continue");
        // @formatter:on
    }

    @Override
    protected void collectArguments(Set<String> args) {
        // @formatter:off
        args.addAll(Arrays.asList(
                        "--version",
                        "--show-version"));
        // @formatter:on
    }

    protected static void printOption(String option, String description) {
        String opt;
        if (option.length() >= 22) {
            System.out.println(String.format("%s%s", "  ", option));
            opt = "";
        } else {
            opt = option;
        }
        System.out.println(String.format("  %-22s%s", opt, description));
    }

    protected int execute(Context.Builder contextBuilder) {
        contextBuilder.arguments(getLanguageId(), programArgs);
        try (Context context = contextBuilder.build()) {
            runVersionAction(versionAction, context.getEngine());
            Value result = context.eval(Source.newBuilder(getLanguageId(), file).build());
            return result.asInt();
        } catch (PolyglotException e) {
            if (e.isExit()) {
                return e.getExitStatus();
            } else if (!e.isInternalError()) {
                printStackTraceSkipTrailingHost(e);
                return -1;
            } else {
                throw e;
            }
        } catch (IOException e) {
            throw abort(e);
        }
    }

    private static void printStackTraceSkipTrailingHost(PolyglotException e) {
        List<PolyglotException.StackFrame> stackTrace = new ArrayList<>();
        for (PolyglotException.StackFrame s : e.getPolyglotStackTrace()) {
            stackTrace.add(s);
        }
        // remove trailing host frames
        for (ListIterator<PolyglotException.StackFrame> iterator = stackTrace.listIterator(stackTrace.size()); iterator.hasPrevious();) {
            PolyglotException.StackFrame s = iterator.previous();
            if (s.isHostFrame()) {
                iterator.remove();
            } else {
                break;
            }
        }
        System.out.println(e.isHostException() ? e.asHostException().toString() : e.getMessage());
        for (PolyglotException.StackFrame s : stackTrace) {
            System.out.println("\tat " + s);
        }
    }
}
