COUNTER = 0
label = 'EPBYMINW2468'

job("MNTLAB-ifilimonau-main-build-job") {

    label($label)
        description()
        keepDependencies(false)

    parameters {
        choiceParam('BRANCH_NAME', ['ifilimonau', 'master'], 'branch name choosing')
        configure {
            project->
                project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
                    parameterDefinitions {
                        'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                            name 'AMOUNT_OF_JOBS'
                            quoteValue 'false'
                            saveJSONParameterToFile 'false'
                            visibleItemCount '4'
                            type 'PT_CHECKBOX'
                            groovyScript """import jenkins.model.*
def inst = Jenkins.instance

def job_pattern = /^/\${label}*MNTLAB-ifilimonau-child*/

def matchedJobs = inst.items.findAll { job ->
    job.name =~ job_pattern
}
matchedJobs.name"""
                            multiSelectDelimiter ','
                            projectName "${jobName}"
                        }
                    }
                }
        }
    }
    steps {
        downstreamParameterized {
            trigger('$AMOUNT_OF_JOBS') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters {
                    predefinedProp('BRANCH_NAME2', '$BRANCH_NAME')
                }
            }
        }
    }
}




while(COUNTER < 4){
    COUNTER += 1

    job("MNTLAB-ifilimonau-child$COUNTER-build-job") {

        label($label)
            description()
            keepDependencies(false)

        scm{
            git {
                remote
                        {
                            github("begemotik/jenkis_dsl", "https")
                        }
                branch("\$BRANCH_NAME2") }
        }

        parameters {
            configure {
                project->
                    project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
                        parameterDefinitions {
                            'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                                name 'BRANCH_NAME2'
                                quoteValue 'false'
                                saveJSONParameterToFile 'false'
                                visibleItemCount '4'
                                type 'PT_CHECKBOX'
                                groovyScript """import jenkins.model.*
def gitURL = "https://github.com/MNT-Lab/build-principals.git"
def command = "git ls-remote -h \$gitURL"

def proc = command.execute()
proc.waitFor()              

def branches = proc.in.text.readLines().collect { 
    it.replaceAll(/[a-z0-9]*\\trefs\\/heads\\//, '') 
}

return branches"""
                                multiSelectDelimiter ','
                                projectName "${jobName}"
                            }
                        }
                    }
            }
        }

        steps {
            shell ("bash script.sh\n" +
                    "tar -czvf \$JOB_BASE_NAME.tar.gz output.txt jobs.groovy\n" +
                    "cp \$JOB_BASE_NAME.tar.gz ../MNTLAB-ifilimonau-main-build-job/")
        }
    }
}
