package hudson.plugins.report.jck;


import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Logger;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;

@Extension
public class JenkinsReportJckGlobalConfig extends GlobalConfiguration {
    private static Logger logger = Logger.getLogger(JenkinsReportJckGlobalConfig.class.getName());

    String diffToolUrl;

    public static JenkinsReportJckGlobalConfig getInstance() {
        return GlobalConfiguration.all().get(JenkinsReportJckGlobalConfig.class);
    }

    public boolean isDiffToolUrlSet() {
        return diffToolUrl != null && !diffToolUrl.trim().isEmpty();
    }

    public String getDiffToolUrl() {
        return diffToolUrl;
    }

    @DataBoundSetter
    public void setDiffToolUrl(String diffToolUrl) {
        this.diffToolUrl = diffToolUrl;
    }

    @DataBoundConstructor
    public JenkinsReportJckGlobalConfig(String diffToolUrl) {
        this.diffToolUrl = diffToolUrl;
    }

    public JenkinsReportJckGlobalConfig() {
        load();
    }


    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        return super.configure(req, json);
    }
}
