# tm4j JIRA utility
This utility is basically intergartion of tm4j with cucumber based framework.
You can push your automted test cases into tm4j directly using this utility and there is no need to write test cases manually.

#Configuration required to use this tm4j utilty are as below
1. use this pluggin into your test runner file - tm4j.JiraStatusReporter
2. Keep attached jira.properties file into your src/test/resource folder
3. Clone this project and convert it into jar file and use that jar to integrate your automation suit with tm4j

#jira.properties looks like
pushTestCasesIntoJira=true
projectId=int
epicName=String
storyName=String
author=String
priority=String
status=String
labels=String
assignee=String
reporter=String
userName=String
userPassword=String
