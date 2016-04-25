/**
 * <tt>jrunlist</tt> operates chromosome runlist files.
 * <p>
 * <strong>AUTHOR</strong>
 * Qiang Wang, wang-q@outlook.com
 * <p>
 * <strong>COPYRIGHT AND LICENSE</strong>
 * This software is copyright (c) 2016 by Qiang Wang.
 * <p>
 * This is free software; you can redistribute it and/or modify it under the same terms as the Perl
 * 5 programming language system itself.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED
 * REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
 *
 * @author Qiang Wang
 * @since 1.7
 */

package com.github.egateam;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import com.github.egateam.commands.*;

@SuppressWarnings("WeakerAccess")
@Parameters
public class Runlist {
    /**
     * The only global options
     */
    @SuppressWarnings("CanBeFinal")
    @Parameter(names = {"--help", "-h"}, description = "Print this help and quit", help = true)
    private boolean help = false;

    public void execute(String[] args) {

        JCommander jc = new JCommander(this);
        jc.addCommand("genome", new Genome());
        jc.addCommand("merge", new Merge());
        jc.addCommand("split", new Split());
        jc.addCommand("some", new Some());
        jc.addCommand("combine", new Combine());
        jc.addCommand("stat", new Stat());

        String parsedCommand;
        try {
            jc.parse(args);
            parsedCommand = jc.getParsedCommand();

            if ( help ) {
                jc.usage();
                return;
            }

            if ( parsedCommand == null ) throw new ParameterException("No command specified");
        } catch ( ParameterException e ) {
            System.err.println(e.getMessage());
            jc.usage();
            return;
        } catch ( Exception e ) {
            e.printStackTrace();
            return;
        }

        Object command = jc.getCommands().get(parsedCommand).getObjects().get(0);

        try {
            if ( command instanceof Genome ) {
                Genome commandNew = (Genome) command;
                commandNew.execute();
            } else if ( command instanceof Merge ) {
                Merge commandNew = (Merge) command;
                commandNew.execute();
            } else if ( command instanceof Split ) {
                Split commandNew = (Split) command;
                commandNew.execute();
            } else if ( command instanceof Some ) {
                Some commandNew = (Some) command;
                commandNew.execute();
            } else if ( command instanceof Combine ) {
                Combine commandNew = (Combine) command;
                commandNew.execute();
            } else if ( command instanceof Stat ) {
                Stat commandNew = (Stat) command;
                commandNew.execute();
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /*
    mvn clean verify

    java -jar target/jrunlist-0.1.0-SNAPSHOT-jar-with-dependencies.jar \
        genome \
        src/test/resources/chr.sizes \
        -o stdout

    java -jar target/jrunlist-0.1.0-SNAPSHOT-jar-with-dependencies.jar \
        merge \
        src/test/resources/I.yml \
        src/test/resources/II.yml \
        -o stdout

    java -jar target/jrunlist-0.1.0-SNAPSHOT-jar-with-dependencies.jar \
        stat \
        src/test/resources/chr.sizes \
        src/test/resources/intergenic.yml \
        -o stdout
     */
    public static void main(String[] args) throws Exception {
        new Runlist().execute(args);
    }
}
