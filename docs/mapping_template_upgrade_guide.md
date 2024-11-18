# Mapping template upgrade guide

## Workflow of mapping template release

![image](img/mapping-template-workflow.png)

### Development phase

* Allow multiple versions in development phase.
* Each development version only records the functional changes compared to the previous development version.

### Release phase

* create the the final release version.
* Merge all change notes (since the last release) into the final release.
* Remove all development and test versions in directory of mef-sonata/template-upgrade.


## When to add a new mapping template release

* Add Api Use Case
* Modify existed Api Use Case
  * Modify user mapping template
  * Modify mapping target file
  * Modify Api orchestration file
  * Modify APi request validation rule file
* Other changes
  * Add/Modify  api spec file
  * Add/Modify open api spec of supported product

## How to create a mapping template release

1. Create a mapping teplate release file(e.g. release.1.5.13.yaml)  under directory of mef-sonata/template-upgrade in the module of kraken-java-sdk-mef.

   Here show an example of mapping tempalte relase

   ```yaml
   ---
   kind: kraken.product.template-upgrade
   apiVersion: v1
   metadata:
     key: kraken.product.template-upgrade.1.5.13
     name: V1.5.13
     labels:
       productSpec: grace
       productVersion: V1.5.13
       publishDate: 2024-11-15
     description: |
       Make some improvements to order template:
         add itemTerm in add order template;
         add expectedCompeletionDate in get order template;
     version: 1
   ```
   **Important fields explanation**

   * productSpec: Standard version of one product.Such as grace,haley in LSO SONATA.
   * productVersion: Release version of mapping template release.
   * publishDate: Release date of mapping template.
   * description: Description of all changes in the current release.
2. Add path of the release file to mef-sonata/product.yaml

   ```yaml
   spec:
     componentPaths:
       - classpath:/mef-sonata/template-upgrade/release.1.5.13.yaml
   ```
