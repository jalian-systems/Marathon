package net.sourceforge.marathon.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IScriptModelClientPart;

public class FixturePropertyHelper {

    private static final String FIXTURE_START = "#{{{ Fixture Properties";
    private static final String FIXTURE_END = "#}}}";
    private final IScriptModelClientPart scriptModel;

    public FixturePropertyHelper(IScriptModelClientPart scriptModel) {
        this.scriptModel = scriptModel;
    }
    
    public Map<String, Object> getFixtureProperties(String script, Pattern fixtureImportMatcher) {
        String fixture;
        if ((fixture = findFixture(script, fixtureImportMatcher)) == null)
            return new HashMap<String, Object>();
        Map<String, Object> props = findFixtureProperties(fixture);
        return props;
    }

    public String findFixture(String script, Pattern fixtureImportMatcher) {
        BufferedReader b = new BufferedReader(new StringReader(script));
        try {
            String line;
            while ((line = b.readLine()) != null) {
                Matcher matcher = fixtureImportMatcher.matcher(line);
                if (matcher.matches()) {
                    String fixture = matcher.group(1);
                    if (getFixtureReader(fixture) != null)
                        return fixture ;
                }
            }
        } catch (IOException e) {
        }
        return null;
    }

    @SuppressWarnings("unchecked") public Map<String, Object> findFixtureProperties(String fixture) {
        HashMap<String, Object> emptyMap = new HashMap<String, Object>();
        String fixtureProperties = getFixturePropertiesPart(fixture);
        if (fixtureProperties == null)
            return emptyMap;
        try {
            return getMapValue((Map<Object, Object>) scriptModel.eval(fixtureProperties));
        } catch (Throwable t) {
            return emptyMap;
        }
    }

    private Map<String, Object> getMapValue(Map<Object, Object> fProps) {
        Map<String, Object> props = new HashMap<String, Object>();
        Set<Entry<Object, Object>> entrySet = fProps.entrySet();
        for (Entry<Object, Object> entry : entrySet) {
            Object value = getValue(entry.getValue());
            if (value != null)
                props.put(entry.getKey().toString(), value);
        }
        return props;
    }

    private Object getValue(Object value) {
        if (value instanceof String)
            return value;
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> l = (List<Object>) value;
            return getListValue(l);
        }
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> m = (Map<Object, Object>) value;
            return getMapValue(m);
        }
        return null;
    }

    private Object getListValue(List<Object> l) {
        List<Object> r = new ArrayList<Object>();
        for (Object object : l) {
            Object value = getValue(object);
            if (value != null)
                r.add(value);
        }
        return r;
    }

    protected String getFixturePropertiesPart(String fixture) {
        BufferedReader b = getFixtureReader(fixture);
        if (b == null)
            return null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String line;
        try {
            boolean collect = false;
            while ((line = b.readLine()) != null) {
                if (line.startsWith(FIXTURE_START)) {
                    collect = true;
                }
                if (collect)
                    pw.println(line);
                if (line.startsWith(FIXTURE_END)) {
                    return sw.toString();
                }
            }
        } catch (IOException e) {
        }
        return null;
    }

    protected BufferedReader getFixtureReader(String fixture) {
        File file = new File(System.getProperty(Constants.PROP_FIXTURE_DIR), fixture + scriptModel.getSuffix());
        if (file.exists())
            try {
                return new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
            }
        return null;
    }


}
