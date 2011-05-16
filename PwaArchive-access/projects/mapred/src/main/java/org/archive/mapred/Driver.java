package org.archive.mapred;

import java.lang.reflect.Method;


public class Driver {
    public void doClass(final String [] args) throws Exception {
        // Redo args so class is stripped.
        final String className = args[0];
        String [] newArgs = rewriteArgs(args, 1);
        // From http://www.javaworld.com/javaworld/javaqa/1999-06/01-outside.html
        Class [] argTypes = new Class[1];
        argTypes[0] = String[].class;
        Method mainMethod =
            Class.forName(className).getDeclaredMethod("main", argTypes);
        mainMethod.invoke(newArgs, new Object [] {newArgs});
    }
    
    public String [] rewriteArgs(final String [] args, final int offset) {
        final String [] newArgs = new String[args.length - offset];
        for (int i = 0; i < args.length; i++) {
            if (i < offset) {
                continue;
            }
            newArgs[i - offset] = args[i];
        }
        return newArgs;
    }
    
    public static void usage(final String message, final int exitCode) {
        if (message != null && message.length() > 0) {
            System.out.println(message);
        }

        System.out.println("Usage: hadoop jar archive-mapred.jar CLASSNAME " +
            "[args]");
        System.out.println("Runs the CLASSNAME main.");
        System.exit(exitCode);
    }
    
    public static void main(String args[]) throws Exception {
        if (args.length < 1) {
            usage(null, 0);
            return;
        }
        
        new Driver().doClass(args);
    }
}