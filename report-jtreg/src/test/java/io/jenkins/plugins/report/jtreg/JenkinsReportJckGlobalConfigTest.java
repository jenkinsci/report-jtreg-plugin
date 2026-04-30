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

    // Tests for kinds with spaces
    @Test
    void testDoCheckKinds_WithSpacesInValue(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("PLAIN TEXT");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals(true, result.getMessage().contains("Values must not contain spaces"));
        assertEquals(true, result.getMessage().contains("PLAIN TEXT"));
    }

    @Test
    void testDoCheckKinds_WithSpacesInMultipleValues(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("PLAIN TEXT, JSON DATA");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals(true, result.getMessage().contains("Values must not contain spaces"));
    }

    @Test
    void testDoCheckKinds_WithSpacesInOneOfMultiple(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckKinds("PLAIN, JSON DATA, PROPERTIES");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals(true, result.getMessage().contains("Values must not contain spaces"));
        assertEquals(true, result.getMessage().contains("JSON DATA"));
    }

    // Tests for additionalFilesToCopy validator
    @Test
    void testDoCheckAdditionalFilesToCopy_ValidSingleFile(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckAdditionalFilesToCopy("build.xml");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testDoCheckAdditionalFilesToCopy_ValidMultipleFiles(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckAdditionalFilesToCopy("../../config.xml,build.xml,archive/reports");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testDoCheckAdditionalFilesToCopy_ValidAbsolutePaths(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckAdditionalFilesToCopy("/backup,/archive");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testDoCheckAdditionalFilesToCopy_EmptyString(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckAdditionalFilesToCopy("");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testDoCheckAdditionalFilesToCopy_NullValue(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckAdditionalFilesToCopy(null);
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testDoCheckAdditionalFilesToCopy_WithSpaces(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckAdditionalFilesToCopy("my file.xml");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals(true, result.getMessage().contains("File paths must not contain spaces"));
        assertEquals(true, result.getMessage().contains("my file.xml"));
    }

    @Test
    void testDoCheckAdditionalFilesToCopy_WithSpacesInMultiple(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckAdditionalFilesToCopy("build.xml,my file.xml,/some path");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals(true, result.getMessage().contains("File paths must not contain spaces"));
        assertEquals(true, result.getMessage().contains("my file.xml"));
        assertEquals(true, result.getMessage().contains("/some path"));
    }

    @Test
    void testDoCheckAdditionalFilesToCopy_WithExtraSpacesAroundCommas(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckAdditionalFilesToCopy("  build.xml  ,  config.xml  ");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    // Tests for targetFolders validator
    @Test
    void testDoCheckTargetFolders_ValidSingleFolder(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckTargetFolders("/tmp");
        // Will be WARNING if /tmp doesn't exist, OK if it does
        assertEquals(true, result.kind == FormValidation.Kind.OK || result.kind == FormValidation.Kind.WARNING);
    }

    @Test
    void testDoCheckTargetFolders_EmptyString(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckTargetFolders("");
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testDoCheckTargetFolders_NullValue(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckTargetFolders(null);
        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    void testDoCheckTargetFolders_WithSpaces(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckTargetFolders("/my folder");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals(true, result.getMessage().contains("Folder paths must not contain spaces"));
        assertEquals(true, result.getMessage().contains("/my folder"));
    }

    @Test
    void testDoCheckTargetFolders_WithSpacesInMultiple(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckTargetFolders("nvr-db:/my db,job-db:/other db");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals(true, result.getMessage().contains("Folder paths must not contain spaces"));
    }

    @Test
    void testDoCheckTargetFolders_MultipleFoldersWithPrefix(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckTargetFolders("nvr-db:/tmp/nvr,job-db:/tmp/job");
        // Will be WARNING if folders don't exist, OK if they do
        assertEquals(true, result.kind == FormValidation.Kind.OK || result.kind == FormValidation.Kind.WARNING);
    }

    @Test
    void testDoCheckTargetFolders_MultipleFoldersWithoutPrefix(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckTargetFolders("/tmp/nvr,/tmp/job");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals(true, result.getMessage().contains("When specifying multiple target folders, all must be prefixed"));
    }

    @Test
    void testDoCheckTargetFolders_MultipleFoldersMixedPrefix(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckTargetFolders("nvr-db:/tmp/nvr,/tmp/job");
        assertEquals(FormValidation.Kind.ERROR, result.kind);
        assertEquals(true, result.getMessage().contains("When specifying multiple target folders, all must be prefixed"));
    }

    @Test
    void testDoCheckTargetFolders_ValidPrefixes(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckTargetFolders("nvr-db:/tmp/nvr,job-db:/tmp/job,out-dir:/tmp/out");
        // Will be WARNING if folders don't exist, OK if they do
        assertEquals(true, result.kind == FormValidation.Kind.OK || result.kind == FormValidation.Kind.WARNING);
    }

    @Test
    void testDoCheckTargetFolders_WithExtraSpacesAroundCommas(JenkinsRule r) {
        FormValidation result = new JenkinsReportJckGlobalConfig().doCheckTargetFolders("  nvr-db:/tmp/nvr  ,  job-db:/tmp/job  ");
        // Will be WARNING if folders don't exist, OK if they do
        assertEquals(true, result.kind == FormValidation.Kind.OK || result.kind == FormValidation.Kind.WARNING);
    }
}

