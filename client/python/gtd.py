#! /usr/bin/env python

import socket
import sys
import getopt

def connect_to_server(port):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        sock.connect(('localhost', port))
    except:
        print "Could not connect to gtd server. Is it running? Try passing the -server flag."
        sys.exit(1)
    return sock

def list_tasks(port):
    #TODO: need a loop to get all output
    sock = connect_to_server(port)
    sock.sendall('LIST\n')
    ret = sock.recv(80)
    print ret

def start_server(port):
    #TODO:
    print "start server"

def create_task(date_specifier, port):
    sock = connect_to_server(port)
    data = sys.stdin.read()
    sock.sendall('TASK %s\n' % date_specifier)
    sock.sendall(data)
    sock.sendall('\n.\n')

    ret = sock.recv(16)
    sock.close()

    sys.exit(0) if ret.strip() == "ACCEPTED" else sys.exit(1)

def usage():
    print "Usage: gtd [OPTION]* [TIME SPECIFIER]"
    print "Display text or file on standard input after TIME SPECIFIER."
    print "Example: echo \"Reminder!\" | gtd 30 mins"
    print ""
    print "OPTIONS:"
    print "-h, --help       Print this message and exit."
    print "-l, --list       List all upcoming tasks the server should display."
    print "                 Exits immediately after."
    print "-p, --port=PORT  Specify a different port than the default."
    print "-s, --server     Start a server instance if not already started."
    print "                 Exits immediately after."
    print ""
    print "TIME SPECIFIER:"
    print "Please see the gtd-server documentation for various ways of specifying"
    print "the time to delay before showing the notification."

def parse_args(argv):
    port = 61212
    try:
        opts, args = getopt.getopt(argv, "hlp:s", ["help", "list", "port=", "server"])
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
        if opt in ("s", "--server"):
            start_server(port)
            sys.exit()

    create_task(' '.join(args), port)

if __name__ == '__main__':
    parse_args(sys.argv[1:])
