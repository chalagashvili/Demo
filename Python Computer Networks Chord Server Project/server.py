"""
Server Module for giving responses to requests
"""
import os
import socket
import requests
import json
import utils


class Server:
    """
    Server Class, holding keys and directory assets
    """

    # pylint: disable=too-many-instance-attributes
    # pylint: disable=too-many-arguments

    def __init__(
            self, host_ip, port, web_port,
            referral_ip, referral_port, server_directory):
        """
        initializes server
        :param host_ip:
        :param port:
        :param web_port:
        :param server_directory:
        """
        self.host_ip = host_ip
        self.port = int(port)
        self.web_port = int(web_port)
        self.referral_ip = referral_ip
        self.referral_port = referral_port
        self.files_dir = server_directory
        self.sock = utils.create_listener_socket(host_ip, port)
        self.finger_table = {}
        self.files = utils.create_local_files_dictionary(self.files_dir)
        self.node_hash = utils.generate_hash(self.host_ip+":"+str(self.port))
        self.node_id = utils.bytes_to_numeric(self.node_hash)
        if self.referral_ip is None:
            self.predecessor = \
                self.node_id, self.host_ip, self.port, self.web_port
        else:
            self.predecessor = None
        self.successor = None
        self.fill_finger_table()
        self.already_joined = []
        if self.referral_ip is not None:
            self.already_joined.append((referral_ip, referral_port))
            self.joining()
        # self.distribute_files_on_startup()

    def fill_finger_table(self):
        """
        fills the finger table
        :return:
        """
        if self.referral_ip is None:
            hash_bytes = utils.generate_hash(self.host_ip+":"+str(self.port))
        else:
            hash_bytes = utils.generate_hash(
                self.referral_ip+":"+str(self.referral_port))
        identifier = utils.bytes_to_numeric(hash_bytes)
        value_ip = self.host_ip
        value_port = self.port
        value_web_port = self.web_port
        if self.referral_ip is not None:
            value_ip = self.referral_ip
            value_port = self.referral_port
            value_web_port = None
        for i in range(0, utils.VAR_BITS, 1):
            index = (self.node_id + 2**i) % 2**utils.VAR_BITS
            self.finger_table[index] = \
                identifier, value_ip, value_port, value_web_port

    def get_successor(self):
        """
        returns server's successor
        :return:
        """
        successor = (self.node_id + 1) % 2**utils.VAR_BITS
        return successor

    def listen(self):
        """
        listening for requests
        :return:
        """
        try:
            while True:
                sock = self.sock.accept()[0]
                msg = sock.recv(4096)
                msg_type = utils.get_received_message_type(msg)
                if msg_type == 1:
                    self.lookup(msg)
                elif msg_type == 3:
                    self.get_join(msg)
                elif msg_type == 5:
                    self.react_to_leave(msg)
                # else:
                #     print("its something else bro")
        except (KeyboardInterrupt, SystemExit):
            print("Server leaving from network")
            self.leave_from_network()

    def lookup(self, data):
        """
        demonstrates lookup
        :return:
        """
        sender_ip, sender_port, key, sender_web_port = \
            utils.parse_lookup_request(data)
        print("[LOOKUP] Got Lookup Request")
        print("[LOOKUP] Seeker IP: " + sender_ip +
              " Port: " + str(sender_port))
        print("[LOOKUP] Got Lookup request for key having value of: " +
              str(key))
        # print(key, "ageraaaa")
        # i_have_key = False
        # for finger in self.finger_table.keys():
        #     if finger <= self.finger_table[finger][0]:
        #         if finger <= key < self.finger_table[finger][0]:
        #             i_have_key = True
        #             break
        #     else:
        #         if finger <= key <
        # self.finger_table[finger][0] + 2**utils.VAR_BITS \
        #                 or finger - 2**utils.VAR_BITS <=
        # key < self.finger_table[finger][0]:
        #             i_have_key = True
        #             break
        if self.in_my_range(key, self.node_id):
            response = utils.assemble_lookup_response(
                self.host_ip, self.port, self.web_port)
            # print("i have, sending to", sender_ip, sender_port)
            sock = socket.create_connection((sender_ip, sender_port))
            sock.send(response)
            sock.close()
        else:
            request = utils.assemble_lookup_request(
                sender_ip, sender_port, key, sender_web_port)
            # succ_id, succ_ip, succ_port, succ_web_port =
            # self.finger_table[self.get_successor()]
            # print("es getyvis", succ_ip, succ_port)
            sock = \
                socket.create_connection((
                    self.finger_table[self.get_successor()][1],
                    self.finger_table[self.get_successor()][2]))
            sock.send(request)
            sock.close()

    def joining(self):
        """
        When this server is a new one and
        sends to other nodes in finger table about joining.
        :return:
        """
        for key in self.finger_table.keys():
            data = utils.assemble_lookup_request(self.host_ip, self.port,
                                                 key, self.web_port)
            sock = socket.create_connection(
                (self.referral_ip, self.referral_port))
            sock.send(data)
            sock.close()
            sock = self.sock.accept()[0]
            data = sock.recv(512)
            parsed_data = utils.parse_lookup_response(data)
            sender_ip = parsed_data[0]
            sender_port = parsed_data[1]
            hash_bytes = utils.generate_hash(sender_ip+":"+str(sender_port))
            hash_id = utils.bytes_to_numeric(hash_bytes)
            # print("key", key)
            self.finger_table[key] = \
                hash_id, parsed_data[0], parsed_data[1], parsed_data[2]
            if (sender_ip, sender_port) not in self.already_joined:
                self.already_joined.append((sender_ip, sender_port))
        join_data = utils.assemble_join_request(
            self.host_ip, self.port, self.web_port)
        for key in self.finger_table.keys():
            if self.finger_table[key][0] == self.node_id:
                continue
            curr_ip = self.finger_table[key][1]
            curr_port = self.finger_table[key][2]
            sock = socket.create_connection((curr_ip, curr_port))
            sock.send(join_data)
            sock.close()
        sock = self.sock.accept()[0]
        data = sock.recv(512)
        join_response = utils.parse_join_response(data)
        succ_hash_id = utils.bytes_to_numeric(
            utils.generate_hash(
                join_response[0] + ":" + str(join_response[1])))
        self.successor = \
            succ_hash_id, join_response[0], join_response[1], join_response[2]
        pred_hash_id = utils.bytes_to_numeric(
            utils.generate_hash(join_response[3]+":"+str(join_response[4])))
        self.predecessor = \
            pred_hash_id, join_response[3], join_response[4], join_response[5]
        if (join_response[0], join_response[1]) not in self.already_joined:
            self.already_joined.append((join_response[0], join_response[1]))
        if (join_response[3], join_response[4]) not in self.already_joined:
            self.already_joined.append((join_response[3], join_response[4]))
        for key in self.finger_table.keys():
            if self.in_my_range(key, self.node_id):
                self.finger_table[key] = \
                    self.node_id, self.host_ip, self.port, self.web_port
        for key in self.finger_table.keys():
            print("[FINGERTABLE AFTER JOINING]  [KEY] ",
                  key, " [VALUE] ", self.finger_table[key])
        print("successor: ", self.successor)
        print("predecessor: ", self.predecessor)

    def get_join(self, data):
        """
        got join request from some other node.
        :param data:
        :return:
        """
        parsed_request = utils.parse_join_request(data)
        if (parsed_request[0], parsed_request[1]) in self.already_joined:
            return
        # print("chavagde", parsed_request[0], parsed_request[1])
        self.already_joined.append((parsed_request[0], parsed_request[1]))
        sender_hash = utils.generate_hash(
            parsed_request[0]+":"+str(parsed_request[1]))
        sender_id = utils.bytes_to_numeric(sender_hash)
        print("[JOIN] Got Join Request")
        print("[JOIN] Joiner IP: " + parsed_request[0] +
              " Port: " + str(parsed_request[1]))
        print("[JOIN] Joiner Node's ID: " + str(sender_id))
        for finger in self.finger_table.keys():
            join_req = utils.assemble_join_request(parsed_request[0],
                                                   parsed_request[1],
                                                   parsed_request[2])
            sock = socket.create_connection((self.finger_table[finger][1],
                                             self.finger_table[finger][2]))
            sock.send(join_req)
            sock.close()
            if finger <= self.finger_table[finger][0]:
                if finger <= sender_id < self.finger_table[finger][0]:
                    self.finger_table[finger] = sender_id, parsed_request[0], \
                                                parsed_request[1], \
                                                parsed_request[2]
                    if finger == self.get_successor():
                        self.successor = sender_id, parsed_request[0], \
                                         parsed_request[1], parsed_request[2]
            else:
                if finger <= sender_id < \
                                self.finger_table[finger][0] + \
                                2**utils.VAR_BITS \
                        or finger - 2**utils.VAR_BITS <= \
                        sender_id < self.finger_table[finger][0]:
                    self.finger_table[finger] = sender_id, parsed_request[0], \
                                                parsed_request[1], \
                                                parsed_request[2]
                    if finger == self.get_successor():
                        self.successor = sender_id, parsed_request[0], \
                                         parsed_request[1], parsed_request[2]
        if self.in_my_range(sender_id, self.node_id):
            data = utils.assemble_join_response(self.host_ip,
                                                self.port,
                                                self.web_port,
                                                self.predecessor[1],
                                                self.predecessor[2],
                                                self.predecessor[3])
            self.send_my_files_to_other_node(
                parsed_request[0],
                parsed_request[2], sender_id)
            self.predecessor =\
                sender_id, parsed_request[0], \
                parsed_request[1], parsed_request[2]
            # print("shecvla ", sender_id)
            self.in_my_range(sender_id, self.node_id)
            sock = socket.create_connection(
                (parsed_request[0], parsed_request[1]))
            sock.send(data)
            sock.close()
        self.in_my_range(sender_id, self.node_id)
        for key in self.finger_table.keys():
            print("[FINGERTABLE AFTER SOMEBODY JOINED]  [KEY] ",
                  key, " [VALUE] ", self.finger_table[key])
        print("successor: ", self.successor)
        print("predecessor: ", self.predecessor)
        # print(self.already_joined)

    def send_my_files_to_other_node(self, o_ip,
                                    o_web_port, sender_id):
        for file in self.files:
            if self.in_my_range(file, sender_id):
                link = "http://" + o_ip + ":" + str(o_web_port)
                files = {}
                with open(self.files[file][0], "rb") as opened:
                    pay_load = opened.read()
                    files["file"] = self.files[file][1], pay_load
                    requests.post(link, files=files)
                os.remove(self.files[file][0])
                print("Transferring file #", file, " to the Node #",
                      sender_id, "address ", o_ip, o_web_port)

    def in_my_range(self, key, node_id):
        """
        checks if the given key value is under this node's range
        :param key:
        :param node_id:
        :return:
        """
        total_keys = 2**utils.VAR_BITS
        my_keys = node_id - self.predecessor[0]
        # print(node_id, "succ ", self.successor, "pred ", self.predecessor)
        if my_keys <= 0:
            my_keys += total_keys
        if self.predecessor[0] < key <= self.predecessor[0] + my_keys:
            return True
        elif self.predecessor[0]\
                < total_keys + key <= self.predecessor[0] + my_keys:
            return True
        return False

    def leave_from_network(self):
        """
        this node leaves the network
        :return:
        """
        if self.successor is None:
            return
        print("leaving from network")
        data = utils.assemble_leave_request(self.host_ip,
                                            self.port, self.web_port,
                                            self.successor[1],
                                            self.successor[2],
                                            self.successor[3],
                                            self.predecessor[1],
                                            self.predecessor[2],
                                            self.predecessor[3])
        for key in self.finger_table.keys():
            if self.finger_table[key][0] != self.node_id:
                sending_ip = self.finger_table[key][1]
                sending_port = self.finger_table[key][2]
                sock = socket.create_connection((sending_ip, sending_port))
                sock.send(data)
                sock.close()
                # print("sending about leaving to", sending_ip, sending_port)
        if self.successor[1] != self.host_ip or self.successor[3] != self.web_port:
            self.send_my_files_to_other_node(
                self.successor[1], self.successor[3],
                utils.bytes_to_numeric(utils.generate_hash(
                    self.successor[1]+":"+str(self.successor[2]))))

    def react_to_leave(self, msg):
        """
        gets leave message and reacts properly
        :return:
        """
        print("heard somebody leaving")
        data = utils.parse_leave_request(msg)
        leaver_addr = data[0], data[1], data[2]
        # print("got leave")
        # print(leaver_addr[0], leaver_addr[1])
        # print("ls", self.already_joined)
        if (leaver_addr[0], leaver_addr[1]) not in self.already_joined:
            return
        self.already_joined.remove((leaver_addr[0], leaver_addr[1]))
        leaver_succ = data[3], data[4], data[5]
        leaver_pred = data[6], data[7], data[8]
        leaver_hash = utils.generate_hash(
            leaver_addr[0]+":"+str(leaver_addr[1]))
        leaver_id = utils.bytes_to_numeric(leaver_hash)
        leaver_succ_id = utils.bytes_to_numeric(
            utils.generate_hash(
                leaver_succ[0]+":"+str(leaver_succ[1])))
        if self.successor[0] == leaver_id:
            self.successor = utils.bytes_to_numeric(
                utils.generate_hash(leaver_succ[0]+":"+str(leaver_succ[1]))), \
                             leaver_succ[0], leaver_succ[1], leaver_succ[2]
        if leaver_succ_id == self.node_id:
            self.predecessor = utils.bytes_to_numeric(
                utils.generate_hash(leaver_pred[0]+":"+str(leaver_pred[1]))), \
                               leaver_pred[0], leaver_pred[1], leaver_pred[2]
        print("[LEAVE] Got Leave Request")
        print("[LEAVE] Leaver IP: "+leaver_addr[0]+" Port: " +
              str(leaver_addr[1]))
        print("[LEAVE] Leaver ID: "+str(leaver_id))
        for finger in self.finger_table.keys():
            if self.finger_table[finger][0] == leaver_id:
                self.finger_table[finger] = utils.bytes_to_numeric(
                    utils.generate_hash(
                        leaver_succ[0]+":"+str(leaver_succ[1]))), \
                             leaver_succ[0], leaver_succ[1], leaver_succ[2]
        tell_others_data = utils.assemble_leave_request(
            data[0], data[1], data[2],
            data[3], data[4], data[5],
            data[6], data[7], data[8])
        for key in self.finger_table.keys():
            print("[FINGER TABLE AFTER SOMEONE'S LEAVE]  [KEY] ",
                  key, " [VALUE] ", self.finger_table[key])
            if self.finger_table[key][0] != self.node_id:
                sending_ip = self.finger_table[key][1]
                sending_port = self.finger_table[key][2]
                sock = socket.create_connection((sending_ip, sending_port))
                sock.send(tell_others_data)
                sock.close()
                # print("told about leaving to", sending_ip, sending_port)
        print("successor: ", self.successor)
        print("predecessor: ", self.predecessor)

    def get_file(self, file_name):
        """
        responds to the web user to show the file
        :param file_name:
        :return:
        """
        key = utils.bytes_to_numeric(utils.generate_hash(file_name))
        mine = self.in_my_range(key, self.node_id)
        if mine:
            if key in self.files.keys():
                return self.files[key][0]
            else:
                return "404 No Such File On The Web"
        else:
            listener_sock = utils.create_listener_socket(self.host_ip, 0)
            request = utils.assemble_lookup_request(
                self.host_ip, listener_sock.getsockname()[1], key, self.web_port)

            sock = \
                socket.create_connection((
                    self.finger_table[self.get_successor()][1],
                    self.finger_table[self.get_successor()][2]))
            sock.send(request)
            sock.close()
            sock = listener_sock.accept()[0]
            data = sock.recv(512)
            parsed_data = utils.parse_lookup_response(data)
            sender_ip = parsed_data[0]
            sender_web_port = parsed_data[2]
            return None, sender_ip, sender_web_port

    def get_meta(self, file_name):
        """
        returns meta information of the file
        :param file_name:
        :return:
        """
        key = utils.bytes_to_numeric(utils.generate_hash(file_name))
        mine = self.in_my_range(key, self.node_id)
        if mine:
            if key in self.files.keys():
                info = self.files[key][0]
                result = {}
                file_name = self.files[key][1]
                file_size = os.stat(info).st_size
                result["file_name"] = file_name
                result["file_size"] = file_size
                fingers = {}
                for finger in self.finger_table:
                    fingers[finger] = \
                        self.finger_table[finger][1], self.finger_table[finger][3]
                result["finger_table"] = fingers
                json_data = json.dumps(result)
                return json_data
            else:
                return "404 No Such File On The Web"
        else:
            listener_sock = utils.create_listener_socket(self.host_ip, 0)
            request = utils.assemble_lookup_request(
                self.host_ip, listener_sock.getsockname()[1], key, self.web_port)

            sock = \
                socket.create_connection((
                    self.finger_table[self.get_successor()][1],
                    self.finger_table[self.get_successor()][2]))
            sock.send(request)
            sock.close()
            sock = listener_sock.accept()[0]
            data = sock.recv(512)
            parsed_data = utils.parse_lookup_response(data)
            sender_ip = parsed_data[0]
            sender_web_port = parsed_data[2]
            return None, sender_ip, sender_web_port

    def put_file(self, file_name, file_content):
        """
        when somebody posts file onto server
        :param file_name:
        :param file_content:
        :return:
        """
        key = utils.bytes_to_numeric(utils.generate_hash(file_name))
        mine = self.in_my_range(key, self.node_id)
        if mine:
            with open(os.path.join(self.files_dir, file_name), "wb+") as open_file:
                open_file.write(file_content)
            self.files = utils.create_local_files_dictionary(self.files_dir)
            return 200
        else:
            listener_sock = utils.create_listener_socket(self.host_ip, 0)
            request = utils.assemble_lookup_request(
                self.host_ip, listener_sock.getsockname()[1], key, self.web_port)

            sock = \
                socket.create_connection((
                    self.finger_table[self.get_successor()][1],
                    self.finger_table[self.get_successor()][2]))
            sock.send(request)
            sock.close()
            sock = listener_sock.accept()[0]
            data = sock.recv(512)
            parsed_data = utils.parse_lookup_response(data)
            sender_ip = parsed_data[0]
            sender_web_port = parsed_data[2]
            return None, sender_ip, sender_web_port

    # def distribute_files_on_startup(self):
    #     for file in self.files:
    #         if self.in_my_range(file, self.node_id):
    #             continue
    #         listener_sock = utils.create_listener_socket(self.host_ip, 0)
    #         request = utils.assemble_lookup_request(
    #             self.host_ip, listener_sock.getsockname()[1], file, self.web_port)
    #         sock = \
    #             socket.create_connection((
    #                 self.finger_table[self.get_successor()][1],
    #                 self.finger_table[self.get_successor()][2]))
    #         sock.send(request)
    #         sock = listener_sock.accept()[0]
    #         data = sock.recv(512)
    #         parsed_data = utils.parse_lookup_response(data)
    #         sender_ip = parsed_data[0]
    #         sender_web_port = parsed_data[2]
    #         link = "http://" + sender_ip + ":" + str(sender_web_port)
    #         files = {}
    #         with open(self.files[file][0], "rb") as opened:
    #             pay_load = opened.read()
    #             files["file"] = self.files[file][1], pay_load
    #             requests.post(link, files=files)
    #         os.remove(self.files[file][0])
    #         print("Transferring file #", file, " to the Node #",
    #               utils.bytes_to_numeric(
    #                   utils.generate_hash(
    #                       sender_ip+":"+str(parsed_data[1]))),
    #               "address ", sender_ip, sender_web_port)
    #         listener_sock.close()

