// modified value
import com.atlassian.jira.issue.ModifiedValue

// users
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.user.util.UserUtil;

// issue
import com.atlassian.jira.issue.Issue

// logging
import org.apache.log4j.Logger
import org.apache.log4j.Level
  
// custom fields

import com.atlassian.jira.ComponentManager
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.atlassian.jira.issue.util.IssueChangeHolder
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.user.ApplicationUser
// request type
import com.atlassian.servicedesk.api.requesttype.RequestTypeService
import com.atlassian.servicedesk.api.requesttype.RequestType
import com.onresolve.scriptrunner.runner.customisers.PluginModule
import com.onresolve.scriptrunner.runner.customisers.WithPlugin

// set logging
def log = Logger.getLogger("com.acme.CreateSubtask")
log.setLevel(Level.DEBUG)

// construct shortcuts
def userUtil = ComponentAccessor.getUserUtil()
def changeHolder = new DefaultIssueChangeHolder()
def issueManager = ComponentAccessor.getIssueManager()
def issueService = ComponentAccessor.getIssueService()
def userManager = ComponentAccessor.getUserManager()
def issue = event.issue as Issue 
def customFieldManager = ComponentAccessor.getCustomFieldManager()


// customer request
def requestTypeService = RequestTypeService
def currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
def issuePField = customFieldManager.getCustomFieldObjects(issue).find {it.name == "Issue Participants"}
def requestPField = customFieldManager.getCustomFieldObjects(issue).find {it.name == "Request participants"}
def modelPField = customFieldManager.getCustomFieldObjects(issue).find {it.name == "Reviewer/s"}

// test scripts
//log.debug issuePField
log.debug "issuePField= " + issue.getCustomFieldValue(issuePField)
//log.debug requestPField
log.debug "requestPField" + issue.getCustomFieldValue(requestPField)
log.debug "modelPField: " + issue.getCustomFieldValue(modelPField)

def parti=[]

if (issue.getCustomFieldValue(issuePField).toString() != "" || issue.getCustomFieldValue(requestPField).toString() != "" ) {

        MutableIssue mutableIssue = issueManager.getIssueObject(issue.getKey())
		log.debug mutableIssue
    
    for (user in issue.getCustomFieldValue(issuePField)) {
			def userValue = user.toString()
			def username = userValue.substring(0, userValue.indexOf('('))
			log.debug "username = " + username
			parti.add(ComponentAccessor.getUserManager().getUserByName(username))
			//log.debug parti
		}
		for (user in issue.getCustomFieldValue(requestPField)) {
			def userValue = user.toString()
			def username = userValue.substring(userValue.indexOf('(') + 1, userValue.indexOf(')'))
			log.debug "username = " + username
			parti.add(ComponentAccessor.getUserManager().getUserByName(username))
			//log.debug parti
		}

   		log.debug "var parti= " + parti
		requestPField.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(requestPField), parti),changeHolder);
		issuePField.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(issuePField), parti),changeHolder);
		modelPField.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(modelPField), parti),changeHolder);
	}
log.debug "Done"	
