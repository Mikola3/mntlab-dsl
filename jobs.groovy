String parent_name = "MNTLAB-Valery_Peshchanka-main-build-job"
String super_massive = "MNTLAB-Valery_Peshchanka-child1-build-job, MNTLAB-Valery_Peshchanka-child2-build-job, MNTLAB-Valery_Peshchanka-child3-build-job, MNTLAB-Valery_Peshchanka-child4-build-job"
String args_main_job = "master, vpeshchanka"
//creating five jobs
for(int i=0; i<5; i++)
{
  if(i == 0)
  {
    job("$parent_name") {
      label("EPBYMINW6593")
      description ('Building necessary jobs')
            wrappers {
        preBuildCleanup()
    }
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
                value "${args_main_job}"
                multiSelectDelimiter ','
                projectName "${jobName}"              	              
            }
	'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                name 'child_jobs'
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
      steps {
    downstreamParameterized {
      		trigger("\$child_jobs") {
                  block {
                      buildStepFailure('FAILURE')
                      failure('FAILURE')
                      unstable('UNSTABLE')
                  }
                parameters {
                  predefinedProp('BRANCHES', '\$BRANCHES')
                }
              }
        }
      }
     publishers {
        archiveArtifacts {
            pattern("*.tar.gz")
        }
    }
    }
  }
  else
  {
    String child_name = "MNTLAB-Valery_Peshchanka-child"  + Integer.toString(i) + "-build-job"
    job("$child_name") {
     label("EPBYMINW6593")
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
def gitURL = "https://github.com/MNT-Lab/mntlab-dsl.git"
def command = "git ls-remote -h \$gitURL"

def proc = command.execute()
proc.waitFor()              

def branches = proc.in.text.readLines().collect { 
    it.replaceAll(/[a-z0-9]*\\trefs\\/heads\\//, '') 
}

return branches
				"""
            }
          }
        }
      }
      scm {
    git
                {
                     remote {
                        github("MNT-Lab/mntlab-dsl", "https")
                    }
                  branch("\$BRANCHES")
                }
  	  }
      steps {
        shell("./script.sh > log.txt && tar -cf \${BRANCHES}_dsl_script.tar.gz log.txt && cp \${BRANCHES}_dsl_script.tar.gz ../MNTLAB-Valery_Peshchanka-main-build-job/ && echo \"Hello\"")
    publishers {
      archiveArtifacts("*.tar.gz")
      }
     }
      wrappers {
            preBuildCleanup()
        }
        publishers {
            archiveArtifacts {
                pattern("*.tar.gz")
            }
        }
    }
  }
}
