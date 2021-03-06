package com.cprime.subtask.updatecomponentsandteam

import com.atlassian.jira.bc.project.component.ProjectComponent
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.index.IssueIndexingService
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.user.ApplicationUser

Collection<String> fieldsToCopy = ["customfield_13610"] // add more in format "customfield_xxxxx" through comma

/* ========= ALL PARAMS ARE SET ABOVE THIS LINE =========== */

log.setLevel(org.apache.log4j.Level.INFO)
log.debug("Project key: " + event.getProject().getKey().toString())

def cloneComponents(Issue _issue) {
        Collection<ProjectComponent> components = _issue.getParentObject().getComponents()
        IssueManager issueManager = ComponentAccessor.getIssueManager()
        MutableIssue mutableIssue = issueManager.getIssueObject(_issue.getKey())
        ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext()?.getLoggedInUser()
        IssueIndexingService indexManager = ComponentAccessor.getComponent(IssueIndexingService.class)
        log.debug("Updating issue " + mutableIssue.getKey().toString() + "with components " + components.toString())
        mutableIssue.setComponent(components)
        issueManager.updateIssue(currentUser, mutableIssue, EventDispatchOption.DO_NOT_DISPATCH, false)
        indexManager.reIndex(_issue)
}

def cloneCustomFields(Issue _issue, Collection<CustomField> _cfs) {
	if (_cfs.size() < 1) { return }
    IssueManager issueManager = ComponentAccessor.getIssueManager()
    MutableIssue mutableIssue = issueManager.getIssueObject(_issue.getKey())
    ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext()?.getLoggedInUser()
    IssueIndexingService indexManager = ComponentAccessor.getComponent(IssueIndexingService.class)
	_cfs.each() {
        if (_issue.getParentObject().getCustomFieldValue(it)) {
            log.debug("Updating issue " + mutableIssue.getKey().toString() + " custom field " + it.toString() +
                      " with value " + _issue.getParentObject().getCustomFieldValue(it).toString())
            mutableIssue.setCustomFieldValue(it, _issue.getParentObject().getCustomFieldValue(it))
            issueManager.updateIssue(currentUser, mutableIssue, EventDispatchOption.DO_NOT_DISPATCH, false)
            indexManager.reIndex(_issue)            
        }
	}
}

Issue issue = event.getIssue()
log.debug("Event issue: " + issue.toString())

if (issue.isSubTask()) {
        Collection<ProjectComponent> components = issue.getParentObject().getComponents()
        log.debug("Parent components: " + components.toString())
        if (!components.isEmpty()) { cloneComponents(issue) }

        CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager()
		Collection<CustomField> objectsToCopy = new HashSet()
        fieldsToCopy.each() { objectsToCopy.add(customFieldManager.getCustomFieldObject(it)) }
        cloneCustomFields(issue, objectsToCopy)
} else {
        Collection<Issue> subTasks = issue.getSubTaskObjects()
        log.debug("My sub-tasks: " + subTasks.toString())
        if (subTasks.isEmpty()) { return }

        Collection<ProjectComponent> components = issue.getComponents()
        log.debug("My components: " + components.toString())
        if (!components.isEmpty()) { subTasks.each() { cloneComponents(it) } }

		CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager()
		Collection<CustomField> objectsToCopy = new HashSet()
		subTasks.each() {
			fieldsToCopy.each() { objectsToCopy.add(customFieldManager.getCustomFieldObject(it)) }
			log.debug("Updating issue " + it.getKey() + " with " + objectsToCopy.toString())
			cloneCustomFields(it, objectsToCopy)
		}		
}
