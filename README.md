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

    ln -s ~/some/path/to/gtd.py ~/bin/gtd
    chmod +x ~/bin/gtd

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

## Screenshot

Here is an example usage screenshot achieved by using the command `date | gtd`.

![Screenshot of GTD in action.](http://www.gregsexton.org/images/gtd/gtd.jpg)

## Specifying a time-out

Here are some example ways to specify the time-out period. Both relative
and absolute examples are provided. For a definitive list: at present
you'll have to look at the code I'm afraid. I would start with the unit
tests though.

### Relative

<table>
    <tr>
        <td>
            now
        </td>
        <td>
            "now" "-" "." ""
        </td>
    </tr>
    <tr>
        <td>
            tomorrow
        </td>
        <td>
            "tomorrow" "1day" "1 day" "day" "a day" "1days" "days1"
        </td>
    </tr>
    <tr>
        <td>
            days
        </td>
        <td>
            "xday" "xdays" "dayx" "monday-sunday"
        </td>
    </tr>
    <tr>
        <td>
            week
        </td>
        <td>
            "7days" "week" "a week" "next week" "xweek" "xweeks"
        </td>
    </tr>
    <tr>
        <td>
            month
        </td>
        <td>
            "month" "a month" "next month" "jan" "january"
        </td>
    </tr>
    <tr>
        <td>
            year
        </td>
        <td>
            "year" "a year" "next year"
        </td>
    </tr>
    <tr>
        <td>
            secs
        </td>
        <td>
            "xseconds" "xsecond" "xsecs" "xsec" "a second" "second" "sec"
        </td>
    </tr>
    <tr>
        <td>
            mins
        </td>
        <td>
            "xminutes" "xminute" "xmins" "xmin" "a minute" "minute" "min"
        </td>
    </tr>
    <tr>
        <td>
            hours
        </td>
        <td>
            "xhours" "xhour" "xhr" "xhrs" "a hour" "an hour" "hour" "hr"
        </td>
    </tr>
</table>

### Absolute

* 2011-04-30
* 2011-04-30 14:32
* 2011-04-30 14:32:17
* 2011-04-30 02:32pm
* 2011-04-30 02:32 pm
* 2011-04-30 2:32 pm
* 2011-04-30 02:32:17pm
* 2011-04-30 02:32:17 pm
* 2011-04-30 2:32:17 pm
* 14:32
* 14:32:17
* 02:32pm
* 02:32 pm
* 2:32 pm
* 02:32:17pm
* 02:32:17 pm
* 2:32:17 pm
