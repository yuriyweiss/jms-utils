#!/bin/bash

APP_HOME="<path_to_application>"

JAVA_HOME="<path_to_JDK>"

PATH="$JAVA_HOME/bin:$PATH"
echo "Path: $PATH"

#get standard JAVA_OPTS (timezone, language, etc.) from Your application launch script
JAVA_OPTS="-Xms128m -Xmx512m -Dfile.encoding=UTF-8 -Duser.timezone=Universal"

for jar_file in $APP_HOME/lib/*.jar; do
    CLASSPATH=$jar_file:$CLASSPATH
done
#for jar_file in $SERIALIZED_OBJECTS_JARS/*.jar; do
#    CLASSPATH=$jar_file:$CLASSPATH
#done


#to work with relative application directories (tmp, lib, etc.)
cd $APP_HOME
echo `pwd`

JAVA_CMD="java -cp $CLASSPATH $JAVA_OPTS yweiss.local.jms.utils.AppLauncher $@"
echo $JAVA_CMD

echo
echo
$JAVA_CMD



