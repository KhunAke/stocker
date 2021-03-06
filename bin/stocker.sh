#!/bin/sh
#/etc/init.d/mydaemon

### BEGIN INIT INFO
# Provides:          stocker
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Short-Description: Starts the MyDaemon service
# Description:       This file is used to start the daemon
#                    and should be placed in /etc/init.d
### END INIT INFO

# Author:   Sheldon Neilson <sheldon[AT]neilson.co.za>
# Url:      www.neilson.co.za
# Date:     25/04/2013

NAME="stocker"
DESC="stocker service"
VERSION="0.0.1"

# The path to Jsvc
EXEC="/usr/bin/jsvc"

# The path to the folder containing MyDaemon.jar
FILE_PATH="/usr/local/$NAME"

# The path to the folder containing the java runtime
JAVA_HOME="/usr/lib/jvm/jre"

# Our classpath including our jar file and the Apache Commons Daemon library
CLASS_PATH="$FILE_PATH/$NAME-$VERSION.jar"

# The fully qualified name of the class to execute
CLASS="com.javath.util.Service"

# Any command line arguments to be passed to the our Java Daemon implementations init() method 
ARGS="myArg1 myArg2 myArg3"

#The user to run the daemon as
USER="stocker"

# The file that will contain our process identification number (pid) for other scripts/programs that need to access it.
PID="/var/run/$NAME.pid"

# System.out writes to this file...
LOG_OUT="$FILE_PATH/var/log/$NAME.out"

# System.err writes to this file...
LOG_ERR="$FILE_PATH/var/log/$NAME.err"

 jsvc_exec()
{   
     cd $FILE_PATH
     $EXEC -home $JAVA_HOME -cp $CLASS_PATH -user $USER -outfile $LOG_OUT -errfile $LOG_ERR -pidfile $PID $1 $CLASS $ARGS
}

case "$1" in
     start)  
         printf "Starting $NAME:  " 
         
         # Start the service
         jsvc_exec
         test $? == 0 && echo "[ OK ]" || echo "[ FAIL ]"
     ;;
     stop)
         printf "Stopping $NAME:  "
         
         # Stop the service
         jsvc_exec "-stop"       
         test $? == 0 && echo "[ OK ]" || echo "[ FAIL ]"
     ;;
     restart)
         if [ -f "$PID" ]; then
             
             printf "Stopping $NAME:  "
             
             # Stop the service
             jsvc_exec "-stop"
             test $? == 0 && echo "[ OK ]" || echo "[ FAIL ]"

             printf "Starting $NAME:  "

             # Start the service
             jsvc_exec
             test $? == 0 && echo "[ OK ]" || echo "[ FAIL ]"
         else
             echo "Daemon not running, no action taken"
             exit 1
         fi
             ;;
     *)
     echo "Usage: /etc/init.d/$NAME {start|stop|restart}" >&2
     exit 3
     ;;
esac

