/**
 * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED
 * REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
 */

package com.github.egateam.jrunlist.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.github.egateam.IntSpan;
import com.github.egateam.commons.ChrRange;
import com.github.egateam.commons.Utils;
import com.github.egateam.jrunlist.util.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"CanBeFinal", "unused"})
@Parameters(commandDescription = "Output covers on chromosomes.\n"
    + "\t\tLike `command combine`, but <infiles> are chromosome positions.\n"
    + "\t\tI:1-100\n"
    + "\t\tI(+):90-150\t Strands will be omitted.\n"
    + "\t\tS288c.I(-):190-200\tSpecies names will be omitted."
)
public class Cover {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Parameter(description = "<infiles>", required = true)
    private List<String> files;

    @Parameter(names = {"--outfile", "-o"}, description = "Output filename. [stdout] for screen.")
    private String outfile;

    @Parameter(names = {"--remove", "-r"}, description = "Remove 'chr0' from chromosome names.")
    private Boolean remove = false;

    private void validateArgs() throws ParameterException {
//        if ( files.size() < 1 ) {
//            throw new ParameterException("This command need one or more input files.");
//        }

        if ( outfile == null ) {
            outfile = files.get(0) + ".yml";
        }
    }

    public void execute() throws Exception {
        validateArgs();

        Map<String, IntSpan> setSingle = new HashMap<>();

        //----------------------------
        // Loading
        //----------------------------
        for ( String infile : files ) {
            for ( String str : Utils.readLines(infile) ) {

                //----------------------------
                // Operating
                //----------------------------
                ChrRange chrRange = new ChrRange(str);
                if ( !chrRange.isEmpty() ) {
                    String chrName = chrRange.getChr();
                    if ( !setSingle.containsKey(chrName) ) {
                        setSingle.put(chrName, new IntSpan());
                    }

                    setSingle.get(chrName).addPair(chrRange.getStart(), chrRange.getEnd());
                }
            }
        }

        //----------------------------
        // Output
        //----------------------------
        StaticUtils.writeRl(outfile, Utils.toRunlist(setSingle));
    }
}
