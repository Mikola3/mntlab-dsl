def git = "MNT-Lab/mntlab-dsl"
def repo = "azaitsau"
def StName = "azaitsau"

def gitURL = "https://github.com/MNT-Lab/mntlab-dsl.git"
def command = "git ls-remote -h $gitURL"

def proc = command.execute()
proc.waitFor()

if ( proc.exitValue() != 0 ) {
    println "Error, ${proc.err.text}"
    System.exit(-1)
}

def branches = proc.in.text.readLines().collect {
    it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
}

job("MNTLAB-${StName}-main-build-job") {
    label("EPBYMINW7425")
    description 'This main-job'
    parameters {
	choiceParam('ChooseBranch', ['azaitsau', 'master'], 'Choose wich branch to use')
        activeChoiceParam('BUILDS_TRIGGER') {
            choiceType('CHECKBOX')
            groovyScript {
                script('["MNTLAB-azaitsau-child1-build-job", "MNTLAB-azaitsau-child2-build-job", "MNTLAB-azaitsau-child3-build-job", "MNTLAB-azaitsau-child4-build-job"]')
            }
        }
    }
    scm {
        github(git, '$ChooseBranch')
    }
    triggers {
        scm('H/1 * * * *')
    }
    steps {
        downstreamParameterized {
            trigger('$BUILDS_TRIGGER') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
               parameters {
                    currentBuild()
		}
	    }
	}	
        shell('chmod +x script.sh && ./script.sh >> output.log && tar -czf ${BRANCH_NAME}_dsl_script.tar.gz output.log')
    }
    publishers { 
	archiveArtifacts('output.log')
    }

}

for (int i = 1; i <5; i++) {
job("MNTLAB-${StName}-child${i}-build-job") {
    label("EPBYMINW7425")
    parameters {
	choiceParam('ChooseBranch', branches, '')
    }
    scm {
        github(git, '$ChooseBranch')
    }
    steps {
        shell('chmod +x script.sh && ./script.sh >> output.log && tar -czf  child${it}_${BUILD_NUMBER}_dsl_script.tar.gz output.log jobs.groovy script.sh')
    }
    publishers { 
        archiveArtifacts {
            pattern('output.log')
            pattern('${ChooseBranch}_dsl_script.tar.gz')
            onlyIfSuccessful()
   }
  }
 }
}
