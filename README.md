# Browser History Histogram for Autopsy

Ingest module for the Autopsy Forensic Analysis platform, developed in JAVA, within the scope of the Computer Science 
Degree of the 'Escola Superior de Tecnologia e Gestão do Instituto Politécnico de Leiria', Portugal

The main goal of this module is to extract all webactivity of a user to a local database and then generate a report to display this information.
This module runs on windows and linux and extract information of Google Chrome and Firefox. You can run it as a [Autopsy](https://www.autopsy.com/)
module or without any dependencies.

## Getting Started

### Prerequisites
* JDK 8 (mandatory)
* [Autopsy](https://www.autopsy.com/) (Optional - to run it  as a autopsy module) 
* Ant, ivy (Optional - if you want to contribute)

### Installing
You have 2 options to use this module:
* As a [Autopsy](https://www.autopsy.com/)  module
    1. Download [Labcif-bhh-autopsy.nbm](https://github.com/labcif/BHH/releases/download/1.0.0/Labcif-bhh-autopsy.nbm) 
    2. Tools - Plugins - Downloaded - Add Plugins... 
    3. Select Labcif-bhh-autopsy.nbm downloaded previously
    4. Click Install - Next - Check terms acceptance - Install - Continue - Finish
    
    
* As a standalone application
    1. Download [Browser-History-Histogram.jar](https://github.com/labcif/BHH/releases/download/1.0.0/Browser-History-Histogram.jar)

### Run
* As a [Autopsy](https://www.autopsy.com/)  module
    1. Running the ingest module will create a database browser-history.db in the same directory as the case opened
        1. Tools - Run Ingest Modules - "Select image" Ex: PC01.EO1
        2. Select Labcif - Browser History Histogram
        3. Finish
    2. After running the ingest module, you will be able to run the report Module. 
        1. Generate Report
        2. Select Labcif - Browser History Histogram
        3. Finish
        
* As a standalone application
    1. Double click on Browser-History-Histogram.jar
    2. Choose a directory
    3. Extract (it will create browser-history.db)
    4. Generate report

## Authors

* **Kevin Baptista**
* **Tomás Honório**
* Work developed under the guidance and coordination of Professors **Patrício Domingues** and **Miguel Frade**


