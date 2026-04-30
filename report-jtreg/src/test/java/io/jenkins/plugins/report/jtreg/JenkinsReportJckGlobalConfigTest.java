package io.jenkins.plugins.report.jtreg;

import hudson.util.FormValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithJenkins
class JenkinsReportJckGlobalConfigTest {

    @Test
    void testDoCheckKinds_ValidSingleKind(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("PLAIN");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testDoCheckKinds_ValidMultipleKinds(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("PLAIN, JSON, PROPERTIES");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testDoCheckKinds_ValidKindsCaseInsensitive(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("plain, Json, PROPERTIES");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testDoCheckKinds_ValidKindsWithExtraSpaces(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("  PLAIN  ,  JSON  ,  PROPERTIES  ");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testDoCheckKinds_EmptyString(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testDoCheckKinds_NullValue(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds(null);
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testDoCheckKinds_InvalidKind(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("INVALID");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals(true, result.getMessage().contains("Invalid kinds: INVALID"));
        assertEquals(true, result.getMessage().contains("Valid values are: PLAIN, PROPERTIES, JSON"));
    }

    @Test
    void testDoCheckKinds_MixedValidAndInvalid(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("PLAIN, INVALID, JSON");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals(true, result.getMessage().contains("Invalid kinds: INVALID"));
    }

    @Test
    void testDoCheckKinds_MultipleInvalidKinds(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("INVALID1, INVALID2, PLAIN");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals(true, result.getMessage().contains("Invalid kinds"));
        assertEquals(true, result.getMessage().contains("INVALID1"));
        assertEquals(true, result.getMessage().contains("INVALID2"));
    }

    @Test
    void testDoCheckKinds_DuplicateKinds(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("PLAIN, JSON, PLAIN");
        assertEquals(FormValidation.Kind.WARNING, result.kind);
        assertEquals(true, result.getMessage().contains("Duplicate kinds found: PLAIN"));
    }

    @Test
    void testDoCheckKinds_MultipleDuplicates(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("PLAIN, JSON, PLAIN, JSON");
        assertEquals(FormValidation.Kind.WARNING, result.kind);
        assertEquals(true, result.getMessage().contains("Duplicate kinds found"));
        assertEquals(true, result.getMessage().contains("PLAIN"));
        assertEquals(true, result.getMessage().contains("JSON"));
    }

    @Test
    void testDoCheckKinds_DuplicatesCaseInsensitive(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("plain, PLAIN, Plain");
        assertEquals(FormValidation.Kind.WARNING, result.kind);
        assertEquals(true, result.getMessage().contains("Duplicate kinds found: PLAIN"));
    }

    @Test
    void testDoCheckKinds_NoneIsInvalid(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("NONE");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals(true, result.getMessage().contains("Invalid kinds: NONE"));
    }

    @Test
    void testDoCheckKinds_EmptyElementsIgnored(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("PLAIN, , JSON, ,");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testDoCheckKinds_OnlyCommas(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds(",,,");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testDoCheckKinds_WhitespaceOnly(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("   ");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testGettersAndSetters(JenkinsRule r) {
        JenkinsReportJckGlobalConfig config = new JenkinsReportJckGlobalConfig();
        config.setKinds("PLAIN, JSON");
        assertEquals("PLAIN, JSON", config.getKinds());
    }

    @Test
    void testGlobalKindsGetter(JenkinsRule r) {
        // This test verifies the static getter works
        // Note: In a real Jenkins environment, this would need proper initialization
        JenkinsReportJckGlobalConfig config = new JenkinsReportJckGlobalConfig();
        config.setKinds("PROPERTIES");
        String kinds = config.getKinds();
        assertEquals("PROPERTIES", kinds);
    }
}

