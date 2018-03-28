String parent_name = "MNTLAB-Valery_Peshchanka-main-build-job"
String super_massive = "MNTLAB-Valery_Peshchanka-child1-build-job, MNTLAB-Valery_Peshchanka-child2-build-job, MNTLAB-Valery_Peshchanka-child3-build-job, MNTLAB-Valery_Peshchanka-child4-build-job"
//creating five jobs
for(int i=0; i<5; i++)
{
  if(i == 0)
  {
    job("$parent_name") {
      description ('Building necessary jobs')
    configure {
    project->
        project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
        parameterDefinitions {
            'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                name 'BRANCHES'
                quoteValue 'false'
                saveJSONParameterToFile 'false'
                visibleItemCount '15'
                type 'PT_SINGLE_SELECT'
                //value "${deployTargets}"
                multiSelectDelimiter ','
                groovy
                projectName "${jobName}"
              	//Alway run tests
                groovyScript """					
def gitURL = "https://github.com/valerapeshchenko/jboss-eap-quickstarts"
def command = "git ls-remote -h \$gitURL"

def proc = command.execute()
proc.waitFor()              

def branches = proc.in.text.readLines().collect { 
    it.replaceAll(/[a-z0-9]*\\trefs\\/heads\\//, '') 
}

return branches
				"""
            }
	'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                name 'child jobs'
                quoteValue 'false'
                saveJSONParameterToFile 'false'
                visibleItemCount '15'
                type 'PT_CHECKBOX'
                value "${super_massive}"
                multiSelectDelimiter ','
                projectName "${jobName}"              	
            }
          }
        }
      }
    }
  }
  else
  {
    String child_name = "MNTLAB-Valery_Peshchanka-child"  + Integer.toString(i) + "-build-job"
    job("$child_name") {
        scm {
            git
                {
                     remote {
                        github("github.com/MNT-Lab/build-principals", "https")
                    }
                     branch("*/vpeshchanka")
                }
        }
        triggers {
            scm('H/1 * * * *')
        }
        steps {
            maven('clean install')
        }
      }
  }
}
