#!/bin/bash
 
sudo apt-get -y install squid apache2-utils
 
sudo cp /etc/squid/squid.conf /etc/squid/squid.conf.default && sudo touch /etc/squid/squid_passwd && sudo chown proxy /etc/squid/squid_passwd && sudo htpasswd -b -c /etc/squid/squid_passwd tfelab TfeLAB2@15
 
sudo sed -i "1i\auth_param basic program /usr/lib/squid/basic_ncsa_auth /etc/squid/squid_passwd \n \
acl ncsa_users proxy_auth REQUIRED \n \
http_access allow ncsa_users \n \
cache deny all \n" /etc/squid/squid.conf
 
sudo sed -i "s/.*http_port 3128.*/http_port 59998/" /etc/squid/squid.conf
 
sudo sed -i '$a\forwarded_for off \
request_header_access Allow allow all \
request_header_access Authorization allow all \
request_header_access WWW-Authenticate allow all \
request_header_access Proxy-Authorization allow all \
request_header_access Proxy-Authenticate allow all \
request_header_access Cache-Control allow all \
request_header_access Content-Encoding allow all \
request_header_access Content-Length allow all \
request_header_access Content-Type allow all \
request_header_access Date allow all \
request_header_access Expires allow all \
request_header_access Host allow all \
request_header_access If-Modified-Since allow all \
request_header_access Last-Modified allow all \
request_header_access Location allow all \
request_header_access Pragma allow all \
request_header_access Accept allow all \
request_header_access Accept-Charset allow all \
request_header_access Accept-Encoding allow all \
request_header_access Accept-Language allow all \
request_header_access Content-Language allow all \
request_header_access Mime-Version allow all \
request_header_access Retry-After allow all \
request_header_access Title allow all \
request_header_access Connection allow all \
request_header_access Proxy-Connection allow all \
request_header_access User-Agent allow all \
request_header_access Cookie allow all \
request_header_access All deny all\n' /etc/squid/squid.conf
 
sudo service squid restart
