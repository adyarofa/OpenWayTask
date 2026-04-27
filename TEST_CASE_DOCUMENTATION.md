# Test Case Documentation

## 1. Components of a Test Case

A well-constructed test case is the foundation of effective automated testing. Every test case should include the following components:

### 1.1 Key Components

| Component | Description | Example |
|-----------|-------------|---------|
| **Test Case ID** | Unique identifier for the test | TC-LOGIN-001 |
| **Test Title** | Clear, descriptive name of what is being tested | "Login with valid credentials" |
| **Objective/Description** | What the test case aims to verify | "Verify that a user can successfully login with correct email and password" |
| **Preconditions** | Initial state/setup required before running the test | • Gmail account exists • Internet connection available • Browser installed |
| **Test Steps** | Sequential, detailed steps to execute the test | 1. Open browser 2. Navigate to Gmail 3. Enter email... |
| **Expected Result** | What should happen if the test passes | "Inbox page loads, unread emails visible" |
| **Test Data** | Input values needed for the test | Email: test@gmail.com, Password: ******* |
| **Postconditions** | State after test completion | "User is logged in to Gmail inbox" |
| **Priority** | Relative importance (Critical, High, Medium, Low) | Critical |
| **Automation Status** | Whether test is automated or manual | Automated |

### 1.2 Test Case Attributes

- **Positive Test Case**: Verifies that the system works correctly with valid inputs
- **Negative Test Case**: Verifies that the system handles invalid inputs gracefully
- **Boundary Test Case**: Tests edge cases and limits (e.g., max email size)
- **Data-Driven Test Case**: Uses multiple sets of test data in a parameterized way

---

## 2. Example: Well-Constructed Test Cases for Gmail Mail Deletion

### TC-DEL-001: Delete Single Email from Inbox

| Field | Value |
|-------|-------|
| **Test Case ID** | TC-DEL-001 |
| **Title** | Delete a single email from Inbox |
| **Type** | Positive Test Case - Functional |
| **Priority** | Critical |
| **Objective** | Verify that a user can delete a single email from the inbox and that the email is removed from the list |
| **Preconditions** | • User is logged into Gmail <br/> • Inbox contains at least one email <br/> • Email is in Inbox (not Trash or other folder) |
| **Test Steps** | 1. Navigate to Gmail Inbox<br/>2. Identify the first email in the list<br/>3. Select the email checkbox<br/>4. Click the Delete button (trash icon)<br/>5. Verify "Undo" notification appears<br/>6. Refresh the Inbox<br/>7. Verify email count decreased by 1 |
| **Expected Result** | • Email is removed from Inbox<br/>• Undo notification appears for 5 seconds<br/>• Email count decreases by exactly 1<br/>• User remains on Inbox page |
| **Postconditions** | • Inbox contains one less email<br/>• Email is moved to Trash folder |
| **Test Data** | None - uses existing emails |
| **Automation Status** | ✅ Automated |

---

### TC-DEL-002: Bulk Delete Multiple Emails

| Field | Value |
|-------|-------|
| **Test Case ID** | TC-DEL-002 |
| **Title** | Delete multiple emails via bulk selection |
| **Type** | Positive Test Case - Functional |
| **Priority** | High |
| **Objective** | Verify that a user can select and delete multiple emails at once |
| **Preconditions** | • User is logged into Gmail<br/>• Inbox contains at least 3 emails |
| **Test Steps** | 1. Navigate to Gmail Inbox<br/>2. Select checkbox for first email<br/>3. Select checkbox for second email<br/>4. Select checkbox for third email<br/>5. Click Delete button<br/>6. Verify notification shows correct number deleted<br/>7. Refresh and verify count decreased by 3 |
| **Expected Result** | • All 3 emails are removed<br/>• Notification shows "3 conversations deleted"<br/>• Undo link is available<br/>• Email count decreases by 3 |
| **Postconditions** | • 3 emails moved to Trash<br/>• Inbox has 3 fewer emails |
| **Test Data** | None - uses existing emails |
| **Automation Status** | ✅ Automated |

---

### TC-DEL-003: Delete Email from Email View

| Field | Value |
|-------|-------|
| **Test Case ID** | TC-DEL-003 |
| **Title** | Delete email while viewing its content |
| **Type** | Positive Test Case - Functional |
| **Priority** | High |
| **Objective** | Verify user can delete an email from the email detail view (not the inbox list) |
| **Preconditions** | • User is logged into Gmail<br/>• Inbox contains at least 1 email |
| **Test Steps** | 1. Navigate to Inbox<br/>2. Click on first email to open it<br/>3. Wait for email content to load<br/>4. Click Delete button (in email header)<br/>5. Verify redirected back to Inbox<br/>6. Verify email count decreased<br/>7. Verify email not visible in Inbox |
| **Expected Result** | • Email detail view closes<br/>• User returns to Inbox<br/>• Deleted email no longer appears in list<br/>• Undo notification appears |
| **Postconditions** | • Email moved to Trash<br/>• User is viewing Inbox |
| **Test Data** | None - uses existing email |
| **Automation Status** | ✅ Automated |

---

### TC-DEL-004: Verify Undo Functionality

| Field | Value |
|-------|-------|
| **Test Case ID** | TC-DEL-004 |
| **Title** | Undo email deletion restores email to Inbox |
| **Type** | Positive Test Case - Functional |
| **Priority** | High |
| **Objective** | Verify that clicking "Undo" after deletion restores the email to Inbox |
| **Preconditions** | • User is logged into Gmail<br/>• Inbox contains at least 1 email |
| **Test Steps** | 1. Record initial email count<br/>2. Select an email<br/>3. Delete the email<br/>4. Verify "Undo" link appears in notification (within 5 seconds)<br/>5. Click the "Undo" link<br/>6. Verify email count returns to initial count<br/>7. Verify email is back in Inbox |
| **Expected Result** | • Email is restored to Inbox<br/>• Email count returns to original value<br/>• Email is visible in email list |
| **Postconditions** | • Email is in Inbox (not Trash)<br/>• All emails restored to original state |
| **Test Data** | None - uses existing email |
| **Automation Status** | ⚠️ Manual (time-dependent) |

---

### TC-DEL-005: Delete Email with Special Characters in Subject

| Field | Value |
|-------|-------|
| **Test Case ID** | TC-DEL-005 |
| **Title** | Delete email with special characters in subject |
| **Type** | Positive Test Case - Edge Case |
| **Priority** | Medium |
| **Objective** | Verify that emails with special characters (émojis, unicode, etc.) can be deleted without errors |
| **Preconditions** | • User is logged into Gmail<br/>• Inbox contains email with special characters in subject (e.g., "Meeting 📅 2025")<br/>• User can identify the email |
| **Test Steps** | 1. Navigate to Inbox<br/>2. Find email with special characters in subject<br/>3. Select the email<br/>4. Delete it<br/>5. Verify deletion succeeds<br/>6. Verify email appears in Trash |
| **Expected Result** | • Email is deleted successfully<br/>• No encoding errors occur<br/>• Email appears in Trash folder with subject intact |
| **Postconditions** | • Email in Trash folder<br/>• Subject remains readable with special characters preserved |
| **Test Data** | Email subject with émojis or unicode |
| **Automation Status** | ⚠️ Manual (requires email with special characters) |

---

### TC-DEL-006: Delete Email from Search Results

| Field | Value |
|-------|-------|
| **Test Case ID** | TC-DEL-006 |
| **Title** | Delete email from search results |
| **Type** | Positive Test Case - Functional |
| **Priority** | Medium |
| **Objective** | Verify that emails can be deleted from search results view |
| **Preconditions** | • User is logged into Gmail<br/>• Inbox contains multiple emails<br/>• Search is accessible |
| **Test Steps** | 1. Search for emails using a keyword<br/>2. Verify search results appear<br/>3. Select an email from results<br/>4. Delete it<br/>5. Verify email disappears from search results<br/>6. Navigate to Inbox and verify email deleted |
| **Expected Result** | • Email disappears from search results<br/>• Email no longer in Inbox<br/>• Email moved to Trash |
| **Postconditions** | • Email in Trash<br/>• Search results updated |
| **Test Data** | Search keyword that matches emails in Inbox |
| **Automation Status** | ⚠️ Manual |

---

### TC-DEL-007: Cannot Delete Email from Trash Folder (Already Deleted)

| Field | Value |
|-------|-------|
| **Test Case ID** | TC-DEL-007 |
| **Title** | Verify email in Trash has "Delete permanently" option, not "Delete" |
| **Type** | Positive Test Case - Functional |
| **Priority** | Medium |
| **Objective** | Verify that once an email is in Trash, the option changes to "Delete permanently" (not "Delete") |
| **Preconditions** | • User is logged into Gmail<br/>• At least one email exists in Trash folder |
| **Test Steps** | 1. Navigate to Trash folder<br/>2. Select an email<br/>3. Check the toolbar button<br/>4. Verify button says "Delete permanently" (or similar)<br/>5. Do not click it |
| **Expected Result** | • Button label is "Delete permanently"<br/>• Tooltip indicates permanent deletion<br/>• Undo option will not be available |
| **Postconditions** | • No changes to Trash contents |
| **Test Data** | None |
| **Automation Status** | ⚠️ Manual |

---

### TC-DEL-008: Negative Test - Try to Delete Without Selecting

| Field | Value |
|-------|-------|
| **Test Case ID** | TC-DEL-008 |
| **Title** | Verify delete button is disabled when no email is selected |
| **Type** | Negative Test Case - Error Handling |
| **Priority** | Medium |
| **Objective** | Verify that the Delete button is disabled/inactive when no email is selected (preventing accidental deletion) |
| **Preconditions** | • User is logged into Gmail<br/>• Inbox contains emails<br/>• No emails are currently selected |
| **Test Steps** | 1. Navigate to Inbox<br/>2. Do not select any email<br/>3. Look at the toolbar<br/>4. Check if Delete button is visible/enabled<br/>5. Try to click Delete button |
| **Expected Result** | • Delete button is grayed out/disabled<br/>• Clicking does nothing<br/>• No error message appears |
| **Postconditions** | • No email deleted<br/>• Inbox unchanged |
| **Test Data** | None |
| **Automation Status** | ⚠️ Manual |

---

### TC-DEL-009: Auto-Delete Emails Older Than 30 Days in Trash

| Field | Value |
|-------|-------|
| **Test Case ID** | TC-DEL-009 |
| **Title** | Verify Gmail auto-deletes emails from Trash after 30 days |
| **Type** | Positive Test Case - Business Logic |
| **Priority** | Low |
| **Objective** | Verify that Gmail automatically and permanently deletes emails from Trash after 30 days |
| **Preconditions** | • Email has been in Trash for exactly 30 days<br/>• User has access to Gmail settings/logs |
| **Test Steps** | 1. Verify email was moved to Trash 30 days ago<br/>2. Check Trash folder<br/>3. Verify email is no longer present<br/>4. Check Gmail backup/recovery options (if available)<br/>5. Confirm email cannot be recovered |
| **Expected Result** | • Email is permanently deleted after 30 days<br/>• Email cannot be restored |
| **Postconditions** | • Email no longer accessible<br/>• Storage freed up |
| **Test Data** | Email timestamp from 30 days ago |
| **Automation Status** | ❌ Not Automatable (time-dependent, long duration) |

---

## 3. Best Practices for Writing Test Cases

### 3.1 Principles
1. **Clear and Concise**: Use simple language, avoid ambiguity
2. **Independent**: Each test should run standalone without depending on other tests
3. **Repeatable**: Results should be consistent across multiple runs
4. **Deterministic**: No random waits or unpredictable element behavior
5. **Focused**: Test one thing at a time (single responsibility)

### 3.2 Common Mistakes to Avoid
- ❌ Vague step descriptions ("Click something")
- ❌ Over-reliance on hard-coded waits (use proper waits instead)
- ❌ Tests that depend on execution order
- ❌ Missing preconditions or postconditions
- ❌ No assertion or expected result
- ❌ Using UI element IDs that change frequently (use stable locators)
- ❌ Not logging test execution details

---

## Summary

A well-structured test case is the backbone of any automation framework. By following the components and best practices outlined above, you ensure that:
- Tests are maintainable and easy to understand
- Issues can be easily reproduced
- Test results are reliable and repeatable
- New team members can quickly understand test intent
- Bugs in the application are caught early and consistently
