job("AKO-Seed") {
	description()
	keepDependencies(false)
	scm {
		git {
			remote {
				github("AnnaKo/seed", "https")
			}
			branch("*/hkavaliova")
		}
	}
	disabled(false)
	triggers {
		scm("*/5 * * * *") {
			ignorePostCommitHooks(false)
		}
	}
	concurrentBuild(false)
	steps {
		shell("""./script.sh > output.txt
tar -cvzf archive.tar.gz output.txt jobs.groovy""")
	}
	wrappers {
		preBuildCleanup {
			deleteDirectories(false)
			cleanupParameter()
		}
	}
}



