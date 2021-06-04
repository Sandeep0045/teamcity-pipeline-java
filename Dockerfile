FROM tomcat:8.0-alpine
COPY target/*.war /usr/local/tomcat/webapps/
# Added to test webhook
EXPOSE 8080
CMD ["catalina.sh", "run"]
