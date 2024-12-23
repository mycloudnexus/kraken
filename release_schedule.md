# Main Branch

In main branch it only maintains **snapshot versions** and the first **rc version** of major releases.

A new major version’s first snapshot version will start right after the previous major version’s release branch is made and the main branch typically will bumped into a snapshot version like 2.0.0-snapshot.0, 3.0.0-snapshot.0 .

## 2.0.0

| Version          | Release Content       | Target Release Date  | Actual Release Date |
| -----------------|:---------------------| --------------------:|--------------------:|
| 2.0.0-snapshot.0 | - sonata property type validation<br>- allow user to set value limits for sonata property(BE only)  |      Dec-23-2024     |                     |
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
| 2.0.0-rc.0 | - to be added               |      Mar-17-2024     |                     |

# v1 Release Branch

Each major release(x.0.0) will have a dedicated release branch, the timing to create the release branch is when the first rc version(rc.0) of x.0.0 is created in main branch.
- When a release branch is created, it inherits the latest version number from the main.
- Since version of rc.0, only critical bug fixes and any security issues are added to the release branch going forward until release x.0.0 is made.
- After release of x.0.0 is made in release branch, any bug fixes or minor features added to the release branch increment the minor version.

v1 release branch is maintaining all the releases related with 1.0.0.

| Version          | Release Content       | Target Release Date  | Actual Release Date |
| -----------------|:---------------------| --------------------:|--------------------:|
| 1.0.0 | [1.0.0 release note](https://github.com/mycloudnexus/kraken/releases/tag/v1.0.0)  |    Nov-21-2024       |           Nov-25-2024          |
| 1.1.0 | - seller contact info set up in control plane(BE only)<br>- auto delete api activity logs out of retention period  |      Dec-27-2024     |                     |
