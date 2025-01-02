Every development related task in Kraken project will be tracked by using a Github issue.

**1. Key Properties**

There are 3 key properties that contribute to effective GitHub issue management :

* **Labels:**
    * **Category:** A single-select label classifying the issue type:
        * Bug
        * New Feature
        * Improvement
        * Documentation
        * Technical Design
    * **Release Branch Candidate:** (Optional) A label indicating the specific release branch (e.g., "1.1.0-candidate", "1.2.0-candidate") if the issue will be also targeted for a particular release branch beside main branch.
    * **"Merge Back to Main"**: This label is applied to issues fixed within a release branch first to ensure the fix is eventually incorporated into the main codebase later.

* **Projects:** indicates if the issue has been scheduled into a sprint or iteration.

* **Milestone:** Specifies the target release version for the issue (e.g., "2.0.0-snaphot.2", "1.0.0").

**2. Workflow**

* **New Issue:**
    * Reporter creates a new issue.
    * Reporter selects the appropriate **Category** label.
    * Reporter optionally selects a **Project** and **Milestone** if known.

* **PO Review:**
    * Product Owner (PO) reviews the issue.
    * PO verifies and corrects the **Category** label, **Project**, and **Milestone** as needed.
    * PO adds the **Release Branch Candidate** label if applicable.

* **Development:**
    * Developer is assigned to the issue.
    * Developer creates a Pull Request (PR) and links the issue to the PR.

* **QA:**
    * QA tests the changes in the PR.
    * QA adds comments to the issue.
    * QA moves the issue to a "Ready for Release" status by using project status upon successful testing.

