for (i in 1..4) {
  freeStyleJob("MNTLAB-ykhodzin-child${i}-build-job") {
    label('EPBYMINW1766')
    configure {
      project->
      project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
          parameterDefinitions {
            'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
              name 'BRANCH'
              quoteValue 'false'
              saveJSONParameterToFile 'false'
              visibleItemCount '*'
              type 'PT_SINGLE_SELECT'
              groovyScript """import jenkins.model.*
def gitURL = "https://github.com/MNT-Lab/mntlab-dsl.git"
def command = "git ls-remote -h \$gitURL"
def proc = command.execute()            
def branches = proc.in.text.readLines().collect { 
it.replaceAll(/[a-z0-9]*\\trefs\\/heads\\//, '')} 
return branches"""           
              multiSelectDelimiter ','
              defaultValue 'ykhodzin'
            }
          }
       }
    } 
    scm {
        github('MNT-Lab/mntlab-dsl', '$BRANCH')
    }
    steps{
      shell("""bash script.sh > output.txt
tar -czvf \${BRANCH}_dsl_script.tar.gz output.txt jobs.groovy
cp \${BRANCH}_dsl_script.tar.gz \$JENKINS_HOME/workspace/MNTLAB-ykhodzin-main-build-job/\${BRANCH}_\${BUILD_TAG}_dsl_script.tar.gz""")
    }
    publishers {
      archiveArtifacts('output.txt, \${BRANCH}_dsl_script.tar.gz')
    }
    wrappers {
      preBuildCleanup()
    }
  }
}

freeStyleJob('MNTLAB-ykhodzin-main-build-job') {
  label('EPBYMINW1766')
  configure {
    project->
    project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
      parameterDefinitions {
        'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
          name 'BUILDS_TRIGGER'
          quoteValue 'false'
          saveJSONParameterToFile 'false'
          visibleItemCount '4'
          type 'PT_CHECKBOX'
          groovyScript 'import jenkins.model.*\
          \ndef matchedJobs = Jenkins.instance.items.findAll\
          \n{ job -> job.name =~ /MNTLAB-ykhodzin-child/}\
          \ndef list = [];matchedJobs.each\
          \n{list << it.name.split("\\\\.")[0]}\
          \nlist'
          multiSelectDelimiter ','    
        }
      }
    }
  }
  parameters {
    choiceParam('BRANCH_NAME', [ 'ykhodzin', 'master'],'')
  }
  scm {
    github('MNT-Lab/build-principals', 'ykhodzin')
  }
  steps {
    downstreamParameterized {
      trigger('$BUILDS_TRIGGER') {
        block {
          buildStepFailure("FAILURE")
          unstable("UNSTABLE")
          failure("FAILURE")
        }
        parameters {
          predefinedProp('BRANCH', '\$BRANCH_NAME')
        }
      }
    }
  }
  publishers {
    archiveArtifacts('*.tar.gz')
  }
  wrappers {
    preBuildCleanup()
  }
}
