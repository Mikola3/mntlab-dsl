def GIT_URL = "https://github.com/MNT-Lab/mntlab-dsl"
def GIT_REPO =  "MNT-Lab/mntlab-dsl"
def GITHUB_BRANCH = "nbuzin"
def STUDENT = "nbuzin"
def JOB_LIST = []
//def MAINNAME = "MNTLAB-${STUDENT}-main-build-job"

def GROOVYSCRIPT = """
def command = "git ls-remote -h ${GIT_URL}"
def proc = command.execute()
proc.waitFor()
def branches = proc.in.text.readLines().collect {
  it.replaceAll(/[a-z0-9]*\trefs\\/heads\\//, '')
}
return branches
"""

//CHILD JOBS

for (i in 1..4) {
    JOB_LIST << "MNTLAB-${STUDENT}-child${i}-build-job"
    job("${JOB_LIST.last()}"){
       label("EPBYMINW2629")
        wrappers {
            preBuildCleanup()
        }
        configure {
        project->
            project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
            parameterDefinitions {
              'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                  name 'BRANCH_NAME'
                  quoteValue 'false'
                  saveJSONParameterToFile 'false'
                  visibleItemCount '15'
                  type 'PT_SINGLE_SELECT'
                  groovyScript script
                  defaultValue "nbuzin"
                  multiSelectDelimiter ','
                  }
              }
            }
        }
        scm {
            git {
                remote { 
                    github("${GIT_REPO}", "https") 
                }
            branch ("${GITHUB_BRANCH}") }
        } 
      
        steps {
            shell("""bash ./script.sh > output.txt 
                     tar -cvzf \${GITHUB_BRANCH}_dsl_script_${i}-${BUILD_NUMBER}.tar.gz output.txt  
                     cp \${GITHUB_BRANCH}_dsl_script_${i}-${BUILD_NUMBER}.tar.gz ../MNTLAB-${STUDENT}-main-build-job""")
        }
        publishers {
            archiveArtifacts("\${GITHUB_BRANCH}_dsl_script_${i}-${BUILD_NUMBER}.tar.gz")
        }
    }
}

//MAIN JOB

job("MNTLAB-${STUDENT}-main-build-job") {
    label("EPBYMINW2629")
    wrappers {
        preBuildCleanup()
    }
    configure {
    project->
        project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
        parameterDefinitions {
            'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                name 'BRANCH'
                quoteValue 'false'
                saveJSONParameterToFile 'false'
                visibleItemCount '15'
                type 'PT_SINGLE_SELECT'
                groovyScript GROOVYSCRIPT
                defaultValue "'nbuzin'"
                multiSelectDelimiter ','
            }
            'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                name 'BUILD_JOBS'
                quoteValue 'false'
                saveJSONParameterToFile 'false'
                visibleItemCount '15'
                type 'PT_CHECKBOX'
                groovyScript "${JOB_LIST.collect{"'$it'"}}"
                multiSelectDelimiter ','
            }
        }
    }
  }
      scm {
                git {
                    remote { 
                        github("NikB97/dsl", "https") 
                    }
                branch ("${GITHUB_BRANCH}") }
            } 
      steps {
          downstreamParameterized {
              trigger("\$BUILD_JOBS") {
                  block {
                      buildStepFailure('FAILURE')
                      failure('FAILURE')
                      unstable('UNSTABLE')
                  }
              }
    }
      publishers {
          archiveArtifacts("*.tar.gz")
        }
    }
}
