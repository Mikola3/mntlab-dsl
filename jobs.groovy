def Type = "child"
def StartName = "MNTLAB-ayarmalovich-"
def EndName = "-build-job"
def Amount = 4

job("${StartName}main${EndName}") {
    label('EPBYMINW2473')
    description("Main Job")
    scm {
        git {
            remote {
                github("MNT-Lab/mntlab-dsl", "https")
            }
            branch("*/\$branches")
        }
    }
    configure {project ->
        project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
            parameterDefinitions {
                'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                    name 'branches'
                    quoteValue 'false'
                    saveJSONParameterToFile 'false'
                    visibleItemCount '5'
                    type 'PT_SINGLE_SELECT'
                    value 'ayarmalovich, master'
                    multiSelectDelimiter ','
                }
                'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                    name 'matchedJobs'
                    quoteValue 'false'
                    saveJSONParameterToFile 'false'
                    visibleItemCount '5'
                    type 'PT_CHECKBOX'
                    groovyScript """import jenkins.model.*
import hudson.model.*
def job_pattern = /EPBYMINW2473/ 
def matchedJobs = Jenkins.instance.getAllItems(jenkins.model.ParameterizedJobMixIn.ParameterizedJob.class).findAll{
  job -> job =~ job_pattern
}
return matchedJobs.name"""
                    multiSelectDelimiter ','
                }
            }
        }
    }
    steps {
        downstreamParameterized {
            trigger("\$matchedJobs") {
                block {
                    buildStepFailure("FAILURE")
                    unstable("UNSTABLE")
                    failure("FAILURE")
                }
                parameters {
                    predefinedProp('branches', '\$branches')

                }
            }
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
for (i=1; i < Amount+1; i++) {
    job("${StartName}${Type}$i${EndName}") {
        label('EPBYMINW2473')
        description("Child Job")
        scm {
            git {
                remote {
                    github("MNT-Lab/mntlab-dsl", "https")
                }
                branch("*/\$branches")
            }
        }
        configure {project ->
            project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
                parameterDefinitions {
                    'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                        name 'branches'
                        quoteValue 'false'
                        saveJSONParameterToFile 'false'
                        visibleItemCount '5'
                        type 'PT_SINGLE_SELECT'
                        groovyScript """
def gitURL = "https://github.com/MNT-Lab/build-principals.git"
def command = "git ls-remote -h \$gitURL"
def proc = command.execute()
proc.waitFor()

def branches = proc.in.text.readLines().collect {
    it.replaceAll(/[a-z0-9]*\\trefs\\/heads\\//, '')
}
return branches"""
                        multiSelectDelimiter ','
                    }
                }
            }
        }
        steps {
            shell("""bash script.sh > output.txt
tar -cvzf ${Type}${i}-\$branches-\${BUILD_NUMBER}_dsl_script.tar.gz output.txt script.sh
cp ${Type}${i}-\$branches-\${BUILD_NUMBER}_dsl_script.tar.gz ../${StartName}main${EndName}/""")
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
