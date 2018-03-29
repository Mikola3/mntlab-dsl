
def jobs = (1..4)
def childlist = "'MNTLAB-achernak-child1-build-job','MNTLAB-achernak-child2-build-job','MNTLAB-achernak-child3-build-job','MNTLAB-achernak-child4-build-job'"
def name = []
jobs.each {name.add("MNTLAB-achernak-child$it-build-job")}

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

name.each {
job("$it") {
        label ('EPBYMINW6122')
        description("THIS is child")
        parameters {choiceParam("branch", ["achernak", "master"], "")
        keepDependencies(false)
        scm { git { remote { github("MNT-Lab/mntlab-dsl", "https")}
            branch("\$branch")
        }}
        disabled(false)
        concurrentBuild(false)
        steps {shell("""chmod +x script.sh
bash -ex script.sh > output.txt
tar czvf \$BUILD_TAG.tar.gz output.txt jobs.groovy
mv \$BUILD_TAG.tar.gz ../MAIN""")}}
wrappers {preBuildCleanup()}}}
