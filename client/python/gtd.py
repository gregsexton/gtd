#! /usr/bin/env python

import socket
import os
import sys
import getopt

def connect_to_server(port):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        sock.connect(('localhost', port))
    except:
        print "Could not connect to gtd server. Is it running? Try passing the --server flag."
        sys.exit(1)
    return sock

def list_tasks(port):
    sock = connect_to_server(port)
    try:
        sock.sendall('LIST\n')
        ret = sock.recv(80)
        acc = []
        if ret == "\n":
            print "No tasks currently."
        else:
            while not "." in ret.split('\n'):
                acc.append(ret)
                ret = sock.recv(80)
            acc.append(ret[:-3]) #remove "\n.\n" from end.
            print ''.join(acc)
    finally:
        sock.close()

def start_server(port):
    return os.system('gtd-server ' + str(port))

def sanatize_task_message(message):
    lst = map(lambda x: ". " if x == "." else x, message.split('\n'))
    return '\n'.join(lst)

def create_task_successful():
    sys.exit(0)
def create_task_unsuccessful():
    print "ERROR: Task request unsuccessful."
    sys.exit(1)

def create_task(msg, date_specifier, port):
    sock = connect_to_server(port)
    try:
        if msg == None:
            data = sys.stdin.read()
            print data,
        else:
            data = msg
        data = sanatize_task_message(data)
        sock.sendall('TASK %s\n' % date_specifier)
        sock.sendall(data)
        sock.sendall('\n.\n')

        ret = sock.recv(16)
    finally:
        sock.close()

    if ret.strip() == "ACCEPTED":
        create_task_successful()
    else:
        create_task_unsuccessful()

def usage():
    print "Usage: gtd [OPTION]* [TIME SPECIFIER]"
    print "Display text or file on standard input after TIME SPECIFIER."
    print "Example: echo \"Reminder!\" | gtd 30 mins"
    print ""
    print "OPTIONS:"
    print "-h, --help        Print this message and exit."
    print "-l, --list        List all upcoming tasks the server should display."
    print "                  Exits immediately after."
    print "-m, --message=MSG Take the message from the arg list rather than"
    print "                  standard in. The message is not echoed back."
    print "-p, --port=PORT   Specify a different port than the default."
    print "-s, --server      Start a server instance if not already started."
    print "                  Exits immediately after."
    print ""
    print "TIME SPECIFIER:"
    print "Please see the gtd-server documentation for various ways of specifying"
    print "the time to delay before showing the notification."
    print ""
    print "Anything read via standard input is echoed to standard output to allow"
    print "for gtd to be used in pipes."

def parse_args(argv):
    port = 61212
    msg = None
    try:
        opts, args = getopt.getopt(argv, "hlm:p:s", ["help", "list", "message=", "port=", "server"])
    except getopt.GetoptError:
        usage()
        sys.exit(1)
    for opt, arg in opts:
        if opt in ("-p", "--port"):
            port = arg
    for opt, arg in opts:
        if opt in ("-h", "--help"):
            usage()
            sys.exit(0)
        if opt in ("-l", "--list"):
            list_tasks(port)
            sys.exit()
        if opt in ("-s", "--server"):
            sys.exit(start_server(port))
        if opt in ("-m", "--message"):
            msg = arg

    create_task(msg, ' '.join(args), port)

if __name__ == '__main__':
    parse_args(sys.argv[1:])
