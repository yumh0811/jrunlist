/**
 * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED
 * REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
 */

package com.github.egateam.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.github.egateam.IntSpan;
import com.github.egateam.util.FileConverterIn;
import com.github.egateam.util.Transform;
import com.github.egateam.util.WriteYAML;
import com.github.egateam.util.YAMLInfo;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"CanBeFinal"})
@Parameters(commandDescription = "Operate spans in a YAML file.\n"
    + "\tList of operations\n"
    + "\t\tcover:  a single span from min to max;\n"
    + "\t\tholes:  all the holes in runlist;\n"
    + "\t\ttrim:   remove N integers from each end of each span of runlist;\n"
    + "\t\tpad:    add N integers from each end of each span of runlist;\n"
    + "\t\texcise: remove all spans smaller than N;\n"
    + "\t\tfill:   fill in all holes smaller than or equals to N."
)
public class Span {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Parameter(description = "<infile>", converter = FileConverterIn.class, required = true)
    private List<File> files;

    @Parameter(names = {"--outfile", "-o"}, description = "Output filename. [stdout] for screen.")
    private String outfile;

    @Parameter(names = {"--op"}, description = "operations: cover, holes, trim, pad, excise or fill.")
    private String op = "cover";

    @Parameter(names = {"--number", "-n"}, description = "Apply this number to trim, pad, excise or fill.")
    private int number = 0;

    @Parameter(names = {"--remove", "-r"}, description = "Remove 'chr0' from chromosome names.")
    private Boolean remove = false;

    private void validateArgs() {
        if ( files.size() != 1 ) {
            throw new ParameterException("This command need one input file.");
        }

        if ( op.startsWith("cover") ) {
            op = "cover";
        } else if ( op.startsWith("hole") ) {
            op = "holes";
        } else if ( op.startsWith("trim") ) {
            op = "trim";
        } else if ( op.startsWith("pad") ) {
            op = "pad";
        } else if ( op.startsWith("excise") ) {
            op = "excise";
        } else if ( op.startsWith("fill") ) {
            op = "fill";
        } else {
            throw new RuntimeException(String.format("op [%s] is invalid", op));
        }

        if ( outfile == null ) {
            outfile = files.get(0) + "." + op + ".yml";
        }
    }

    public void execute() throws Exception {
        validateArgs();

        //----------------------------
        // Loading
        //----------------------------
        YAMLInfo                          yaml  = new YAMLInfo();
        Map<String, Map<String, IntSpan>> setOf = yaml.invoke(files.get(0), remove);

        //----------------------------
        // Operating
        //----------------------------
        Map<String, Map<String, IntSpan>> opResultOf = new HashMap<>();
        for ( String name : yaml.getSortedNames() ) {
            Map<String, IntSpan> setOne = setOf.get(name);

            Map<String, IntSpan> setOP = new HashMap<>();
            for ( String chr : yaml.getSortedChrs() ) {
                if ( setOne.containsKey(chr) ) {
                    if ( op.equals("cover") || op.equals("holes") ) {
                        Method  methodOP  = IntSpan.class.getMethod(op);
                        IntSpan opIntSpan = (IntSpan) methodOP.invoke(setOne.get(chr));
                        setOP.put(chr, opIntSpan);
                    } else {
                        Method  methodOP  = IntSpan.class.getMethod(op, int.class);
                        IntSpan opIntSpan = (IntSpan) methodOP.invoke(setOne.get(chr), number);
                        setOP.put(chr, opIntSpan);
                    }
                }
            }

            opResultOf.put(name, setOP);
        }

        //----------------------------
        // Output
        //----------------------------
        if ( yaml.isMultiKey() ) {
            new WriteYAML(
                outfile,
                new Transform(opResultOf, remove).toRunlist()
            ).invoke();
        } else {
            new WriteYAML(
                outfile,
                new Transform(opResultOf.get(YAMLInfo.getSingleKey()), remove).toRunlist()
            ).invoke();
        }
    }
}
