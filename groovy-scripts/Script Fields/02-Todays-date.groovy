import com.atlassian.jira.issue.Issue

def totalSubTasks = issue.getSubTaskObjects().size()
if (totalSubTasks > 0){
 def completedSubTasks = issue.getSubTaskObjects().findAll{(Issue) it.getResolution() != null}.size()
 def result = (completedSubTasks*100) / totalSubTasks
 return (long) result
}
return null
