def cf = customFieldManager.getCustomFieldObjects(issue).find {it.name == 'Email'}
issueInputParameters.addCustomFieldValue(cf.id, '')
