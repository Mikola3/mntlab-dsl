def STUDENT_NAME = "Pavel__Kislouski"
def GITHUB_REPOSITORY = "https://github.com/MNT-Lab/mntlab-dsl"
def GITHUB_BRANCH = "pkislouski"
def Jobs = []
def mainJob = "MNTLAB-${STUDENT_NAME}-main-build-job"
def script = """
def command = "git ls-remote -h ${GITHUB_REPOSITORY}"
def proc = command.execute()
proc.waitFor()
def branches = proc.in.text.readLines().collect {
  it.replaceAll(/[a-z0-9]*\trefs\\/heads\\//, '')
}
return branches
"""

for (int i = 1; i <5; i++) {
    Jobs << "MNTLAB-${STUDENT_NAME}-child${i}-build-job"
    job("${Jobs.last()}"){
      label("EPBYMINW7296")
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
                  defaultValue "pkislouski"
                  multiSelectDelimiter ','
                  projectName "example"
              }
          }
        }
      }
      scm {
        git(GITHUB_REPOSITORY, "\$BRANCH_NAME")
      }
      steps {
        shell("./script.sh > log.txt && tar -cf child${i}-\$BUILD_NUMBER.tar.gz jobs.groovy log.txt && cp child${i}-\$BUILD_NUMBER.tar.gz ../${mainJob}")
      }
      publishers {
        archiveArtifacts("child${i}-\$BUILD_NUMBER.tar.gz")
      }
   }
}
job(mainJob) {
    label("EPBYMINW7296")
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
                groovyScript script
                defaultValue "'pkislouski'"
                multiSelectDelimiter ','
                projectName "example"
            }
            'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                name 'BUILD_JOBS'
                quoteValue 'false'
                saveJSONParameterToFile 'false'
                visibleItemCount '15'
                type 'PT_CHECKBOX'
                groovyScript "${Jobs.collect{"'$it'"}}"
                multiSelectDelimiter ','
                projectName "example"
            }
        }
    }
  }
  scm {
    git(GITHUB_REPOSITORY, "\$BRANCH")
  }
  steps {
    downstreamParameterized {
              trigger("\$BUILD_JOBS") {
                  block {
                      buildStepFailure('FAILURE')
                      failure('FAILURE')
                      unstable('UNSTABLE')
                  }
                  parameters {
                      predefinedProp('BRANCH_NAME', '\$BRANCH')
                  }
              }
    }
    publishers {
      archiveArtifacts("*.tar.gz")
    }
  }
}
