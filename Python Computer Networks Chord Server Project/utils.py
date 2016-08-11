"""
Utility functions to make our lives a bit easier
"""
import os
import socket
import struct
import hashlib
import math
VAR_BITS = 8


def create_listener_socket(host_ip, port):
    """
    creates socket, that is used for
    :param host_ip:
    :param port:
    :return:
    """
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind((host_ip, int(port)))
    sock.listen(100)
    return sock


def assemble_lookup_request(host_ip, port, hash_code, web_port):
    """
    creates message for lookup
    :param host_ip:
    :param port:
    :param hash_code:
    :param web_port:
    :return:
    """
    data = bytes()
    message_type = struct.pack("!B", 1)
    data += message_type
    ip_bytes = struct.pack("!I", ip2int(host_ip))
    port_bytes = struct.pack("!H", port)
    data += ip_bytes + port_bytes
    hash_int_bytes = numeric_to_bytes(hash_code)
    payload_length = struct.pack("!B", len(hash_int_bytes))
    data += payload_length + hash_int_bytes
    data += struct.pack("!H", web_port)
    return data


def assemble_lookup_response(host_ip, port, web_port):
    """
    :param host_ip:
    :param port:
    :param web_port:
    :return:
    """
    data = bytes()
    data += struct.pack("!B", 2)
    data += struct.pack("!I", ip2int(host_ip))
    data += struct.pack("!H", port)
    data += struct.pack("!H", web_port)
    return data


def assemble_join_request(host_ip, port, web_port):
    """
    creates join request
    :param host_ip:
    :param port:
    :param web_port:
    :return:
    """
    data = bytes()
    data += struct.pack("!B", 3)
    data += struct.pack("!I", ip2int(host_ip))
    data += struct.pack("!H", port)
    data += struct.pack("!H", web_port)
    return data

# pylint: disable=too-many-arguments


def assemble_join_response(
        succ_ip, succ_port, succ_web, pred_ip, pred_port, pred_web):
    """
    :param succ_ip:
    :param succ_port:
    :param succ_web:
    :param pred_ip:
    :param pred_port:
    :param pred_web:
    :return:
    """
    data = bytes()
    data += struct.pack("!B", 4)
    data += struct.pack("!I", ip2int(succ_ip))
    data += struct.pack("!H", succ_port)
    data += struct.pack("!H", succ_web)
    data += struct.pack("!I", ip2int(pred_ip))
    data += struct.pack("!H", pred_port)
    data += struct.pack("!H", pred_web)
    return data

    # pylint: disable=too-many-arguments


def assemble_leave_request(host_ip, port, web, succ_ip,
                           succ_port, succ_web, pred_ip, pred_port, pred_web):
    """
    :param host_ip:
    :param port:
    :param web:
    :param succ_ip:
    :param succ_port:
    :param succ_web:
    :param pred_ip:
    :param pred_port:
    :param pred_web:
    :return:
    """
    data = bytes()
    data += struct.pack("!B", 5)
    ip_int_value = ip2int(host_ip)
    data += struct.pack("!I", ip_int_value)
    data += struct.pack("!H", port)
    data += struct.pack("!H", web)

    succ_ip_int_value = ip2int(succ_ip)
    data += struct.pack("!I", succ_ip_int_value)
    data += struct.pack("!H", succ_port)
    data += struct.pack("!H", succ_web)

    pred_ip_int_value = ip2int(pred_ip)
    data += struct.pack("!I", pred_ip_int_value)
    data += struct.pack("!H", pred_port)
    data += struct.pack("!H", pred_web)
    return data


def get_received_message_type(data):
    """
    checks message type of data
    :param data:
    :return:
    """
    first_byte = data[0:1]
    message_type = struct.unpack("!B", first_byte)[0]
    return message_type


def parse_lookup_request(data):
    """
    extract info from lookup request message
    :param data:
    :return:
    """
    ip_bytes = data[1:5]
    ip_int_value = struct.unpack("!I", ip_bytes)[0]
    ip_string = int2ip(ip_int_value)
    port = struct.unpack("!H", data[5:7])[0]
    payload_length = struct.unpack("!B", data[7:8])[0]
    payload_bytes = data[8:8+payload_length]
    # print("bytes", payload_bytes)
    hash_key = bytes_to_numeric(payload_bytes)
    # print("ricxvi", hash_key)
    web_port = struct.unpack("!H", data[8+payload_length:])[0]
    return ip_string, port, hash_key, web_port


def parse_lookup_response(data):
    """
    extract info from lookup response message
    :param data:
    :return:
    """
    ip_bytes = data[1:5]
    ip_int_value = struct.unpack("!I", ip_bytes)[0]
    ip_to_string = int2ip(ip_int_value)
    port_bytes = data[5:7]
    port_int_value = struct.unpack("!H", port_bytes)[0]
    web_port = struct.unpack("!H", data[7:9])[0]
    return ip_to_string, port_int_value, web_port


def parse_join_request(data):
    """
    extract info from join request message
    :param data:
    :return:
    """
    ip_bytes = data[1:5]
    ip_int_value = struct.unpack("!I", ip_bytes)[0]
    ip_to_string = int2ip(ip_int_value)
    port_bytes = data[5:7]
    port_int_value = struct.unpack("!H", port_bytes)[0]
    web_port = struct.unpack("!H", data[7:9])[0]
    return ip_to_string, port_int_value, web_port


def parse_join_response(data):
    """
    extract info from join response message
    :param data:
    :return:
    """
    succ_ip_bytes = data[1:5]
    succ_ip_int_value = struct.unpack("!I", succ_ip_bytes)[0]
    succ_ip_to_string = int2ip(succ_ip_int_value)
    succ_port_bytes = data[5:7]
    succ_port_int_value = struct.unpack("!H", succ_port_bytes)[0]
    succ_web_port = struct.unpack("!H", data[7:9])[0]

    pred_ip_bytes = data[9:13]
    pred_ip_int_value = struct.unpack("!I", pred_ip_bytes)[0]
    pred_ip_to_string = int2ip(pred_ip_int_value)
    pred_port_bytes = data[13:15]
    pred_port_int_value = struct.unpack("!H", pred_port_bytes)[0]

    pred_web_port = struct.unpack("!H", data[15:17])[0]

    return succ_ip_to_string, succ_port_int_value, succ_web_port, \
        pred_ip_to_string, pred_port_int_value, pred_web_port


def parse_leave_request(data):
    """
    extracts info from leave request message
    :param data:
    :return:
    """

    ip_int_value = struct.unpack("!I", data[1:5])[0]
    ip_to_string = int2ip(ip_int_value)
    port_int_value = struct.unpack("!H", data[5:7])[0]
    web_port = struct.unpack("!H", data[7:9])[0]

    succ_ip_int_value = struct.unpack("!I", data[9:13])[0]
    succ_ip_to_string = int2ip(succ_ip_int_value)
    succ_port_int_value = struct.unpack("!H", data[13:15])[0]
    succ_web_port = struct.unpack("!H", data[15:17])[0]

    pred_ip_int_value = struct.unpack("!I", data[17:21])[0]
    pred_ip_to_string = int2ip(pred_ip_int_value)
    pred_port_int_value = struct.unpack("!H", data[21:23])[0]
    pred_web_port = struct.unpack("!H", data[23:25])[0]

    return ip_to_string, port_int_value, web_port, \
        succ_ip_to_string, succ_port_int_value, succ_web_port, \
        pred_ip_to_string, pred_port_int_value, pred_web_port

# stackoverflow-დან არის აღებული


def bytes_to_numeric(data):
    """
    gets first VAR_BIT bits from data and converts into integer
    :param data:
    :return:
    """
    value = int.from_bytes(data, "big")
    value <<= 160 - len(data) * 8
    bit_string = int("1" * VAR_BITS, 2)
    bit_string <<= 160 - VAR_BITS
    current = (bit_string & value)
    current >>= 160 - VAR_BITS
    return current


def int2ip(addr):
    """
    converts integer to stringed ip
    :param addr:
    :return:
    """
    return socket.inet_ntoa(struct.pack("!I", addr))

# stackoverflow-დან არის აღებული


def numeric_to_bytes(number):
    """
    gets bytes of number having length of >8
    :param number:
    :return:
    """
    return number.to_bytes(math.ceil(number.bit_length() / VAR_BITS), "big")


def ip2int(addr):
    """
    converts stringed IP to Integer
    :param addr:
    :return:
    """
    return struct.unpack("!I", socket.inet_aton(addr))[0]


# def scan_interfaces():
#     """
#     Scans interfaces and returns as dictionary
#     :return:
#     """
#     my_interfaces = {}
#     interfaces = netifaces.interfaces()
#     for interface in interfaces:
#         ip_mask = netifaces.ifaddresses(interface)
#         af_inet = ip_mask.get(netifaces.AF_INET)
#         if af_inet:
#             my_interfaces[interface] = ip_mask[netifaces.AF_INET]
#     return my_interfaces


def generate_hash(string):
    """
    generates hash
    :param string:
    :return:
    """
    hasher = hashlib.sha1()
    hasher.update(string.encode())
    hash_code = hasher.digest()
    return hash_code


def create_local_files_dictionary(files_dir):
    """
    reads files and inserts to special dictionary
    :param files_dir:
    :return:
    """
    result = {}
    files = os.listdir(files_dir)
    for file in files:
        result[bytes_to_numeric(
            generate_hash(file))] = os.path.join(files_dir, file), file
    return result
