package io.jenkins.plugins.report.jtreg.recreate;

import java.util.List;

import io.jenkins.plugins.report.jtreg.writers.WriterKinds;

//fixme mayb enot needed and args can be reused as they are> It takes string[]....Recreate it?
public interface ExportArgs{

    public String getUrl() ;
    public String getOut() ;
    public String getNvrDb();
    public String getJobDb() ;
    public String getArchivesRegex();
    public List<String> getAdditionalFiles();
    public List<WriterKinds> getKinds();
    public boolean isNoRestore();


}
