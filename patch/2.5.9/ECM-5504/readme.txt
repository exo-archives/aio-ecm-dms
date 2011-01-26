Summary

    * Status: Some localization bugs when viewing comments of a document
    * CCP Issue: N/A. Product Jira Issue: ECM-5504.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
In File Explorer: there are some localization bugs when viewing comments of a document.
   1. Number of comment(s) is always in plural (in English and French).
   2. Date and time are always in English.
   3. When delete a comment, there's an encoding error in Vietnamese pop-up message.

Fix description

How is the problem fixed?
    * Use text comment(s) for both plural and singular instead of comments
    * Display the date-time relevant with the current selected locale
    * Use JDK tool called native2ascii to convert Vietnamese property file into Unicode text

Patch file: ECM-5504.patch

Tests to perform

Reproduction test
1. Select one document
2. Write comment for this selected document
3. View the comment

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
* Function or ClassName change: no

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM

Support Comment
* Support review: patch validated

QA Feedbacks
*

