# Based on SSL commands from here: https://docs.confluent.io/2.0.0/kafka/ssl.html


rm kafka.server.truststore.jks > /dev/null 2>&1
rm kafka.client.truststore.jks > /dev/null 2>&1
rm kafka.server.keystore.jks > /dev/null 2>&1
rm kafka.client.keystore.jks > /dev/null 2>&1
rm ca-* > /dev/null 2>&1
rm cert-* > /dev/null 2>&1

#Step 1

keytool -keystore kafka.server.keystore.jks -alias localhost -validity 365 -genkey \
  -dname "CN=localhost, OU=Private, O=Private, L=Leeds, S=Leeds, C=UK" \
  -storepass secret

#Step 2
openssl req -new -x509 -keyout ca-key -out ca-cert -days 365 -passout pass:secret \
  -subj "/C=UK/ST=Leeds/L=Leeds/O=Private/OU=Private/CN=localhost"
keytool -keystore kafka.server.truststore.jks -alias CARoot -import -file ca-cert -storepass secret -noprompt
keytool -keystore kafka.client.truststore.jks -alias CARoot -import -file ca-cert -storepass secret -noprompt


#Step 3
keytool -keystore kafka.server.keystore.jks -alias localhost -certreq -file cert-file -storepass secret -noprompt
openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file -out cert-signed -days 365 -CAcreateserial -passin pass:secret

keytool -keystore kafka.server.keystore.jks -alias CARoot -import -file ca-cert -storepass secret -noprompt
keytool -keystore kafka.server.keystore.jks -alias localhost -import -file cert-signed -storepass secret -noprompt
