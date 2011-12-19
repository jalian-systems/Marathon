package net.sourceforge.marathon.ruby;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.regex.Pattern;

import net.sourceforge.marathon.script.FixturePropertyHelper;

import org.junit.Before;
import org.junit.Test;

public class TestFixturePropertyHelper {

    // @formatter:off
    private String typicalTestScript = 
            "#{{{ Marathon\n" +
            "require_fixture 'default'\n" +
            "#}}} Marathon\n" +
            "\n" +
            "def test\n" +
            "\n" +
            "    $java_recorded_version=\"1.6.0_26\"\n" +
            "    with_window(\"SwingSet2\") {\n" +
            "        select(\"JColorChooser\", \"true\")\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "end\n" +
            "\n"
            ;
    protected String fixtureProperties = 
        "#{{{ Fixture Properties\n" +
        "fixture_properties = {\n" +
        "       :main_class => 'SwingSet2',\n" +
        "       :program_arguments => '',\n" +
        "       :vm_arguments => '',\n" +
        "       :working_directory => '',\n" +
        "       :java_executable => '%java.home%/bin/java',\n" +
        "       :java_properties => { :user_name => 'KD' },\n" +
        "       :class_path => [ '%marathon.project.dir%/../tutorial/deployment/webstart/examples/webstart_AppWithCustomProgressIndicator/lib/SwingSet2.jar' ]\n" +
        "}\n" +
        "#}}}\n" ;

    // @formatter: on
    
    private static final Pattern FIXTURE_IMPORT_MATCHER = Pattern.compile("\\s*require_fixture\\s\\s*['\"](.*)['\"].*");
    private RubyScriptModel rubyScriptModel;

    @Before
    public void setup() {
        rubyScriptModel = new RubyScriptModel();
    }
    
    @Test public void testFindFixture() {
        FixturePropertyHelper model = new FixturePropertyHelper(rubyScriptModel) {
            @Override protected BufferedReader getFixtureReader(String fixture) {
                return new BufferedReader(new StringReader(fixtureProperties));
            }
        };
        assertEquals("default", model.findFixture(typicalTestScript, FIXTURE_IMPORT_MATCHER));
    }
    
    @Test public void testFindFixtureProperties() {
        FixturePropertyHelper model = new FixturePropertyHelper(rubyScriptModel) {
            @Override protected String getFixturePropertiesPart(String fixture) {
                return fixtureProperties ;
            }
        };
        model.findFixtureProperties("default");
    }

    @Test public void testFindFixtureProperties2() {
        FixturePropertyHelper model = new FixturePropertyHelper(rubyScriptModel) {
            @Override protected BufferedReader getFixtureReader(String fixture) {
                return new BufferedReader(new StringReader(fixtureProperties));
            }
        };
        model.findFixtureProperties("default");
    }
}
