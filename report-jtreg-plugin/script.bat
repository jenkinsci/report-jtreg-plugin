::
:: Why is this here?
:: After refactoring the codebase of this plugin into multiple modules, the report-jtreg-lib remained as a dependency
:: to the report-jtreg-plugin module (the Jenkins plugin itself). However, because of the Jenkins security hardening
:: (https://www.jenkins.io/blog/2018/03/15/jep-200-lts/), the plugin can't load classes from external modules and it
:: throws an class filter exception. After trying many different solutions to the problem, this seemed like the only
:: doable workaround.
::
:: What does this do?
:: This script is run in the compile phase of the report-jtreg-plugin module (the report-jtreg-lib module is already
:: compiled) and it copies the target folder of the lib module to the target folder of the plugin module. Since the
:: report-jtreg-lib dependency has the "provided" scope, the classes do not clash and the plugin works as intended with
:: the lib classes in the plugin jar/hpi.
::

xcopy /E /Y ..\report-jtreg-lib\target\* target\
