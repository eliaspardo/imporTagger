# Feature File Importer for XRay

## Why are you here?

Tired of having to log into XRay, import the feature files, get back the test and precondition ids, having to manually tag all of them... and then repeat the process for every single feature file you've written??

## About
The Feature File Importer for XRay is born from the excruciating pain that comes from importing Feature Files to XRay (https://www.getxray.app/) and tagging them afterwards.
This tool aims to relieve that pain by effortlessly logging in, selecting some files and having them tagged automatically, all through a GUI and supporting all OSs.

## Installation
There are installers for Windows, Mac and Linux in the repo. Follow through to complete installation. Note these bundles are not signed - security alerts are expected.

This app has been developed using Windows, basic functional tests have been run on other platforms, but issues might appear. Please raise a ticket if you experience anything unexpected.

**MacOS:** to run the app the following command must be run: ```xattr -cr Xray\ Importer.app/```

## How To Use 
Configure your project defaults in: ```{installation_path}\app\resources\default.properties```

The tool's UI usage is pretty much self-explanatory. 

Run the app, log into XRay using your client ID and your client secret, select the feature files you want to import and a test info file with the details about the components to be set.

The tool will automatically import those test cases to XRay (if not there already) and tag tests and preconditions.

### FAQ
- On how to create an XRay API Key - https://docs.getxray.app/display/XRAYCLOUD/Global+Settings%3A+API+Keys#GlobalSettings:APIKeys-CreateanAPIKey
- Some test Feature Files and Test Info files can be found in src/test/resources

## Collaborate
I'm no developer and pretty short on time, so there's plenty of improvements and refactors to be done. If you're interested in collaborating, contact me at eliaspardo@gmail.com