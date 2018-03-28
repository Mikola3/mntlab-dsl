def git = "aliaksandr-lahutsin/testRepoForDSL"
def repo = "alahutsin"

def gitURL = "https://github.com/aliaksandr-lahutsin/testRepoForDSL.git"
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

job("MNTLAB-alahutsin-main-build-job") {
    parameters {
	choiceParam('BRANCH_NAME', ['alahutsin', 'master'], '')
        activeChoiceParam('BUILDS_TRIGGER') {
            choiceType('CHECKBOX')
            groovyScript {
                script('["MNTLAB-alahutsin-child1-build-job", "MNTLAB-alahutsin-child2-build-job", "MNTLAB-alahutsin-child3-build-job", "MNTLAB-alahutsin-child4-build-job"]')
            }
        }
    }
    scm {
        github(git, '$BRANCH_NAME')
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
        shell('chmod +x do.sh && ./do.sh >> output.log && ls && tar -czf main_${BUILD_NUMBER}_dsl_do.tar.gz output.log jobs.groovy do.sh')
    }
    publishers { 
	archiveArtifacts('output.log')
    }

}

job("MNTLAB-alahutsin-child1-build-job") {
    parameters {
	choiceParam('BRANCH_NAME', branches, '')
    }
    scm {
        github(git, '$BRANCH_NAME')
    }
    steps {
	    shell("""chmod +x do.sh && ./do.sh > output.log &&
tar -czf child1_${BUILD_NUMBER}_dsl_do.tar.gz output.log jobs.groovy do.sh
cp child1_${BUILD_NUMBER}_dsl_do.tar.gz ../MNTLAB-alahutsin-main-build-job/child1_${BUILD_NUMBER}_dsl_do.tar.gz""")
    }
    publishers { 
        archiveArtifacts {
            pattern('output.log')
            pattern('child1_${BUILD_NUMBER}_dsl_do.tar.gz')
            onlyIfSuccessful()
   }
  }
 }
job("MNTLAB-alahutsin-child2-build-job") {
    parameters {
	choiceParam('BRANCH_NAME', branches, '')
    }
    scm {
        github(git, '$BRANCH_NAME')
    }
    steps {
		shell("""chmod +x do.sh && ./do.sh > output.log &&
tar -czf child2_${BUILD_NUMBER}_dsl_do.tar.gz output.log jobs.groovy do.sh && pwd
cp child2_${BUILD_NUMBER}_dsl_do.tar.gz ../MNTLAB-alahutsin-main-build-job/child2_${BUILD_NUMBER}_dsl_do.tar.gz""")
    }
    publishers { 
        archiveArtifacts {
            pattern('output.log')
            pattern('child2_${BUILD_NUMBER}_dsl_do.tar.gz')
            onlyIfSuccessful()
   }
  }
 }
  job("MNTLAB-alahutsin-child3-build-job") {
    parameters {
	choiceParam('BRANCH_NAME', branches, '')
    }
    scm {
        github(git, '$BRANCH_NAME')
    }
    steps {
	    shell("""chmod +x do.sh && ./do.sh > output.log &&
tar -czf child3_${BUILD_NUMBER}_dsl_do.tar.gz output.log jobs.groovy do.sh && pwd
cp child3_${BUILD_NUMBER}_dsl_do.tar.gz ../MNTLAB-alahutsin-main-build-job/child3_${BUILD_NUMBER}_dsl_do.tar.gz""")
    }
    publishers { 
        archiveArtifacts {
            pattern('output.log')
            pattern('child3_${BUILD_NUMBER}_dsl_do.tar.gz')
            onlyIfSuccessful()
   }
  }
 }
  job("MNTLAB-alahutsin-child4-build-job") {
    parameters {
	choiceParam('BRANCH_NAME', branches, '')
    }
    scm {
        github(git, '$BRANCH_NAME')
    }
    steps {
	    shell("""chmod +x do.sh && ./do.sh > output.log &&
tar -czf child4_${BUILD_NUMBER}_dsl_do.tar.gz output.log jobs.groovy do.sh && pwd
cp child4_${BUILD_NUMBER}_dsl_do.tar.gz ../MNTLAB-alahutsin-main-build-job/child4_${BUILD_NUMBER}_dsl_do.tar.gz""")
    }
    publishers { 
        archiveArtifacts {
            pattern('output.log')
            pattern('child4_${BUILD_NUMBER}_dsl_do.tar.gz')
            onlyIfSuccessful()
   }
  }
 }
