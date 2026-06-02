package io.jenkins.plugins.report.jtreg;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WithJenkins
class JtregPipelineStepTest {

    @Test
    void jtregPublisherCanRunInPipeline(JenkinsRule r) throws Exception {
        WorkflowJob job = r.createProject(WorkflowJob.class, "pipeline-jtreg");
        job.setDefinition(new CpsFlowDefinition(
                "node {\n" +
                "  writeFile file: 'dummy.txt', text: 'hello'\n" +
                "  step([$class: 'JtregReportPublisher', reportFileGlob: 'dummy.txt'])\n" +
                "}",
                true));

        WorkflowRun run = r.buildAndAssertStatus(hudson.model.Result.FAILURE, job);

        assertNotNull(run);
        assertEquals(hudson.model.Result.FAILURE, run.getResult());
    }
}

