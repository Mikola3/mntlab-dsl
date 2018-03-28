def STUDENT_NAME = "kklimov"
def GITHUB_REPOSITORY = "https://github.com/MNT-Lab/mntlab-dsl"
def GITHUB_BRANCH = "kklimov"
def Jobs = []
def mainName = "MNTLAB-${STUDENT_NAME}-main-build-job"
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
      label("EPBYMINW2033")
      wrappers {
        preBuildCleanup()
      }
      scm {
        git(GITHUB_REPOSITORY, GITHUB_BRANCH)
      }
      steps {
        shell("bash ./script.sh > output.txt && tar -cvzf child${i}-\$BUILD_NUMBER.tar.gz output.txt && cp child${i}-\$BUILD_NUMBER.tar.gz ../${mainName}")
      }
      publishers {
        archiveArtifacts("child${i}-\$BUILD_NUMBER.tar.gz")
      }
   }
}
job(mainName) {
    label("EPBYMINW2033")
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
                defaultValue "kklimov"
                multiSelectDelimiter ','
                projectName "dwer"
            }
            'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                name 'BUILD_JOBS'
                quoteValue 'false'
                saveJSONParameterToFile 'false'
                visibleItemCount '15'
                type 'PT_CHECKBOX'
                groovyScript "${Jobs.collect{"'$it'"}}"
                multiSelectDelimiter ','
                projectName "dwer"
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
              }
    }
    publishers {
      archiveArtifacts("*.tar.gz")
    }
  }
}
