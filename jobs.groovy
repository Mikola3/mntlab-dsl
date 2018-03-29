for (i in 1..4) {
  job("MNTLAB-uvalchkou-child${i}-build-job") {
    label('EPBYMINW2471')
    scm{
      git { 
        remote 
            {
              github("MNT-Lab/mntlab-dsl", "https") 
            } 
        branch("\$branch") }
    }
   
    configure {
    project->
        project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
        parameterDefinitions {
            'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                name 'branch'
                quoteValue 'false'
                saveJSONParameterToFile 'false'
                visibleItemCount '15'
                type 'PT_SINGLE_SELECT'
                groovyScript """
import jenkins.model.*
def gitURL = "https://github.com/MNT-Lab/mntlab-dsl"
def command = "git ls-remote -h \$gitURL"

def proc = command.execute()
 
def branches = proc.in.text.readLines().collect { 
    it.split('/')[2]
}

branches"""
                multiSelectDelimiter ','
                projectName "${jobName}"
                defaultValue 'uvalchkou'
            }
        }
    }
}
  steps {
    shell("""chmod +x ./script.sh
./script.sh > out.txt
tar czvf \$BUILD_TAG.tar.gz out.txt jobs.groovy
cp \$BUILD_TAG.tar.gz ../MNTLAB-uvalchkou-main-build-job/""")
  }
	  publishers {
        archiveArtifacts {
            pattern('$BUILD_TAG.tar.gz')
        }
    }
  }

}




job("MNTLAB-uvalchkou-main-build-job") {
  label('EPBYMINW2471')
    scm{
      git { 
        remote 
            {
              github("MNT-Lab/mntlab-dsl", "https") 
            } 
        branch("\$branch") }
    }
    
    configure {
    project->
        project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
        parameterDefinitions {
            'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                name 'child_jobs'
                quoteValue 'false'
                saveJSONParameterToFile 'false'
                visibleItemCount '15'
                type 'PT_CHECKBOX'
                value "MNTLAB-uvalchkou-child1-build-job, MNTLAB-uvalchkou-child2-build-job, MNTLAB-uvalchkou-child3-build-job, MNTLAB-uvalchkou-child4-build-job"
                multiSelectDelimiter ','
                projectName "${jobName}"
            }
        }
    }
}
    	parameters {
        choiceParam('branch', ['uvalchkou', 'master'], '')
            } 
  		steps {
          downstreamParameterized {
              trigger('$child_jobs') {
                 block { 
                   buildStepFailure("FAILURE")
                   unstable("FAILURE")
                   failure("UNSTABLE")
                 }
			  parameters {predefinedProp('branch', '$branch')}}}
    }
}
