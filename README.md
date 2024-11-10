# imporTagger for XRay

![example workflow](https://github.com/eliaspardo/imporTagger/actions/workflows/build-gradle-project.yml/badge.svg)
![example workflow](https://github.com/eliaspardo/imporTagger/actions/workflows/package-msi.yml/badge.svg)
![example workflow](https://github.com/eliaspardo/imporTagger/actions/workflows/package-dmg.yml/badge.svg)
![example workflow](https://github.com/eliaspardo/imporTagger/actions/workflows/package-deb.yml/badge.svg)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Why you're here

If you're tired of manually logging into XRay, importing feature files, retrieving test and precondition IDs, and tagging each one by hand—only to repeat the process for every file—you've come to the right place!
## About
imporTagger for XRay was created to eliminate the frustration of manually importing Gherkin Feature Files into XRay (XRay) and tagging them afterward. 

This tool provides a simple and efficient solution through a graphical user interface (GUI), allowing you to:

- Log in effortlessly
- Select and import multiple feature files
- Automatically tag test cases and preconditions with the appropriate IDs

All of this is done seamlessly, with support for Windows, macOS, and Linux.
## Installation
You’ll find installers for Windows, macOS, and Linux in the repository. Just download the appropriate version for your system and follow the installation steps. 

Note that these bundles are unsigned, so you may encounter security warnings during installation.

**MacOS** -  To run the app, execute the following command in the installation folder: ```xattr -cr Xray\ Importer.app/```

This application was developed primarily on Windows. While basic functional tests have been performed on other platforms, issues may arise. If you encounter any, please raise a ticket!

## How To Use 
1. Set your project defaults in the configuration file located at: ```{installation_path}\app\resources\default.properties```
2. Launch the app and log in with your XRay client ID and client secret. 
3. Select the feature files you want to import and provide a Test Info file with details about the components to be set. 
4. The tool will automatically import the test cases into XRay (if they don't already exist) and tag the related tests and preconditions.

The UI is intuitive and should be easy to navigate, even for first-time users.

### FAQ
- **How do I create an XRay API Key?**
Follow the instructions in https://docs.getxray.app/display/XRAYCLOUD/Global+Settings%3A+API+Keys#GlobalSettings:APIKeys-CreateanAPIKey

- **Where can I find sample files?** Test Feature Files and Test Info files are available in the src/test/resources directory.

## Collaborate
I'm not a full-time developer and have limited time, so there are many opportunities for improvement and refactoring. If you're interested in contributing, feel free to reach out to me at eliaspardo@gmail.com. All contributions are welcome!
