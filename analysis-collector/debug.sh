export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
rm -rf $HUDSON_HOME/plugins/analysis-collector*
mvn clean hpi:run