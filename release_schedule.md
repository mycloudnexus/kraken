# Main Branch Versions

In main branch it only maintains **snapshot versions** of major releases.

A new major version’s first snapshot version will start right after the previous major version’s release branch is made and the main branch typically will bumped into a snapshot version like 2.0.0-snapshot.0, 3.0.0-snapshot.0 etc.

## 2.0.0 release schedule in Main branch

| Version          | Release Content       | Target Release Date  | Actual Release Date |
| -----------------|:---------------------| --------------------:|--------------------:|
| 2.0.0-snapshot.0 | - sonata property type validation<br>- allow user to set value limits for sonata property(BE only)  |      Dec-23-2024     |         Dec-26-2024            |
| 2.0.0-snapshot.1 | - seller contact info set up in control plane(BE only)<br>- auto delete api activity logs out of retention period |      Dec-30-2024     |                     |
| 2.0.0-snapshot.2 | - to be added               |      Jan-06-2024     |                     |
| 2.0.0-snapshot.3 | - to be added               |      Jan-13-2024     |                     |
| 2.0.0-snapshot.4 | - to be added               |      Jan-20-2024     |                     |
| 2.0.0-snapshot.5 | - to be added               |      Jan-27-2024     |                     |
| 2.0.0-snapshot.6 | - to be added               |      Feb-10-2024     |                     |
| 2.0.0-snapshot.7 | - to be added               |      Feb-17-2024     |                     |
| 2.0.0-snapshot.8 | - to be added               |      Feb-24-2024     |                     |
| 2.0.0-snapshot.9 | - to be added               |      Mar-03-2024     |                     |
| 2.0.0-snapshot.10 | - to be added               |      Mar-10-2024     |                     |
| 2.0.0-snapshot.11 | - to be added               |      Mar-17-2024     |                     |

# Release Branch Versions

Each major release(x.0.0) will have a dedicated release branch, the timing to create the release branch for x.0.0 is when the last snapshot release of it is created in main branch.
- The release branch of x.0.0 will be named with v{{x}}, e.g. v1, v2, v3 .....
- After a release branch is created, it will be bumped into the rc.0 version of x.0.0, e.g. 1.0.0-rc.0.
- Since version of rc.0, only critical bug fixes and any security issues are added to the release branch going forward until release x.0.0 is made.
- After release of x.0.0 is made in release branch, any further bug fixes or minor features added to the release branch will increment the minor version which will be carefully planned depending customer issues.


## Release Schedule in v1 Release Branch

v1 release branch is maintaining all the releases related with 1.0.0.

| Version          | Release Content       | Target Release Date  | Actual Release Date |
| -----------------|:---------------------| --------------------:|--------------------:|
| 1.0.0 | [1.0.0 release note](https://github.com/mycloudnexus/kraken/releases/tag/v1.0.0)  |    Nov-21-2024       |           Nov-25-2024          |
| 1.1.0 | - seller contact info set up in control plane(BE only)<br>- auto delete api activity logs out of retention period  |      Dec-27-2024     |                     |
