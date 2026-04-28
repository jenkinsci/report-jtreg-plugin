package io.jenkins.plugins.report.jtreg.recreate;


public interface PrefixableResult {

   boolean isResultsArchive(String s);
   String getPrefix() ;

   }
