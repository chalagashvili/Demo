"""
Module used for get/post REST API handling
"""
from flask import Flask
from flask import request
import flask
app = Flask(__name__)
node_server = None


def start_app(web_port):
    """
    starts the flask app
    :param web_port:
    :return:
    """
    print("started server on port", web_port)
    app.run(port=web_port)


@app.route("/<file_name>")
def read_file(file_name):
    """
    handle file reading mode
    :param file_name:
    :return:
    """
    print("Got Read Request From Browser For ", file_name, " File")
    file_data = node_server.get_file(file_name)
    if file_data[0] is None:
        redirect_link = "http://" + file_data[1] + ":" + \
                        str(file_data[2]) + "/" + str(file_name)
        print("Redirecting request for file", file_name,
              " to node ", file_data[1], file_data[2])
        return flask.redirect(redirect_link)
    if file_data == "404 No Such File On The Web":
        print("Such File Does not EXIST")
        return file_data
    return flask.send_file(file_data)


@app.route("/<file_name>/meta", methods=["GET"])
def get_file(file_name):
    """
    handle file meta getting mode
    :param file_name:
    :return:
    """
    print("Got Meta Data Request From Browser For ", file_name, " File")
    meta_data = node_server.get_meta(file_name)
    if meta_data[0] is None:
        redirect_link = "http://" + meta_data[1] + ":" + \
                        str(meta_data[2]) + "/" + str(file_name) + "/meta"
        print("Redirecting request for file meta",
              file_name, " to node ", meta_data[1], meta_data[2])
        return flask.redirect(redirect_link)
    return meta_data


@app.route("/", methods=["POST"])
def put_file():
    """
    handle file putting into server mode
    :return:
    """
    file_itself = request.files["file"]
    file_content = file_itself.read()
    file_name = request.files["file"].filename
    print("Post File To The Server With The Address ",
          node_server.host_ip, node_server.web_port)
    print("File Name is ", file_name)
    result = node_server.put_file(file_name, file_content)
    if result == 200:
        print("File Successfully Added To This Server")
        return file_name
    if result[0] is None:
        o_ip = result[1]
        o_web_port = result[2]
        redirect_link = "http://" + o_ip + ":" + str(o_web_port)
        print("Redirect File Posting To The Server With The Address ",
              o_ip, o_web_port)
        return flask.redirect(redirect_link, code=307)

