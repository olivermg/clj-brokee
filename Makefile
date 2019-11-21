nrepl:
	clj -R:nrepl -m nrepl.cmdline

repl-clj:
	# clj -A:clj-repl --repl   # enable this as soon as bug in clj is fixed
	clj --repl

repl-cljs-node:
	clojure -A:cljs-repl -m figwheel.main --build node --repl

repl-cljs-browser:
	clojure -A:cljs-repl -m figwheel.main --build browser --repl


pom:
	clj -Spom

jar: pom
	clj -A:pack mach.pack.alpha.skinny --no-libs --project-path clj-brokee.jar

deploy: jar
	mvn deploy:deploy-file -Dfile=clj-brokee.jar -DpomFile=pom.xml -DrepositoryId=clojars -Durl=https://clojars.org/repo


clean:
	rm -rf target clj-brokee.jar


.phony: nrepl pom jar deploy clean
