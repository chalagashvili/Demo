#!/usr/bin/python3
"""
Main Launcher Module
"""
import sys
import _thread
import server
import flask_api


def main():
    """
    runs the show
    :return:
    """
    # faces = utils.scan_interfaces()
    # host_ip = faces["lo"][0]["addr"]
    host_ip, host_port = (sys.argv[1]).split(":")
    web_port = sys.argv[2]
    server_directory = sys.argv[3]
    ref_ip = None
    ref_port = None
    if len(sys.argv) == 5:
        referral_node = sys.argv[4].split(":")
        ref_ip = referral_node[0]
        ref_port = int(referral_node[1])
    node = server.Server(
        host_ip, host_port, web_port, ref_ip, ref_port, server_directory)
    try:
        flask_api.node_server = node
        _thread.start_new_thread(flask_api.start_app, (node.web_port, ))
    except _thread.error:
        print("Could not start the thread")
    node.listen()


if __name__ == '__main__':
    main()
