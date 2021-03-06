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
def customerField = customFieldManager.getCustomFieldObjects(issue).find {it.name == "Customer Request Type"}
def participantField = customFieldManager.getCustomFieldObjects(issue).find {it.name == "Request participants"}


// test scripts
log.debug customerField
log.debug issue.getCustomFieldValue(customerField)
log.debug issue.getCustomFieldValue(customerField).toString()

def parti=[]
if (issue.getCustomFieldValue(customerField).toString() == "hqit/c3c42cac-c5b6-4105-a989-df39111a885a" || issue.getCustomFieldValue(customerField).toString() == "hqit/a2e109d7-1d51-4ba2-a7ef-bd1510889071" || issue.getCustomFieldValue(customerField).toString() == "hqit/4ef3368d-4db0-46f8-99c6-8bada8b9142b"){  // custom request type = Internet Issues (wired connection) , VPN Issues , Wifi Issues
		for (user in issue.getCustomFieldValue(participantField)) {
			def userValue = user.toString()
			def username = userValue.substring(userValue.indexOf('(') + 1, userValue.indexOf(')'))
			log.debug "username = " + username
			parti.add(ComponentAccessor.getUserManager().getUserByName(username))
		}
		parti.add(ComponentAccessor.getUserManager().getUserByName("jonathan.obiniana"))
		parti.add(ComponentAccessor.getUserManager().getUserByName("jerome.verendia"))
   		log.debug parti
		participantField.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(participantField), parti),changeHolder);
	}
