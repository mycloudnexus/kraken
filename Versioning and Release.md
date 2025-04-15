

## Introduction 

This document provides details about kraken versioing and release strategy.

## Versioning

Here's a common versioning strategy that aligns well with this approach:

### Semantic Versioning: MAJOR.MINOR.PATCH

Major Version: Indicates significant changes.   

Minor Version: Indicates enhancements, critical bug fix and security issues.   

Patch Version:  no Patch release for Kraken, so it is always 0.   


### Extra Versioning:

- snapshot version : 
  - x.0.0-snapshot.x represents x.0.0 under development and the code is unstable 
  - snapshot version will start from snapshot.0

- release candidate version
  - Release candidate marks a feature freeze of the upcoming release.
  - Release candidate version will start from rc.0.
 
### Branch Management

Kraken will maintain release branches in addition to the main branch. 
End of maintenance for a branch will be announced. 
It is recommended to use the latest stable branch available.

### Versioning at Main branch:

- In main branch it only maintains **snapshot versions** and the first **rc version** of major releases.
- A new major version’s first snapshot version will start right after the previous major version’s stable branch is made and the main branch typically will bumped into a snapshot version like 2.0.0-snapshot.0, 3.0.0-snapshot.0 .
  - 1.0.0-snapshot.0, 1.0.0-snapshot.1, ……., 1.0.0-rc.0, 2.0.0-snapshot.0, 2.0.0-snapshot.1 …….

### Versioning at Release Branches:

- a new release branch for x.0.0 will be created based on x.0.0-rc.0 in main.
- When a release branch is created, it inherits the latest version number from the main.
  - For example, if the mian is at 1.0.0-rc.0, the release branch would start at 1.0.0-rc.0.
- Only bug fixes and any security issues are added to the release branch going forward until release x.0.0 is made.
- After release x.0.0 is made in release branch, any bug fixes or minor features added to the release branch increment the minorversion.
  - For example, for release branch of 1.0.0, if 1.0.0 is released, later when a bug fix or new feature are applied, it becomes 1.1.0.
  - minor release or patch release on a stable branch should be always backward compatible

### Tagging Releases:

Once a major release criteria is met, a tag is created on the release branch with the final version number and a release will be created based on the tag. 
This tag serves as a permanent marker for that specific release.   


## Release Strategy

Any new features added since the last release will be available in the next minor
or major release. These will include bug fixes as well. 

Major release will be running on 3 months cadence.
Minor release will be depending on demand.
