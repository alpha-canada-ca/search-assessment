# Search Assessment Tool

# Overview 
The Search Assessment Tool simply analyzes and assesses the performance of the existing search services (the CRA contextual and global search for now). This back-end assessment tool was built to include other government departments in the future. 
This tool fetches the top 100 search terms and the expected result of each from Airtable (3rd party SaaS) using its API, and checks how these search terms perform with respect to the their target URLs. Finally, it provides a score for the the search page in question. 

Assessments only run once, and the results are archived in another Airtable base (a table/spreadsheet).
This tool was meant to automatically get the top 100 search terms from Adobe Analytics and populate them in Airtable, but no access was allowed to Adobe Analytics as of this writing, and the workflow may change as a result. For this reason, this tool is still a **work in progress**.

This is a back-end service that exposes its services through API endpoints using Rest. The UI was built in a separate project using Angular.


# Getting Started
This is a standalone Java application that has an embedded server. It doesn't require a container such as Tomcat, jetty, etc. It was built using a simple lightweight framework called Dropwizard. As a result, running this application is very simple.
### 1.	Installation process
After the code is cloned from its repository, it can be ran as a Java application.
To run the jar file, you need to pass the following arguments: `server configuration.yml`
### 2.	Software dependencies
All the dependencies are in the pom file.
### 3.	Latest releases
This is still in alpha and hasn't been deployed to production.
### 4.	API references
`/lastUpdate` to get the latest update date available

**Required parameters:** 

*source* ('cra' is the only value supported for now)

`/analyze` to analyze and assess the top 100 search terms (will take up to 10 minutes), or to get the latest assessment available from the archive table (the first run will archive the assessments and the subsequent runs will fetch the assessment from the archive table)

**Required parameters:** 

*source* ('cra' is the only value supported for now)

*isContextual* ('true' for the contextual CRA search, 'false' for the global search)

*lang* ('fr' for French search terms and 'en' [default] for English search terms)

---

As of this writing, the application is deployed in the Azure alpha environment:

dev: `https://cra-alpha-search-analysis-dev.azurewebsites.net/analyze?source=cra&isContextual=false&lang=fr`

stage: `https://cra-alpha-search-analysis.azurewebsites.net/analyze?source=cra&isContextual=false&lang=fr`



# Build and Deploy
To build the jar file for deployment purposes, just run maven install.
An uber jar will be built with all the dependencies.
After that, a Docker container can be built for deployment purposes.

To build and deploy on Azure, use the following:

### Update Docker Container
`az acr build --registry CRAsearch --subscription Pay-As-You-Go (cra-arc.alpha.canada.ca) --image search-assessment .`
(latest - used for dev)

`az acr build --registry CRAsearch --subscription Pay-As-You-Go (cra-arc.alpha.canada.ca) --image search-assessment:stage .` 
(stage)

Using the Azure console, you can deploy the changes if "App Service" is not configured to do so automatically.