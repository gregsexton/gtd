# Readme

## Summary

Get Things Displayed (GTD) is a small utility application that enables
displaying text and images in large on your primary display. Text and
images can be displayed immediately or set on a timer using a rich
variety of ways of expressing the time-out. Both relative and absolute
times are possible. Once displayed, a 'notification' can be dragged
anywhere and can easily be dismissed by pressing escape. Notifications
float on top of all windows so they cannot be accidentally missed.

Notification text is sized and wrapped intelligently to be as readable
as possible from a distance. GTD honours text with line breaks so that,
code for example, is displayed correctly.

GTD uses a server/client model. The GTD server is written in Clojure and
is responsible for displaying notifications and queuing future
notifications.  The client is a small and simple script, written in
Python, that sends notifications to the server. Both the client and
server should be cross-platform compatible and have been tested on
Windows and Mac OS X.  The client is written in Python for it's fast
start up time and low overhead compared to the JVM.

The client application is designed for the command line and is very
flexible. In keeping with the UNIX philosophy it does its single job
well and is very useful in conjunction with other programs via piping.
It is also easily scripted and integrated with any decent text editor.

## Requirements

A recent version of the JVM and Python are all that is required to run
GTD.

Clojure jars are not required as all necessary Clojure libraries are
included within the GTD jar. For those of you familiar with Clojure you
may wish to run GTD using Leiningen and this is possible.

## Installation

Installation is very simple. Simply copy the gtd-x.x.x.jar file
somewhere and run this to start the server. There are various ways of
forking a JVM and getting the server to run as a daemon. On Windows
double clicking the jar file should take care of it. I recommend using
the packaged app if you're on Mac OS X. Again this is just a case of
running the app by double clicking on it.

Once the server is running, the Python gtd.py script can be used to send
notifications to the server. I recommend symlinking to this from
somewhere in your path. e.g.

> ln -s ~/some/path/to/gtd.py ~/bin/gtd
> chmod +x ~/bin/gtd

Creating a gtd-server script somewhere in your path and adding to it the
commands you use to start the server is recommended. This allows you to
then start the server using `gtd-server` but also `gtd --server`.

## Suggested usages

Here are a few potential usages to demonstrate the possibilities of
GTD.

* Display the time and date with: `date | gtd`.
* Display reminders: `echo "Team meeting" | gtd 30 min` or `gtd -m "Coffee" 11.30`.
* Display an image: `ls <somefile> | gtd`.
* Display phone numbers to be easily dialed: `gtd -m "+44 (0)123 456 7890"`.
* Display the results of unit tests or a lengthy build when finished:
  `lein test|gtd` or `make; gtd -m "Build finished."`.
* Display a file to someone stood behind you: `cat blah.clj|gtd`.
* View the bottom of a log from across the room: `tail blah.log|gtd`.
* See the currently added notifications and when they will be displayed: `gtd --list`.
