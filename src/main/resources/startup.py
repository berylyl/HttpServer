from socket import *
sock = socket(AF_INET, SOCK_STREAM)
#sock.connect(('v134233.sqa.cm4.tbsite.net',8001))
sock.connect(('vkvm160039.sqa.cm6.tbsite.net',8001))
#sock.send("1 Cache-Control max-age=5")
#sock.send("1 rsp_code 200\r\n") 
#sock.send("1 rsp_code 200\r\n1 rsp_body_size 104")
#sock.send("1 rsp_code 200\r\n1 rsp_body_size  64")
#sock.send("1 rsp_code 200\r\n1 rsp_body_size 30\r\n1 Content-Length auto\r\n1 Cache-Control max-age=10")
sock.send("1 rsp_code 200\r\n1 rsp_body_size 32\r\n1 Content-Length auto\r\n1 Cache-Control no-cache\r\n1 add_rsp_header_Vary Accept-Encoding")
#sock.send("1 remove_all")

print sock.recv(1)

sock.close()
