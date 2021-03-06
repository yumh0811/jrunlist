/**
 * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED
 * REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
 */

package com.github.egateam.jrunlist.util;

import com.github.egateam.IntSpan;
import com.github.egateam.commons.Utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class RlInfo {
    private final Set<String> allChrs;
    private final Set<String> allNames;
    private       boolean     isMulti;
    private static final String SINGLE_KEY = "__single";

    public static String getSingleKey() {
        return SINGLE_KEY;
    }

    public RlInfo() {
        allChrs = new HashSet<>();
        allNames = new HashSet<>();
        isMulti = false;
    }

    private Set<String> getAllChrs() {
        return allChrs;
    }

    private Set<String> getAllNames() {
        return allNames;
    }

    public boolean isMulti() {
        return isMulti;
    }

    public List<String> getSortedChrs() {
        ArrayList<String> sorted = new ArrayList<>(getAllChrs());
        Collections.sort(sorted);

        return sorted;
    }

    public List<String> getSortedNames() {
        ArrayList<String> sorted = new ArrayList<>(getAllNames());
        Collections.sort(sorted);

        return sorted;
    }

    public Map<String, Map<String, IntSpan>> load(String fileName, boolean remove) throws Exception {
        Map<String, ?> runlistOf = StaticUtils.readRl(fileName);

        // check depth of YAML
        // get one (maybe not first) value from Map
        isMulti = !(runlistOf.entrySet().iterator().next().getValue() instanceof String);

        Map<String, Map<String, IntSpan>> setOf = new HashMap<>();
        if ( isMulti ) {
            for ( Map.Entry<String, ?> entry : runlistOf.entrySet() ) {
                String key = entry.getKey();
                //noinspection unchecked
                HashMap<String, String> value = (HashMap<String, String>) entry.getValue();

                setOf.put(key, Utils.toIntSpan(value));
                allChrs.addAll(value.keySet());

                allNames.add(key);
            }
        } else {
            allChrs.addAll(runlistOf.keySet());
            allNames.add(getSingleKey());
            setOf.put(getSingleKey(), Utils.toIntSpan((Map<String, String>) runlistOf));
        }

        return setOf;
    }

    public Map<String, IntSpan> loadSingle(String fileName, boolean remove) throws Exception {
        Map<String, ?> runlistSingle = StaticUtils.readRl(fileName);

        // check depth of YAML
        // get one (maybe not first) value from Map
        if ( !(runlistSingle.entrySet().iterator().next().getValue() instanceof String) ) {
            throw new RuntimeException(
                String.format("File [%s] shouldn't be a multikey YAML.", fileName)
            );
        }

        allChrs.addAll(runlistSingle.keySet());
        return Utils.toIntSpan((Map<String, String>) runlistSingle);
    }

    // Create empty IntSpan for each name:chr
    private void fillUp(Map<String, Map<String, IntSpan>> setOf) {
        for ( String name : getSortedNames() ) {
            Map<String, IntSpan> setOne = setOf.get(name);

            for ( String chr : getSortedChrs() ) {
                if ( !setOne.containsKey(chr) ) {
                    setOne.put(chr, new IntSpan());
                }
            }
        }
    }

    // Create empty IntSpan for each chr
    private void fillUpSingle(Map<String, IntSpan> setSingle) {
        for ( String chr : getSortedChrs() ) {
            if ( !setSingle.containsKey(chr) ) {
                setSingle.put(chr, new IntSpan());
            }
        }
    }

    public Map<String, Map<String, IntSpan>> opResult(String op, Map<String, Map<String, IntSpan>> setOf, Map<String, IntSpan> setSingle) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        fillUp(setOf);
        fillUpSingle(setSingle);

        Map<String, Map<String, IntSpan>> opResultOf = new HashMap<>();
        for ( String name : getSortedNames() ) {
            Map<String, IntSpan> setOne = setOf.get(name);

            Map<String, IntSpan> setOP = new HashMap<>();
            for ( String chr : getSortedChrs() ) {
                Method  methodOP  = IntSpan.class.getMethod(op, IntSpan.class);
                IntSpan opIntSpan = (IntSpan) methodOP.invoke(setOne.get(chr), setSingle.get(chr));
                setOP.put(chr, opIntSpan);
            }

            opResultOf.put(name, setOP);
        }
        return opResultOf;
    }

}
