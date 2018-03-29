
def jobs = (1..4)
def childlist = "'MNTLAB-achernak-child1-build-job','MNTLAB-achernak-child2-build-job','MNTLAB-achernak-child3-build-job','MNTLAB-achernak-child4-build-job'"
def names = []
jobs.each {names.add("MNTLAB-achernak-child$it-build-job")}

job("MNTLAB-achernak-main-build-job") {
        label ('EPBYMINW6122')
        description("This is main")
        parameters {choiceParam("branch", ["achernak", "master"], "")
        activeChoiceParam('CHILD') {
           description('Choose child builds')
           choiceType('CHECKBOX')
                groovyScript {script("return[$childlist]")}}}
        scm { git { remote { github("MNT-Lab/mntlab-dsl", "https")}
            branch("\$branch")
        }}
        disabled(false)
        concurrentBuild(false)
        steps {downstreamParameterized {
                        trigger('$CHILD') {
                                block { buildStepFailure('FAILURE')
                                        unstable('FAILURE')
                                        failure('UNSTABLE')}
                                parameters {predefinedProp('branch', '$branch')}}}}
        wrappers {preBuildCleanup()}}

names.each {
job("$it") {
        label ('EPBYMINW6122')
        description("THIS is child")
         configure {project ->
            project / 'properties' << 'hudson.model.ParametersDefinitionProperty' {
                parameterDefinitions {
                    'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                        name 'branch'
                        quoteValue 'false'
                        saveJSONParameterToFile 'false'
                        visibleItemCount '5'
                        type 'PT_SINGLE_SELECT'
                        groovyScript """
def gitURL = "https://github.com/MNT-Lab/mntlab-dsl.git"
def command = "git ls-remote -h \$gitURL"
def proc = command.execute()
proc.waitFor()
def branch = proc.in.text.readLines().collect {it.replaceAll(/[a-z0-9]*\\trefs\\/heads\\//, '')}
return branch"""
                        defaultValue 'achernak'
                        multiSelectDelimiter ','}}}}
        keepDependencies(false)
        scm { git { remote { github("MNT-Lab/mntlab-dsl", "https")}
            branch("\$branch")
        }}
        disabled(false)
        concurrentBuild(false)
        steps {shell("""chmod +x script.sh
bash -ex script.sh > output.txt
tar czvf \$BUILD_TAG.tar.gz output.txt jobs.groovy
cp \$BUILD_TAG.tar.gz ../MNTLAB-achernak-main-build-job""")}
        wrappers {preBuildCleanup()}
        publishers {archiveArtifacts {pattern("$BUILD_TAG.tar.gz")}}}}
