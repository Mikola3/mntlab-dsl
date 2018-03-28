def jobs = (1..4)
def childlist = "'CHILD_1','CHILD_2','CHILD_3','CHILD_4'"
def name = []
jobs.each {name.add("CHILD_$it")}

job("MAIN") {
        description("This is main")
        parameters {choiceParam("branch", ["achernak", "master"], "")
        activeChoiceParam('CHILD') {
           description('Choose child builds')
           choiceType('CHECKBOX')
                groovyScript {script("return[$childlist]")}}}
        scm {
        git {
            remote {
                github("AlexandrSher/dsl", "https")
            }
            branch("\$branch")
        }
}
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
        description("THIS is child")
        parameters {choiceParam("branch", ["achernak", "master"], "")
        keepDependencies(false)
        scm {
        git {
            remote {
                github("AlexandrSher/dsl", "https")
            }
            branch("\$branch")
        }
}
        disabled(false)
        concurrentBuild(false)
        steps {shell("""chmod +x script.sh
bash -ex script.sh > output.txt
tar czvf \$BUILD_TAG.tar.gz output.txt jobs.groovy
mv \$BUILD_TAG.tar.gz /var/lib/jenkins/workspace/MAIN/""")}}
wrappers {preBuildCleanup()}}}
