COUNTER = 0

job("MNTLAB-hkavaliova-main-build-job") {
    label('EPBYMINW7432')
    description("this is a master job for running childs")
    scm{
        git {
            remote
                    {
                        github("MNT-Lab/mntlab-dsl", "https")
                    }
            branch("\$BRANCH_NAME") }
    }

    configure {
        project->
            project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
                parameterDefinitions {
                    'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                        name 'SelectChilds'
                        quoteValue 'false'
                        saveJSONParameterToFile 'false'
                        visibleItemCount '15'
                        type 'PT_CHECKBOX'
                        value "MNTLAB-hkavaliova-child1-build-job, MNTLAB-hkavaliova-child2-build-job, MNTLAB-hkavaliova-child3-build-job, MNTLAB-hkavaliova-child4-build-job"
                        multiSelectDelimiter ','
                    }
                }
            }
    }
    parameters {
        choiceParam('BRANCH_NAME', ['hkavaliova', 'master'], '')
    }
    steps {
        downstreamParameterized {
            trigger("$SelectChilds") {
                block {
                    buildStepFailure("FAILURE")
                    unstable("UNSTABLE")
                    failure("FAILURE")
                }
                parameters {predefinedProp('selbran', "$BRANCH_NAME")
                }
            }
        }
        wrappers {
            preBuildCleanup {
                deleteDirectories(false)
                cleanupParameter()
            }
        }
    }
}

while(COUNTER < 4) {
    COUNTER += 1
    job("MNTLAB-hkavaliova-child$COUNTER-build-job") {
        label('EPBYMINW7432')
        description("this is a child-one job")
        scm {
            git {
                remote {
                    github("MNT-Lab/mntlab-dsl", "https")
                }
                branch("\$selbran")
            }
        }
        configure {
            project ->
                project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
                    parameterDefinitions {
                        'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                            name 'selbran'
                            quoteValue 'false'
                            saveJSONParameterToFile 'false'
                            visibleItemCount '15'
                            type 'PT_SINGLE_SELECT'
                            groovyScript """
    import jenkins.model.*
    def gitURL = "https://github.com/MNT-Lab/mntlab-dsl"
    def command = "git ls-remote -h \$gitURL"
    
    def proc = command.execute()
     
    def selbran = proc.in.text.readLines().collect { 
        it.split('/')[2]
    }
    
    return selbran"""
                            multiSelectDelimiter ','
                            defaultValue 'hkavaliova'
                        }
                    }
                }
        }
        steps {
            shell("""./script.sh > output.txt
    tar -cvzf archive-\$BUILD_TAG.tar.gz output.txt jobs.groovy
    cp archive-\$BUILD_TAG.tar.gz ../MNTLAB-hkavaliova-main-build-job/archived-\$BUILD_TAG.tar.gz""")
        }
        wrappers {
            preBuildCleanup {
                deleteDirectories(false)
                cleanupParameter()
            }
        }
    }
}

