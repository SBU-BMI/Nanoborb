call mvn install:install-file -Dfile=ImageBox-1.1.1.jar -DgroupId=com.ebremer -DartifactId=ImageBox -Dversion=1.1.1 -Dpackaging=jar -DgeneratePom=true 
call mvn install:install-file -Dfile=jcef.jar -DgroupId=jcef -DartifactId=jcef -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
call mvn install:install-file -Dfile=jogl-all.jar -DgroupId=jogl-all -DartifactId=jogl-all -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
call mvn install:install-file -Dfile=jogl-all-natives-windows-amd64.jar -DgroupId=jogl-all-natives-windows-amd64 -DartifactId=jogl-all-natives-windows-amd64 -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
call mvn install:install-file -Dfile=gluegen-rt.jar -DgroupId=gluegen-rt -DartifactId=gluegen-rt -Dversion=1.0 -Dpackaging=gluegen-rt -DgeneratePom=true
call mvn install:install-file -Dfile=gluegen-rt-natives-windows-amd64.jar -DgroupId=gluegen-rt-natives-windows-amd64 -DartifactId=gluegen-rt-natives-windows-amd64 -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true