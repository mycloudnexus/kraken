The Doc is describing the branching and versioning strategy of Kraken.

**Branching Strategy:**

*   **`main`Branch:** This branch represents the integration branch for ongoing development of the *next* major release. It *exclusively* contains snapshot versions.
*   **`release/x.0` Branches:** These branches are created for each major release (x.0). For example: `release/1.0`, `release/2.0`, `release/3.0`.

**Versioning Strategy:**

*   **snapshot Versions:** On `main`, versions follow the pattern `x.0.0-snapshot.n`. Examples: `2.0.0-snapshot.0`, `2.0.0-snapshot.1`.
*   **Release Candidate (RC) Versions:** On the **`release/x.0`** branch, RC versions are tagged: `x.0.0-rc.n` (e.g., `1.0.0-rc.0`, `1.0.0-rc.1`, `2.0.0-rc.0`).
*   **Release Versions:** On the `release/x.0` branch, the final release version is tagged: `x.0.0` (e.g., `1.0.0`, `2.0.0`).
*   **Minor Release Versions:** On the `release/x.0` branch, after the `x.0.0` release, subsequent bug fixes, minor features and improvements result in minor version increments: `x.y.0` (e.g., `1.1.0`, `1.2.0`, `2.1.0`).

**Workflow:**

1.  **Development:** Development happens on local branches, which are merged into `main`.
2.  **snapshot Builds:** Continuous integration (CI) builds on `main` generate snapshot versions (`x.0.0-snapshot.n`).
3.  **Release Branch Creation:** When the features for the next major release development are considered complete (or a specific feature set is targeted), a `release/x.0` branch is created *from the latest commit on `main`*. The creation of the release branch signals the start of the release process for `x.0`.
4.  **Initial RC Version:** Immediately after the `release/x.0` branch is created, it is bumped to the first RC version: `x.0.0-rc.0`.
5.  **Release Branch Stabilization:** Only critical bug fixes and security patches can be merged into the `release/x.0` branch since the initial RC version. Each set of fixes results in a new RC version: `x.0.0-rc.1`, `x.0.0-rc.2`, and so on.
6.  **Final Release:** Once the release branch is stable and testing is complete, the final `x.0.0` version and tag is created on the `release/x.0` branch.
7.  **Post-Release Maintenance (Minor Releases):** After the `x.0.0` release, bug fixes and small features are merged into the `release/x.0` branch, resulting in minor version increments: `x.1.0`, `x.2.0`, etc.
8.  **New Major Release Cycle:** After the `x.0.0` release (or even during the stabilization of the `release/x.0` branch), development of the next major version (`x+1.0.0`) continues on `main` with snapshot versions (`x+1.0.0-snapshot.n`).


**Example Timeline (Major Release 2.0):**

*   `main`: `2.0.0-snapshot.0` -> `2.0.0-snapshot.1` -> ... (Release branch `release/2.0` is created here)
*   `release/2.0`: `2.0.0-rc.0` -> `2.0.0-rc.1` -> `2.0.0` (Release) -> `2.1.0` -> `2.2.0` -> ...
*   `main` (concurrently): `3.0.0-snapshot.0` -> ...

