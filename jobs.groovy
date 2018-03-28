job("MNTLAB-amatiev-main-build-job") {
    label('EPBYMINW2472')
	description()
	keepDependencies(false)
  
  configure {
    project->
        project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
        parameterDefinitions {
            'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                name 'BRANCH_NAME'
                quoteValue 'false'
                saveJSONParameterToFile 'false'
                visibleItemCount '1'
                type 'PT_SINGLE_SELECT'
                value "amatiev, master"
                multiSelectDelimiter ','
                projectName "MNTLAB-amatiev-main-build-job"
            }
          'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                name 'BUILDS_TRIGGER'
                quoteValue 'false'
                saveJSONParameterToFile 'false'
                visibleItemCount '5'
                type 'PT_CHECKBOX'
                groovyScript """import jenkins.model.*
def ji = Jenkins.instance


def job_pattern = /child/


def matchedJobs = ji.items.findAll { job ->
   job.name =~ job_pattern 
}

matchedJobs.name"""
                multiSelectDelimiter ','
                projectName "MNTLAB-amatiev-main-build-job"
            }
        }
    }
}
  
  
	disabled(false)
	concurrentBuild(false)
	steps {
        downstreamParameterized {
            trigger('$BUILDS_TRIGGER') {
                block {
                    buildStepFailure('never')
                    failure('never')
                    unstable('never')
                }
                parameters {
                    predefinedProp('GIT_BRANCH', '$BRANCH_NAME')
                             }
            }
           
        }
    }
  
  publishers {
        archiveArtifacts('*tar.gz')
    }
  
	wrappers {
		preBuildCleanup {
			deleteDirectories(false)
			cleanupParameter()
		}
	}
}





for (i in 1..4){
job("MNTLAB-amatiev-child${i}-build-job") {
	description()
	keepDependencies(false)
     label('EPBYMINW2472')
  
  
  
  configure {
    project->
        project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
        parameterDefinitions {
            'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                name 'GIT_BRANCH'
                quoteValue 'false'
                saveJSONParameterToFile 'false'
                visibleItemCount '1'
                type 'PT_SINGLE_SELECT'
                groovyScript """import jenkins.model.*

def gitURL = "https://github.com/MNT-Lab/mntlab-dsl.git"
def command = "git ls-remote -h \$gitURL"

def proc = command.execute()
proc.waitFor()

def branches = proc.in.text.readLines().collect {
    it.replaceAll(/[a-z0-9]*\\trefs\\/heads\\//, '')
}

return branches"""
                multiSelectDelimiter ','
                defaultValue'amatiev'
              projectName "MNTLAB-amatiev-child${i}-build-job"
            }
         
        }
    }
}
  
    
	scm {
		git {
			remote {
				github("MNT-Lab/mntlab-dsl", "https")
			}
			branch("\$GIT_BRANCH")
		}
	}
	disabled(false)
	concurrentBuild(false)
	steps {
		shell("""touch ./output.txt
./my_echo.sh
tar -cvzf \${GIT_BRANCH}_\${BUILD_TAG}_dsl_script.tar.gz ./output.txt ./jobs.groovy
cp \${GIT_BRANCH}_\${BUILD_TAG}_dsl_script.tar.gz ../MNTLAB-amatiev-main-build-job""")
	}
	wrappers {
		preBuildCleanup {
			deleteDirectories(false)
			cleanupParameter()
		}
	}
}
}
