from tornado.wsgi import WSGIContainer
from tornado.httpserver import HTTPServer
from tornado.ioloop import IOLoop

from flask.ext.api import FlaskAPI, status
from flask import request

import pprint
# import json
# from flask import jsonify

app = FlaskAPI(__name__)


@app.route("/hello")
def index():
    return "hello, world!"

@app.route("/messages/1", methods=['POST'])
def post_one_message():
    print request.data
    return "not found", status.HTTP_404_NOT_FOUND

@app.route("/messages", methods=['POST'])
def post_messages():
    print request.data
    return request.data, 200

@app.route("/cas", methods=['GET'])
def test_cas():
    print pprint.pformat(request.environ, depth=5)
    print request.cookies
    return "error", status.HTTP_401_UNAUTHORIZED

@app.route("/errors/500", methods=['POST'])
def error_messages():
    # print request.data
    return "error", status.HTTP_500_INTERNAL_SERVER_ERROR

@app.route("/errors/404", methods=['POST'])
def error_messages_404():
    # print request.data
    return "error", status.HTTP_404_NOT_FOUND

if __name__ == '__main__':
    http_server = HTTPServer(WSGIContainer(app))
    port = 12000
    http_server.listen(port)
    print("starting server on port {}".format(port))
    IOLoop.instance().start()
